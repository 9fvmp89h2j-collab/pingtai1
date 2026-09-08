<template>
  <main class="checkin-page">
    <header class="topbar">
      <router-link to="/home-map" class="brand" aria-label="返回杏林探险地图">
        <img :src="detectiveAvatar" alt="" class="brand-mark">
        <span><b>小铜人</b><em>中医侦探社</em></span>
      </router-link>
      <div class="level-chip"><FlagOutlined /> 第 1 关 · 报到处</div>
      <router-link to="/auth/login" class="login-link">已有身份卡？去登录</router-link>
    </header>

    <section class="hero" aria-labelledby="checkin-title">
      <p class="eyebrow">DETECTIVE ID ASSEMBLY</p>
      <h1 id="checkin-title">侦探身份组装局</h1>
      <p>完成四项轻任务，领取你的第一张小侦探身份卡</p>
    </section>

    <div class="workspace">
      <section class="assembly-panel" aria-label="身份组装步骤">
        <ol class="progress-list">
          <li v-for="step in steps" :key="step.id" :class="{ active: currentStep === step.id, done: isStepDone(step.id) }">
            <button type="button" :disabled="!canOpenStep(step.id) || registered" @click="openStep(step.id)">
              <span class="step-dot"><CheckOutlined v-if="isStepDone(step.id)" /><span v-else>{{ step.id }}</span></span>
              <span><b>{{ step.short }}</b><small>{{ step.label }}</small></span>
            </button>
          </li>
        </ol>

        <form class="task-card" @submit.prevent="handlePrimaryAction">
          <Transition name="step" mode="out-in">
            <section :key="currentStep" class="task-content">
              <template v-if="currentStep === 1">
                <p class="task-kicker">任务 01 · 装配形象</p>
                <h2>先选一个你喜欢的小侦探形象吧！</h2>
                <p class="task-hint">每位搭档都有自己的侦探专长，选中后会装进右边的身份卡。</p>
                <div class="avatar-picker" role="radiogroup" aria-label="选择小侦探形象">
                  <button
                    v-for="(item, index) in avatars"
                    :key="item.id"
                    type="button"
                    class="avatar-option"
                    :class="{ selected: selectedAvatar === index }"
                    role="radio"
                    :aria-checked="selectedAvatar === index"
                    @click="selectAvatar(index)"
                  >
                    <span class="avatar-frame"><img :src="item.image" :alt="item.name"></span>
                    <span class="avatar-copy"><b>{{ item.name }}</b><small>{{ item.specialty }}</small></span>
                    <span v-if="selectedAvatar === index" class="selected-mark"><CheckOutlined /> 已选中</span>
                  </button>
                </div>
                <div class="character-line" role="status">“{{ selectedAvatarData.line }}”</div>
              </template>

              <template v-else-if="currentStep === 2">
                <p class="task-kicker">任务 02 · 建立档案</p>
                <h2>创建你的侦探账号</h2>
                <p class="task-hint">请和家长一起填写。邮箱只用于账号找回和安全通知。</p>
                <div class="field-grid">
                  <label class="field">
                    <span>侦探账号</span>
                    <div class="input-shell" :class="fieldState('username')">
                      <UserOutlined />
                      <input v-model.trim="form.username" autocomplete="username" maxlength="20" placeholder="3—20位字母、数字或下划线" @input="clearError('username')">
                    </div>
                    <small v-if="errors.username" class="error">{{ errors.username }}</small>
                    <small v-else-if="usernameValid" class="success-text"><CheckCircleFilled /> 这个登录代号可以使用</small>
                  </label>
                  <label class="field">
                    <span>家长联系邮箱</span>
                    <div class="input-shell" :class="fieldState('email')">
                      <MailOutlined />
                      <input v-model.trim="form.email" type="email" autocomplete="email" maxlength="100" placeholder="用于接收密码重置验证码" @input="clearError('email')">
                    </div>
                    <small v-if="errors.email" class="error">{{ errors.email }}</small>
                    <small v-else-if="emailValid" class="success-text"><CheckCircleFilled /> 邮箱格式正确</small>
                  </label>
                  <label class="field">
                    <span>侦探密码</span>
                    <div class="input-shell" :class="fieldState('password')">
                      <LockOutlined />
                      <input v-model="form.password" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" maxlength="50" placeholder="至少8位字符，建议包含字母和数字" @input="clearError('password')">
                      <button type="button" class="icon-button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                        <EyeInvisibleOutlined v-if="showPassword" /><EyeOutlined v-else />
                      </button>
                    </div>
                    <small v-if="errors.password" class="error">{{ errors.password }}</small>
                    <div v-else class="password-meter" :data-strength="passwordStrength"><i /><i /><i /><span>{{ passwordLabel }}</span></div>
                  </label>
                </div>
              </template>

              <template v-else-if="currentStep === 3">
                <p class="task-kicker">任务 03 · 写入昵称</p>
                <h2>给自己取一个侦探昵称吧！</h2>
                <p class="task-hint">昵称会印在身份卡正面，使用2—8个汉字或字母。</p>
                <div class="nickname-box">
                  <label class="field nickname-field">
                    <span>我的侦探昵称</span>
                    <div class="input-shell" :class="fieldState('name')">
                      <EditOutlined />
                      <input v-model.trim="form.name" maxlength="8" placeholder="例如：杏林小神探" @input="clearError('name')">
                      <span class="counter">{{ form.name.length }}/8</span>
                    </div>
                    <small v-if="errors.name" class="error">{{ errors.name }}</small>
                  </label>
                  <button type="button" class="random-button" :class="{ rolling: nameRolling }" @click="randomizeName">
                    <ReloadOutlined /> {{ nameRolling ? '名字摇动中…' : '摇一摇名字' }}
                  </button>
                </div>
                <div class="name-suggestions">
                  <span>灵感纸条</span>
                  <button v-for="name in names.slice(0, 3)" :key="name" type="button" @click="chooseName(name)">{{ name }}</button>
                </div>
              </template>

              <template v-else>
                <p class="task-kicker">任务 04 · 最终核验</p>
                <h2>{{ registered ? '身份卡已正式生效！' : '准备生成身份卡' }}</h2>
                <p class="task-hint">{{ registered ? (autoLoginFailed ? '身份卡已生成，请登录后从第二关开始探险。' : '盖章完成，三份见面礼已经放进你的探险行囊，马上进入第二关地图。') : '检查装配结果，确认安全守则后就可以盖章啦。' }}</p>
                <ul class="check-list">
                  <li><CheckCircleFilled /><span><b>侦探形象</b>{{ selectedAvatarData.name }}</span></li>
                  <li><CheckCircleFilled /><span><b>登录代号</b>{{ form.username }}</span></li>
                  <li><CheckCircleFilled /><span><b>家长邮箱</b>{{ form.email }}</span></li>
                  <li><CheckCircleFilled /><span><b>侦探昵称</b>{{ form.name }}</span></li>
                </ul>
                <label v-if="!registered" class="agreement">
                  <input v-model="agreed" type="checkbox">
                  <span><b>和家长一起确认</b> 我只观察、只学习，不自己针刺；同意使用家长邮箱进行账号找回和安全通知。</span>
                </label>
                <p v-if="errors.agreement" class="error agreement-error">{{ errors.agreement }}</p>
              </template>
            </section>
          </Transition>

          <div class="task-actions">
            <button v-if="currentStep > 1 && !registered" type="button" class="secondary-button" @click="currentStep--"><ArrowLeftOutlined /> 上一步</button>
            <button v-if="!registered" type="submit" class="primary-button" :disabled="loading">
              <LoadingOutlined v-if="loading" />
              <template v-if="currentStep < 4">装配下一项 <ArrowRightOutlined /></template>
              <template v-else>{{ loading ? '正在登记身份…' : '生成我的身份卡' }} <IdcardOutlined v-if="!loading" /></template>
            </button>
            <router-link v-else :to="autoLoginFailed ? '/auth/login' : '/home-map'" class="primary-button success-button">{{ autoLoginFailed ? '带着身份卡去登录' : '进入第二关地图' }} <ArrowRightOutlined /></router-link>
          </div>
        </form>
      </section>

      <aside class="preview-column" aria-label="小侦探身份卡预览">
        <div class="completion"><span>身份组装</span><b>{{ completionCount }}/4</b><i><span :style="{ width: `${completionCount * 25}%` }" /></i></div>
        <div class="card-stage" :class="{ revealed: registered, stamping: stampActive }">
          <div class="identity-card">
            <div class="card-front">
              <span class="waiting-seal"><IdcardOutlined /></span>
              <h2>身份卡装配中</h2>
              <p>完成左边的四项任务<br>正式身份卡就会出现</p>
              <span class="assembly-code">ASSEMBLY · {{ completionCount }}/4</span>
            </div>
            <div class="card-back">
              <div class="card-heading"><span>小铜人侦探社</span><small>见习侦探身份卡</small></div>
              <p class="card-no">NO. {{ cardNumber }}</p>
              <div class="portrait"><img :src="selectedAvatarData.image" :alt="selectedAvatarData.name"></div>
              <h2>{{ form.name || '昵称待写入' }}</h2>
              <strong>铜人见习侦探</strong>
              <dl><div><dt>侦探专长</dt><dd>{{ selectedAvatarData.specialty }}</dd></div><div><dt>加入日期</dt><dd>{{ joinDate }}</dd></div></dl>
              <p class="oath">只观察 · 只学习 · 不自己针刺</p>
              <div class="stamp">侦探社<br>已认证</div>
            </div>
          </div>
        </div>

        <section class="reward-drawer" :class="{ unlocked: rewardsVisible }" aria-live="polite">
          <header><GiftOutlined /><span><b>{{ rewardsVisible ? '见面礼已领取' : '完成后领取见面礼' }}</b><small>三份奖励将随身份卡一起解锁</small></span></header>
          <div class="rewards">
            <div v-for="reward in rewards" :key="reward.title" class="reward">
              <img :src="reward.image" alt="">
              <span><b>{{ reward.title }}</b><small>{{ reward.copy }}</small></span>
              <CheckCircleFilled v-if="rewardsVisible" />
              <LockOutlined v-else />
            </div>
          </div>
        </section>
      </aside>
    </div>

    <div v-if="notice" class="toast" role="status">{{ notice }}</div>
    <div v-if="celebrating" class="celebration" aria-hidden="true"><i v-for="n in 18" :key="n" :style="{ '--i': n }" /></div>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftOutlined, ArrowRightOutlined, CheckCircleFilled, CheckOutlined, EditOutlined,
  EyeInvisibleOutlined, EyeOutlined, FlagOutlined, GiftOutlined, IdcardOutlined,
  LoadingOutlined, LockOutlined, MailOutlined, ReloadOutlined, UserOutlined
} from '@ant-design/icons-vue'
import { register } from '@/api/user'
import { useUserStore } from '@/store/user'
import detectiveAvatar from '@/assets/characters/copper-detective-guide-512.png'
import herbalAvatar from '@/assets/characters/herbal-apprentice-hero-ready.png'
import teacherAvatar from '@/assets/characters/copper-teacher-stand.png'
import { generatedRewardAssets } from '@/data/generatedRewardAssets'

