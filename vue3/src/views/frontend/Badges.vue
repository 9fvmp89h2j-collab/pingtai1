<template>
  <main class="badges-page">
    <section class="badges-hero">
      <div class="badges-hero__copy">
        <p class="eyebrow">
          杏林成长 · 徽章墙
        </p>
        <h1>把每一次探险都贴上星光徽章</h1>
        <p class="lead">
          完成故事、点亮穴位、走过经络星河、记住安全边界，都会让徽章墙更亮一点。这里记录的是小药师的成长，不是考试排名。
        </p>
        <div class="badge-stats">
          <span><b>{{ earnedCount }}</b> 已获得</span>
          <span><b>{{ progressCount }}</b> 进行中</span>
          <span><b>{{ lockedCount }}</b> 未解锁</span>
        </div>
      </div>
      <div
        class="hero-medal"
        aria-hidden="true"
      >
        <img
          :src="heroBadgeIcon"
          alt=""
        >
        <span class="shine shine--one" />
        <span class="shine shine--two" />
        <span class="shine shine--three" />
      </div>
    </section>

    <section class="story-archive" aria-labelledby="story-archive-title">
      <div class="story-archive__heading">
        <div>
          <p class="eyebrow">杏林故事 · 案件归档</p>
          <h2 id="story-archive-title">故事收藏墙</h2>
          <span>每完成一个案件，就会留下一张属于你的故事档案卡。</span>
        </div>
        <strong>{{ archivedStoryCount }}/{{ storyArchiveCards.length }} 已归档</strong>
      </div>
      <div class="story-archive__grid">
        <article
          v-for="card in storyArchiveCards"
          :key="card.id"
          class="story-archive-card"
          :class="{ archived: card.archived, locked: card.locked }"
        >
          <div class="story-archive-card__cover">
            <img :src="card.image" :alt="card.title" />
            <span>{{ card.archived ? '已归档' : card.available ? '待调查' : '内容准备中' }}</span>
          </div>
          <div class="story-archive-card__body">
            <small>第 {{ card.number }} 案</small>
            <h3>{{ card.title }}</h3>
            <p>{{ card.archived ? '这段线索已经留在你的侦探档案里。' : card.available ? '进入故事馆，开始寻找文化线索。' : '小铜人正在整理下一卷竹简。' }}</p>
            <div class="story-archive-card__footer">
              <span class="archive-stars" :aria-label="`案件星级 ${card.stars} / 3`">
                <i v-for="star in 3" :key="star" class="fa-star" :class="star <= card.stars ? 'fa-solid' : 'fa-regular'" aria-hidden="true" />
              </span>
              <button v-if="card.available && !card.locked" type="button" @click="goStory(card)">
                {{ card.archived ? '再次调查' : '开始调查' }}
              </button>
              <span v-else class="story-archive-card__status">{{ card.locked ? '完成上一案后解锁' : '敬请期待' }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>

    <section class="badge-layout">
      <aside class="growth-panel">
        <div class="growth-card">
          <p>成长进度</p>
          <h2>{{ earnedCount }}/{{ badges.length }}</h2>
          <span>继续完成今日任务，就能让更多徽章亮起来。</span>
          <div class="growth-bar">
            <i :style="{ width: `${overallProgress}%` }" />
          </div>
        </div>

        <div class="legend-card">
          <p>徽章状态</p>
          <div class="legend-item">
            <i class="legend-dot legend-dot--earned" />
            <span>已获得：已经点亮，可以展示</span>
          </div>
          <div class="legend-item">
            <i class="legend-dot legend-dot--progress" />
            <span>进行中：正在收集星光</span>
          </div>
          <div class="legend-item">
            <i class="legend-dot legend-dot--locked" />
            <span>未解锁：完成前置任务后出现</span>
          </div>
        </div>

        <div class="next-panel">
          <p>下一枚推荐</p>
          <h3>{{ nextBadge?.name }}</h3>
          <span>{{ nextBadge?.condition }}</span>
          <button
            type="button"
            @click="goLanding"
          >
            去完成今日任务
          </button>
        </div>
      </aside>

      <section class="badge-wall">
        <div class="section-head">
          <p>我的徽章墙</p>
          <h2>杏林谷成长奖励</h2>
          <span>徽章按成长路线摆放，卡片越亮表示越接近获得。</span>
        </div>

        <div class="badge-grid">
          <article
            v-for="badge in badges"
            :key="badge.id"
            class="badge-card"
            :class="[`badge-card--${badgeState(badge)}`, `badge-card--${badge.tier}`]"
          >
            <div class="badge-card__ribbon">
              {{ stateText(badge) }}
            </div>
            <div class="badge-card__icon">
              <img
                :src="badge.icon"
                :alt="badge.name"
              >
            </div>
            <div class="badge-card__body">
              <p>{{ tierText(badge.tier) }}</p>
              <h3>{{ badge.name }}</h3>
              <span>{{ badge.description }}</span>
            </div>
            <div class="badge-card__condition">
              <b>获得条件</b>
              <span>{{ badge.condition }}</span>
            </div>
            <div class="badge-progress">
              <div>
                <span>进度</span>
                <b>{{ badge.progress }}/{{ badge.total }}</b>
              </div>
              <div class="badge-progress__bar">
                <i :style="{ width: `${progressPercent(badge)}%` }" />
              </div>
            </div>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGameState } from '@/composables/useGameState'
