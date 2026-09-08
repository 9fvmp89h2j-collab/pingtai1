var audioCtx = null

function getCtx() {
  if (!audioCtx) {
    try {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)()
    } catch (e) {
      return null
    }
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume()
  }
  return audioCtx
}

function playTone(freq, duration, type, volume, ramp) {
  var ctx = getCtx()
  if (!ctx) return
  var osc = ctx.createOscillator()
  var gain = ctx.createGain()
  osc.type = type || 'sine'
  osc.frequency.setValueAtTime(freq, ctx.currentTime)
  if (ramp) {
    osc.frequency.linearRampToValueAtTime(ramp, ctx.currentTime + duration)
  }
  gain.gain.setValueAtTime(volume || 0.15, ctx.currentTime)
  gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration)
  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.start(ctx.currentTime)
  osc.stop(ctx.currentTime + duration)
}

function playChord(freqs, duration, type, volume) {
  var ctx = getCtx()
  if (!ctx) return
  freqs.forEach(function(f) {
    playTone(f, duration, type, (volume || 0.15) / Math.sqrt(freqs.length))
  })
}

function playMelody(notes, baseDelay) {
  var ctx = getCtx()
  if (!ctx) return
  notes.forEach(function(note, i) {
    var t = ctx.currentTime + (baseDelay || 0.12) * i
    var osc = ctx.createOscillator()
    var gain = ctx.createGain()
    osc.type = note[2] || 'sine'
    osc.frequency.setValueAtTime(note[0], t)
    gain.gain.setValueAtTime(note[1] || 0.12, t)
    gain.gain.exponentialRampToValueAtTime(0.001, t + note[3] || 0.15)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start(t)
    osc.stop(t + (note[3] || 0.15))
  })
}

export function useSoundEffects() {
  function click() {
    playTone(800, 0.08, 'sine', 0.08, 1200)
  }

  function select() {
    playTone(600, 0.1, 'sine', 0.1, 900)
  }

  function place() {
    playTone(500, 0.12, 'triangle', 0.12, 700)
  }

  function remove() {
    playTone(400, 0.1, 'triangle', 0.08, 250)
  }

  function success() {
    playChord([523, 659, 784], 0.3, 'sine', 0.12)
  }

  function error() {
    playTone(200, 0.25, 'sawtooth', 0.06)
    setTimeout(function() { playTone(160, 0.3, 'sawtooth', 0.05) }, 150)
  }

  function match(count) {
    var base = 400 + count * 80
    playTone(base, 0.15, 'sine', 0.1, base * 1.5)
  }

  function comboSound(combo) {
    var base = 600 + combo * 60
    playChord([base, base * 1.25, base * 1.5], 0.2, 'triangle', 0.15)
  }

  function special() {
    playChord([400, 500, 600, 800], 0.4, 'triangle', 0.15)
    setTimeout(function() {
      playChord([600, 750, 900, 1200], 0.3, 'sine', 0.12)
    }, 200)
  }

  function warning() {
    playTone(880, 0.1, 'square', 0.06)
    setTimeout(function() { playTone(880, 0.1, 'square', 0.06) }, 200)
  }

  function hint() {
    playTone(1000, 0.08, 'sine', 0.08)
    setTimeout(function() { playTone(1200, 0.08, 'sine', 0.08) }, 80)
    setTimeout(function() { playTone(1400, 0.1, 'sine', 0.08) }, 160)
  }

  function clearAll() {
    playTone(300, 0.15, 'triangle', 0.08)
    setTimeout(function() { playTone(200, 0.2, 'triangle', 0.06) }, 100)
  }

  function roundComplete() {
    playChord([523, 659, 784, 1047], 0.5, 'sine', 0.1)
  }

  function victory() {
    playMelody([
      [523, 0.1, 'sine', 0.15],
      [587, 0.1, 'sine', 0.15],
      [659, 0.1, 'sine', 0.15],
      [784, 0.1, 'sine', 0.15],
      [880, 0.1, 'sine', 0.15],
      [1047, 0.15, 'sine', 0.3]
    ], 0.12)
  }

  function collect() {
    playChord([659, 784, 1047], 0.4, 'sine', 0.12)
  }

  function boardDrop() {
    playTone(150, 0.08, 'triangle', 0.05)
  }

  function boardSwap() {
    playTone(440, 0.06, 'sine', 0.06, 550)
  }

  function buttonHover() {
    playTone(1200, 0.04, 'sine', 0.04)
  }

  function pageTransition() {
    playTone(600, 0.15, 'sine', 0.08, 800)
  }

  return {
    click: click,
    select: select,
    place: place,
    remove: remove,
    success: success,
    error: error,
    match: match,
    comboSound: comboSound,
    special: special,
    warning: warning,
    hint: hint,
    clearAll: clearAll,
    roundComplete: roundComplete,
    victory: victory,
    collect: collect,
    boardDrop: boardDrop,
    boardSwap: boardSwap,
    buttonHover: buttonHover,
    pageTransition: pageTransition
  }
}