const currentStep = ref(1)
const router = useRouter()
const userStore = useUserStore()
const selectedAvatar = ref(0)
const showPassword = ref(false)
const loading = ref(false)
const agreed = ref(false)
const registered = ref(false)
const autoLoginFailed = ref(false)
const stampActive = ref(false)
const rewardsVisible = ref(false)
const celebrating = ref(false)
const nameRolling = ref(false)
const notice = ref('')
const errors = reactive({})
const form = reactive({ username: '', email: '', password: '', name: '' })

const steps = [
  { id: 1, short: '形象', label: '选择搭档' },
  { id: 2, short: '档案', label: '创建账号' },
  { id: 3, short: '昵称', label: '写入名字' },
  { id: 4, short: '盖章', label: '生成身份卡' }
]
const avatars = [
  { id: 'copper-detective', name: '明察探探', specialty: '细节观察', line: '任何小线索，都逃不过我的眼睛！', image: detectiveAvatar },
  { id: 'herbal-apprentice', name: '草木寻踪', specialty: '草本辨认', line: '跟着草木的香气，我们去找答案吧！', image: herbalAvatar },
  { id: 'copper-teacher', name: '经络星探', specialty: '经络探索', line: '把穴位连成星路，秘密就会亮起来！', image: teacherAvatar }
]
const names = ['杏林小神探', '铜铃小侦探', '草木寻踪家', '经络小星探', '青囊小助手']
const rewards = [
  { title: '小侦探身份卡', copy: '进入侦探社的专属凭证', image: generatedRewardAssets.detectiveAgencyBadge },
  { title: '初识草本徽章', copy: '第一枚杏林成长徽章', image: generatedRewardAssets.xinglinLeaf },
  { title: '探险小卷轴', copy: '记录接下来的每次发现', image: generatedRewardAssets.storyArchiveCard }
]

