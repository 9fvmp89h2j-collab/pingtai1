import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import { fileURLToPath } from 'node:url'

const scriptDir = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(scriptDir, '..')
const dataDir = path.join(root, 'vue3', 'public', 'assets', 'copper-man', 'data')
const outputPath = path.join(root, 'springboot', 'src', 'main', 'resources', 'data.sql')
const context = { window: {} }

vm.createContext(context)
for (const file of ['acupoints-data.js', 'modeled-acupoint-locations.js', 'acupoint-positions.js']) {
  vm.runInContext(fs.readFileSync(path.join(dataDir, file), 'utf8'), context)
}

const acupointData = context.window.ACUPOINT_DATA
const locations = context.window.MODELED_ACUPOINT_LOCATIONS
const positions = context.window.ACUPOINT_POSITIONS
const positionByModelId = new Map(positions.map((item) => [item.modelId, item]))

function sql(value) {
  if (value === null || value === undefined || value === '') return 'NULL'
  return `'${String(value).replaceAll('\\', '\\\\').replaceAll("'", "''")}'`
}

function bodyAreaFor(location) {
  const view = location?.view || ''
  const standard = location?.standard || ''
  if (/手部|腕部|手|腕|掌|指/.test(view)) return '手腕部'
  if (/前臂|肘/.test(view)) return '前臂与肘部'
  if (/上臂|肩/.test(view)) return '肩臂部'
  if (/足|踝/.test(view)) return '踝足部'
  if (/下肢|腿|膝|髋/.test(view)) return '髋腿部'
  if (/腹/.test(view)) return '腹部'
  if (/胸/.test(view)) return '胸部'
  if (/颈|面|头/.test(view)) return '头面与颈部'

  if (/手|腕|掌|指/.test(standard)) return '手腕部'
  if (/前臂|肘/.test(standard)) return '前臂与肘部'
  if (/肩|上臂|腋/.test(standard)) return '肩臂部'
  if (/足|趾|踝|跟/.test(standard)) return '踝足部'
  if (/腿|膝|胫|股|髋|下肢/.test(standard)) return '髋腿部'
  if (/腹|脐/.test(standard)) return '腹部'
  if (/胸|乳|肋/.test(standard)) return '胸部'
  if (/面部|颈|项|耳|口|鼻|眉|额|颅/.test(standard)) return '头面与颈部'
  return '身体地图相应区域'
}

function friendlyView(location, bodyArea) {
  const view = String(location?.view || '').replace(/局部放大视图|视图/g, '').replaceAll('/', '、')
  return view || bodyArea
}

function copyIndex(point, index, length) {
  const seed = [...`${point.code}${point.name}`].reduce((sum, char) => sum + char.codePointAt(0), index)
  return seed % length
}

function childDescription(point, bodyArea, index, hasModelPosition) {
  const routeImages = ['星光路牌', '文化车站', '地图坐标', '经络小旗', '星河门牌', '探案印章']
  const image = routeImages[copyIndex(point, index, routeImages.length)]
  const modeledTemplates = [
    `${point.name}是${point.meridianName}的${image}。它告诉我们：这条文化路线会经过${bodyArea}。`,
    `小侦探找到了${point.name}。档案上写着“${point.meridianName}”，它的地图区域是${bodyArea}。`,
    `${point.code}是${point.name}的星点编号。记住编号和${point.meridianName}，就找到了它的文化档案。`,
    `点亮${point.name}后，${bodyArea}的地图多了一颗星。它是${point.meridianName}路线的一站。`,
    `${point.name}带着一张“${point.meridianName}”路线卡。把它收进档案，再认一认${bodyArea}。`,
    `今天的${image}叫${point.name}。它不是按钮，而是帮我们读懂${point.meridianName}的文化标记。`,
    `在${bodyArea}的星点档案里，${point.name}和${point.meridianName}站在同一条路线上。`,
    `小铜人把${point.name}标成${point.code}。这枚${image}帮我们记住${bodyArea}和${point.meridianName}。`,
    `${point.name}的档案有两个关键词：${bodyArea}和${point.meridianName}。把它们连起来，线索就完整了。`,
    `身体文化地图上，${point.name}是${bodyArea}的一枚${image}。它属于${point.meridianName}。`,
    `跟着${point.meridianName}路线，小侦探在${bodyArea}遇见了${point.name}。这是今天的文化新名字。`,
    `${point.name}对小侦探说：“请把我放进${point.meridianName}档案。我的地图标签是${bodyArea}。”`
  ]
  const catalogTemplates = [
    `${point.name}是${point.meridianName}的${image}。今天先认识它的名字，等待它以后加入3D地图。`,
    `${point.code}号档案属于${point.name}。它在${point.meridianName}的文化路线上，现在先住在图鉴里。`,
    `新名字${point.name}已收入图鉴。它的路线牌写着${point.meridianName}，暂时没有3D星点。`,
    `${point.name}带来一张${point.meridianName}文化卡。我们只记名字和路线，不在身体上寻找。`,
    `图鉴里的${image}叫${point.name}。它属于${point.meridianName}，还在等待3D地图坐标。`,
    `小侦探给${point.name}贴上${point.code}标签。看到${point.meridianName}这个路线名，档案就不会迷路。`,
    `${point.name}暂时是一颗“图鉴星”。先把它和${point.meridianName}放在一起，以后再见它的3D亮光点。`,
    `${point.name}的两个档案词是${point.code}和${point.meridianName}。现在它只出现在文化图鉴中。`
  ]
  const templates = hasModelPosition ? modeledTemplates : catalogTemplates
  return templates[copyIndex(point, index + point.number, templates.length)]
}

