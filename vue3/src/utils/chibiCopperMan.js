import * as THREE from 'three'

const FRONT = 1

function scaledSphere(group, position, scale, material, segments = 40) {
  const mesh = new THREE.Mesh(new THREE.SphereGeometry(1, segments, Math.round(segments * 0.72)), material)
  mesh.position.set(...position)
  mesh.scale.set(...scale)
  group.add(mesh)
  return mesh
}

function torus(group, position, radius, tube, rotation, material, arc = Math.PI * 2) {
  const mesh = new THREE.Mesh(new THREE.TorusGeometry(radius, tube, 10, 56, arc), material)
  mesh.position.set(...position)
  mesh.rotation.set(...rotation)
  group.add(mesh)
  return mesh
}

function flattenedCylinder(group, position, radius, depth, scale, rotation, material, segments = 48) {
  const mesh = new THREE.Mesh(new THREE.CylinderGeometry(radius, radius, depth, segments), material)
  mesh.position.set(...position)
  mesh.scale.set(...scale)
  mesh.rotation.set(...rotation)
  group.add(mesh)
  return mesh
}

function radiusAt(radii, t) {
  const scaled = t * (radii.length - 1)
  const index = Math.min(Math.floor(scaled), radii.length - 2)
  return THREE.MathUtils.lerp(radii[index], radii[index + 1], scaled - index)
}