import { storyCases, storyChapterCatalog } from '@/data/storyCases'
import { generatedRewardAssets } from '@/data/generatedRewardAssets'

const router = useRouter()
const { badges, storyArchiveIds, storyProgress } = useGameState()
const heroBadgeIcon = generatedRewardAssets.safetyObservationBadge

const storyArchiveCards = computed(() => storyChapterCatalog.map((chapter, index) => {
  const storyCase = storyCases.find((item) => item.id === chapter.id)
  const previous = storyChapterCatalog[index - 1]
  const archived = storyArchiveIds.value.includes(chapter.id)
  return {
    ...chapter,
    image: storyCase?.cover || chapter.image,
    available: Boolean(storyCase?.available),
    archived,
    locked: !storyCase?.available || (index > 0 && !storyArchiveIds.value.includes(previous.id)),
    stars: Number(storyProgress.value?.[chapter.id]?.stars || 0)
  }
}))
const archivedStoryCount = computed(() => storyArchiveCards.value.filter((card) => card.archived).length)

const earnedCount = computed(() => badges.value.filter((badge) => badgeState(badge) === 'earned').length)
const progressCount = computed(() => badges.value.filter((badge) => badgeState(badge) === 'progress').length)
const lockedCount = computed(() => badges.value.filter((badge) => badgeState(badge) === 'locked').length)
const overallProgress = computed(() => Math.round((earnedCount.value / badges.value.length) * 100))
const nextBadge = computed(() => {
  return badges.value.find((badge) => badgeState(badge) === 'progress') ||
    badges.value.find((badge) => badgeState(badge) === 'available') ||
    badges.value.find((badge) => badgeState(badge) === 'locked')
})

function badgeState(badge) {
  if (badge.status === 'earned' || badge.progress >= badge.total) return 'earned'
  if (badge.status === 'in-progress') return 'progress'
  if (badge.status === 'available') return 'available'
  return 'locked'
}

function stateText(badge) {
  const map = {
    earned: '已获得',
    progress: '进行中',
    available: '可挑战',
    locked: '未解锁'
  }
  return map[badgeState(badge)]
}

function tierText(tier) {
  const map = {
    bronze: '青铜徽章',
    silver: '白银徽章',
    gold: '金色徽章',
    platinum: '杏林徽章',
    legendary: '传说徽章'
  }
  return map[tier] || '成长徽章'
}

function progressPercent(badge) {
  if (!badge.total) return 0
  return Math.min(100, Math.round((badge.progress / badge.total) * 100))
}

function goLanding() {
  router.push('/home-map')
}

function goStory(card) {
  router.push({ path: '/doctor-story', query: { case: card.id } })
}
</script>

