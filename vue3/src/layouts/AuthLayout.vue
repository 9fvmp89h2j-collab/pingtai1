<template>
  <div class="auth-layout" :class="{ 'register-layout': isRegister }">
    <router-view v-if="isRegister" />

    <template v-else>
      <header class="auth-topbar">
        <router-link to="/home-map" class="auth-brand" aria-label="返回杏林探险地图">
          <img :src="detectiveStandard" alt="">
          <span><b>小铜人</b><em>中医侦探社</em></span>
        </router-link>
        <span class="station-chip">身份核验处</span>
        <router-link :to="secondaryLink.to" class="secondary-link">{{ secondaryLink.text }}</router-link>
      </header>

      <main class="auth-stage">
        <section class="auth-story" aria-label="小铜人登录引导">
          <span class="story-kicker">DETECTIVE CHECK-IN</span>
          <h1>{{ pageCopy.title }}</h1>
          <p>{{ pageCopy.subtitle }}</p>
          <ul>
            <li><CheckCircleFilled />登录后可跨设备保存探案进度</li>
            <li><CheckCircleFilled />继续收集徽章、星砂与铜片</li>
            <li><SafetyCertificateFilled />只观察、只学习，不自己针刺</li>
          </ul>
          <img :src="detectiveWelcome" alt="挥手欢迎小侦探归队的小铜人" class="story-character">
        </section>

        <section class="auth-container" :aria-labelledby="headingId">
          <div class="auth-header">
            <span>{{ pageCopy.eyebrow }}</span>
            <h2 :id="headingId">{{ pageCopy.formTitle }}</h2>
            <p>{{ pageCopy.formHint }}</p>
          </div>

          <div class="auth-content">
            <router-view />
          </div>

          <router-link to="/home-map" class="map-link"><ArrowLeftOutlined />暂不登录，返回探险地图</router-link>
        </section>
      </main>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeftOutlined, CheckCircleFilled, SafetyCertificateFilled } from '@ant-design/icons-vue'
import detectiveStandard from '@/assets/copper-detective-2026/detective-standard.png'
import detectiveWelcome from '@/assets/copper-detective-2026/detective-welcome.png'

const route = useRoute()
const isRegister = computed(() => route.name === 'Register')
const isForgotPassword = computed(() => route.name === 'ForgotPassword')
const headingId = computed(() => isForgotPassword.value ? 'forgot-heading' : 'login-heading')

const pageCopy = computed(() => isForgotPassword.value
  ? {
      eyebrow: '身份卡修复任务',
      title: '找回身份卡的通行密令',
      subtitle: '和家长一起核对档案信息，安全地设置一个新密码。',
      formTitle: '修复登录密码',
      formHint: '请填写创建身份卡时登记的信息'
    }
  : {
      eyebrow: '侦探归队核验',
      title: '欢迎归队，小侦探！',
      subtitle: '核验身份卡，继续调查身体地图里还没解开的文化线索。',
      formTitle: '登录侦探身份卡',
      formHint: '输入创建身份卡时使用的账号和密码'
    })

const secondaryLink = computed(() => isForgotPassword.value
  ? { to: '/auth/login', text: '想起密码了？去登录' }
  : { to: '/auth/register', text: '还没有身份卡？去报到' })
</script>

<style scoped>
* { box-sizing: border-box; }

.auth-layout {
  --green: var(--site-primary-deep);
  --green-2: var(--site-primary);
  --gold: var(--site-copper);
  --paper: var(--site-paper);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  color: #283d31;
  background: #eff4e4 url('@/assets/maps/xinglin-detective-map-bg.png') center / cover fixed;
  font-family: var(--site-body-font), 'Microsoft YaHei', sans-serif;
}

.auth-layout::before {
  position: fixed;
  inset: 0;
  content: '';
  background: linear-gradient(120deg, rgba(251, 249, 233, .93), rgba(238, 244, 220, .86));
  backdrop-filter: blur(3px);
}

.auth-topbar,
.auth-stage { position: relative; z-index: 1; }

.auth-topbar {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  min-height: 70px;
  padding: 0 clamp(18px, 4vw, 64px);
  align-items: center;
  border-bottom: 1px solid rgba(176, 132, 55, .35);
  background: rgba(255, 252, 242, .88);
  backdrop-filter: blur(12px);
}

.auth-brand {
  display: flex;
  width: max-content;
  align-items: center;
  gap: 9px;
  color: var(--green);
  text-decoration: none;
}

.auth-brand img { width: 48px; height: 48px; object-fit: contain; }
.auth-brand span { display: grid; line-height: 1.05; }
.auth-brand b { font: 800 19px 'Noto Serif SC', serif; }
.auth-brand em { margin-top: 5px; font-size: 12px; font-style: normal; letter-spacing: 2px; }

.station-chip {
  padding: 7px 15px;
  border: 1px solid #d9b463;
  border-radius: 999px;
  background: #f8e6b8;
  color: #77521d;
  font-size: 13px;
  font-weight: 800;
}

.secondary-link {
  justify-self: end;
  padding: 9px 14px;
  border: 1px solid #c8aa68;
  border-radius: 999px;
  color: var(--green);
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
}

