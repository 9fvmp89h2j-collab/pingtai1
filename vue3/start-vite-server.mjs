import { appendFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'vite'

const root = dirname(fileURLToPath(import.meta.url))
const logPath = join(root, 'frontend-dev.log')

function log(message) {
  appendFileSync(logPath, `[${new Date().toISOString()}] ${message}\n`, 'utf8')
}

try {
  writeFileSync(logPath, '', 'utf8')
  process.chdir(root)

  const server = await createServer({
    configFile: join(root, 'vite.config.js'),
    server: {
      host: '127.0.0.1',
      port: 8800,
      strictPort: true,
      open: false,
    },
  })

  await server.listen()

  const localUrls = server.resolvedUrls?.local?.join(', ') || 'http://127.0.0.1:8800/'
  log(`Vite ready: ${localUrls}`)

  setInterval(() => {}, 60 * 60 * 1000)
} catch (error) {
  log(error?.stack || String(error))
  process.exitCode = 1
}