<style scoped>
.badges-page {
  --cream: #fff8e8;
  --paper: #fffdf3;
  --green: #2f7d68;
  --deep-green: #24584b;
  --mint: #dff2d6;
  --gold: #ffd35a;
  --bronze: #b8863b;
  --soft-text: #65766f;
  min-height: calc(100vh - 64px);
  padding: 34px 0 56px;
  color: #263f37;
  background:
    radial-gradient(circle at 12% 8%, rgba(255, 211, 90, 0.2), transparent 24%),
    linear-gradient(180deg, #f6ffe6 0%, var(--cream) 48%, #fffaf0 100%);
}

.badges-hero,
.badge-layout {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
}

.badges-hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 28px;
  align-items: center;
  min-height: 300px;
  padding: 34px 42px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(255, 253, 243, 0.96), rgba(223, 242, 214, 0.76)),
    radial-gradient(circle at 82% 20%, rgba(255, 211, 90, 0.34), transparent 28%);
  border: 1px solid rgba(184, 134, 59, 0.16);
  border-radius: 8px;
  box-shadow:
    inset 0 0 0 8px rgba(255, 248, 232, 0.45),
    0 24px 48px rgba(46, 83, 63, 0.12);
}

.badges-hero::before {
  content: "";
  position: absolute;
  inset: 14px;
  pointer-events: none;
  border: 1px dashed rgba(184, 134, 59, 0.22);
  border-radius: 8px;
}

.story-archive {
  width: min(1180px, calc(100% - 32px));
  margin: 24px auto 0;
  padding: 24px;
  background: rgba(255, 253, 243, 0.88);
  border: 2px solid rgba(47, 125, 104, 0.12);
  border-radius: 8px;
  box-shadow: 0 16px 34px rgba(46, 83, 63, 0.1);
}

.story-archive__heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.story-archive__heading h2 {
  margin: 0;
  color: var(--deep-green);
  font-size: 30px;
  font-weight: 950;
}

.story-archive__heading span {
  display: block;
  margin-top: 7px;
  color: var(--soft-text);
}

.story-archive__heading > strong {
  flex: 0 0 auto;
  padding: 8px 12px;
  color: #684913;
  background: #fff3bd;
  border-radius: 999px;
}

.story-archive__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.story-archive-card {
  overflow: hidden;
  background: #fffdf3;
  border: 2px solid rgba(214, 166, 91, 0.2);
  border-radius: 8px;
}

.story-archive-card.archived {
  border-color: rgba(47, 125, 104, 0.44);
  box-shadow: inset 0 0 0 4px rgba(223, 242, 214, 0.5);
}

.story-archive-card.locked {
  filter: saturate(0.55);
}

.story-archive-card__cover {
  position: relative;
  height: 124px;
  overflow: hidden;
  background: #edf5df;
}

.story-archive-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 12px;
}

.story-archive-card__cover span {
  position: absolute;
  top: 9px;
  right: 9px;
  padding: 4px 8px;
  color: #fffdf3;
  font-size: 11px;
  font-weight: 950;
  background: var(--green);
  border-radius: 999px;
}

.story-archive-card.locked .story-archive-card__cover span {
  background: #8da099;
}

.story-archive-card__body {
  display: grid;
  gap: 7px;
  padding: 13px;
}

.story-archive-card__body small {
  color: var(--bronze);
  font-weight: 900;
}

.story-archive-card__body h3 {
  margin: 0;
  color: var(--deep-green);
  font-size: 18px;
  font-weight: 950;
}

.story-archive-card__body p {
  min-height: 44px;
  margin: 0;
  color: var(--soft-text);
  font-size: 12px;
  line-height: 1.55;
}

.story-archive-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 30px;
}

.archive-stars {
  color: #d99c2c;
  white-space: nowrap;
}

.archive-stars i {
  margin-right: 2px;
}

