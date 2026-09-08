<template>
  <main class="safety-page">
    <section v-if="stage === 'intro'" class="intro">
      <img :src="classroomPreview" alt="安全小课堂" class="art">
      <div class="intro-content"><img :src="detective" alt="" class="mascot"><p>安全守护训练营</p><h1>安全小课堂</h1><span>看一看生活中的小情境，选出你的安全行动！</span><button type="button" @click="startScenes">开始情境闯关</button></div>
    </section>
    <section v-else-if="stage === 'scenes'" class="game">
      <header><button type="button" @click="stage = 'intro'">← 返回课堂</button><strong>★ 第三关 · 情境选择</strong><div class="hud"><span class="hearts">{{ '♥'.repeat(lives) }}<i>{{ '♥'.repeat(3 - lives) }}</i></span><b>★ {{ score }}</b></div></header>
      <div class="progress"><span :style="{ width: `${progress}%` }"></span><b>{{ currentIndex + 1 }} / {{ scenes.length }}</b></div>
      <article class="scene-card"><div class="illustration"><img :src="scene.image" alt=""><em>{{ currentIndex + 1 }}</em></div><div class="copy"><small>生活小情境</small><h1>{{ scene.title }}</h1><p>{{ scene.prompt }}</p><div class="choices"><button class="safe" type="button" :class="{ selected: selected === true }" :disabled="answered" @click="choose(true)">✓ {{ scene.safeLabel }}</button><button class="stop" type="button" :class="{ selected: selected === false }" :disabled="answered" @click="choose(false)">✕ {{ scene.unsafeLabel }}</button></div><div v-if="answered" class="feedback" :class="{ good: isCorrect }"><strong>{{ isCorrect ? '太棒啦！安全小侦探 +1' : '再想一想，保护自己最重要' }}</strong><p>{{ scene.explanation }}</p><button v-if="!isCorrect" type="button" @click="retry">重新选择</button><button v-else class="next" type="button" @click="next">{{ isLast ? '完成关卡' : '下一情境' }}</button></div></div></article>
    </section>
      <section v-else class="complete"><img :src="bell" alt="安全铃铛徽章"><small>情境闯关完成</small><h1>你是安全小侦探！</h1><p>只观察、只学习，不自行针刺；身体不舒服要告诉大人。</p><div>答对 <b>{{ score }}</b> / {{ scenes.length }} 获得 <b>{{ score }}</b> 颗星星</div><button type="button" @click="stage = 'intro'">再玩一次</button></section>
  </main>
</template>
<script setup>
import { computed, ref } from 'vue'
import classroomPreview from '@/assets/safety/safety-classroom-preview.png'
import detective from '@/assets/characters/copper-detective-guide.png'
import { generatedMaterialIcons } from '@/data/generatedRewardAssets'