function organicLimb(group, controlPoints, radii, material, segments = 48, radialSegments = 28) {
  const curve = new THREE.CatmullRomCurve3(
    controlPoints.map((point) => new THREE.Vector3(...point)),
    false,
    'centripetal'
  )
  const frames = curve.computeFrenetFrames(segments, false)
  const positions = []
  const normals = []
  const indices = []

  for (let i = 0; i <= segments; i += 1) {
    const t = i / segments
    const center = curve.getPointAt(t)
    const radius = radiusAt(radii, t)
    for (let j = 0; j < radialSegments; j += 1) {
      const angle = (j / radialSegments) * Math.PI * 2
      const normal = new THREE.Vector3()
        .addScaledVector(frames.normals[i], Math.cos(angle))
        .addScaledVector(frames.binormals[i], Math.sin(angle))
        .normalize()
      positions.push(center.x + normal.x * radius, center.y + normal.y * radius, center.z + normal.z * radius)
      normals.push(normal.x, normal.y, normal.z)
    }
  }

  for (let i = 0; i < segments; i += 1) {
    for (let j = 0; j < radialSegments; j += 1) {
      const next = (j + 1) % radialSegments
      const a = i * radialSegments + j
      const b = (i + 1) * radialSegments + j
      const c = (i + 1) * radialSegments + next
      const d = i * radialSegments + next
      indices.push(a, b, d, b, c, d)
    }
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  geometry.setAttribute('normal', new THREE.Float32BufferAttribute(normals, 3))
  geometry.setIndex(indices)
  const mesh = new THREE.Mesh(geometry, material)
  group.add(mesh)
  return mesh
}

function makeTorso(group, material) {
  const profile = [
    [6.5, 28], [12.4, 27], [18.2, 22], [21.2, 13], [22.7, 1],
    [22.9, -12], [21.4, -24], [17.5, -34], [10.5, -39]
  ].map(([radius, y]) => new THREE.Vector2(radius, y))
  const torso = new THREE.Mesh(new THREE.LatheGeometry(profile, 72), material)
  torso.scale.z = 0.72
  group.add(torso)
}

function makeEar(group, side, bronze, patina) {
  const x = 29.5 * side
  const ear = scaledSphere(group, [x, 52, -0.8], [5.2, 9.5, 5.3], bronze, 44)
  ear.rotation.z = 0.08 * side
  const inner = torus(group, [x + 0.1 * side, 52, 3.6], 3.35, 0.72, [0, 0, 0], patina, Math.PI * 1.55)
  inner.rotation.z = side < 0 ? 0.55 : -2.05
}

function makeHairline(group, patina) {
  const curve = new THREE.CatmullRomCurve3([
    new THREE.Vector3(-19.2, 68.5, 18.1),
    new THREE.Vector3(-11.8, 70.8, 20.2),
    new THREE.Vector3(-4.5, 67.6, 22.1),
    new THREE.Vector3(0, 65.4, 23),
    new THREE.Vector3(4.5, 67.6, 22.1),
    new THREE.Vector3(11.8, 70.8, 20.2),
    new THREE.Vector3(19.2, 68.5, 18.1)
  ])
  group.add(new THREE.Mesh(new THREE.TubeGeometry(curve, 64, 0.68, 10, false), patina))
}

function makeFace(group, bronze, dark, eyeWhite, iris, black) {
  for (const side of [-1, 1]) {
    const x = 9.4 * side
    scaledSphere(group, [x, 54.5, 25], [6.1, 7.7, 2.65], dark, 40)
    scaledSphere(group, [x, 54.5, 25.4], [5.45, 6.95, 2.75], eyeWhite, 40)
    scaledSphere(group, [x, 54.2, 27.65], [3.4, 4.7, 1.25], iris, 32)
    scaledSphere(group, [x, 54.1, 28.7], [2.1, 3.25, 0.75], black, 28)
    scaledSphere(group, [x - 0.85, 56.3, 29.35], [0.82, 1.12, 0.32], eyeWhite, 18)

    const brow = new THREE.Mesh(new THREE.TorusGeometry(5.1, 0.68, 10, 30, Math.PI * 0.62), dark)
    brow.position.set(x, 65.4, 25.1)
    brow.rotation.z = side < 0 ? 0.22 : -0.22
    group.add(brow)
  }

  scaledSphere(group, [0, 45.8, 27], [4.25, 3.25, 3.4], bronze, 32)
  scaledSphere(group, [-1.25, 45.7, 29.75], [0.62, 0.42, 0.25], dark, 16)
  scaledSphere(group, [1.25, 45.7, 29.75], [0.62, 0.42, 0.25], dark, 16)

  const smileCurve = new THREE.QuadraticBezierCurve3(
    new THREE.Vector3(-7.8, 41.4, 28.1),
    new THREE.Vector3(0, 35.3, 29.7),
    new THREE.Vector3(7.8, 41.4, 28.1)
  )
  group.add(new THREE.Mesh(new THREE.TubeGeometry(smileCurve, 36, 0.78, 10, false), dark))
  const lip = new THREE.Mesh(new THREE.TorusGeometry(5.1, 0.48, 8, 28, Math.PI * 0.9), dark)
  lip.position.set(0, 39.4, 28.7)
  lip.rotation.z = 0.12
  group.add(lip)
}

function makeHands(group, bronze, patina) {
  for (const side of [-1, 1]) {
    const x = 32.5 * side
    scaledSphere(group, [x, -19.5, 5], [6.1, 7.2, 5], bronze, 36)
    for (let i = 0; i < 4; i += 1) {
      const fingerX = x + side * (i - 1.5) * 1.05
      const finger = scaledSphere(group, [fingerX, -24.1, 8.4], [0.8, 3.2 - Math.abs(i - 1.5) * 0.35, 0.85], bronze, 18)
      finger.rotation.z = -0.08 * side
    }
    scaledSphere(group, [x - side * 4.7, -19.1, 7.3], [1.55, 3.8, 1.45], bronze, 20)
    torus(group, [x, -14.5, 4.6], 5.1, 0.45, [0, Math.PI / 2, side * 0.46], patina)
  }
}

function makeFeet(group, bronze, patina) {
  for (const side of [-1, 1]) {
    const x = 11.8 * side
    flattenedCylinder(group, [x, -88, 7.5], 8.2, 10.5, [1.05, 1, 1.55], [0, 0, 0], bronze, 48)
    for (let i = 0; i < 5; i += 1) {
      const toeX = x + side * (i - 2) * 1.45
      scaledSphere(group, [toeX, -88.1, 19.2 - Math.abs(i - 2) * 0.75], [1.15, 1, 1.8 - Math.abs(i - 2) * 0.12], bronze, 18)
    }
    torus(group, [x, -72.5, 1.5], 7.1, 0.46, [Math.PI / 2, 0, 0], patina)
  }
}

export function createChibiCopperMan({ bodyVisible = true } = {}) {
  const group = new THREE.Group()
  group.name = '可旋转Q版小铜人'

  const bronze = new THREE.MeshPhysicalMaterial({ color: 0xc99845, metalness: 0.34, roughness: 0.58, clearcoat: 0.1, clearcoatRoughness: 0.74 })
  const bronzeLight = new THREE.MeshPhysicalMaterial({ color: 0xd9a953, metalness: 0.3, roughness: 0.55, clearcoat: 0.12, clearcoatRoughness: 0.7 })
  const dark = new THREE.MeshStandardMaterial({ color: 0x5b3517, metalness: 0.32, roughness: 0.5 })
  const patina = new THREE.MeshStandardMaterial({ color: 0x268579, metalness: 0.28, roughness: 0.62 })
  const eyeWhite = new THREE.MeshStandardMaterial({ color: 0xffefc9, roughness: 0.24 })
  const iris = new THREE.MeshStandardMaterial({ color: 0x7b4b19, roughness: 0.2 })
  const black = new THREE.MeshStandardMaterial({ color: 0x160d08, roughness: 0.16 })

  scaledSphere(group, [0, 52, 0], [29, 31, 25.8], bronzeLight, 64)
  makeEar(group, -1, bronze, patina)
  makeEar(group, 1, bronze, patina)
  makeHairline(group, patina)
  scaledSphere(group, [0, 87.5, 0], [7, 6.7, 6.5], bronze, 40)
  torus(group, [0, 81.3, 0], 7.6, 1.4, [Math.PI / 2, 0, 0], bronze)
  torus(group, [0, 80.9, 0], 7.8, 0.48, [Math.PI / 2, 0, 0], patina)
  makeFace(group, bronze, dark, eyeWhite, iris, black)

  scaledSphere(group, [0, 24.5, -0.5], [10.5, 8, 9], bronze, 36)
  makeTorso(group, bronzeLight)
  scaledSphere(group, [0, -2.5, 15.8], [1.25, 1.25, 0.78], dark, 18)

  organicLimb(group, [[-10.5, 20, 0], [-19.3, 10.5, 0.6], [-26, -2, 1.8], [-32.5, -15.5, 4]], [10.5, 8.8, 6.9, 5.6], bronze)
  organicLimb(group, [[10.5, 20, 0], [19.3, 10.5, 0.6], [26, -2, 1.8], [32.5, -15.5, 4]], [10.5, 8.8, 6.9, 5.6], bronze)
  makeHands(group, bronze, patina)

  organicLimb(group, [[-8.5, -34, 0], [-11.7, -48, 1], [-12.1, -66, 1.8], [-11.8, -84, 3]], [12, 11.4, 9.6, 8.2], bronze)
  organicLimb(group, [[8.5, -34, 0], [11.7, -48, 1], [12.1, -66, 1.8], [11.8, -84, 3]], [12, 11.4, 9.6, 8.2], bronze)
  makeFeet(group, bronze, patina)

  group.traverse((child) => {
    if (child.isMesh) {
      child.visible = bodyVisible
      child.castShadow = false
      child.receiveShadow = false
    }
  })
  return group
}

export function mapAcupointToChibi(position) {
  const x = Number(position?.x || 0)
  const y = Number(position?.y || 0)
  const z = Number(position?.z || 0)

  if (y >= 58) {
    return { x: x * 1.48, y: 28 + (y - 58) * 2.72, z: z * 1.72 + Math.sign(z || FRONT) * 0.7 }
  }
  if (y >= -12) {
    const isArm = Math.abs(x) > 15
    return { x: x * (isArm ? 1.12 : 1.28), y: -23 + (y + 12) * 0.73, z: z * (isArm ? 1.28 : 1.8) + Math.sign(z || FRONT) * 0.55 }
  }
  return { x: x * 0.98, y: -91 + (y + 95) * (67 / 83), z: z * 1.42 + Math.sign(z || FRONT) * 0.5 }
}

export function mapAcupointToRealObj(position) {
  const sourceX = Number(position?.x || 0)
  const sourceY = Number(position?.y || 0)
  const sourceZ = Number(position?.z || 0)
  const direction = Math.abs(sourceZ) < 2 ? FRONT : Math.sign(sourceZ)

  if (sourceY >= 58) {
    const x = sourceX * 1.48
    const surface = 38 * Math.sqrt(Math.max(0.08, 1 - (x / 35) ** 2))
    return { x, y: 4 + (sourceY - 58) * 2.8, z: direction * surface }
  }

  if (sourceY >= -12) {
    const isArm = Math.abs(sourceX) > 18
    const x = sourceX * (isArm ? 1.45 : 1.25)
    const surface = isArm
      ? 13
      : 32 * Math.sqrt(Math.max(0.08, 1 - (x / 30) ** 2))
    return { x, y: -58 + (sourceY + 12) * 0.87, z: direction * surface }
  }

  const x = sourceX * 0.98
  return { x, y: -90 + (sourceY + 95) * (32 / 83), z: direction * 14 }
}