.auth-stage {
  display: grid;
  grid-template-columns: minmax(360px, .95fr) minmax(390px, .72fr);
  width: min(1120px, calc(100% - 40px));
  min-height: calc(100vh - 70px);
  margin: 0 auto;
  padding: 54px 0;
  align-items: center;
  gap: clamp(32px, 6vw, 82px);
}

.auth-story {
  position: relative;
  min-height: 590px;
  padding: 54px 46px 44px;
  overflow: hidden;
  border: 1px solid rgba(190, 146, 62, .52);
  border-radius: 32px;
  background: linear-gradient(145deg, rgba(255, 250, 234, .96), rgba(230, 241, 211, .91));
  box-shadow: 0 24px 70px rgba(75, 65, 32, .14);
}

.auth-story::after {
  position: absolute;
  right: -130px;
  bottom: -180px;
  width: 520px;
  height: 520px;
  border: 1px dashed rgba(183, 132, 41, .35);
  border-radius: 50%;
  content: '';
}

.story-kicker {
  color: #a27025;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 4px;
}

.auth-story h1 {
  max-width: 520px;
  margin: 10px 0 12px;
  color: var(--green);
  font: 900 clamp(38px, 4vw, 58px) / 1.18 'Noto Serif SC', serif;
  letter-spacing: 3px;
}

.auth-story > p {
  max-width: 520px;
  margin: 0;
  color: #625d4d;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.8;
}

.auth-story ul {
  position: relative;
  z-index: 2;
  display: grid;
  width: min(430px, 100%);
  margin: 28px 0 0;
  padding: 0;
  gap: 10px;
  list-style: none;
}

.auth-story li {
  display: flex;
  min-height: 44px;
  padding: 9px 13px;
  align-items: center;
  gap: 9px;
  border: 1px solid rgba(98, 137, 90, .24);
  border-radius: 12px;
  background: rgba(255, 253, 245, .74);
  color: #405744;
  font-size: 13px;
  font-weight: 700;
}

.auth-story li :deep(svg) { color: #43815c; }

.story-character {
  position: absolute;
  z-index: 1;
  right: 15px;
  bottom: -24px;
  width: min(310px, 48%);
  height: 360px;
  object-fit: contain;
  object-position: center bottom;
  filter: drop-shadow(0 18px 17px rgba(96, 63, 25, .18));
}

.auth-container {
  width: 100%;
  padding: clamp(30px, 4vw, 48px);
  border: 1px solid #dec480;
  border-radius: 26px;
  background: rgba(255, 250, 239, .96);
  box-shadow: 0 20px 55px rgba(76, 64, 29, .14);
}

.auth-header { margin-bottom: 26px; }
.auth-header > span { color: #a36f24; font-size: 11px; font-weight: 900; letter-spacing: 2px; }
.auth-header h2 { margin: 7px 0 7px; color: var(--green); font: 900 clamp(26px, 2.5vw, 34px) 'Noto Serif SC', serif; }
.auth-header p { margin: 0; color: #756d5a; font-size: 13px; line-height: 1.65; }

.map-link {
  display: flex;
  width: max-content;
  margin: 20px auto 0;
  align-items: center;
  gap: 7px;
  color: #617565;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
}

.register-layout { display: block; min-height: 100vh; overflow: visible; background: transparent; }
.register-layout::before { display: none; }

:focus-visible { outline: 3px solid #d8a641 !important; outline-offset: 3px; }

@media (max-width: 860px) {
  .auth-stage { grid-template-columns: 1fr; max-width: 660px; padding: 28px 0 40px; }
  .auth-story { min-height: 300px; padding: 34px 30px; }
  .auth-story h1 { font-size: 36px; max-width: 70%; }
  .auth-story > p { max-width: 66%; font-size: 14px; }
  .auth-story ul { width: 64%; margin-top: 20px; }
  .story-character { width: 34%; height: 285px; }
}

@media (max-width: 560px) {
  .auth-topbar { grid-template-columns: 1fr auto; min-height: 62px; padding: 0 13px; }
  .auth-brand img { width: 41px; height: 41px; }
  .auth-brand b { font-size: 16px; }
  .auth-brand em { font-size: 9px; }
  .station-chip { display: none; }
  .secondary-link { padding: 8px 10px; font-size: 11px; }
  .auth-stage { width: calc(100% - 20px); padding: 14px 0 26px; gap: 12px; }
  .auth-story { min-height: 242px; padding: 25px 21px; border-radius: 20px; }
  .story-kicker { font-size: 9px; letter-spacing: 2px; }
  .auth-story h1 { max-width: 67%; margin-top: 6px; font-size: 28px; letter-spacing: 1px; }
  .auth-story > p { max-width: 64%; font-size: 11px; line-height: 1.65; }
  .auth-story ul { display: none; }
  .story-character { right: 3px; width: 39%; height: 220px; }
  .auth-container { padding: 26px 20px; border-radius: 20px; }
  .auth-header { margin-bottom: 20px; }
  .auth-header h2 { font-size: 25px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { scroll-behavior: auto !important; animation-duration: .01ms !important; transition-duration: .01ms !important; }
}
</style>
