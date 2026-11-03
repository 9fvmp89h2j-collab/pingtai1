/**
 * 解析图片/媒体 URL：支持 http(s)、站点路径 /files/...、以及 src/assets 下文件（含误填的本机路径，按文件名匹配）
 */
const ASSET_URL_BY_FILENAME = Object.fromEntries(
  Object.entries(
    import.meta.glob('@/assets/**/*.{png,jpg,jpeg,gif,webp,svg}', { as: 'url', eager: true })
  ).map(([fullPath, url]) => [fullPath.split('/').pop(), url])
)

function encodePathSegments(p) {
  let s = p.replace(/\\/g, '/').trim()
  if (!s) return ''
  if (!/^https?:\/\//i.test(s) && !s.startsWith('/')) s = `/${s}`
  return s
    .split('/')
    .map((seg) => (seg ? encodeURIComponent(seg) : ''))
    .join('/')
}

export function resolveMediaUrl(path) {
  if (!path || typeof path !== 'string') return ''
  let p = path.trim()
  if (!p) return ''
  if (/^https?:\/\//i.test(p)) return p

  p = p.replace(/\\/g, '/')
  // Some older API responses included the frontend proxy prefix. Keep media
  // URLs relative to the current host so they also work after deployment.
  p = p.replace(/^\/api(?=\/files\/)/i, '')

  // 后端上传目录：库中常为 files/bussiness/...（无首斜杠），不能与 src/assets 同名文件误匹配
  const isServerFilesPath =
    /^files\//i.test(p) || p.includes('/files/') || p.startsWith('/files/')
  if (!isServerFilesPath) {
    const baseName = p.split('/').pop() || ''
    if (baseName && ASSET_URL_BY_FILENAME[baseName]) {
      return ASSET_URL_BY_FILENAME[baseName]
    }
  }

  // 旧数据库中的开发机绝对路径无法由浏览器访问，交给调用方使用本地兜底图。
  if (/^[a-z]:\//i.test(p)) return ''

  return encodePathSegments(p)
}