const selectedAvatarData = computed(() => avatars[selectedAvatar.value])
const usernameValid = computed(() => /^[a-zA-Z0-9_]{3,20}$/.test(form.username))
const emailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
const passwordStrength = computed(() => {
  if (!form.password) return 0
  if (form.password.length < 8) return 1
  if (form.password.length >= 8 && /[a-zA-Z]/.test(form.password) && /\d/.test(form.password)) return 3
  return 2
})
const passwordLabel = computed(() => ['等待输入', '还差一点', '安全合格', '很可靠'][passwordStrength.value])
const completionCount = computed(() => registered.value ? 4 : Math.min(currentStep.value, 4) - (currentStep.value === 1 && selectedAvatar.value < 0 ? 1 : 0))
const cardNumber = computed(() => `XTR-${new Date().getFullYear()}-${String(form.username || 'NEW').slice(-4).toUpperCase().padStart(4, '0')}`)
const joinDate = new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date()).replaceAll('/', '.')

const isStepDone = (id) => registered.value || id < currentStep.value
const canOpenStep = (id) => id <= currentStep.value
const openStep = (id) => { if (canOpenStep(id)) currentStep.value = id }
const selectAvatar = (index) => { selectedAvatar.value = index }
const clearError = (key) => { delete errors[key] }
const fieldState = (key) => ({
  invalid: !!errors[key],
  valid: key === 'username'
    ? usernameValid.value
    : key === 'email'
      ? emailValid.value
      : key === 'password'
        ? passwordStrength.value >= 2
        : form.name.length >= 2
})
const chooseName = (name) => { form.name = name; clearError('name') }

