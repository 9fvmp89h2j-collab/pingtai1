import { chromium } from '@playwright/test'
import { mkdir } from 'node:fs/promises'
import { resolve } from 'node:path'

const outputDir = resolve('../output/copper-man-qa')
await mkdir(outputDir, { recursive: true })

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe'
})
const page = await browser.newPage({ viewport: { width: 1672, height: 941 }, deviceScaleFactor: 1 })
const errors = []
let objStatus = null

page.on('console', (entry) => {
  if (entry.type() === 'error') errors.push(entry.text())
})
page.on('pageerror', (error) => errors.push(error.message))
page.on('response', (response) => {
  if (new URL(response.url()).pathname.endsWith('/assets/copper-man/modelo/corpo.obj')) {
    objStatus = response.status()
  }
})

await page.addInitScript(() => {
  localStorage.setItem('token', JSON.stringify('copper-man-qa-token'))
  localStorage.setItem('userInfo', JSON.stringify({ id: 99001, username: 'DPF', nickname: 'DPF', userType: 'ADMIN' }))
})

await page.route('**/api/**', async (route) => {
  const url = new URL(route.request().url())
  if (!url.pathname.startsWith('/api/')) {
    await route.continue()
    return
  }

  const data = url.pathname.includes('/acupuncture/copper-man/daily-case') ? {
    persisted: true,
    caseDate: '2026-09-06',
    targetCodes: ['LU-1', 'LU-2', 'LU-3'],
    featuredPoints: [
      { code: 'LU-1', name: '中府', meridianName: '手太阴肺经', bodyArea: '胸部', childDescription: '观察胸前的穴位星点。', positionX: -13.2678, positionY: 46, positionZ: -1.1 },
      { code: 'LU-2', name: '云门', meridianName: '手太阴肺经', bodyArea: '胸部', childDescription: '观察锁骨下方的穴位星点。', positionX: -11.2678, positionY: 48.6, positionZ: -3.4 },
      { code: 'LU-3', name: '天府', meridianName: '手太阴肺经', bodyArea: '上臂', childDescription: '观察上臂的穴位星点。', positionX: -22.9678, positionY: 32.4, positionZ: -4.7 }
    ],
    discoveredCodes: [],
    copperTokens: 0,
    starSand: 0,
    completedCases: 0
  } : {}

  await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: '200', data }) })
})

await page.goto('http://127.0.0.1:5174/#/copper-man', { waitUntil: 'networkidle' })
await page.getByRole('button', { name: '开始观察' }).click()
const acknowledge = page.getByRole('button', { name: '我知道了' })
if (await acknowledge.count()) await acknowledge.click()
await page.getByText(/成人针灸铜人已就位/).waitFor({ timeout: 30000 })
await page.waitForTimeout(1200)

const canvas = page.locator('canvas[aria-label="可旋转的小铜人三维模型"]')
await canvas.waitFor()
const canvasSize = await canvas.evaluate((element) => ({ width: element.width, height: element.height }))
await page.screenshot({ path: resolve(outputDir, 'adult-copper-front.png'), fullPage: true })

await page.getByRole('button', { name: '背面' }).click()
await page.waitForTimeout(800)
await page.screenshot({ path: resolve(outputDir, 'adult-copper-back.png'), fullPage: true })

if (objStatus !== 200) throw new Error(`corpo.obj returned ${objStatus}`)
if (errors.length) throw new Error(`Browser console errors:\n${errors.join('\n')}`)

console.log(JSON.stringify({ objStatus, canvasSize, errors: errors.length, outputDir }, null, 2))
await browser.close()
