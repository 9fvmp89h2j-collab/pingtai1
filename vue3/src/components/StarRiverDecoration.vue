<template>
  <div class="star-river-deco" aria-hidden="true">
    <!-- 深空星云光晕 -->
    <div class="nebula nebula-1"></div>
    <div class="nebula nebula-2"></div>
    <div class="nebula nebula-3"></div>

    <!-- 经络流动线（更淡更自然） -->
    <svg class="meridian-lines" viewBox="0 0 1440 900" preserveAspectRatio="none">
      <defs>
        <linearGradient id="ml-grad-1" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="rgba(84,182,161,0.08)" />
          <stop offset="50%" stop-color="rgba(90,167,216,0.14)" />
          <stop offset="100%" stop-color="rgba(84,182,161,0.04)" />
        </linearGradient>
        <linearGradient id="ml-grad-2" x1="100%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="rgba(184,115,51,0.08)" />
          <stop offset="50%" stop-color="rgba(244,201,93,0.16)" />
          <stop offset="100%" stop-color="rgba(184,115,51,0.04)" />
        </linearGradient>
        <linearGradient id="ml-grad-3" x1="0%" y1="50%" x2="100%" y2="50%">
          <stop offset="0%" stop-color="rgba(84,182,161,0.07)" />
          <stop offset="50%" stop-color="rgba(120,205,184,0.14)" />
          <stop offset="100%" stop-color="rgba(84,182,161,0.03)" />
        </linearGradient>
      </defs>
      <path class="meridian-path mp-1"
        d="M-50,140 C200,90 320,320 500,180 S720,100 960,240 S1200,160 1500,280"
        stroke="url(#ml-grad-1)" stroke-width="2" fill="none" />
      <path class="meridian-path mp-2"
        d="M-50,400 C180,460 350,260 550,420 S800,520 1020,380 S1300,470 1500,420"
        stroke="url(#ml-grad-2)" stroke-width="1.8" fill="none" />
      <path class="meridian-path mp-3"
        d="M-50,620 C240,560 380,740 600,600 S850,670 1080,570 S1340,640 1500,600"
        stroke="url(#ml-grad-3)" stroke-width="1.8" fill="none" />

      <!-- 经络穴位星点（稀疏、大小不一） -->
      <circle v-for="p in acupointStars" :key="p.id"
        :cx="p.x" :cy="p.y" :r="p.r" :fill="p.fill" :opacity="p.op"
        class="acupoint-star" :style="{ animationDelay: p.delay + 's', animationDuration: p.dur + 's' }" />
    </svg>

    <!-- 星尘（极小星点铺底） -->
    <div class="stars-layer stardust">
      <span v-for="i in 60" :key="'sd-'+i"
        class="stardust-dot"
        :style="{
          left: randomPct(i, 0),
          top: randomPct(i, 1),
          animationDelay: randomPct(i, 2),
          animationDuration: (2 + (i % 3) * 0.6) + 's'
        }"
      ></span>
    </div>

    <!-- 普通星星（自然分布） -->
    <div class="stars-layer">
      <span v-for="i in 50" :key="'ns-'+i"
        class="natural-star"
        :style="{
          left: randomPct(i + 100, 0),
          top: randomPct(i + 100, 1),
          animationDelay: randomPct(i + 100, 2),
          animationDuration: (2.5 + (i % 6) * 0.5) + 's',
          width: (1.5 + (i % 4) * 0.8) + 'px',
          height: (1.5 + (i % 4) * 0.8) + 'px',
          opacity: (0.3 + (i % 5) * 0.12)
        }"
      ></span>
    </div>

    <!-- 亮星团簇（几个小星座） -->
    <div class="stars-layer">
      <span v-for="i in 12" :key="'bs-'+i"
        class="bright-dot"
        :style="{
          left: randomPct(i + 200, 0),
          top: randomPct(i + 200, 1),
          animationDelay: randomPct(i + 200, 2),
          animationDuration: (3 + (i % 4) * 0.8) + 's',
          width: (4 + (i % 4)) + 'px',
          height: (4 + (i % 4)) + 'px',
        }"
      ></span>
    </div>

    <!-- 流星（少量、随机间隔） -->
    <div class="shooting-stars">
      <span v-for="i in 3" :key="'sh-'+i"
        class="shooting-star"
        :style="{
          left: randomPct(i + 300, 0),
          top: (15 + i * 18) + '%',
          animationDelay: (i * 9 + 5) + 's',
          animationDuration: (2 + i * 0.4) + 's'
        }"
      ></span>
    </div>
  </div>