const randomizeName = () => {
  if (nameRolling.value) return
  nameRolling.value = true
  let ticks = 0
  const timer = window.setInterval(() => {
    form.name = names[Math.floor(Math.random() * names.length)]
    ticks += 1
    if (ticks >= 7) {
      window.clearInterval(timer)
      nameRolling.value = false
      clearError('name')
    }
  }, 80)
}

const validateStep = () => {
  if (currentStep.value === 2) {
    if (!form.username) errors.username = '先写下你的登录代号'
    else if (!usernameValid.value) errors.username = '请使用3—20位字母、数字或下划线'
    if (!form.email) errors.email = '请填写家长联系邮箱'
    else if (!emailValid.value) errors.email = '请输入正确的邮箱格式'
    if (form.password.length < 8) errors.password = '密码至少需要8位字符'
    return !errors.username && !errors.email && !errors.password
  }
  if (currentStep.value === 3) {
    if (!/^[\u4e00-\u9fa5a-zA-Z0-9]{2,8}$/.test(form.name)) errors.name = '昵称需要2—8个汉字、字母或数字'
    return !errors.name
  }
  if (currentStep.value === 4 && !agreed.value) {
    errors.agreement = '请先和家长一起确认安全守则'
    return false
  }
  return true
}

const showNotice = (message) => {
  notice.value = message
  window.setTimeout(() => { notice.value = '' }, 2800)
}

