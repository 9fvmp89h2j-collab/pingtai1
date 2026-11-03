import { chromium } from '../vue3/node_modules/playwright/index.mjs'
import fs from 'node:fs/promises'
import path from 'node:path'

const outputDir = path.resolve('softcopy_application', 'screenshots')
await fs.mkdir(outputDir, { recursive: true })

const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
})
const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, deviceScaleFactor: 1 })

try {
  console.log('login')
  await page.goto('http://127.0.0.1:8800/#/auth/login', { waitUntil: 'networkidle' })
  await page.locator('#auth-username').fill('admin')
  await page.locator('#auth-password').fill('123456')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForFunction(() => Boolean(JSON.parse(localStorage.getItem('token') || 'null')))

  await page.goto('http://127.0.0.1:8800/#/copper-man', { waitUntil: 'networkidle' })
  await page.locator('.model-viewer canvas').waitFor({ state: 'visible', timeout: 30000 })
  await page.getByText('身体地图出现了神秘星点！').waitFor({ state: 'visible', timeout: 30000 })
  await page.screenshot({ path: path.join(outputDir, '01-3D小铜人探案欢迎页.png') })

  console.log('main')
  await page.getByRole('button', { name: /开始观察/ }).click()
  const safetyAck = page.getByRole('button', { name: '我知道了' })
  if (await safetyAck.isVisible().catch(() => false)) {
    await page.screenshot({ path: path.join(outputDir, '02-学习安全边界提示.png') })
    await safetyAck.click()
  }
  await page.waitForTimeout(2500)
  await page.screenshot({ path: path.join(outputDir, '03-3D小铜人互动学习主界面.png') })

  console.log('atlas')
  await page.locator('.atlas-launch').click({ force: true })
  await page.locator('.atlas-book').waitFor({ state: 'visible', timeout: 15000 })
  await page.waitForTimeout(2000)
  await page.locator('.atlas-card').first().click()
  await page.waitForTimeout(800)
  await page.screenshot({ path: path.join(outputDir, '04-百穴图鉴检索与详情页.png') })
  console.log('done')
} finally {
  await browser.close()
}
