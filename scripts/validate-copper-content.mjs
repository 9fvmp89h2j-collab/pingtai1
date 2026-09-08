import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const dataSql = fs.readFileSync(path.join(root, 'springboot', 'src', 'main', 'resources', 'data.sql'), 'utf8')
const migrationSql = fs.readFileSync(path.join(root, 'springboot', 'src', 'main', 'resources', 'db', 'migration', 'V4__copper_content_center.sql'), 'utf8')
const traditionalUseSql = fs.readFileSync(path.join(root, 'springboot', 'src', 'main', 'resources', 'db', 'migration', 'V6__child_traditional_use.sql'), 'utf8')

function assert(condition, message) {
  if (!condition) throw new Error(message)
}

const rows = dataSql.split(/\r?\n/).filter((line) => line.startsWith('INSERT INTO `acupoint_knowledge`'))
const modeled = rows.filter((line) => line.includes('转动3D小铜人'))
const atlasOnly = rows.filter((line) => line.includes('还没有3D坐标'))
const safetyCopy = '只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长。'
const dangerousChildCopy = /(按揉|按摩|刺激穴位|针刺|扎针|治疗|治愈|疗效|自己取穴)/

assert(rows.length === 361, `Expected 361 acupoints, found ${rows.length}`)
assert(modeled.length === 100, `Expected 100 3D-ready acupoints, found ${modeled.length}`)
assert(atlasOnly.length === 261, `Expected 261 atlas-only acupoints, found ${atlasOnly.length}`)
assert(rows.every((line) => line.includes(safetyCopy)), 'Every acupoint must contain the permanent safety reminder')
assert(rows.every((line) => !dangerousChildCopy.test(line.split("'GB/T")[0].split("'项目现有穴位目录")[0])), 'Unsafe child-facing wording found')
assert(traditionalUseSql.includes('child_traditional_use'), 'Missing child traditional-use field migration')
assert(traditionalUseSql.includes('traditional_use_source_name'), 'Missing traditional-use source migration')
assert(traditionalUseSql.includes('position_x IS NOT NULL AND position_y IS NOT NULL AND position_z IS NOT NULL'), 'Traditional-use seed must be limited to the 100 modeled points')
assert(!dangerousChildCopy.test(traditionalUseSql), 'Unsafe traditional-use wording found')

const storyCodes = ['missing-bamboo', 'exam-copper-man', 'broken-star-river', 'silent-safety-bell']
for (const code of storyCodes) {
  const start = migrationSql.indexOf(`('${code}',`)
  assert(start >= 0, `Missing seeded story: ${code}`)
  const end = migrationSql.indexOf('), CURRENT_TIMESTAMP)', start)
  const story = migrationSql.slice(start, end > start ? end : start + 10000)
  for (const section of ["'pages'", "'clues'", "'reasoning'", "'safety'", "'rewards'"]) {
    assert(story.includes(section), `${code} is missing ${section}`)
  }
}

console.log('Copper content validation passed: 361 acupoints (100 3D-ready with child traditional-use fields + 261 atlas-only), 4 complete stories.')