const handlePrimaryAction = async () => {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!validateStep()) return
  if (currentStep.value < 4) {
    currentStep.value += 1
    return
  }
  loading.value = true
  const payload = {
    username: form.username,
    password: form.password,
    confirmPassword: form.password,
    name: form.name,
    email: form.email,
    userType: 'USER',
    avatar: selectedAvatarData.value.id
  }
  try {
    await register(payload, { showDefaultMsg: false })
    let autoLoginSucceeded = false
    try {
      await userStore.login({ username: form.username, password: form.password })
      autoLoginSucceeded = true
    } catch (loginError) {
      autoLoginFailed.value = true
      console.warn('注册成功，但自动登录失败，请手动登录后继续探险', loginError)
    }
    registered.value = true
    window.localStorage.setItem('xiaotongren-detective-avatar', selectedAvatarData.value.id)
    window.setTimeout(() => { stampActive.value = true }, 850)
    window.setTimeout(() => {
      rewardsVisible.value = true
      celebrating.value = true
      showNotice(autoLoginSucceeded ? '报到成功！即将进入第二关地图' : '报到成功！请登录后进入第二关地图')
    }, 1450)
    if (autoLoginSucceeded) {
      window.setTimeout(() => router.replace('/home-map'), 2200)
    }
    window.setTimeout(() => { celebrating.value = false }, 3300)
  } catch (error) {
    const message = error?.response?.data?.message || error?.message || '登记失败，请稍后再试'
    if (message.includes('用户名')) {
      currentStep.value = 2
      errors.username = '这个登录代号已经被使用，换一个试试吧'
    } else {
      showNotice(message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.checkin-page { --green:#174f39; --green-2:#2f7654; --gold:#bd852d; --paper:#fffaf0; --ink:#283d31; min-height:100vh; color:var(--ink); background:#eff4e4 url('@/assets/maps/xinglin-detective-map-bg.png') center/cover fixed; font-family:'Noto Sans SC','Microsoft YaHei',sans-serif; position:relative; overflow:hidden; }
.checkin-page::before { content:""; position:fixed; inset:0; background:linear-gradient(180deg,rgba(251,249,233,.94),rgba(241,245,224,.9)); backdrop-filter:blur(3px); }
.topbar,.hero,.workspace { position:relative; z-index:1; }
.topbar { height:70px; padding:0 clamp(18px,4vw,64px); display:flex; align-items:center; border-bottom:1px solid rgba(176,132,55,.35); background:rgba(255,252,242,.88); backdrop-filter:blur(12px); }
.brand { display:flex; align-items:center; gap:9px; color:var(--green); text-decoration:none; }.brand-mark{width:48px;height:48px;object-fit:contain}.brand span{display:grid;line-height:1.05}.brand b{font:800 19px 'Noto Serif SC'}.brand em{font-style:normal;font-size:12px;letter-spacing:2px;margin-top:5px}
.level-chip { margin:auto; color:#78531d; background:#f8e6b8; border:1px solid #dbb96b; border-radius:20px; padding:7px 14px; font-weight:700; font-size:13px; }.level-chip svg{margin-right:6px}.login-link{color:var(--green);text-decoration:none;font-weight:700;font-size:14px;padding:9px 14px;border:1px solid #c8aa68;border-radius:20px}
.hero{text-align:center;padding:25px 20px 18px}.eyebrow{margin:0;color:#a5762a;letter-spacing:4px;font-size:10px;font-weight:800}.hero h1{margin:5px 0 6px;color:var(--green);font:900 clamp(29px,3vw,43px) 'Noto Serif SC';letter-spacing:4px}.hero>p:last-child{margin:0;color:#6c634f;font-size:14px}
.workspace{width:min(1260px,calc(100% - 36px));margin:0 auto 36px;display:grid;grid-template-columns:minmax(0,1.28fr) minmax(340px,.72fr);gap:22px;align-items:start}
.assembly-panel,.reward-drawer{background:rgba(255,250,239,.94);border:1px solid #dec480;box-shadow:0 18px 55px rgba(76,64,29,.12)}.assembly-panel{border-radius:24px;overflow:hidden}
.progress-list{list-style:none;margin:0;padding:18px 24px;display:grid;grid-template-columns:repeat(4,1fr);background:rgba(235,239,212,.65);border-bottom:1px solid #dfce9f}.progress-list li{position:relative}.progress-list li:not(:last-child)::after{content:"";position:absolute;left:62%;right:-38%;top:18px;border-top:2px dashed #cbb983}.progress-list li.done:not(:last-child)::after{border-color:#6aa17e}.progress-list button{width:100%;border:0;background:none;display:flex;align-items:center;justify-content:center;gap:9px;color:#85775b;font-family:inherit;cursor:pointer;position:relative;z-index:1}.progress-list button:disabled{cursor:default}.progress-list button>span:last-child{display:grid;text-align:left}.progress-list b{font-size:13px}.progress-list small{font-size:10px;margin-top:2px}.step-dot{width:36px;height:36px;border-radius:50%;display:grid;place-items:center;background:#e7dfc6;border:2px solid #cbbf9d;font-weight:800}.active .step-dot{background:var(--green);border-color:#dcb55c;color:white;box-shadow:0 0 0 4px rgba(47,118,84,.13)}.done .step-dot{background:#4e8a68;border-color:#4e8a68;color:white}.active button,.done button{color:var(--green)}
.task-card{min-height:530px;padding:31px clamp(24px,4vw,48px) 25px;display:flex;flex-direction:column}.task-content{flex:1}.task-kicker{margin:0 0 5px;color:#a36f24;font-weight:800;font-size:12px;letter-spacing:1px}.task-content h2{margin:0;color:var(--green);font:800 clamp(23px,2.5vw,31px) 'Noto Serif SC'}.task-hint{margin:8px 0 24px;color:#6b6659;font-size:14px}
.avatar-picker{display:grid;grid-template-columns:repeat(3,1fr);gap:13px}.avatar-option{min-width:0;border:1px solid #dccb9f;border-radius:17px;background:#fffdf6;padding:15px 10px 12px;font-family:inherit;cursor:pointer;transition:.2s;position:relative;color:#3f493f}.avatar-option:hover{transform:translateY(-3px);border-color:#af8a42}.avatar-option.selected{border:2px solid var(--green-2);background:#f2f7e9;box-shadow:0 8px 23px rgba(32,89,59,.14)}.avatar-frame{display:block;width:112px;height:112px;margin:0 auto 10px;border-radius:50%;background:#f0dfb3;overflow:hidden;border:4px solid #d0a955}.avatar-option:nth-child(2) .avatar-frame{background:#dce8bf}.avatar-option:nth-child(3) .avatar-frame{background:#edd8bc}.avatar-frame img{width:100%;height:100%;object-fit:contain}.avatar-copy{display:grid}.avatar-copy b{color:var(--green);font-size:16px}.avatar-copy small{font-size:11px;color:#766b54;margin-top:3px}.selected-mark{display:block;margin:9px auto 0;color:#2e7651;font-size:11px;font-weight:700}.character-line{margin:18px auto 0;padding:10px 16px;border-radius:12px;background:#f7edd2;color:#73501e;text-align:center;font-size:13px;width:max-content;max-width:100%}
.field-grid{display:grid;gap:18px;max-width:570px}.field{display:grid;gap:7px}.field>span{font-weight:700;color:#405144;font-size:14px}.input-shell{height:53px;display:flex;align-items:center;gap:11px;border:1.5px solid #cfbd8c;border-radius:12px;background:#fffdf8;padding:0 15px;color:#a27329;transition:.2s}.input-shell:focus-within{border-color:var(--green-2);box-shadow:0 0 0 4px rgba(47,118,84,.11)}.input-shell.invalid{border-color:#b84a3d}.input-shell.valid{border-color:#75a083}.input-shell input{border:0;outline:0;background:none;width:100%;font:500 15px inherit;color:#304435}.input-shell input::placeholder{color:#a19987}.icon-button{border:0;background:none;color:#88642d;cursor:pointer;font-size:17px}.error{color:#ae3d34;font-size:12px;margin:0}.success-text{color:#3d7957}.success-text svg{margin-right:5px}.password-meter{display:flex;align-items:center;gap:5px;color:#8b806b;font-size:11px}.password-meter i{width:48px;height:4px;border-radius:3px;background:#ddd4bf}.password-meter[data-strength="1"] i:first-child,.password-meter[data-strength="2"] i:nth-child(-n+2),.password-meter[data-strength="3"] i{background:#4c8a66}.password-meter span{margin-left:4px}
.nickname-box{display:grid;grid-template-columns:1fr auto;gap:12px;align-items:end;max-width:620px}.random-button{height:53px;border:1px solid #c9a558;border-radius:12px;background:#f6e3ad;color:#76511b;padding:0 18px;font:700 14px inherit;cursor:pointer}.random-button.rolling svg{animation:spin .6s linear infinite}.counter{font-size:11px;color:#8c816b}.name-suggestions{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-top:19px;color:#8a7b5e;font-size:12px}.name-suggestions button{border:1px dashed #bfa66e;background:#fff9e8;color:#725520;border-radius:16px;padding:6px 11px;font-family:inherit;cursor:pointer}
.check-list{list-style:none;margin:0;max-width:560px;padding:0;display:grid;gap:10px}.check-list li{display:grid;grid-template-columns:24px 1fr;align-items:center;border:1px solid #ded2ae;border-radius:12px;background:#fffdf7;padding:12px;color:#4d5c4d}.check-list svg{color:#43805c}.check-list span{display:flex;justify-content:space-between;gap:20px}.check-list b{color:#75674c}.agreement{margin-top:17px;display:flex;gap:10px;align-items:flex-start;padding:13px;border-radius:12px;background:#edf3e3;color:#52604f;font-size:13px;line-height:1.6}.agreement input{width:18px;height:18px;margin-top:2px;accent-color:var(--green)}.agreement-error{margin-top:7px}
.task-actions{display:flex;justify-content:flex-end;gap:11px;padding-top:22px;border-top:1px dashed #dbcba3}.primary-button,.secondary-button{height:48px;border-radius:24px;padding:0 24px;display:inline-flex;align-items:center;justify-content:center;gap:9px;font:800 15px inherit;cursor:pointer;text-decoration:none}.primary-button{border:1px solid #c9a44f;background:var(--green);color:#fff9e7;box-shadow:0 4px 0 #a8792b}.primary-button:hover:not(:disabled){transform:translateY(-1px);filter:brightness(1.07)}.primary-button:disabled{opacity:.65;cursor:wait}.secondary-button{border:1px solid #c8ba95;background:#fffaf0;color:#665b45}.success-button{min-width:230px}
.preview-column{display:grid;gap:15px}.completion{background:rgba(255,252,242,.9);border:1px solid #dbc485;border-radius:14px;padding:11px 14px;display:grid;grid-template-columns:1fr auto;gap:7px;color:#6c6048;font-size:12px}.completion b{color:var(--green);font-size:15px}.completion i{grid-column:1/-1;height:6px;border-radius:5px;background:#e3ddc9;overflow:hidden}.completion i span{display:block;height:100%;background:linear-gradient(90deg,#6c9c6b,#bd852d);transition:width .4s}
.card-stage{height:440px;perspective:1200px}.identity-card{height:100%;position:relative;transform-style:preserve-3d;transition:transform .9s cubic-bezier(.2,.8,.2,1)}.card-stage.revealed .identity-card{transform:rotateY(180deg)}.card-front,.card-back{position:absolute;inset:0;backface-visibility:hidden;border-radius:23px;border:2px solid #c79742;box-shadow:0 20px 45px rgba(81,57,20,.22);overflow:hidden}.card-front{display:grid;place-content:center;text-align:center;background:rgba(247,238,206,.93);border-style:dashed;color:#807252}.card-front::before{content:"";position:absolute;inset:13px;border:1px dashed #cdb274;border-radius:15px}.waiting-seal{width:88px;height:88px;margin:auto;border-radius:50%;display:grid;place-items:center;background:#e9dfc0;color:#a1844d;font-size:37px}.card-front h2{margin:15px 0 6px;color:#6e5c38;font:800 23px 'Noto Serif SC'}.card-front p{margin:0;line-height:1.7;font-size:13px}.assembly-code{margin-top:22px;font-size:10px;letter-spacing:2px}.card-back{transform:rotateY(180deg);background:linear-gradient(145deg,#fff6d8,#efe0ae);padding:22px;text-align:center}.card-back::before{content:"";position:absolute;inset:10px;border:1px dashed #bf9448;border-radius:15px}.card-back>*{position:relative;z-index:1}.card-heading{display:flex;justify-content:space-between;align-items:center;color:var(--green);border-bottom:1px solid #d5b66e;padding-bottom:9px}.card-heading span{font:800 17px 'Noto Serif SC'}.card-heading small{font-size:10px}.card-no{margin:8px 0 3px;font-size:9px;letter-spacing:2px;color:#8f743e}.portrait{width:142px;height:142px;border:6px double #b77e27;border-radius:50%;overflow:hidden;background:#f3dfad;margin:4px auto}.portrait img{width:100%;height:100%;object-fit:contain}.card-back h2{margin:5px 0;color:#5d431d;font:900 24px 'Noto Serif SC'}.card-back>strong{display:inline-block;background:var(--green);color:#ffe8ad;padding:5px 16px;border-radius:14px;font-size:13px}.card-back dl{margin:10px 0;display:grid;grid-template-columns:1fr 1fr;border-block:1px dashed #cbae69;padding-block:8px}.card-back dl div{display:grid}.card-back dt{font-size:9px;color:#8e7849}.card-back dd{margin:2px 0 0;font-size:12px;font-weight:700;color:#4f4d37}.oath{margin:8px 0;font-size:11px;color:#645c45}.stamp{position:absolute;z-index:3!important;right:22px;bottom:18px;width:68px;height:68px;border:4px double #a53e32;border-radius:50%;display:grid;place-items:center;color:#a53e32;font:800 13px/1.25 'Noto Serif SC';transform:rotate(-13deg) scale(2.2);opacity:0}.stamping .stamp{animation:stamp-in .48s .12s cubic-bezier(.3,1.5,.5,1) forwards}
.reward-drawer{border-radius:17px;padding:15px 17px;transition:.4s}.reward-drawer header{display:flex;align-items:center;gap:10px;color:#73501f}.reward-drawer header>svg{font-size:22px}.reward-drawer header span{display:grid}.reward-drawer header b{font-size:14px}.reward-drawer header small{font-size:10px;color:#89795e;margin-top:2px}.rewards{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:12px}.reward{position:relative;text-align:center;filter:grayscale(.8);opacity:.6;transition:.4s}.unlocked .reward{filter:none;opacity:1}.reward img{width:52px;height:52px;object-fit:contain}.reward span{display:grid}.reward b{font-size:10px;color:var(--green)}.reward small{font-size:8px;color:#7b705d;margin-top:2px}.reward>svg{position:absolute;right:4px;top:0;color:#4b855e}.unlocked .reward{animation:reward-pop .45s both}.unlocked .reward:nth-child(2){animation-delay:.12s}.unlocked .reward:nth-child(3){animation-delay:.24s}
.toast{position:fixed;z-index:30;left:50%;bottom:25px;transform:translateX(-50%);background:#173f2f;color:white;border:1px solid #d9b45b;border-radius:25px;padding:12px 22px;box-shadow:0 10px 35px #0004}.celebration{position:fixed;z-index:20;inset:0;pointer-events:none}.celebration i{--angle:calc(var(--i) * 20deg);position:absolute;left:68%;top:45%;width:8px;height:14px;border-radius:3px;background:hsl(calc(35 + var(--i)*4),55%,55%);animation:burst 1.5s ease-out both;transform:rotate(var(--angle)) translateY(-30px)}
.step-enter-active,.step-leave-active{transition:.22s}.step-enter-from{opacity:0;transform:translateX(12px)}.step-leave-to{opacity:0;transform:translateX(-10px)}
@keyframes spin{to{transform:rotate(360deg)}}@keyframes stamp-in{to{transform:rotate(-13deg) scale(1);opacity:.82}}@keyframes reward-pop{0%{transform:scale(.75)}70%{transform:scale(1.08)}100%{transform:scale(1)}}@keyframes burst{to{transform:rotate(var(--angle)) translateY(-360px);opacity:0}}
:focus-visible{outline:3px solid #d8a641!important;outline-offset:3px}
@media(max-width:920px){.workspace{grid-template-columns:1fr;max-width:720px}.preview-column{grid-row:2}.card-stage{height:460px}.task-card{min-height:500px}.login-link{font-size:0}.login-link::after{content:'去登录';font-size:13px}}
@media(max-width:600px){.checkin-page{overflow:auto}.topbar{height:62px;padding:0 13px}.brand-mark{width:41px;height:41px}.brand b{font-size:16px}.brand em{font-size:9px}.level-chip{font-size:10px;padding:6px 9px}.hero{padding:18px 12px 14px}.hero h1{font-size:27px;letter-spacing:2px}.hero>p:last-child{font-size:12px}.workspace{width:calc(100% - 16px);gap:12px}.progress-list{padding:13px 8px}.progress-list button{display:grid;gap:4px}.progress-list button>span:last-child{text-align:center}.progress-list small{display:none}.step-dot{width:32px;height:32px;margin:auto}.progress-list li:not(:last-child)::after{top:16px}.task-card{min-height:560px;padding:24px 15px 18px}.task-content h2{font-size:22px}.task-hint{font-size:12px;margin-bottom:18px}.avatar-picker{gap:7px}.avatar-option{padding:10px 4px}.avatar-frame{width:78px;height:78px;border-width:3px}.avatar-copy b{font-size:13px}.avatar-copy small{font-size:9px}.selected-mark{font-size:9px}.character-line{font-size:11px}.nickname-box{grid-template-columns:1fr}.random-button{width:100%}.task-actions{justify-content:stretch}.primary-button{flex:1;padding:0 13px;font-size:13px}.secondary-button{padding:0 14px;font-size:13px}.card-stage{height:430px}.rewards{gap:3px}.reward img{width:46px;height:46px}.celebration i{left:50%}}
@media(prefers-reduced-motion:reduce){*,*::before,*::after{animation-duration:.01ms!important;animation-delay:0s!important;transition-duration:.01ms!important}.card-stage.revealed .identity-card{transform:none}.card-front{display:none}.card-back{transform:none}.stamp{transform:rotate(-13deg) scale(1);opacity:.82}}
</style>