</template>

<script setup>
function randomPct(seed, offset) {
  const x = ((seed * 137.5 + offset * 271.8) % 100)
  return (2 + x * 0.96).toFixed(1) + '%'
}

const acupointStars = [
  { id: 'ap1', x: 140, y: 142, r: 4, op: 0.55, fill: 'rgba(244,201,93,0.85)', delay: 0, dur: 3 },
  { id: 'ap2', x: 300, y: 200, r: 3.5, op: 0.5, fill: 'rgba(244,201,93,0.78)', delay: 0.6, dur: 3.5 },
  { id: 'ap3', x: 480, y: 178, r: 4.5, op: 0.6, fill: 'rgba(255,224,145,0.9)', delay: 1.2, dur: 2.8 },
  { id: 'ap4', x: 640, y: 138, r: 3, op: 0.45, fill: 'rgba(184,115,51,0.75)', delay: 1.8, dur: 3.2 },
  { id: 'ap5', x: 820, y: 168, r: 4, op: 0.55, fill: 'rgba(244,201,93,0.85)', delay: 0.3, dur: 3.6 },
  { id: 'ap6', x: 1000, y: 220, r: 3.5, op: 0.5, fill: 'rgba(184,115,51,0.8)', delay: 0.9, dur: 2.9 },
  { id: 'ap7', x: 1180, y: 175, r: 4, op: 0.55, fill: 'rgba(255,224,145,0.9)', delay: 1.5, dur: 3.3 },

  { id: 'ap8', x: 160, y: 430, r: 4, op: 0.5, fill: 'rgba(255,200,120,0.88)', delay: 0.4, dur: 3.1 },
  { id: 'ap9', x: 340, y: 340, r: 3.5, op: 0.45, fill: 'rgba(245,190,110,0.82)', delay: 1.0, dur: 3.4 },
  { id: 'ap10', x: 520, y: 408, r: 4.5, op: 0.55, fill: 'rgba(255,210,130,0.9)', delay: 0.2, dur: 2.7 },
  { id: 'ap11', x: 700, y: 478, r: 3, op: 0.4, fill: 'rgba(240,180,100,0.8)', delay: 0.8, dur: 3.7 },
  { id: 'ap12', x: 860, y: 428, r: 4, op: 0.5, fill: 'rgba(255,200,120,0.88)', delay: 1.4, dur: 3.0 },
  { id: 'ap13', x: 1040, y: 388, r: 3.5, op: 0.45, fill: 'rgba(245,190,110,0.82)', delay: 0.5, dur: 3.5 },
  { id: 'ap14', x: 1220, y: 442, r: 4, op: 0.5, fill: 'rgba(255,210,130,0.9)', delay: 1.1, dur: 2.8 },

  { id: 'ap15', x: 180, y: 600, r: 4, op: 0.5, fill: 'rgba(120,230,200,0.88)', delay: 0.3, dur: 3.2 },
  { id: 'ap16', x: 360, y: 670, r: 3.5, op: 0.45, fill: 'rgba(110,220,190,0.82)', delay: 0.9, dur: 3.6 },
  { id: 'ap17', x: 540, y: 608, r: 4.5, op: 0.55, fill: 'rgba(130,240,210,0.9)', delay: 1.5, dur: 2.9 },
  { id: 'ap18', x: 720, y: 638, r: 3, op: 0.4, fill: 'rgba(100,210,180,0.8)', delay: 0.6, dur: 3.3 },
  { id: 'ap19', x: 880, y: 628, r: 4, op: 0.5, fill: 'rgba(120,230,200,0.88)', delay: 1.2, dur: 3.1 },
  { id: 'ap20', x: 1060, y: 578, r: 3.5, op: 0.45, fill: 'rgba(110,220,190,0.82)', delay: 0.1, dur: 3.4 },
  { id: 'ap21', x: 1240, y: 612, r: 4, op: 0.5, fill: 'rgba(130,240,210,0.9)', delay: 0.7, dur: 2.8 },
]
</script>