.story-archive-card__footer button {
  min-height: 30px;
  padding: 4px 9px;
  color: #fffdf3;
  font-size: 11px;
  font-weight: 950;
  background: var(--green);
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.story-archive-card__status {
  color: #8da099;
  font-size: 11px;
  font-weight: 900;
  text-align: right;
}

.badges-hero__copy,
.hero-medal {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.eyebrow,
.section-head p,
.growth-card p,
.legend-card p,
.next-panel p {
  margin: 0 0 10px;
  color: var(--bronze);
  font-size: 15px;
  font-weight: 900;
}

.badges-hero h1 {
  max-width: 760px;
  margin: 0;
  color: var(--deep-green);
  font-size: clamp(34px, 5vw, 58px);
  font-weight: 950;
  line-height: 1.08;
  text-shadow: 0 4px 0 rgba(255, 243, 189, 0.82);
}

.lead {
  max-width: 720px;
  margin: 18px 0 0;
  color: #50665d;
  font-size: 17px;
  line-height: 1.8;
}

.badge-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.badge-stats span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 13px;
  color: #24584b;
  font-weight: 900;
  background: rgba(255, 253, 243, 0.82);
  border: 1px solid rgba(47, 125, 104, 0.12);
  border-radius: 999px;
}

.badge-stats b {
  color: #8a5d1d;
  font-size: 20px;
}

.hero-medal {
  display: grid;
  min-height: 210px;
  place-items: center;
}

.hero-medal img {
  width: 190px;
  height: 190px;
  object-fit: contain;
  filter: drop-shadow(0 20px 20px rgba(91, 61, 24, 0.18));
}

.shine {
  position: absolute;
  width: 12px;
  height: 12px;
  background: var(--gold);
  border: 2px solid rgba(255, 253, 243, 0.95);
  border-radius: 50%;
  box-shadow: 0 0 18px rgba(255, 211, 90, 0.92);
  animation: shine-pulse 2.2s ease-in-out infinite;
}

.shine--one {
  top: 28px;
  right: 68px;
}

.shine--two {
  left: 36px;
  top: 96px;
  animation-delay: 0.25s;
}

.shine--three {
  right: 42px;
  bottom: 56px;
  animation-delay: 0.5s;
}

.badge-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 22px;
  align-items: start;
  margin-top: 24px;
}

.growth-panel {
  position: sticky;
  top: 88px;
  display: grid;
  gap: 14px;
}

.growth-card,
.legend-card,
.next-panel,
.badge-wall {
  background: rgba(255, 253, 243, 0.86);
  border: 2px solid rgba(47, 125, 104, 0.12);
  border-radius: 8px;
  box-shadow: 0 16px 34px rgba(46, 83, 63, 0.1);
}

.growth-card,
.legend-card,
.next-panel {
  padding: 18px;
}

.growth-card h2 {
  margin: 0;
  color: var(--deep-green);
  font-size: 42px;
  font-weight: 950;
}

.growth-card span,
.next-panel span {
  display: block;
  margin-top: 6px;
  color: var(--soft-text);
  line-height: 1.6;
}

.growth-bar,
.badge-progress__bar {
  height: 12px;
  margin-top: 14px;
  overflow: hidden;
  background: #e6f0da;
  border: 1px solid rgba(47, 125, 104, 0.1);
  border-radius: 999px;
}

.growth-bar i,
.badge-progress__bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--green), var(--gold));
  border-radius: inherit;
}

.legend-card {
  display: grid;
  gap: 10px;
}

.legend-item {
  display: flex;
  gap: 9px;
  align-items: center;
  color: #536b61;
  line-height: 1.45;
}

.legend-dot {
  width: 12px;
  height: 12px;
  flex: 0 0 auto;
  border-radius: 50%;
}

.legend-dot--earned {
  background: var(--green);
}

.legend-dot--progress {
  background: var(--gold);
}

.legend-dot--locked {
  background: #a9b8b1;
}

.next-panel h3 {
  margin: 0;
  color: var(--deep-green);
  font-size: 21px;
  font-weight: 950;
}

.next-panel button {
  width: 100%;
  min-height: 42px;
  margin-top: 14px;
  color: #fff8e8;
  font-weight: 950;
  background: var(--green);
  border: 0;
  border-radius: 999px;
  cursor: pointer;
}

.badge-wall {
  padding: 24px;
}

.section-head {
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  color: var(--deep-green);
  font-size: 28px;
  font-weight: 950;
}

