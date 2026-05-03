const CHINESE_CHAR_RE = /[\u3400-\u9fff]/
const LATIN_OR_TONE_RE = /[A-Za-z\u00fc\u01d6\u01d8\u01da\u01dc\u0101\u00e1\u01ce\u00e0\u0113\u00e9\u011b\u00e8\u012b\u00ed\u01d0\u00ec\u014d\u00f3\u01d2\u00f2\u016b\u00fa\u01d4\u00f9]/

const PINYIN_OPTIONS = {
  style: 'tone',
  segment: true,
  heteronym: false
}

let pinyinLoader = null
let pinyinRuntime = null

function isChineseChar(char) {
  return CHINESE_CHAR_RE.test(char)
}

function normalizePinyin(value) {
  const text = String(value || '').trim().replace(/\s+/g, ' ')
  if (!text || !LATIN_OR_TONE_RE.test(text) || text.includes('?')) {
    return ''
  }

  return text
}

export async function ensureStoryPinyinReady() {
  if (pinyinRuntime) return pinyinRuntime
  if (!pinyinLoader) {
    pinyinLoader = import('pinyin').then((module) => {
      pinyinRuntime = module.default || module.pinyin || module
      return pinyinRuntime
    })
  }
  return pinyinLoader
}

function getRunPinyin(runText) {
  if (!pinyinRuntime) {
    return Array.from(runText).map(() => '')
  }

  try {
    const result = pinyinRuntime(runText, PINYIN_OPTIONS)
    return Array.from(runText).map((char, index) => normalizePinyin(result[index]?.[0] || char))
  } catch {
    return Array.from(runText).map(() => '')
  }
}

function buildAutoSegments(chunk, offset) {
  const segments = []
  let textBuffer = ''
  let textStart = offset
  let chineseBuffer = ''
  let chineseStart = offset

  const flushText = () => {
    if (!textBuffer) return
    segments.push({
      type: 'text',
      key: `text-${textStart}`,
      text: textBuffer
    })
    textBuffer = ''
  }

  const flushChinese = () => {
    if (!chineseBuffer) return
    const pinyinList = getRunPinyin(chineseBuffer)
    Array.from(chineseBuffer).forEach((char, index) => {
      segments.push({
        type: 'char',
        key: `char-${chineseStart + index}`,
        text: char,
        pinyin: pinyinList[index] || ''
      })
    })
    chineseBuffer = ''
  }

  Array.from(chunk).forEach((char, index) => {
    const absoluteIndex = offset + index
    if (isChineseChar(char)) {
      flushText()
      if (!chineseBuffer) {
        chineseStart = absoluteIndex
      }
      chineseBuffer += char
      return
    }

    flushChinese()
    if (!textBuffer) {
      textStart = absoluteIndex
    }
    textBuffer += char
  })

  flushChinese()
  flushText()

  return segments
}

function findMatchedTerm(text, glossary, startIndex) {
  for (const entry of glossary) {
    if (text.startsWith(entry.word, startIndex)) {
      return entry
    }
  }
  return null
}

export function buildStoryReadingSegments(text, glossary = [], fullPinyin = false) {
  if (!text) return []

  const sortedGlossary = [...glossary].sort((a, b) => b.word.length - a.word.length)
  const segments = []
  let cursor = 0

  while (cursor < text.length) {
    const matched = findMatchedTerm(text, sortedGlossary, cursor)

    if (matched) {
      segments.push({
        type: 'term',
        key: `term-${cursor}-${matched.word}`,
        text: matched.word,
        pinyin: matched.pinyin,
        entry: matched
      })
      cursor += matched.word.length
      continue
    }

    let next = cursor + 1
    while (next < text.length && !findMatchedTerm(text, sortedGlossary, next)) {
      next += 1
    }

    const chunk = text.slice(cursor, next)
    if (fullPinyin) {
      segments.push(...buildAutoSegments(chunk, cursor))
    } else {
      segments.push({
        type: 'text',
        key: `text-${cursor}`,
        text: chunk
      })
    }

    cursor = next
  }

  return segments
}
