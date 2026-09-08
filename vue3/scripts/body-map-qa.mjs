import { chromium } from '@playwright/test'
import { mkdir, readFile } from 'node:fs/promises'
import { resolve } from 'node:path'

const baseUrl = process.env.BODY_MAP_QA_URL || 'http://127.0.0.1:5173/#/body-map'
const outputDir = resolve('../output/body-map-qa')
const referencePath = process.env.BODY_MAP_QA_REFERENCE || resolve('../../预览/身体地图.png')
const executablePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'

const areaRows = [
  ['胸部', 'LU'], ['肩臂部', 'LU'], ['前臂与肘部', 'LI'], ['手腕部', 'LI'],
  ['头面与颈部', 'ST'], ['腹部', 'ST'], ['髋腿部', 'ST'], ['踝足部', 'ST']
]
const mockAcupoints = Array.from({ length: 100 }, (_, index) => {
  const [bodyArea, meridianCode] = areaRows[index % areaRows.length]
  const number = index + 1
  const name = index === 0 ? '中府' : index === 1 ? '合谷' : index === 2 ? '中脘' : index === 3 ? '足三里' : `测试穴位${number}`
  return {
    code: `${meridianCode}-${number}`,
    pointNumber: number,
    name,
    pinyin: `Test ${number}`,
    meridianCode,
    meridianName: `${meridianCode}测试经络`,
    bodyArea,
    regionCode: { '胸部': 'chest', '肩臂部': 'shoulder', '前臂与肘部': 'forearm', '手腕部': 'wrist', '头面与颈部': 'head', '腹部': 'belly', '髋腿部': 'leg', '踝足部': 'ankle' }[bodyArea],
    bodyView: ['肩臂部', '前臂与肘部', '髋腿部'].includes(bodyArea) ? 'back' : 'front',
    placementHint: `先看${['肩臂部', '前臂与肘部', '髋腿部'].includes(bodyArea) ? '右边的背面人物' : '左边的正面人物'}，再点击“${bodyArea}”圆圈，沿着箭头把“${name}星”送回这个身体区域。`,
    childMapDescription: `“${name}”在身体地图中属于${bodyArea}。这里只判断身体大区域。`,
    childDescription: `${name}是经络文化地图上的一颗星。`,
    childTraditionalUse: `传统认识中，“${name}”常与身体舒适等身体话题相关。这里只了解传统文化。`,
    safetyTip: '只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长。',
    traditionalUseSourceName: '经络腧穴学课程资料（儿童化改写）',
    positionX: index,
    positionY: index + 1,
    positionZ: index + 2
  }
})

await mkdir(outputDir, { recursive: true })
const browser = await chromium.launch({ headless: true, executablePath })

async function preparePage(viewport) {
  const page = await browser.newPage({ viewport, deviceScaleFactor: 1 })
  const consoleErrors = []
  page.on('console', (entry) => {
    if (entry.type() === 'error') consoleErrors.push(entry.text())
  })
  page.on('pageerror', (error) => consoleErrors.push(error.message))
  await page.addInitScript(() => {
    localStorage.setItem('token', JSON.stringify('body-map-qa-token'))
    localStorage.setItem('userInfo', JSON.stringify({ id: 99001, username: 'DPF', nickname: 'DPF', userType: 'ADMIN' }))
  })
  await page.route('**/api/**', async (route) => {
    const url = route.request().url()
    if (!new URL(url).pathname.startsWith('/api/')) {
      await route.continue()
      return
    }
    const data = url.includes('/acupuncture/body-map/acupoints')
      ? mockAcupoints
      : url.includes('/game/progress/events') ? { state: null } : {}
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: '200', data }) })
  })
  return { page, consoleErrors }
}

