import html2canvas from 'html2canvas'
import { h } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { getCurrentUser, uploadUserCertificate } from '@/api/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

/**
 * 将荣誉证书内容渲染为 JPEG Blob（参考 vue-cert-poster + html2canvas）
 */
export async function renderCertificateToBlob({ displayName, levelName, userId }) {
  const wrap = document.createElement('div')
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const dateStr = `${y}年${d.getMonth() + 1}月${d.getDate()}日`
  const certNo = `QB-${userId ?? '0'}-${y}${m}${day}`

  Object.assign(wrap.style, {
    position: 'fixed',
    left: '-99999px',
    top: '0',
    width: '600px',
    height: '460px',
    boxSizing: 'border-box',
    padding: '44px 52px 36px',
    background: 'linear-gradient(165deg, #fffef9 0%, #faf3e6 55%, #f4ead4 100%)',
    border: '10px double #b8941f',
    fontFamily: '"Microsoft YaHei", "PingFang SC", "Hiragino Sans GB", sans-serif',
    color: '#332e2a',
    lineHeight: '1.45'
  })

  const title = document.createElement('div')
  title.textContent = '气血能量·九级荣誉证书'
  Object.assign(title.style, {
    textAlign: 'center',
    fontSize: '26px',
    fontWeight: '800',
    letterSpacing: '4px',
    marginBottom: '20px',
    color: '#6b5318'
  })
  wrap.appendChild(title)

  const greet = document.createElement('div')
  greet.textContent = `授予 ${displayName || '同学'}`
  Object.assign(greet.style, {
    textAlign: 'center',
    fontSize: '18px',
    fontWeight: '700',
    color: '#977412',
    marginBottom: '16px'
  })
  wrap.appendChild(greet)

  const body = document.createElement('div')
  body.textContent =
    `你在「小银针大魔法」平台持续学习与实践，已达成气血能量最高阶「${levelName || '针道传承荣耀使'}」。` +
    '特颁此证，以谢坚持，望继续探索针灸文化的魅力。'
  Object.assign(body.style, {
    fontSize: '15px',
    textIndent: '2em',
    marginBottom: '28px',
    minHeight: '120px'
  })
  wrap.appendChild(body)

  const no = document.createElement('div')
  no.textContent = `证书编号：${certNo}`
  Object.assign(no.style, {
    position: 'absolute',
    left: '52px',
    bottom: '36px',
    fontSize: '13px',
    color: '#5c534c'
  })
  wrap.appendChild(no)

  const sig = document.createElement('div')
  sig.textContent = '小银针大魔法平台\n敬授'
  Object.assign(sig.style, {
    position: 'absolute',
    right: '52px',
    bottom: '36px',
    fontSize: '14px',
    textAlign: 'right',
    whiteSpace: 'pre-line',
    color: '#5c534c'
  })
  wrap.appendChild(sig)

  const dateEl = document.createElement('div')
  dateEl.textContent = dateStr
  Object.assign(dateEl.style, {
    position: 'absolute',
    right: '52px',
    bottom: '96px',
    fontSize: '13px',
    color: '#5c534c'
  })
  wrap.appendChild(dateEl)

  document.body.appendChild(wrap)
  try {
    const canvas = await html2canvas(wrap, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#faf3e6',
      logging: false,
      scrollX: 0,
      scrollY: 0
    })
    return await new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (blob) resolve(blob)
          else reject(new Error('证书图片生成失败'))
        },
        'image/jpeg',
        0.92
      )
    })
  } finally {
    document.body.removeChild(wrap)
  }
}

export function previewCertificateImage(url) {
  const full = resolveMediaUrl(url)
  if (!full) {
    message.warning('暂无证书图片地址')
    return
  }
  Modal.info({
    title: '九级荣誉证书',
    icon: null,
    okText: '关闭',
    width: 720,
    centered: true,
    content: h('div', { style: { textAlign: 'center' } }, [
      h('img', {
        src: full,
        alt: '荣誉证书',
        style: { maxWidth: '100%', borderRadius: '6px', border: '1px solid #f0f0f0' }
      })
    ])
  })
}

/**
 * 九级用户：若已有 honor 则预览；否则生成海报并上传，再预览
 */
export async function openUserCertificateFlow(userStore) {
  const stopLoad = message.loading('加载中…', 0)
  try {
    let user = userStore.userInfo
    if (!user?.honor) {
      const fresh = await getCurrentUser({ showDefaultMsg: false })
      if (fresh?.honor) {
        userStore.updateUserInfo({ honor: fresh.honor })
      }
      user = fresh || user
    }
    const levelInfo = await userStore.ensureLevelInfo({ showDefaultMsg: false })
    stopLoad()

    if (user?.honor) {
      previewCertificateImage(user.honor)
      return
    }

    const lv = typeof levelInfo?.level === 'number' ? levelInfo.level : 0
    if (lv < 9) {
      message.warning('达到九级后即可生成并查看荣誉证书')
      return
    }

    const stopGen = message.loading('正在生成证书…', 0)
    try {
      const displayName = user?.name || user?.username || '同学'
      const levelName = levelInfo?.levelName || '针道传承荣耀使'
      const userId = user?.id ?? userStore.userId
      const blob = await renderCertificateToBlob({ displayName, levelName, userId })
      const file = new File([blob], 'certificate.jpg', { type: 'image/jpeg' })
      const data = await uploadUserCertificate(file, { showDefaultMsg: false })
      stopGen()
      if (data?.honor) {
        userStore.updateUserInfo({ ...userStore.userInfo, ...data })
        message.success('证书已保存')
        previewCertificateImage(data.honor)
      } else {
        message.warning('未返回证书路径，请稍后在我的信息中重试')
      }
    } catch (e) {
      stopGen()
      console.error(e)
      const msg = e?.message || (typeof e?.msg === 'string' ? e.msg : null) || '证书生成或上传失败'
      message.error(msg)
    }
  } catch (e) {
    stopLoad()
    console.error(e)
    message.error('加载失败')
  }
}