<style scoped>
.star-river-deco {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* === 深空星云 === */
.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  animation: nebulaShift 12s ease-in-out infinite alternate;
}

.nebula-1 {
  width: 500px; height: 350px;
  top: -120px; left: -100px;
  background: radial-gradient(ellipse, rgba(84,182,161,0.08), transparent 70%);
  animation-delay: 0s;
}

.nebula-2 {
  width: 600px; height: 400px;
  bottom: -150px; right: -120px;
  background: radial-gradient(ellipse, rgba(90,167,216,0.07), transparent 70%);
  animation-delay: 4s;
}

.nebula-3 {
  width: 400px; height: 300px;
  top: 40%; left: 40%;
  background: radial-gradient(ellipse, rgba(244,201,93,0.05), transparent 70%);
  animation-delay: 8s;
}

@keyframes nebulaShift {
  0% { transform: translate(0, 0) scale(1); opacity: 0.6; }
  50% { transform: translate(20px, -15px) scale(1.1); opacity: 0.8; }
  100% { transform: translate(-10px, 10px) scale(0.95); opacity: 0.5; }
}

/* === 经络线 === */
.meridian-lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.meridian-path {
  stroke-dasharray: 12 10;
  animation: meridianFlow 8s linear infinite;
}

.meridian-path.mp-2 { animation-duration: 10s; animation-direction: reverse; }
.meridian-path.mp-3 { animation-duration: 12s; }

@keyframes meridianFlow {
  0% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -44; }
}

/* === 穴位星点 === */
.acupoint-star {
  animation: apPulse 3s ease-in-out infinite;
  transform-origin: center;
  transform-box: fill-box;
}

@keyframes apPulse {
  0%, 100% { opacity: 0.4; transform: scale(0.85); }
  50% { opacity: 0.75; transform: scale(1.2); }
}

/* === 星尘 === */
.stardust-dot {
  position: absolute;
  width: 1px;
  height: 1px;
  border-radius: 50%;
  background: rgba(255,241,194,0.5);
  animation: dustTwinkle 2.5s ease-in-out infinite;
}

@keyframes dustTwinkle {
  0%, 100% { opacity: 0.1; }
  50% { opacity: 0.5; }
}

/* === 普通星星 === */
.natural-star {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,247,223,0.92) 0%, rgba(244,201,93,0.3) 60%, transparent 100%);
  animation: naturalTwinkle 3s ease-in-out infinite;
}

@keyframes naturalTwinkle {
  0%, 100% { opacity: 0.25; transform: scale(0.9); }
  30% { opacity: 0.7; transform: scale(1.1); }
  60% { opacity: 0.35; transform: scale(0.85); }
}

/* === 亮星团簇 === */
.bright-dot {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,241,194,0.95) 0%, rgba(84,182,161,0.42) 50%, transparent 75%);
  animation: brightPulse 3.5s ease-in-out infinite;
  box-shadow: 0 0 6px rgba(244,201,93,0.45), 0 0 14px rgba(84,182,161,0.18);
}

@keyframes brightPulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  35% { opacity: 0.85; transform: scale(1.35); }
  65% { opacity: 0.45; transform: scale(0.9); }
}

/* === 流星 === */
.shooting-star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #fff;
  border-radius: 50%;
  animation: shoot 4s linear infinite;
  opacity: 0;
  box-shadow: 0 0 6px 1px rgba(255,247,223,0.7), 0 0 12px 3px rgba(84,182,161,0.22);
}

.shooting-star::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 0;
  width: 60px;
  height: 1.5px;
  background: linear-gradient(90deg, transparent, rgba(255,247,223,0.45), rgba(244,201,93,0.78), #fff7df);
  transform: translateY(-50%);
  border-radius: 0 1px 1px 0;
}

@keyframes shoot {
  0% { opacity: 0; transform: translate(0, 0) rotate(-25deg); }
  2% { opacity: 1; }
  10% { opacity: 0; transform: translate(-150px, 70px) rotate(-25deg); }
  100% { opacity: 0; transform: translate(-150px, 70px) rotate(-25deg); }
}

@media (prefers-reduced-motion: reduce) {
  .star-river-deco * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