const desktop = await preparePage({ width: 1672, height: 941 })
await desktop.page.goto(baseUrl, { waitUntil: 'networkidle' })
await desktop.page.getByRole('heading', { name: '认识身体区域，把穴位星星送回家' }).waitFor()
const desktopBodyMap = await desktop.page.evaluate(() => {
  const stages = [...document.querySelectorAll('[data-body-view]')]
  return stages.map((stage) => {
    const box = stage.getBoundingClientRect()
    const image = stage.querySelector('img')
    const hotspots = [...stage.querySelectorAll('.region-hotspot')]
    return {
      view: stage.dataset.bodyView,
      box: { left: box.left, top: box.top, right: box.right, bottom: box.bottom },
      imageLoaded: Boolean(image?.complete && image.naturalWidth && image.naturalHeight),
      hotspotCount: hotspots.length,
      arrowCount: stage.querySelectorAll('.hotspot-arrow').length,
      hotspotsInside: hotspots.every((hotspot) => {
        const point = hotspot.getBoundingClientRect()
        const centerX = point.left + point.width / 2
        const centerY = point.top + point.height / 2
        return centerX >= box.left && centerX <= box.right && centerY >= box.top && centerY <= box.bottom
      })
    }
  })
})
if (desktopBodyMap.length !== 2 || desktopBodyMap.some((view) => !view.imageLoaded || !view.hotspotsInside)) {
  throw new Error(`Desktop front/back body map invalid: ${JSON.stringify(desktopBodyMap)}`)
}
if (desktopBodyMap.find((view) => view.view === 'front')?.hotspotCount !== 5 || desktopBodyMap.find((view) => view.view === 'back')?.hotspotCount !== 3) {
  throw new Error(`Expected 5 front and 3 back hotspots: ${JSON.stringify(desktopBodyMap)}`)
}
if (desktopBodyMap.reduce((sum, view) => sum + view.arrowCount, 0) !== 8) throw new Error('Expected one direction arrow for every hotspot')
if (desktopBodyMap[0].box.left >= desktopBodyMap[1].box.left) throw new Error('Desktop body views are not arranged front-left/back-right')
const desktopOverflow = await desktop.page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
if (desktopOverflow > 1) throw new Error(`Desktop horizontal overflow: ${desktopOverflow}px`)
await desktop.page.screenshot({ path: resolve(outputDir, 'body-map-desktop-1672x941.png'), fullPage: true })

await desktop.page.getByText('归位提示', { exact: true }).first().waitFor()
await desktop.page.getByText('每张卡片都会告诉你要观察的身体区域').waitFor()
if (await desktop.page.getByText(/转动3D小铜人/).count()) throw new Error('Body map still displays the 3D copper-man instruction')
const desktopCards = await desktop.page.evaluate(() => {
  const grid = document.querySelector('.point-grid')
  const card = document.querySelector('.point-card')
  return {
    count: document.querySelectorAll('.point-card').length,
    columns: getComputedStyle(grid).gridTemplateColumns.split(' ').length,
    titleSize: Number.parseFloat(getComputedStyle(card.querySelector('.point-copy strong')).fontSize),
    guideSize: Number.parseFloat(getComputedStyle(card.querySelector('.point-guide')).fontSize)
  }
})
if (desktopCards.count !== 4 || desktopCards.columns !== 2 || desktopCards.titleSize < 24 || desktopCards.guideSize < 13) {
  throw new Error(`Desktop cards are not a readable 2x2 grid: ${JSON.stringify(desktopCards)}`)
}

await desktop.page.getByRole('button', { name: '点亮这个区域' }).click()
await desktop.page.getByRole('button', { name: /中府/ }).click()
await desktop.page.getByText('读卡片上的“归位提示”').waitFor()
await desktop.page.getByText('点击地图的“胸部”').waitFor()
await desktop.page.getByRole('button', { name: /把中府送到胸部/ }).click()
await desktop.page.getByRole('heading', { name: '中府' }).waitFor()
await desktop.page.getByText('在身体地图怎么归位', { exact: true }).waitFor()
await desktop.page.getByText('身体地图知识', { exact: true }).waitFor()
await desktop.page.getByText('传统用途', { exact: true }).waitFor()
await desktop.page.screenshot({ path: resolve(outputDir, 'body-map-knowledge-dialog.png'), fullPage: true })
await desktop.page.getByRole('button', { name: '记住了，继续探案' }).click()
await desktop.page.getByText('2 / 10').waitFor()
await desktop.page.reload({ waitUntil: 'networkidle' })
await desktop.page.getByText('2 / 10').waitFor()
await desktop.page.screenshot({ path: resolve(outputDir, 'body-map-desktop-progress.png'), fullPage: true })

const referenceViewport = await preparePage({ width: 1265, height: 871 })
await referenceViewport.page.goto(baseUrl, { waitUntil: 'networkidle' })
await referenceViewport.page.getByRole('heading', { name: '认识身体区域，把穴位星星送回家' }).waitFor()
await referenceViewport.page.screenshot({ path: resolve(outputDir, 'body-map-reference-viewport-1265x871.png') })
await referenceViewport.page.close()