.section-head span {
  display: block;
  margin-top: 8px;
  color: var(--soft-text);
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.badge-card {
  position: relative;
  display: grid;
  gap: 12px;
  min-height: 350px;
  padding: 16px;
  overflow: hidden;
  background:
    radial-gradient(circle at 88% 12%, rgba(255, 211, 90, 0.18), transparent 26%),
    linear-gradient(180deg, #fffdf3 0%, #fff8e8 100%);
  border: 2px solid rgba(214, 166, 91, 0.18);
  border-radius: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.badge-card:hover {
  box-shadow: 0 16px 26px rgba(36, 88, 75, 0.13);
  transform: translateY(-3px);
}

.badge-card--earned {
  border-color: rgba(47, 125, 104, 0.42);
  box-shadow: inset 0 0 0 4px rgba(223, 242, 214, 0.46);
}

.badge-card--progress,
.badge-card--available {
  border-color: rgba(255, 211, 90, 0.72);
}

.badge-card--locked {
  filter: saturate(0.65);
}

.badge-card--locked .badge-card__icon img {
  opacity: 0.48;
}

.badge-card__ribbon {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 5px 10px;
  color: #fff8e8;
  font-size: 12px;
  font-weight: 950;
  background: var(--green);
  border-radius: 999px;
}

.badge-card--progress .badge-card__ribbon,
.badge-card--available .badge-card__ribbon {
  color: #684913;
  background: var(--gold);
}

.badge-card--locked .badge-card__ribbon {
  background: #8da099;
}

.badge-card__icon {
  display: grid;
  width: 94px;
  height: 94px;
  place-items: center;
  margin-top: 12px;
  background: #fff3bd;
  border: 2px solid rgba(214, 166, 91, 0.18);
  border-radius: 50%;
  box-shadow:
    inset 0 -6px 0 rgba(184, 134, 59, 0.08),
    0 8px 16px rgba(154, 106, 44, 0.1);
}

.badge-card__icon img {
  width: 70px;
  height: 70px;
  object-fit: contain;
}

.badge-card__body p {
  margin: 0 0 4px;
  color: var(--bronze);
  font-size: 12px;
  font-weight: 900;
}

.badge-card__body h3 {
  margin: 0;
  color: var(--deep-green);
  font-size: 21px;
  font-weight: 950;
}

.badge-card__body span {
  display: block;
  margin-top: 8px;
  color: #566b62;
  line-height: 1.55;
}

.badge-card__condition {
  padding: 10px;
  background: rgba(223, 242, 214, 0.54);
  border: 1px solid rgba(47, 125, 104, 0.08);
  border-radius: 8px;
}

.badge-card__condition b,
.badge-card__condition span {
  display: block;
}

.badge-card__condition b {
  color: #24584b;
  font-size: 12px;
}

.badge-card__condition span {
  margin-top: 3px;
  color: #607269;
  line-height: 1.45;
}

.badge-progress {
  align-self: end;
}

.badge-progress > div:first-child {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #24584b;
  font-weight: 900;
}

.badge-progress__bar {
  height: 10px;
}

@keyframes shine-pulse {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(0.82);
  }

  50% {
    opacity: 1;
    transform: scale(1.12);
  }
}

@media (max-width: 1100px) {
  .badges-hero,
  .badge-layout {
    width: min(100% - 24px, 1180px);
  }

  .badge-layout {
    grid-template-columns: 1fr;
  }

  .growth-panel {
    position: static;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .story-archive__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .badges-hero {
    grid-template-columns: 1fr;
    padding: 28px 22px;
  }

  .hero-medal {
    min-height: 160px;
  }

  .hero-medal img {
    width: 150px;
    height: 150px;
  }

  .growth-panel,
  .badge-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .badges-page {
    padding-top: 22px;
  }

  .badges-hero h1 {
    font-size: 32px;
  }

  .badge-wall {
    padding: 16px;
  }

  .growth-panel,
  .badge-grid {
    grid-template-columns: 1fr;
  }

  .story-archive {
    width: min(100% - 24px, 1180px);
    padding: 16px;
  }

  .story-archive__heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .story-archive__grid {
    grid-template-columns: 1fr;
  }
}
</style>
