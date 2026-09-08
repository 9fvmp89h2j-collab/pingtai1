import { createReadStream, existsSync, statSync } from 'node:fs'
import { createServer, request } from 'node:http'
import { extname, join, normalize } from 'node:path'
import { fileURLToPath } from 'node:url'
import { dirname } from 'node:path'

const root = dirname(fileURLToPath(import.meta.url))
const distDir = join(root, 'dist')
const port = 8800
const backendHost = '127.0.0.1'
const backendPort = 8889

const contentTypes = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.webp': 'image/webp',
  '.mp3': 'audio/mpeg',
  '.woff2': 'font/woff2'
}

function proxyToBackend(req, res) {
  const proxyReq = request(
    {
      hostname: backendHost,
      port: backendPort,
      path: req.url,
      method: req.method,
      headers: req.headers
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode || 502, proxyRes.headers)
      proxyRes.pipe(res)
    }
  )

  proxyReq.on('error', () => {
    res.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end('Backend proxy failed')
  })

  req.pipe(proxyReq)
}

function serveFile(req, res) {
  const url = new URL(req.url || '/', `http://127.0.0.1:${port}`)
  const decodedPath = decodeURIComponent(url.pathname)
  const safePath = normalize(decodedPath).replace(/^(\.\.[/\\])+/, '')
  let filePath = join(distDir, safePath)

  if (!filePath.startsWith(distDir)) {
    res.writeHead(403)
    res.end('Forbidden')
    return
  }

  if (!existsSync(filePath) || statSync(filePath).isDirectory()) {
    filePath = join(distDir, 'index.html')
  }

  const ext = extname(filePath).toLowerCase()
  res.writeHead(200, { 'Content-Type': contentTypes[ext] || 'application/octet-stream' })
  createReadStream(filePath).pipe(res)
}

createServer((req, res) => {
  if (req.url?.startsWith('/api') || req.url?.startsWith('/files')) {
    proxyToBackend(req, res)
    return
  }
  serveFile(req, res)
}).listen(port, '127.0.0.1', () => {
  console.log(`Frontend static server ready: http://127.0.0.1:${port}/`)
})