const comparison = await browser.newPage({ viewport: { width: 2600, height: 940 }, deviceScaleFactor: 1 })
const referenceData = (await readFile(referencePath)).toString('base64')
const implementationData = (await readFile(resolve(outputDir, 'body-map-reference-viewport-1265x871.png'))).toString('base64')
await comparison.setContent(`
  <style>body{margin:0;padding:18px;background:#2d241c;color:#fff;font:700 16px sans-serif}.grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}.label{margin-bottom:8px}img{display:block;width:100%;aspect-ratio:1265/871;object-fit:contain;background:#000}</style>
  <div class="grid"><div><div class="label">用户参考图 · 1265×871</div><img src="data:image/png;base64,${referenceData}"></div><div><div class="label">正背双人体实现 · 1265×871</div><img src="data:image/png;base64,${implementationData}"></div></div>
`)
await comparison.screenshot({ path: resolve(outputDir, 'body-map-comparison.png') })
await comparison.close()

const mobile = await preparePage({ width: 390, height: 844 })
await mobile.page.goto(baseUrl, { waitUntil: 'networkidle' })
await mobile.page.getByRole('heading', { name: '认识身体区域，把穴位星星送回家' }).waitFor()
const mobileBodyMap = await mobile.page.evaluate(() => [...document.querySelectorAll('[data-body-view]')].map((stage) => {
  const box = stage.getBoundingClientRect()
  return { view: stage.dataset.bodyView, top: box.top, bottom: box.bottom, width: box.width }
}))
if (mobileBodyMap.length !== 2 || mobileBodyMap[0].top >= mobileBodyMap[1].top || mobileBodyMap.some((view) => view.width < 280)) {
  throw new Error(`Mobile body views are not large and vertically stacked: ${JSON.stringify(mobileBodyMap)}`)
}
const mobileOverflow = await mobile.page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
if (mobileOverflow > 1) throw new Error(`Mobile horizontal overflow: ${mobileOverflow}px`)
const mobileCardColumns = await mobile.page.locator('.point-grid').evaluate((grid) => getComputedStyle(grid).gridTemplateColumns.split(' ').length)
if (mobileCardColumns !== 1) throw new Error(`Expected one readable card per row on mobile, got ${mobileCardColumns}`)
await mobile.page.screenshot({ path: resolve(outputDir, 'body-map-mobile-390x844.png'), fullPage: true })

const responsiveChecks = []
const responsiveErrors = []
for (const viewport of [{ width: 1440, height: 900 }, { width: 1024, height: 768 }]) {
  const check = await preparePage(viewport)
  await check.page.goto(baseUrl, { waitUntil: 'networkidle' })
  await check.page.getByRole('heading', { name: '认识身体区域，把穴位星星送回家' }).waitFor()
  const overflow = await check.page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
  if (overflow > 1) throw new Error(`${viewport.width}px viewport horizontal overflow: ${overflow}px`)
  await check.page.screenshot({ path: resolve(outputDir, `body-map-${viewport.width}x${viewport.height}.png`), fullPage: true })
  responsiveChecks.push({ viewport, overflow })
  responsiveErrors.push(...check.consoleErrors)
  await check.page.close()
}

const defaultNav = await preparePage({ width: 1200, height: 800 })
await defaultNav.page.goto('http://127.0.0.1:5173/#/doctor-story', { waitUntil: 'networkidle' })
const defaultHeader = defaultNav.page.locator('.xinglin-navbar')
await defaultHeader.waitFor()
const defaultNavHeight = await defaultHeader.evaluate((element) => element.getBoundingClientRect().height)
if (defaultNavHeight !== 84 || await defaultHeader.evaluate((element) => element.classList.contains('xinglin-navbar--body-map'))) {
  throw new Error(`Default navbar regression: height=${defaultNavHeight}`)
}

const allErrors = [...desktop.consoleErrors, ...mobile.consoleErrors, ...responsiveErrors, ...defaultNav.consoleErrors]
if (allErrors.length) throw new Error(`Browser console errors:\n${allErrors.join('\n')}`)

console.log(JSON.stringify({
  desktopScreenshot: resolve(outputDir, 'body-map-desktop-1672x941.png'),
  comparisonScreenshot: resolve(outputDir, 'body-map-comparison.png'),
  progressScreenshot: resolve(outputDir, 'body-map-desktop-progress.png'),
  referenceViewportScreenshot: resolve(outputDir, 'body-map-reference-viewport-1265x871.png'),
  knowledgeScreenshot: resolve(outputDir, 'body-map-knowledge-dialog.png'),
  mobileScreenshot: resolve(outputDir, 'body-map-mobile-390x844.png'),
  desktopOverflow,
  mobileOverflow,
  desktopBodyMap,
  mobileBodyMap,
  responsiveChecks,
  defaultNavHeight,
  interactions: ['front and back images loaded', '5 front and 3 back hotspots mapped', '8 hotspot arrows rendered', '100 acupoints loaded', 'region learned', 'point selected', 'knowledge dialog shown', 'point placed', 'state restored after reload'],
  consoleErrors: 0
}, null, 2))

await browser.close()