const bell = generatedMaterialIcons['safety-bell']
import safetyBell from '@/assets/characters/safety-bell-sprite-wave.png'
const stage = ref('intro'); const currentIndex = ref(0); const selected = ref(null); const score = ref(0); const lives = ref(3)
const scenes = [
  { title: '身体有点不舒服', prompt: '肚肚不舒服了，你会怎么做？', safeLabel: '告诉家长', unsafeLabel: '自己找针试试', answer: true, explanation: '不舒服要告诉家长，请医生来判断。', image: safetyBell },
  { title: '发现了小针具', prompt: '桌上有尖尖的针具，你会怎么做？', safeLabel: '请大人收好', unsafeLabel: '拿来玩一玩', answer: true, explanation: '尖锐物品很危险，不能拿来玩。', image: detective },
  { title: '看安全科普动画', prompt: '学习穴位动画时，正确的做法是？', safeLabel: '认真观察学习', unsafeLabel: '照着给自己扎针', answer: true, explanation: '动画帮助我们学习知识，不能代替专业操作。', image: safetyBell },
  { title: '想认识一个穴位', prompt: '你有问题时，可以向谁请教？', safeLabel: '问老师或医生', unsafeLabel: '自己上网乱试', answer: true, explanation: '有疑问要问可信赖的大人和专业人员。', image: detective },
  { title: '同学说想试一试', prompt: '同学请你帮忙做治疗，你会？', safeLabel: '礼貌拒绝并告诉老师', unsafeLabel: '偷偷帮他操作', answer: true, explanation: '小朋友不能给别人针刺或治疗。', image: safetyBell },
  { title: '学习身体小知识', prompt: '认识身体地图时，安全的方式是？', safeLabel: '看图认识位置', unsafeLabel: '模仿治疗动作', answer: true, explanation: '我们只观察、只学习，不模仿治疗。', image: detective }
]
const scene = computed(() => scenes[currentIndex.value]); const answered = computed(() => selected.value !== null); const isCorrect = computed(() => selected.value === scene.value.answer); const isLast = computed(() => currentIndex.value === scenes.length - 1); const progress = computed(() => ((currentIndex.value + (answered.value ? 1 : 0)) / scenes.length) * 100)
function startScenes() { stage.value = 'scenes'; currentIndex.value = 0; selected.value = null; score.value = 0; lives.value = 3 }
function choose(value) { selected.value = value; if (value === scene.value.answer) score.value += 1; else lives.value = Math.max(0, lives.value - 1) }
function retry() { selected.value = null }
function next() { if (isLast.value) stage.value = 'complete'; else { currentIndex.value += 1; selected.value = null } }
</script>
<style scoped>
.safety-page{min-height:calc(100vh - 56px);background:#f3dfb7;color:#3d2915;font-family:"Microsoft YaHei",sans-serif}.intro{position:relative;max-width:1920px;margin:auto;aspect-ratio:16/9;overflow:hidden}.art{width:100%;height:100%;object-fit:cover}.intro-content{position:absolute;inset:18% 28% 10%;display:flex;flex-direction:column;align-items:center;text-align:center}.mascot{height:86px;object-fit:contain}.intro-content p,.copy small,.complete small{color:#2f8068;font-weight:900;letter-spacing:1px}.intro h1,.complete h1{margin:8px 0;color:#244f3d;font-size:clamp(36px,5vw,72px);font-weight:950}.intro span,.complete p{color:#765b36;font-size:clamp(15px,1.6vw,22px);line-height:1.7}.intro button,.complete button,.next{margin-top:20px;border:0;border-radius:999px;background:#2f8068;color:#fff;padding:14px 34px;font-size:18px;font-weight:900;cursor:pointer;box-shadow:0 5px #1f5c49}.game{max-width:1160px;margin:auto;padding:22px 24px 54px}.game header{display:flex;align-items:center;justify-content:space-between;gap:16px}.game header button,.feedback button{border:1px solid #cda665;border-radius:999px;background:#fff8e8;color:#624522;padding:10px 18px;font-weight:800;cursor:pointer}.hud{display:flex;gap:18px}.hearts{color:#df4160;letter-spacing:3px}.hearts i{color:#ddc9a2;font-style:normal}.hud b{color:#b4771b}.progress{display:flex;gap:12px;align-items:center;max-width:520px;margin:25px auto 18px;color:#705333}.progress span{height:12px;flex:1;border-radius:99px;background:#eaa526}.progress:before{content:"";position:absolute}.progress{background:#dbc28f;border-radius:99px;height:12px}.progress b{background:#f3dfb7;padding-left:8px}.scene-card{display:grid;grid-template-columns:42% 58%;background:#fff9e9;border:5px solid #2f8068;border-radius:28px;overflow:hidden;box-shadow:0 14px #84571f33}.illustration{position:relative;display:grid;place-items:center;min-height:430px;background:#f8e9c8}.illustration img{width:72%;max-height:330px;object-fit:contain;filter:drop-shadow(0 10px 8px #65401633)}.illustration em{position:absolute;top:18px;left:20px;display:grid;place-items:center;width:44px;height:44px;border-radius:50%;background:#e8a31b;color:#fff;font-style:normal;font-size:22px;font-weight:900}.copy{padding:45px 46px}.copy h1{margin:8px 0 12px;font-size:clamp(28px,3vw,44px)}.copy>p{font-size:20px;line-height:1.6;color:#775936}.choices{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-top:28px}.choices button{min-height:72px;border:3px solid;border-radius:18px;background:#fffdf5;font-size:20px;font-weight:900;cursor:pointer}.safe{border-color:#63a98d;color:#2f8068}.stop{border-color:#e9a1a4;color:#c64a51}.choices .selected{box-shadow:0 0 0 4px #e9b33d inset}.feedback{margin-top:22px;padding:16px 18px;border-radius:16px;background:#fff0e6;color:#b25435}.feedback.good{background:#e8f5e8;color:#287553}.feedback p{margin:6px 0 12px;line-height:1.6}.complete{text-align:center;max-width:680px;margin:auto;padding:12vh 24px}.complete img{width:130px}.complete b{color:#df9a1b;font-size:26px}.sr-only{position:absolute;width:1px;height:1px;overflow:hidden}@media(max-width:760px){.intro-content{inset:12% 10% 8%}.mascot{height:52px}.intro h1,.complete h1{font-size:36px}.intro span{font-size:14px}.intro button,.complete button{font-size:15px;padding:10px 20px}.game{padding:16px 12px 36px}.scene-card{grid-template-columns:1fr}.illustration{min-height:220px}.illustration img{max-height:190px}.copy{padding:26px 20px}.copy h1{font-size:30px}.copy>p{font-size:17px}.choices{grid-template-columns:1fr}.choices button{min-height:58px;font-size:17px}}
</style>