const allPoints = acupointData.points
  .map((point) => ({ ...point, position: positionByModelId.get(point.modelId), location: locations[point.code] }))

if (allPoints.length !== 361) {
  throw new Error(`Expected 361 acupoints in the existing catalog, found ${allPoints.length}`)
}

const modeledPoints = allPoints.filter((point) => point.position && point.location)

if (modeledPoints.length !== 100) {
  throw new Error(`Expected 100 modeled acupoints, found ${modeledPoints.length}`)
}

if (new Set(allPoints.map((point) => point.code)).size !== allPoints.length) {
  throw new Error('Duplicate acupoint codes found in modeled data')
}

const unclassifiedPoint = modeledPoints.find((point) => bodyAreaFor(point.location) === '身体地图相应区域')
if (unclassifiedPoint) {
  throw new Error(`Body area is not classified for ${unclassifiedPoint.code}`)
}

const statements = allPoints.map((point, index) => {
  const hasModelPosition = Boolean(point.position && point.location)
  const bodyArea = hasModelPosition ? bodyAreaFor(point.location) : '经络档案区'
  const view = friendlyView(point.location, bodyArea)
  const childLocation = hasModelPosition
    ? `转动3D小铜人，看向${view}。在${bodyArea}找到“${point.name}星”的发光点，点一下就完成观察。`
    : `“${point.name}星”现在住在${point.meridianName}图鉴里。它还没有3D坐标，先读名字和文化卡。`
  const description = childDescription(point, bodyArea, index, hasModelPosition)
  const safety = '只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长。'

  const values = [
    point.code,
    point.number,
    point.name,
    point.pinyin,
    point.meridianCode,
    point.meridianName,
    point.meridianEnglish,
    point.modelId,
    bodyArea,
    point.location?.standard,
    childLocation,
    description,
    safety,
    point.location?.source || '项目现有穴位目录',
    point.location?.sourceLink,
    point.position?.x,
    point.position?.y,
    point.position?.z,
    index + 1,
    1
  ].map((value, valueIndex) => [1, 15, 16, 17, 18, 19].includes(valueIndex)
    ? (value === null || value === undefined ? 'NULL' : String(value))
    : sql(value))

  return `INSERT INTO \`acupoint_knowledge\` (\`code\`, \`point_number\`, \`name\`, \`pinyin\`, \`meridian_code\`, \`meridian_name\`, \`meridian_english\`, \`model_id\`, \`body_area\`, \`standard_location\`, \`child_location\`, \`child_description\`, \`safety_tip\`, \`source_name\`, \`source_link\`, \`position_x\`, \`position_y\`, \`position_z\`, \`sort_order\`, \`enabled\`) VALUES (${values.join(', ')}) ON DUPLICATE KEY UPDATE \`point_number\`=VALUES(\`point_number\`), \`name\`=VALUES(\`name\`), \`pinyin\`=VALUES(\`pinyin\`), \`meridian_code\`=VALUES(\`meridian_code\`), \`meridian_name\`=VALUES(\`meridian_name\`), \`meridian_english\`=VALUES(\`meridian_english\`), \`model_id\`=VALUES(\`model_id\`), \`body_area\`=VALUES(\`body_area\`), \`standard_location\`=VALUES(\`standard_location\`), \`child_location\`=VALUES(\`child_location\`), \`child_description\`=VALUES(\`child_description\`), \`safety_tip\`=VALUES(\`safety_tip\`), \`source_name\`=VALUES(\`source_name\`), \`source_link\`=VALUES(\`source_link\`), \`position_x\`=VALUES(\`position_x\`), \`position_y\`=VALUES(\`position_y\`), \`position_z\`=VALUES(\`position_z\`), \`sort_order\`=VALUES(\`sort_order\`), \`enabled\`=VALUES(\`enabled\`);`
})

const header = [
  '-- Generated by scripts/generate-copper-man-seed.mjs.',
  '-- Source: all 361 acupoints in the existing project catalog; 100 currently have 3D positions and GB/T 12346-2021 teaching metadata.',
  '-- Child-facing copy intentionally excludes treatment claims and needle-operation guidance.',
  ''
].join('\n')

const revisionSeed = `
INSERT INTO copper_acupoint_revision (acupoint_code, version_no, status, payload_json, published_at)
SELECT a.code, 1, 'PUBLISHED', JSON_OBJECT(
  'code', a.code, 'pointNumber', a.point_number, 'name', a.name, 'pinyin', a.pinyin,
  'meridianCode', a.meridian_code, 'meridianName', a.meridian_name,
  'meridianEnglish', a.meridian_english, 'modelId', a.model_id, 'bodyArea', a.body_area,
  'standardLocation', a.standard_location, 'childLocation', a.child_location,
  'childDescription', a.child_description, 'safetyTip', a.safety_tip,
  'sourceName', a.source_name, 'sourceLink', a.source_link,
  'positionX', a.position_x, 'positionY', a.position_y, 'positionZ', a.position_z,
  'sortOrder', a.sort_order, 'enabled', a.enabled
), CURRENT_TIMESTAMP
FROM acupoint_knowledge a
WHERE NOT EXISTS (
  SELECT 1 FROM copper_acupoint_revision r
  WHERE r.acupoint_code = a.code AND r.version_no = 1
);
`

fs.writeFileSync(outputPath, `${header}${statements.join('\n')}\n${revisionSeed}`, 'utf8')
console.log(`Wrote ${allPoints.length} acupoints (${modeledPoints.length} with 3D positions) to ${outputPath}`)
