import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = (relative) => fs.readFileSync(path.join(root, relative), 'utf8')
const mainlineSource = read('src/data/mainline.js')
const mapLevelsSource = read('src/data/mapLevels.js')
const taskConfig = JSON.parse(read('src/data/task_config.json'))
const routerSource = read('src/router/index.js')
const taskSource = read('src/data/tasks.js')
const backendSource = read('../springboot/src/main/java/org/example/springboot/service/GameStateService.java')
const mainlineConfigSource = read('../springboot/src/main/java/org/example/springboot/service/MainlineConfigService.java')
const backendGameSource = `${backendSource}\n${mainlineConfigSource}`
const materialSource = read('src/data/materials.js')

const levelMatches = [...mainlineSource.matchAll(/id: '([^']+)'[\s\S]*?order: (\d+)[\s\S]*?route: '([^']+)'[\s\S]*?requiredTaskIds: \[([^\]]+)\]/g)]
const levels = levelMatches.map((match) => ({
  id: match[1], order: Number(match[2]), route: match[3],
  requiredTaskIds: [...match[4].matchAll(/'([^']+)'/g)].map((item) => item[1])
}))

const errors = []
if (levels.length !== 8) errors.push(`expected 8 mainline levels, found ${levels.length}`)
levels.forEach((level, index) => {
  if (level.order !== index + 1) errors.push(`level order is not sequential at ${level.id}`)
  if (!routerSource.includes(`path: '${level.route.slice(1)}'`) && !routerSource.includes(`path: '${level.route}'`)) {
    errors.push(`route is not declared in router: ${level.route}`)
  }
  level.requiredTaskIds.forEach((taskId) => {
    if (!mainlineConfigSource.includes(`"${taskId}"`) && !mainlineConfigSource.includes(`'${taskId}'`)) {
      errors.push(`mainline task is not handled by backend: ${taskId}`)
    }
  })
  if (!mainlineConfigSource.includes(`new LevelSeed("${level.id}", ${level.order}`) || !mainlineConfigSource.includes(`"${level.route}"`)) {
    errors.push(`backend map definition does not match frontend: ${level.id}`)
  }
})

const materialIds = new Set([...materialSource.matchAll(/id: '([^']+)'/g)].map((match) => match[1]))
const rewardAndCostIds = [
  ...backendGameSource.matchAll(/(?:new (?:RewardSpec|MaterialCost)|material)\("([a-z0-9-]+)"/g),
  ...[...taskSource.matchAll(/rewards:\s*\[([\s\S]*?)\]/g)]
    .flatMap((match) => [...match[1].matchAll(/id: '([^']+)'/g)])
].map((match) => match[1])
for (const materialId of [...new Set(rewardAndCostIds)]) {
  if (!materialIds.has(materialId)) errors.push(`reward or cost material is missing from registry: ${materialId}`)
}

if (taskConfig.mainline || taskConfig.mainlineSource !== 'src/data/mainline.js') {
  errors.push('task_config.json must reference mainline.js instead of redefining map levels')
}
if (!mapLevelsSource.includes("from './mainline'")) {
  errors.push('mapLevels.js must remain a compatibility re-export of mainline.js')
}
if (taskSource.includes('/meridian-river')) errors.push('stale /meridian-river route remains in tasks.js')
if (taskSource.includes('agency-archive')) errors.push('stale agency-archive task id remains in tasks.js')

if (errors.length) {
  console.error(errors.map((error) => `- ${error}`).join('\n'))
  process.exitCode = 1
} else {
  console.log(`Game consistency OK: ${levels.length} mainline levels, ${materialIds.size} registered material ids`)
}
