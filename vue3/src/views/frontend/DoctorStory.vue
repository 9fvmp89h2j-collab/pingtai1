<template>
  <main class="story-page">
    <div class="story-page__texture" aria-hidden="true" />

    <div class="story-shell">
      <header class="page-heading">
        <img :src="bambooSlipIcon" class="page-heading__icon" alt="" />
        <div>
          <p class="page-heading__eyebrow">杏林谷 · 儿童文化探案</p>
          <h1 id="story-page-title">针灸故事馆</h1>
          <p>跟着小铜人老师，用眼睛观察、用线索推理，认识中医文化。</p>
        </div>
        <div class="case-mascot" aria-hidden="true">
          <img :src="detectiveGuide" alt="" />
          <span>今日案件</span>
        </div>
      </header>

      <nav class="story-stepper" aria-label="案件进度">
        <button
          v-for="step in stepItems"
          :key="step.id"
          type="button"
          :class="['step-item', { active: currentStep === step.id, done: stepIsDone(step.id) }]"
          :disabled="!stepIsReachable(step.id)"
          :aria-current="currentStep === step.id ? 'step' : undefined"
          @click="jumpToStep(step.id)"
        >
          <span class="step-item__number">
            <i v-if="stepIsDone(step.id)" class="fa-solid fa-check" aria-hidden="true" />
            <span v-else>{{ step.number }}</span>
          </span>
          <span>{{ step.label }}</span>
        </button>
      </nav>

      <div class="story-workspace">
        <aside class="chapter-rail" aria-label="故事章节">
          <div class="rail-heading">
            <span class="section-kicker">案件目录</span>
            <strong>{{ storyArchiveIds.length }} / {{ chapters.length }} 已归档</strong>
          </div>

          <button
            v-for="chapter in chapters"
            :key="chapter.id"
            type="button"
            class="chapter-card"
            :class="{
              'chapter-card--active': chapter.id === activeCaseId,
              'chapter-card--locked': chapter.locked,
              'chapter-card--archived': chapter.progress.completed
            }"
            :disabled="chapter.locked"
            @click="selectChapter(chapter)"
          >
            <span class="chapter-card__art">
              <img :src="chapter.image" :alt="chapter.title" />
              <span v-if="chapter.progress.completed" class="chapter-card__check">
                <i class="fa-solid fa-check" aria-hidden="true" />
              </span>
            </span>
            <span class="chapter-card__copy">
              <span class="chapter-card__eyebrow">第{{ chapter.number }}章</span>
              <strong>{{ chapter.title }}</strong>
              <span v-if="chapter.locked" class="chapter-card__status">
                <i class="fa-solid fa-lock" aria-hidden="true" /> 完成上一案解锁
              </span>
              <span v-else class="chapter-card__stars" :aria-label="`${chapter.progress.stars} 颗星`">
                <i
                  v-for="star in 3"
                  :key="star"
                  class="fa-star"
                  :class="star <= chapter.progress.stars ? 'fa-solid' : 'fa-regular'"
                  aria-hidden="true"
                />
                <small>{{ chapter.progress.completed ? '已归档' : '可调查' }}</small>
              </span>
            </span>
            <span v-if="chapter.id === activeCaseId" class="chapter-card__tag">当前案件</span>
          </button>

          <button type="button" class="all-stories-btn" @click="showAllStories">
            <i class="fa-solid fa-book-open" aria-hidden="true" />
            查看故事收藏墙
            <i class="fa-solid fa-arrow-right" aria-hidden="true" />
          </button>
        </aside>

        <section class="story-stage" aria-labelledby="current-case-title">
          <Transition name="reward-pop">
            <div v-if="rewardToast" class="story-reward-toast" role="status" aria-live="polite">
              <i class="fa-solid fa-star" aria-hidden="true" />
              {{ rewardToast }}
            </div>
          </Transition>
          <template v-if="currentCase">
            <header class="case-heading">
              <div>
                <span class="case-heading__label">
                  <i class="fa-solid fa-magnifying-glass" aria-hidden="true" />
                  正在调查 · 第{{ currentCase.number }}案
                </span>
                <h2 id="current-case-title">{{ currentCase.title }}</h2>
                <p>{{ currentCase.subtitle }}</p>
              </div>
              <div class="case-score" aria-label="当前案件星级">
                <span>案件星级</span>
                <div>
                  <i
                    v-for="star in 3"
                    :key="star"
                    class="fa-star"
                    :class="star <= storyProgress.stars ? 'fa-solid' : 'fa-regular'"
                    aria-hidden="true"
                  />
                </div>
              </div>
            </header>

            <section v-if="currentStep === 'read'" class="case-panel read-panel">
              <div class="case-hero">
                <img :src="currentCase.sceneImage" :alt="`${currentCase.title}调查现场`" />
                <span class="case-hero__stamp">CASE {{ currentCase.caseCode || currentCase.number }}</span>
                <div class="case-hero__caption">
                  <strong>先读故事，再开始调查</strong>
                  <span>每一页都可能藏着重要线索。</span>
                </div>
                <img :src="currentCase.guideImage" class="case-hero__guide" alt="小铜人侦探老师" />
              </div>

              <div class="read-intro">
                <div>
                  <span class="section-kicker">案件简报</span>
                  <h3>{{ currentCase.title }}</h3>
                  <p>{{ currentCase.summary || currentCase.subtitle }}</p>
                </div>
                <div class="read-goal">
                  <span>阅读进度</span>
                  <strong>{{ readPageCount }} / {{ currentCase.pages.length }}</strong>
                </div>
              </div>

              <div class="page-preview-grid" aria-label="故事绘本页面">
                <button
                  v-for="(page, index) in currentCase.pages"
                  :key="page.id"
                  type="button"
                  class="page-preview"
                  :class="{ 'page-preview--read': isPageRead(page.id) }"
                  @click="openReader(index)"
                >
                  <span class="page-preview__image">
                    <img :src="page.image" :alt="page.title" />
                    <span class="page-preview__state">
                      <i :class="isPageRead(page.id) ? 'fa-solid fa-check' : 'fa-solid fa-book-open'" aria-hidden="true" />
                    </span>
                  </span>
                  <span class="page-preview__copy">
                    <small>第{{ index + 1 }}页</small>
                    <strong>{{ page.title }}</strong>
                  </span>
                </button>
              </div>

              <div class="stage-action-bar">
                <div class="guide-hint">
                  <img :src="detectiveGuide" alt="" />
                  <span>{{ allPagesRead ? '绘本读完啦，去现场找线索吧！' : '认真观察，线索不会自己跳出来哦。' }}</span>
                </div>
                <button type="button" class="primary-action" @click="startReading">
                  <i :class="allPagesRead ? 'fa-solid fa-magnifying-glass' : 'fa-solid fa-book-open'" aria-hidden="true" />
                  {{ allPagesRead ? '进入调查现场' : (readPageCount ? '继续阅读' : '开始阅读') }}
                </button>
              </div>
            </section>

            <section v-else-if="currentStep === 'clues'" class="case-panel clues-panel">
              <div class="stage-title-row">
                <div>
                  <span class="section-kicker">第一关 · 观察现场</span>
                  <h3>找出 {{ currentCase.clues.length }} 条线索</h3>
                  <p>点击现场里闪着小光圈的地方，也可以点击下方的线索卡。</p>
                </div>
                <div class="mini-counter" :class="{ complete: allCluesFound }">
                  <strong>{{ foundClueCount }} / {{ currentCase.clues.length }}</strong>
                  <span>{{ allCluesFound ? '线索齐了' : '已发现' }}</span>
                </div>
              </div>

              <div class="investigation-scene">
                <img :src="currentCase.sceneImage" :alt="`${currentCase.title}调查现场`" />
                <span class="scene-note scene-note--top">点击现场中的小光圈</span>
                <button
                  v-for="clue in currentCase.clues"
                  :key="clue.id"
                  type="button"
                  class="hotspot"
                  :class="{ found: isClueFound(clue.id) }"
                  :style="hotspotStyle(clue)"
                  :aria-label="isClueFound(clue.id) ? `已发现${clue.name}` : `发现${clue.name}`"
                  :aria-pressed="isClueFound(clue.id)"
                  @click="findClue(clue)"
                >
                  <i :class="isClueFound(clue.id) ? 'fa-solid fa-check' : 'fa-solid fa-plus'" aria-hidden="true" />
                  <span>{{ isClueFound(clue.id) ? '已发现' : '线索' }}</span>
                </button>
              </div>

              <div class="clue-card-grid" aria-label="线索卡">
                <button
                  v-for="clue in currentCase.clues"
                  :key="`card-${clue.id}`"
                  type="button"
                  class="clue-card"
                  :class="{ found: isClueFound(clue.id) }"
                  :aria-pressed="isClueFound(clue.id)"
                  @click="findClue(clue)"
                >
                  <span class="clue-card__icon">
                    <img v-if="clue.icon" :src="clue.icon" alt="" aria-hidden="true" />
                    <i v-else :class="isClueFound(clue.id) ? 'fa-solid fa-check' : 'fa-solid fa-fingerprint'" aria-hidden="true" />
                  </span>
                  <span>
                    <strong>{{ clue.name }}</strong>
                    <small>{{ isClueFound(clue.id) ? clue.description : '点击现场寻找它' }}</small>
                  </span>
                </button>
              </div>

              <div class="stage-action-bar">
                <div class="guide-hint">
                  <img :src="detectiveGuide" alt="" />
                  <span>{{ allCluesFound ? '太棒了！现在把线索拼成一个解释。' : '找齐线索可以点亮第二颗星，但不限制你继续调查。' }}</span>
                </div>
                <button type="button" class="primary-action" :disabled="!allCluesFound" @click="startReasoning">
                  开始推理
                  <i class="fa-solid fa-arrow-right" aria-hidden="true" />
                </button>
              </div>
            </section>

            <section v-else-if="currentStep === 'reasoning'" class="case-panel reasoning-panel">
              <div class="stage-title-row">
                <div>
                  <span class="section-kicker">第二关 · 拼合推理</span>
                  <h3>{{ currentCase.reasoning.title }}</h3>
                  <p>{{ currentCase.reasoning.prompt }}</p>
                </div>
                <span class="stage-badge"><i class="fa-solid fa-puzzle-piece" aria-hidden="true" /> 线索板</span>
              </div>

              <div class="evidence-board">
                <div class="evidence-board__header">
                  <span>把你发现的线索放进推理板</span>
                  <small>可拖动，也可以直接点击线索卡</small>
                </div>
                <div class="evidence-slots" @dragover.prevent>
                  <div
                    v-for="(clueId, index) in evidenceSlots"
                    :key="`slot-${index}`"
                    class="evidence-slot"
                    @drop="dropEvidence($event, index)"
                  >
                    <span class="evidence-slot__number">{{ index + 1 }}</span>
                    <template v-if="clueId">
                      <strong>{{ clueById(clueId)?.name }}</strong>
                      <button type="button" aria-label="移除线索" @click="removeEvidence(clueId)">
                        <i class="fa-solid fa-xmark" aria-hidden="true" />
                      </button>
                    </template>
                    <span v-else class="evidence-slot__empty">拖入线索</span>
                  </div>
                </div>
                <div class="evidence-pieces">
                  <button
                    v-for="clue in currentCase.clues"
                    :key="`piece-${clue.id}`"
                    type="button"
                    class="evidence-piece"
                    :class="{ pinned: evidenceOrder.includes(clue.id) }"
                    draggable="true"
                    @dragstart="dragEvidence($event, clue.id)"
                    @click="toggleEvidence(clue.id)"
                  >
                    <img v-if="clue.icon" :src="clue.icon" alt="" aria-hidden="true" />
                    <i v-else class="fa-solid fa-tag" aria-hidden="true" />
                    {{ clue.name }}
                  </button>
                </div>
              </div>

              <div class="answer-options" role="group" :aria-label="currentCase.reasoning.title">
                <button
                  v-for="option in currentCase.reasoning.options"
                  :key="option.id"
                  type="button"
                  class="answer-option"
                  :class="{ selected: selectedReasoning === option.id, correct: reasoningSubmitted && option.correct, wrong: reasoningSubmitted && selectedReasoning === option.id && !option.correct }"
                  :aria-pressed="selectedReasoning === option.id"
                  @click="chooseReasoning(option)"
                >
                  <span class="answer-option__radio"><i v-if="selectedReasoning === option.id" class="fa-solid fa-check" aria-hidden="true" /></span>
                  <span><strong>{{ option.title }}</strong><small>{{ option.detail }}</small></span>
                </button>
              </div>

              <p v-if="stageFeedback" class="stage-feedback" :class="{ success: feedbackType === 'success' }" role="status" aria-live="polite">
                <i :class="feedbackType === 'success' ? 'fa-solid fa-sparkles' : 'fa-solid fa-lightbulb'" aria-hidden="true" />
                {{ stageFeedback }}
              </p>

              <div class="stage-action-bar">
                <div class="guide-hint">
                  <img :src="detectiveGuide" alt="" />
                  <span>{{ reasoningSubmitted ? '再看一眼线索，想好后可以重新选择。' : '没有关系，侦探可以多观察几次再作答。' }}</span>
                </div>
                <button type="button" class="primary-action" :disabled="!selectedReasoning" @click="submitReasoning">
                  {{ reasoningSubmitted && reasoningCorrect ? '进入安全判断' : '提交推理' }}
                  <i class="fa-solid fa-arrow-right" aria-hidden="true" />
                </button>
              </div>
            </section>

            <section v-else-if="currentStep === 'safety'" class="case-panel safety-panel">
              <div class="stage-title-row">
                <div>
                  <span class="section-kicker">第三关 · 安全判断</span>
                  <h3>{{ currentCase.safety.title }}</h3>
                  <p>{{ currentCase.safety.prompt }}</p>
                </div>
                <span class="stage-badge stage-badge--safe"><i class="fa-solid fa-shield-heart" aria-hidden="true" /> 安全星</span>
              </div>

              <div class="safety-story-banner">
                <img :src="safetyBellIcon" alt="安全铃铛" />
                <div>
                  <strong>小铜人老师的安全提醒</strong>
                  <span>这里学习的是中医文化和身体认知，不提供自行针刺或治疗指导。</span>
                </div>
              </div>

              <div class="answer-options" role="group" :aria-label="currentCase.safety.title">
                <button
                  v-for="option in currentCase.safety.options"
                  :key="option.id"
                  type="button"
                  class="answer-option answer-option--safety"
                  :class="{ selected: selectedSafety === option.id, correct: safetySubmitted && option.correct, wrong: safetySubmitted && selectedSafety === option.id && !option.correct }"
                  :aria-pressed="selectedSafety === option.id"
                  @click="chooseSafety(option)"
                >
                  <span class="answer-option__radio"><i v-if="selectedSafety === option.id" class="fa-solid fa-check" aria-hidden="true" /></span>
                  <span><strong>{{ option.title }}</strong><small>{{ option.detail }}</small></span>
                </button>
              </div>

              <p v-if="stageFeedback" class="stage-feedback" :class="{ success: feedbackType === 'success' }" role="status" aria-live="polite">
                <i :class="feedbackType === 'success' ? 'fa-solid fa-shield-heart' : 'fa-solid fa-lightbulb'" aria-hidden="true" />
                {{ stageFeedback }}
              </p>

              <div class="stage-action-bar">
                <div class="guide-hint">
                  <img :src="safetyBellIcon" alt="" />
                  <span>答错可以重新选择，安全知识要记在心里。</span>
                </div>
                <button type="button" class="primary-action primary-action--gold" :disabled="!selectedSafety" @click="submitSafety">
                  {{ safetySubmitted && safetyCorrect ? '查看案件结果' : '提交安全判断' }}
                  <i class="fa-solid fa-arrow-right" aria-hidden="true" />
                </button>
              </div>
            </section>

            <section v-else class="case-panel reward-panel">
              <div class="reward-hero">
                <div class="reward-hero__stars" role="img" :aria-label="`案件星级 ${storyProgress.stars} / 3`">
                  <i
                    v-for="star in 3"
                    :key="star"
                    class="fa-star"
                    :class="star <= storyProgress.stars ? 'fa-solid' : 'fa-regular'"
                    aria-hidden="true"
                  />
                </div>
                <img :src="detectiveSuccess" alt="小铜人侦探庆祝案件完成" />
                <span class="reward-hero__stamp">CASE CLOSED</span>
                <h3>{{ storyProgress.stars >= 3 ? '三星归档！' : '案件完成！' }}</h3>
                <p>{{ storyProgress.stars >= 3 ? '你找齐了所有线索，也记住了安全边界。' : '你已经找到了案件的关键答案，可以继续收集星星。' }}</p>
              </div>
              <div class="reward-list">
                  <div v-for="reward in displayedRewards" :key="`${reward.id}-${reward.count}`" class="reward-row">
                    <img :src="reward.icon" :alt="reward.name" />
                    <span><strong>{{ reward.name }}</strong><small>{{ reward.id === 'story-archive-badge' ? '三星案件奖励' : '故事任务奖励' }}</small></span>
                  <b>x{{ reward.count }}</b>
                </div>
              </div>
              <div class="stage-action-bar">
                <div class="guide-hint">
                  <img :src="detectiveGuide" alt="" />
                  <span>故事会被收藏到侦探社档案墙里。</span>
                </div>
                <div class="reward-actions">
                  <button type="button" class="secondary-action" @click="replayCase">再调查一次</button>
                  <button type="button" class="primary-action" @click="goMap">
                    返回探险地图
                    <i class="fa-solid fa-map" aria-hidden="true" />
                  </button>
                </div>
              </div>
            </section>
          </template>

          <section v-else class="case-panel coming-soon-panel">
            <img :src="detectiveGuide" alt="小铜人侦探老师" />
            <span class="section-kicker">下一卷竹简正在整理中</span>
            <h2>新案件即将开放</h2>
            <p>先把已经发现的故事收藏起来，新的线索会在杏林谷继续出现。</p>
            <button type="button" class="primary-action" @click="selectChapter(chapters[0])">回到当前案件</button>
          </section>
        </section>

        <aside class="mission-rail" aria-label="案件任务与奖励">
          <section class="side-card mission-card">
            <div class="side-card__heading">
              <h2>本案目标</h2>
              <strong>{{ overallProgress }}%</strong>
            </div>
            <div class="overall-progress" role="progressbar" aria-label="案件总进度" aria-valuemin="0" aria-valuemax="100" :aria-valuenow="overallProgress">
              <span :style="{ width: `${overallProgress}%` }" />
            </div>
            <div v-for="objective in objectives" :key="objective.id" class="mission-item" :class="{ complete: objective.done }">
              <span class="mission-item__icon"><i :class="objective.icon" aria-hidden="true" /></span>
              <div>
                <strong>{{ objective.name }}</strong>
                <small>{{ objective.done ? '已完成' : objective.description }}</small>
              </div>
              <b>{{ objective.progress }}/{{ objective.total }}</b>
            </div>
          </section>

          <section class="side-card inventory-card">
            <div class="side-card__heading">
              <h2>线索背包</h2>
              <span>{{ foundClueCount }}/{{ currentCase?.clues.length || 0 }}</span>
            </div>
            <div class="clue-inventory">
              <div v-for="clue in currentCase?.clues || []" :key="`inventory-${clue.id}`" class="inventory-item" :class="{ found: isClueFound(clue.id) }">
                <span>
                  <img v-if="isClueFound(clue.id) && clue.icon" :src="clue.icon" alt="" aria-hidden="true" />
                  <i v-else :class="isClueFound(clue.id) ? 'fa-solid fa-check' : 'fa-solid fa-lock'" aria-hidden="true" />
                </span>
                <strong>{{ isClueFound(clue.id) ? clue.name : '未发现线索' }}</strong>
              </div>
            </div>
          </section>

          <section class="side-card star-card reward-progress-card" aria-label="本案奖励进度">
            <div class="side-card__heading">
              <h2>本案奖励进度</h2>
              <span>不计时 · 不扣分</span>
            </div>
            <div class="star-goal"><i class="fa-solid fa-star" aria-hidden="true" /><span>读完{{ currentCase?.pages.length || 0 }}页绘本</span><b :class="{ done: allPagesRead }">{{ allPagesRead ? '完成' : '待完成' }}</b></div>
            <div class="star-goal"><i class="fa-solid fa-star" aria-hidden="true" /><span>找齐{{ currentCase?.clues.length || 0 }}条线索</span><b :class="{ done: allCluesFound }">{{ allCluesFound ? '完成' : '待完成' }}</b></div>
            <div class="star-goal"><i class="fa-solid fa-star" aria-hidden="true" /><span>推理与安全判断</span><b :class="{ done: reasoningCorrect && safetyCorrect }">{{ reasoningCorrect && safetyCorrect ? '完成' : '待完成' }}</b></div>
          </section>

          <section class="safety-note">
            <img :src="safetyBellIcon" alt="安全铃铛" />
            <div>
              <strong>安全提醒</strong>
              <p>故事馆只用于中医文化科普和身体认知，不提供自行针刺或治疗指导。</p>
            </div>
          </section>
        </aside>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="reader">
        <div v-if="readerOpen" class="reader-overlay" role="presentation" @click.self="closeReader">
          <section ref="readerDialog" class="reader-dialog" role="dialog" aria-modal="true" aria-labelledby="reader-title" tabindex="-1" @keydown.esc="closeReader">
            <header class="reader-dialog__header">
              <div>
                <span>{{ currentCase.title }} · 第{{ readerIndex + 1 }}页</span>
                <h2 id="reader-title">{{ currentReaderPage.title }}</h2>
              </div>
              <button ref="readerCloseButton" type="button" aria-label="关闭阅读" @click="closeReader">
                <i class="fa-solid fa-xmark" aria-hidden="true" />
              </button>
            </header>
            <div class="reader-dialog__body">
              <div class="reader-dialog__illustration">
                <img :src="currentReaderPage.image" :alt="currentReaderPage.title" />
                <span class="reader-dialog__page-mark">{{ readerIndex + 1 }} / {{ currentCase.pages.length }}</span>
              </div>
              <div class="reader-dialog__story">
                <span class="section-kicker">竹简故事</span>
                <p>{{ currentReaderPage.text }}</p>
                <div class="reader-dialog__tip">
                  <img :src="detectiveGuide" alt="" />
                  <span>{{ currentReaderPage.tip }}</span>
                </div>
              </div>
            </div>
            <footer class="reader-dialog__footer">
              <div class="reader-dots" aria-label="绘本页码">
                <button
                  v-for="(page, index) in currentCase.pages"
                  :key="page.id"
                  type="button"
                  :class="{ active: index === readerIndex, read: isPageRead(page.id) }"
                  :aria-label="`打开第${index + 1}页`"
                  @click="readerIndex = index"
                />
              </div>
              <button type="button" class="reader-next-btn" @click="nextReaderPage">
                {{ readerIndex === currentCase.pages.length - 1 ? '完成阅读' : '读下一页' }}
                <i class="fa-solid fa-arrow-right" aria-hidden="true" />
              </button>
            </footer>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import detectiveGuide from '@/assets/characters/copper-detective-guide-512.png'
import { generatedMaterialIcons, generatedRewardAssets } from '@/data/generatedRewardAssets'
import { storyCases as fallbackStoryCases, storyChapterCatalog as fallbackStoryChapterCatalog } from '@/data/storyCases'
import { useGameState } from '@/composables/useGameState'
import { listPublishedCopperStories, saveCopperStoryProgress as saveServerStoryProgress } from '@/api/CopperContentApi'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'

const bambooSlipIcon = generatedMaterialIcons['bamboo-slip-shard']
const detectiveSuccess = generatedRewardAssets.detectiveComplete
const safetyBellIcon = generatedMaterialIcons['safety-bell']

const router = useRouter()
const route = useRoute()
const {
  storyArchiveIds,
  dailyTasks,
  getStoryProgress,
  saveStoryProgress: saveLocalStoryProgress,
  hydrateStoryProgress,
  completeStoryCase
} = useGameState()

const storyCases = ref([...fallbackStoryCases])
const storyChapterCatalog = ref([...fallbackStoryChapterCatalog])

const activeCaseId = ref('missing-bamboo')
const currentStep = ref('read')
const readerOpen = ref(false)
const readerIndex = ref(0)
const readerDialog = ref(null)
const readerCloseButton = ref(null)
const readerTrigger = ref(null)
const selectedReasoning = ref('')
const selectedSafety = ref('')
const reasoningSubmitted = ref(false)
const safetySubmitted = ref(false)
const stageFeedback = ref('')
const feedbackType = ref('success')
const result = ref(null)
const rewardToast = ref('')
let rewardToastTimer = null

const stepItems = [
  { id: 'read', number: 1, label: '阅读' },
  { id: 'clues', number: 2, label: '找线索' },
  { id: 'reasoning', number: 3, label: '推理' },
  { id: 'safety', number: 4, label: '安全' },
  { id: 'reward', number: 5, label: '奖励' }
]

const STORY_ORDER = ['missing-bamboo', 'exam-copper-man', 'broken-star-river', 'silent-safety-bell']

const currentCase = computed(() => storyCases.value.find((item) => item.id === activeCaseId.value) || null)
const storyProgress = computed(() => getStoryProgress(activeCaseId.value))
const chapters = computed(() => storyChapterCatalog.value.map((chapter) => {
  const content = storyCases.value.find((item) => item.id === chapter.id)
  const chapterIndex = storyChapterCatalog.value.findIndex((item) => item.id === chapter.id)
  const previous = storyChapterCatalog.value[chapterIndex - 1]
  const locked = !content?.available || Boolean(previous && !getStoryProgress(previous.id).completed)
  return {
    ...chapter,
    hasContent: Boolean(content?.available),
    locked,
    progress: getStoryProgress(chapter.id)
  }
}))

const currentReaderPage = computed(() => currentCase.value?.pages[readerIndex.value] || currentCase.value?.pages[0] || {})
const readPageCount = computed(() => storyProgress.value.readPageIds.length)
const foundClueCount = computed(() => storyProgress.value.clueIds.length)
const allPagesRead = computed(() => Boolean(currentCase.value) && readPageCount.value >= currentCase.value.pages.length)
const allCluesFound = computed(() => Boolean(currentCase.value) && foundClueCount.value >= currentCase.value.clues.length)
const reasoningCorrect = computed(() => currentCase.value?.reasoning.options.some((option) => option.id === storyProgress.value.reasoningAnswerId && option.correct) || false)
const safetyCorrect = computed(() => currentCase.value?.safety.options.some((option) => option.id === storyProgress.value.safetyAnswerId && option.correct) || false)
const evidenceOrder = computed(() => storyProgress.value.evidenceOrder || [])
const evidenceSlots = computed(() => [0, 1, 2, 3].map((index) => evidenceOrder.value[index] || null))
const overallProgress = computed(() => {
  if (!currentCase.value) return 0
  const total = currentCase.value.pages.length + currentCase.value.clues.length + 2
  const done = readPageCount.value + foundClueCount.value + (reasoningCorrect.value ? 1 : 0) + (safetyCorrect.value ? 1 : 0)
  return Math.round((done / total) * 100)
})
const objectives = computed(() => [
  {
    id: 'read',
    name: `读完${currentCase.value?.pages.length || 0}页绘本`,
    description: '观察案件背景',
    progress: readPageCount.value,
    total: currentCase.value?.pages.length || 0,
    done: allPagesRead.value,
    icon: 'fa-solid fa-book-open'
  },
  {
    id: 'clues',
    name: `找出${currentCase.value?.clues.length || 0}条线索`,
    description: '点击调查现场',
    progress: foundClueCount.value,
    total: currentCase.value?.clues.length || 0,
    done: allCluesFound.value,
    icon: 'fa-solid fa-fingerprint'
  },
  {
    id: 'reasoning',
    name: '完成案件推理',
    description: '选出合理解释',
    progress: reasoningCorrect.value ? 1 : 0,
    total: 1,
    done: reasoningCorrect.value,
    icon: 'fa-solid fa-puzzle-piece'
  },
  {
    id: 'safety',
    name: '完成安全判断',
    description: '记住学习边界',
    progress: safetyCorrect.value ? 1 : 0,
    total: 1,
    done: safetyCorrect.value,
    icon: 'fa-solid fa-shield-heart'
  }
])
const dailyTaskRewards = computed(() => {
  const taskId = currentCase.value?.rewards?.dailyTaskId
  return dailyTasks.value.find((task) => task.id === taskId)?.rewards || []
})
const displayedRewards = computed(() => {
  if (result.value?.rewardsGranted?.length) return result.value.rewardsGranted
  const rewards = [...dailyTaskRewards.value]
  if (storyProgress.value.stars >= 3) {
    rewards.push(...(currentCase.value?.rewards.threeStars || []))
  }
  return rewards
})

function isPageRead(pageId) {
  return storyProgress.value.readPageIds.includes(pageId)
}

function isClueFound(clueId) {
  return storyProgress.value.clueIds.includes(clueId)
}

function clueById(clueId) {
  return currentCase.value?.clues.find((clue) => clue.id === clueId)
}

function hotspotStyle(clue) {
  return {
    left: `${clue.hotspot?.left ?? 50}%`,
    top: `${clue.hotspot?.top ?? 50}%`
  }
}

function saveStoryProgress(storyId, patch) {
  const next = { ...getStoryProgress(storyId), ...patch }
  saveLocalStoryProgress(storyId, patch)
  saveServerStoryProgress(storyId, { progress: next, completed: Boolean(next.completed) }).catch(() => {})
}

function stepIndex(stepId) {
  return stepItems.findIndex((step) => step.id === stepId)
}

function stepIsDone(stepId) {
  const index = stepIndex(stepId)
  if (stepId === 'reward') return storyProgress.value.completed
  if (stepId === 'read') return allPagesRead.value
  if (stepId === 'clues') return allCluesFound.value
  if (stepId === 'reasoning') return allCluesFound.value && reasoningCorrect.value
  if (stepId === 'safety') return reasoningCorrect.value && safetyCorrect.value
  return index < stepIndex(currentStep.value)
}

function stepIsReachable(stepId) {
  if (stepId === 'read') return true
  if (stepId === 'clues') return allPagesRead.value
  if (stepId === 'reasoning') return allPagesRead.value && allCluesFound.value
  if (stepId === 'safety') return reasoningCorrect.value
  return storyProgress.value.completed
}

function deriveStep() {
  if (storyProgress.value.completed) return 'reward'
  if (!allPagesRead.value) return 'read'
  if (!allCluesFound.value) return 'clues'
  if (!reasoningCorrect.value) return 'reasoning'
  if (!safetyCorrect.value) return 'safety'
  return 'safety'
}

function selectChapter(chapter) {
  if (chapter.locked || !chapter.hasContent) return
  activeCaseId.value = chapter.id
  currentStep.value = deriveStep()
  result.value = null
  clearFeedback()
}

function showAllStories() {
  router.push('/badges')
}

function openReader(index) {
  if (!currentCase.value) return
  readerIndex.value = index
  readerTrigger.value = document.activeElement
  readerOpen.value = true
  focusReaderDialog()
}

function startReading() {
  if (allPagesRead.value) {
    currentStep.value = 'clues'
    return
  }

  const nextIndex = currentCase.value.pages.findIndex((page) => !isPageRead(page.id))
  readerIndex.value = nextIndex >= 0 ? nextIndex : 0
  readerOpen.value = true
  readerTrigger.value = document.activeElement
  focusReaderDialog()
}

function closeReader() {
  readerOpen.value = false
  nextTick(() => readerTrigger.value?.focus?.())
}

function focusReaderDialog() {
  nextTick(() => {
    window.setTimeout(() => {
      const closeButton = document.querySelector('.reader-dialog button[aria-label="关闭阅读"]') ||
        readerCloseButton.value ||
        readerDialog.value?.querySelector('button')
      closeButton?.focus()
    }, 0)
  })
}

function handleWindowKeydown(event) {
  if (readerOpen.value && event.key === 'Escape') closeReader()
}

function nextReaderPage() {
  const page = currentReaderPage.value
  const readPageIds = [...new Set([...storyProgress.value.readPageIds, page.id])]
  saveStoryProgress(activeCaseId.value, { readPageIds })
  updateMilestoneStars()

  if (readerIndex.value < currentCase.value.pages.length - 1) {
    readerIndex.value += 1
    return
  }

  readerOpen.value = false
  currentStep.value = 'clues'
  message.success('绘本读完啦！现在去现场找线索吧。')
}

function findClue(clue) {
  if (isClueFound(clue.id)) return
  const clueIds = [...storyProgress.value.clueIds, clue.id]
  saveStoryProgress(activeCaseId.value, { clueIds })
  updateMilestoneStars()
  stageFeedback.value = `发现线索：${clue.name}`
  feedbackType.value = 'success'
  if (clueIds.length === currentCase.value.clues.length) {
    message.success('线索全部找齐！可以开始推理了。')
  }
}

function startReasoning() {
  if (!allPagesRead.value) {
    message.warning(`先读完${currentCase.value.pages.length}页绘本，再开始推理吧。`)
    return
  }
  if (!allCluesFound.value) {
    message.warning(`先找齐${currentCase.value.clues.length}条线索，再开始推理吧。`)
    return
  }
  currentStep.value = 'reasoning'
  clearFeedback()
}

function toggleEvidence(clueId) {
  const current = evidenceOrder.value
  const next = current.includes(clueId)
    ? current.filter((id) => id !== clueId)
    : [...current, clueId].slice(0, 4)
  saveStoryProgress(activeCaseId.value, { evidenceOrder: next })
}

function dragEvidence(event, clueId) {
  event.dataTransfer?.setData('text/plain', clueId)
}

function dropEvidence(event, index) {
  const clueId = event.dataTransfer?.getData('text/plain')
  if (!clueId) return
  const next = evidenceOrder.value.filter((id) => id !== clueId)
  next.splice(index, 0, clueId)
  saveStoryProgress(activeCaseId.value, { evidenceOrder: next.slice(0, 4) })
}

function removeEvidence(clueId) {
  saveStoryProgress(activeCaseId.value, {
    evidenceOrder: evidenceOrder.value.filter((id) => id !== clueId)
  })
}

function chooseReasoning(option) {
  selectedReasoning.value = option.id
  reasoningSubmitted.value = false
  clearFeedback()
}

function submitReasoning() {
  if (!allCluesFound.value) {
    message.warning(`先找齐${currentCase.value.clues.length}条线索，再提交推理。`)
    return
  }
  const option = currentCase.value.reasoning.options.find((item) => item.id === selectedReasoning.value)
  if (!option) return

  saveStoryProgress(activeCaseId.value, { reasoningAnswerId: option.id })
  reasoningSubmitted.value = true

  if (!option.correct) {
    feedbackType.value = 'hint'
    stageFeedback.value = '再看看窗边的竹叶和书架后的卷轴，线索会告诉你答案。'
    return
  }

  feedbackType.value = 'success'
  stageFeedback.value = currentCase.value.reasoning.explanation
  setTimeout(() => {
    currentStep.value = 'safety'
    clearFeedback()
  }, 500)
}

function chooseSafety(option) {
  selectedSafety.value = option.id
  safetySubmitted.value = false
  clearFeedback()
}

function submitSafety() {
  if (!reasoningCorrect.value) {
    message.warning('先答对推理题，再进行安全判断。')
    return
  }
  const option = currentCase.value.safety.options.find((item) => item.id === selectedSafety.value)
  if (!option) return

  saveStoryProgress(activeCaseId.value, { safetyAnswerId: option.id })
  safetySubmitted.value = true

  if (!option.correct) {
    feedbackType.value = 'hint'
    stageFeedback.value = '安全铃铛提醒你：不能自己拿尖锐物品尝试针刺。'
    return
  }

  feedbackType.value = 'success'
  stageFeedback.value = currentCase.value.safety.explanation
  setTimeout(() => void finishCase(), 500)
}

async function finishCase() {
  const stars = (allPagesRead.value ? 1 : 0) +
    (allCluesFound.value ? 1 : 0) +
    (reasoningCorrect.value && safetyCorrect.value ? 1 : 0)
  const completion = await completeStoryCase(activeCaseId.value, {
    stars,
    reasoningAnswerId: storyProgress.value.reasoningAnswerId,
    safetyAnswerId: storyProgress.value.safetyAnswerId
  }, currentCase.value.rewards)

  result.value = completion
  saveServerStoryProgress(activeCaseId.value, {
    progress: { ...getStoryProgress(activeCaseId.value), stars, completed: true },
    completed: true
  }).catch(() => {})
  currentStep.value = 'reward'
  clearFeedback()
}

function replayCase() {
  result.value = null
  currentStep.value = 'read'
  readerIndex.value = 0
  selectedReasoning.value = ''
  selectedSafety.value = ''
  reasoningSubmitted.value = false
  safetySubmitted.value = false
  clearFeedback()
}

function jumpToStep(stepId) {
  if (!stepIsReachable(stepId)) return
  if (stepId === 'read') {
    currentStep.value = 'read'
    return
  }
  currentStep.value = stepId
  clearFeedback()
}

function showRewardToast(text) {
  rewardToast.value = text
  window.clearTimeout(rewardToastTimer)
  rewardToastTimer = window.setTimeout(() => {
    rewardToast.value = ''
  }, 1800)
}

function updateMilestoneStars() {
  const stars = (allPagesRead.value ? 1 : 0) + (allCluesFound.value ? 1 : 0)
  if (stars <= storyProgress.value.stars) return
  saveStoryProgress(activeCaseId.value, { stars })
  showRewardToast(stars === 1 ? '阅读星点亮！' : '搜证星点亮！')
}

function clearFeedback() {
  stageFeedback.value = ''
}

function goMap() {
  router.push('/home-map')
}

function normalizePublishedStory(story, index) {
  const cover = resolveMediaUrl(story.coverPath || story.pages?.[0]?.image || '')
  const reasoningOptions = story.reasoning?.options || []
  const safetyOptions = story.safety?.options || []
  const cluePositions = [{ left: 17, top: 22 }, { left: 72, top: 28 }, { left: 31, top: 74 }, { left: 82, top: 70 }]
  return {
    ...story,
    id: story.storyCode,
    number: ['一', '二', '三', '四', '五', '六'][index] || String(index + 1),
    caseCode: String(index + 1).padStart(2, '0'),
    cover,
    sceneImage: cover,
    guideImage: detectiveGuide,
    available: true,
    pages: (story.pages || []).map((page, pageIndex) => ({
      ...page,
      id: page.id || `scene-${pageIndex + 1}`,
      image: resolveMediaUrl(page.image || cover)
    })),
    clues: (story.clues || []).map((clue, clueIndex) => ({
      ...clue,
      id: clue.id || `clue-${clueIndex + 1}`,
      icon: bambooSlipIcon,
      pageId: clue.pageId || story.pages?.[0]?.id,
      hotspot: clue.hotspot || cluePositions[clueIndex % cluePositions.length]
    })),
    reasoning: {
      title: '你认为最合理的答案是什么？',
      prompt: story.reasoning?.prompt || '请根据线索做出判断。',
      options: reasoningOptions.map((option, optionIndex) => typeof option === 'string'
        ? { id: `reason-${optionIndex}`, title: option, detail: option, correct: optionIndex === Number(story.reasoning?.answer || 0) }
        : option),
      explanation: story.reasoning?.explanation || '把线索连起来，就能找到答案。'
    },
    safety: {
      title: '安全小判断',
      prompt: story.safety?.prompt || '学习身体文化知识时应该怎么做？',
      options: safetyOptions.map((option, optionIndex) => typeof option === 'string'
        ? { id: `safety-${optionIndex}`, title: option, detail: option, correct: optionIndex === Number(story.safety?.answer || 0) }
        : option),
      explanation: story.safety?.message || '只观察、不模仿，有问题问成人。'
    },
    rewards: {
      dailyTaskId: 'daily-read-copper-story',
      mainTaskId: 'main-mist-in-xinglin',
      threeStars: (story.rewards || []).map((reward) => ({
        id: reward.code,
        name: reward.name,
        count: reward.amount,
        icon: bambooSlipIcon
      }))
    }
  }
}

async function loadPublishedStories() {
  try {
    const published = await listPublishedCopperStories()
    if (!published?.length) return
    const normalized = published
      .map((story, index) => ({ story, index, order: STORY_ORDER.indexOf(story.storyCode) }))
      .sort((a, b) => (a.order < 0 ? STORY_ORDER.length : a.order) - (b.order < 0 ? STORY_ORDER.length : b.order))
      .map(({ story }, index) => normalizePublishedStory(story, index))
    storyCases.value = normalized
    storyChapterCatalog.value = normalized.map((story) => ({
      id: story.id,
      number: story.number,
      title: story.title,
      image: story.cover || bambooSlipIcon
    }))
    hydrateStoryProgress(Object.fromEntries(normalized.map((story) => [story.id, story.progress])))
    const requested = String(route.query.story || '')
    const requestedIndex = normalized.findIndex((story) => story.id === requested)
    const requestedUnlocked = requestedIndex <= 0 || Boolean(getStoryProgress(normalized[requestedIndex - 1].id).completed)
    const nextInvestigable = normalized.find((story, storyIndex) =>
      (storyIndex === 0 || getStoryProgress(normalized[storyIndex - 1].id).completed) &&
      !getStoryProgress(story.id).completed
    ) || normalized.find((story, storyIndex) => storyIndex === 0 || getStoryProgress(normalized[storyIndex - 1].id).completed)
    activeCaseId.value = requestedUnlocked && requested ? requested : (nextInvestigable?.id || normalized[0].id)
  } catch {
    // 旧静态故事作为后端暂不可用时的只读兼容基线。
  }
}

onMounted(async () => {
  await loadPublishedStories()
  currentStep.value = deriveStep()
  window.addEventListener('keydown', handleWindowKeydown)
})

onBeforeUnmount(() => {
  window.clearTimeout(rewardToastTimer)
  window.removeEventListener('keydown', handleWindowKeydown)
})
</script>

<style scoped>
.story-page {
  --green: #236d5c;
  --green-deep: #184f45;
  --green-soft: #e8f3e8;
  --ink: #4c3827;
  --muted: #806d5a;
  --gold: #d7902d;
  --gold-light: #f7ca6a;
  --paper: #fffaf0;
  --paper-deep: #f5ead5;
  --line: #e4d0ad;
  position: relative;
  min-height: calc(100vh - 56px);
  overflow: hidden;
  color: var(--ink);
  background: #f8f1df;
  font-family: "Noto Sans SC", "Microsoft YaHei", "PingFang SC", sans-serif;
}

.story-page__texture {
  position: absolute;
  inset: 0;
  opacity: 0.045;
  background-image: url("@/assets/maps/xinglin-detective-map-bg.png");
  background-position: center;
  background-size: cover;
  pointer-events: none;
}

.story-shell {
  position: relative;
  width: min(1480px, calc(100% - 112px));
  margin: 0 auto;
  padding: 26px 0 50px;
}

.page-heading {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 108px;
  padding: 0 12px 18px;
}

.page-heading__icon {
  width: 88px;
  height: 88px;
  object-fit: contain;
  filter: drop-shadow(0 8px 8px rgba(115, 73, 25, 0.2));
}

.page-heading__eyebrow,
.section-kicker {
  margin: 0 0 5px;
  color: var(--gold);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.page-heading h1 {
  margin: 0;
  color: var(--green);
  font-family: "STKaiti", "KaiTi", "Microsoft YaHei", sans-serif;
  font-size: clamp(34px, 3vw, 48px);
  font-weight: 900;
  letter-spacing: 2px;
  line-height: 1.08;
}

.page-heading > div > p:last-child {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 15px;
  font-weight: 700;
}

.case-mascot {
  display: grid;
  align-items: center;
  justify-items: center;
  margin-left: auto;
  color: var(--green);
  font-size: 12px;
  font-weight: 900;
}

.case-mascot img {
  width: 72px;
  height: 72px;
  object-fit: contain;
  filter: drop-shadow(0 5px 6px rgba(96, 58, 18, 0.2));
}

.story-stepper {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 18px;
  padding: 9px 12px;
  border: 1px solid rgba(216, 194, 155, 0.92);
  border-radius: 16px;
  background: rgba(255, 250, 240, 0.78);
  box-shadow: 0 8px 20px rgba(100, 68, 31, 0.06);
}

.step-item {
  display: inline-flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 40px;
  color: #8b7662;
  border: 0;
  border-radius: 999px;
  background: transparent;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.step-item:disabled {
  cursor: not-allowed;
  opacity: 0.56;
}

.step-item.active {
  color: #fffaf0;
  background: var(--green);
  box-shadow: 0 5px 14px rgba(35, 109, 92, 0.22);
}

.step-item.done:not(.active) {
  color: var(--green);
}

.step-item__number {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border: 1px solid currentColor;
  border-radius: 50%;
  font-size: 11px;
}

.story-workspace {
  display: grid;
  grid-template-columns: 292px minmax(560px, 1fr) 272px;
  gap: 22px;
  align-items: start;
}

.chapter-rail,
.mission-rail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rail-heading,
.side-card__heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
}

.rail-heading {
  padding: 2px 6px 4px;
}

.rail-heading strong,
.side-card__heading > span {
  color: var(--muted);
  font-size: 11px;
  font-weight: 800;
}

.chapter-card {
  position: relative;
  display: grid;
  grid-template-columns: 82px 1fr;
  min-height: 110px;
  padding: 12px;
  overflow: hidden;
  text-align: left;
  color: var(--ink);
  border: 1px solid var(--line);
  border-radius: 16px;
  background: rgba(255, 250, 240, 0.92);
  box-shadow: 0 7px 18px rgba(100, 68, 31, 0.08);
  cursor: pointer;
  transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease;
}

.chapter-card:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(73, 94, 59, 0.14);
}

.chapter-card:disabled {
  cursor: not-allowed;
  opacity: 0.74;
}

.chapter-card--active {
  border: 3px solid var(--green);
  background: #fffaf1;
  box-shadow: 0 9px 23px rgba(35, 109, 92, 0.16);
}

.chapter-card--archived:not(.chapter-card--active) {
  border-color: #a9c9a4;
}

.chapter-card__art {
  position: relative;
  display: grid;
  width: 70px;
  height: 82px;
  place-items: center;
  overflow: hidden;
  border-radius: 13px;
  background: #f5ead1;
}

.chapter-card__art img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.chapter-card__check {
  position: absolute;
  right: 4px;
  bottom: 4px;
  display: grid;
  width: 21px;
  height: 21px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: #74ac55;
  font-size: 11px;
}

.chapter-card__copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding-left: 8px;
}

.chapter-card__eyebrow,
.chapter-card__status,
.chapter-card__stars {
  color: var(--muted);
  font-size: 11px;
  font-weight: 800;
}

.chapter-card__copy strong {
  color: #2e4f46;
  font-size: 18px;
  line-height: 1.2;
}

.chapter-card__status i {
  margin-right: 3px;
}

.chapter-card__stars {
  color: #e7a52c;
}

.chapter-card__stars small {
  margin-left: 5px;
  color: var(--muted);
  font-size: 10px;
}

.chapter-card__tag {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px 10px;
  color: #fff;
  border-radius: 0 12px 0 16px;
  background: #78b844;
  font-size: 11px;
  font-weight: 900;
}

.all-stories-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 46px;
  padding: 0 16px;
  color: #a66617;
  border: 1px solid #e4c894;
  border-radius: 14px;
  background: rgba(255, 250, 240, 0.92);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.story-stage {
  min-width: 0;
}

.case-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 12px;
  padding: 2px 4px;
}

.case-heading__label {
  color: var(--gold);
  font-size: 12px;
  font-weight: 900;
}

.case-heading h2 {
  margin: 4px 0 0;
  color: var(--green-deep);
  font-size: clamp(24px, 2.4vw, 34px);
  font-weight: 950;
}

.case-heading p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
}

.case-score {
  flex: 0 0 auto;
  min-width: 112px;
  padding: 10px 12px;
  text-align: center;
  border: 1px solid #ebd09a;
  border-radius: 14px;
  background: rgba(255, 248, 219, 0.76);
}

.case-score span {
  display: block;
  color: var(--muted);
  font-size: 11px;
  font-weight: 800;
}

.case-score i,
.reward-hero__stars i {
  margin: 3px 2px 0;
  color: #e5a42e;
  font-size: 21px;
}

.case-panel,
.side-card,
.safety-note {
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 250, 240, 0.94);
  box-shadow: 0 10px 26px rgba(100, 68, 31, 0.08);
}

.case-panel {
  padding: 16px;
}

.case-hero {
  position: relative;
  height: 300px;
  overflow: hidden;
  border-radius: 14px;
  background: #d8bd91;
}

.case-hero > img:first-child {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.case-hero::after,
.investigation-scene::after {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
  background: linear-gradient(180deg, rgba(50, 27, 8, 0.02), rgba(50, 27, 8, 0.4));
}

.case-hero__stamp {
  position: absolute;
  top: 14px;
  left: 14px;
  z-index: 1;
  padding: 7px 11px;
  color: #fff9e8;
  border: 1px solid rgba(255, 244, 194, 0.7);
  border-radius: 999px;
  background: rgba(73, 43, 18, 0.72);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.12em;
}

.case-hero__caption {
  position: absolute;
  right: 16px;
  bottom: 15px;
  z-index: 1;
  display: grid;
  gap: 3px;
  max-width: 270px;
  padding: 11px 14px;
  color: #fff9ed;
  border-left: 3px solid var(--gold-light);
  border-radius: 9px;
  background: rgba(58, 37, 19, 0.74);
}

.case-hero__caption strong {
  font-size: 17px;
}

.case-hero__caption span {
  font-size: 12px;
  opacity: 0.88;
}

.case-hero__guide {
  position: absolute;
  bottom: -20px;
  left: 4%;
  z-index: 2;
  width: 142px;
  height: 172px;
  object-fit: contain;
  filter: drop-shadow(0 10px 8px rgba(58, 34, 12, 0.28));
}

.read-intro,
.stage-title-row,
.stage-action-bar,
.side-card__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.read-intro {
  padding: 18px 4px 14px;
}

.read-intro h3,
.stage-title-row h3 {
  margin: 0;
  color: var(--green-deep);
  font-size: 22px;
  font-weight: 950;
}

.read-intro p,
.stage-title-row p {
  max-width: 590px;
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.read-goal,
.mini-counter {
  flex: 0 0 auto;
  padding: 9px 14px;
  text-align: right;
  border-radius: 13px;
  background: var(--green-soft);
}

.read-goal span,
.mini-counter span {
  display: block;
  color: var(--muted);
  font-size: 11px;
  font-weight: 800;
}

.read-goal strong,
.mini-counter strong {
  color: var(--green);
  font-size: 20px;
}

.mini-counter.complete {
  background: #edf6d9;
}

.page-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.page-preview {
  display: grid;
  gap: 7px;
  padding: 7px;
  text-align: left;
  border: 1px solid var(--line);
  border-radius: 13px;
  background: #fffdf5;
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease;
}

.page-preview:hover,
.page-preview:focus-visible {
  border-color: var(--green);
  transform: translateY(-2px);
}

.page-preview--read {
  border-color: #afd39d;
  background: #f6fbea;
}

.page-preview__image {
  position: relative;
  height: 104px;
  overflow: hidden;
  border-radius: 9px;
  background: #eee0c4;
}

.page-preview__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.page-preview__state {
  position: absolute;
  right: 7px;
  bottom: 7px;
  display: grid;
  width: 23px;
  height: 23px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: rgba(35, 109, 92, 0.86);
  font-size: 11px;
}

.page-preview__copy {
  display: grid;
  gap: 2px;
  padding: 2px 3px 4px;
}

.page-preview__copy small {
  color: var(--gold);
  font-size: 10px;
  font-weight: 900;
}

.page-preview__copy strong {
  overflow: hidden;
  color: var(--green-deep);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stage-action-bar {
  align-items: flex-end;
  margin-top: 16px;
  padding-top: 13px;
  border-top: 1px solid #ebdcc3;
}

.guide-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.45;
}

.guide-hint img {
  width: 42px;
  height: 47px;
  flex: 0 0 auto;
  object-fit: contain;
}

.primary-action,
.secondary-action,
.reader-next-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  min-height: 46px;
  padding: 0 18px;
  border: 0;
  border-radius: 13px;
  font-size: 14px;
  font-weight: 950;
  cursor: pointer;
  transition: transform 160ms ease, box-shadow 160ms ease, opacity 160ms ease;
}

.primary-action {
  min-width: 164px;
  color: #fffaf0;
  background: var(--green);
  box-shadow: 0 8px 16px rgba(35, 109, 92, 0.2);
}

.primary-action--gold {
  color: #fffaf0;
  background: #c87919;
}

.primary-action:hover:not(:disabled),
.reader-next-btn:hover,
.secondary-action:hover {
  box-shadow: 0 10px 20px rgba(74, 57, 34, 0.18);
  transform: translateY(-2px);
}

.primary-action:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.secondary-action {
  color: var(--green);
  border: 1px solid #b9d2b0;
  background: #f3f8ea;
}

.reward-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.stage-title-row {
  align-items: flex-start;
  margin-bottom: 14px;
}

.stage-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
  padding: 8px 10px;
  color: #8b5c1c;
  border: 1px solid #ecd49c;
  border-radius: 999px;
  background: #fff7d8;
  font-size: 11px;
  font-weight: 900;
}

.stage-badge--safe {
  color: var(--green);
  border-color: #b8d7ad;
  background: #eef8e7;
}

.investigation-scene {
  position: relative;
  height: auto;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 14px;
  background: #e3ceb0;
}

.investigation-scene > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scene-note {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 3;
  padding: 7px 10px;
  color: #fff9eb;
  border-radius: 999px;
  background: rgba(37, 71, 57, 0.8);
  font-size: 11px;
  font-weight: 900;
}

.hotspot {
  position: absolute;
  z-index: 4;
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  color: #fff;
  border: 3px solid rgba(255, 252, 221, 0.96);
  border-radius: 50%;
  background: rgba(218, 142, 35, 0.92);
  box-shadow: 0 0 0 6px rgba(245, 197, 98, 0.27), 0 8px 16px rgba(76, 45, 12, 0.2);
  cursor: pointer;
  transform: translate(-50%, -50%);
  animation: hotspot-pulse 1.8s ease-in-out infinite;
}

.hotspot.found {
  color: #fff;
  background: var(--green);
  box-shadow: 0 0 0 6px rgba(151, 213, 137, 0.28), 0 8px 16px rgba(35, 109, 92, 0.2);
  animation: none;
}

.hotspot span {
  position: absolute;
  top: calc(100% + 5px);
  white-space: nowrap;
  color: #fff9ec;
  font-size: 10px;
  font-weight: 900;
  text-shadow: 0 1px 4px rgba(45, 25, 10, 0.75);
}

.clue-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 9px;
  margin-top: 12px;
}

.clue-card {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 9px;
  align-items: center;
  min-width: 0;
  padding: 10px;
  text-align: left;
  border: 1px solid #e4d0ad;
  border-radius: 12px;
  background: #fffdf6;
  cursor: pointer;
}

.clue-card.found {
  border-color: #abd39a;
  background: #f4faeb;
}

.clue-card__icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #a96818;
  border-radius: 10px;
  background: #fff2c7;
}

.clue-card__icon img {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.clue-card.found .clue-card__icon {
  color: #fff;
  background: #7db45e;
}

.clue-card > span:last-child {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.clue-card strong,
.clue-card small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clue-card strong {
  color: var(--green-deep);
  font-size: 13px;
}

.clue-card small {
  color: var(--muted);
  font-size: 10px;
}

.evidence-board {
  padding: 13px;
  border: 1px dashed #cba870;
  border-radius: 14px;
  background: #fbf2dc;
}

.evidence-board__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  color: var(--green-deep);
  font-size: 13px;
  font-weight: 900;
}

.evidence-board__header small {
  color: var(--muted);
  font-size: 10px;
  font-weight: 700;
}

.evidence-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 10px;
}

.evidence-slot {
  position: relative;
  display: flex;
  min-height: 58px;
  align-items: center;
  justify-content: center;
  padding: 8px 8px 8px 29px;
  color: var(--green-deep);
  border: 1px dashed #c9aa70;
  border-radius: 11px;
  background: #fffaf0;
  text-align: center;
  font-size: 11px;
  font-weight: 900;
}

.evidence-slot__number {
  position: absolute;
  top: 7px;
  left: 7px;
  display: grid;
  width: 18px;
  height: 18px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: var(--gold);
  font-size: 10px;
}

.evidence-slot button {
  position: absolute;
  top: 4px;
  right: 5px;
  width: 20px;
  height: 20px;
  color: var(--muted);
  border: 0;
  background: transparent;
  cursor: pointer;
}

.evidence-slot__empty {
  color: #a58e77;
  font-weight: 700;
}

.evidence-pieces {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 10px;
}

.evidence-piece {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  color: #7a5b31;
  border: 1px solid #e5c98e;
  border-radius: 999px;
  background: #fffaf0;
  font-size: 11px;
  font-weight: 900;
  cursor: grab;
}

.evidence-piece img {
  width: 22px;
  height: 22px;
  object-fit: contain;
}

.evidence-piece.pinned {
  color: #fff;
  border-color: var(--green);
  background: var(--green);
}

.answer-options {
  display: grid;
  gap: 10px;
  margin-top: 15px;
}

.answer-option {
  display: grid;
  grid-template-columns: 30px 1fr;
  gap: 10px;
  align-items: center;
  padding: 12px;
  text-align: left;
  border: 2px solid #e5d4b8;
  border-radius: 14px;
  background: #fffdf6;
  cursor: pointer;
  transition: border-color 160ms ease, background 160ms ease;
}

.answer-option:hover,
.answer-option.selected {
  border-color: var(--green);
  background: #f4faef;
}

.answer-option.correct {
  border-color: #75ad57;
  background: #eff8e7;
}

.answer-option.wrong {
  border-color: #dca46c;
  background: #fff4e6;
}

.answer-option__radio {
  display: grid;
  width: 26px;
  height: 26px;
  place-items: center;
  color: #fff;
  border: 2px solid #cbb38d;
  border-radius: 50%;
  background: #fffaf0;
}

.answer-option.selected .answer-option__radio {
  border-color: var(--green);
  background: var(--green);
}

.answer-option > span:last-child {
  display: grid;
  gap: 4px;
}

.answer-option strong {
  color: var(--green-deep);
  font-size: 15px;
  line-height: 1.4;
}

.answer-option small {
  color: var(--muted);
  font-size: 11px;
  line-height: 1.5;
}

.stage-feedback {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin: 12px 0 0;
  padding: 10px 12px;
  color: #95601f;
  border-radius: 10px;
  background: #fff4d7;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.5;
}

.stage-feedback.success {
  color: #337052;
  background: #edf7e7;
}

.story-reward-toast {
  position: fixed;
  top: 86px;
  left: 50%;
  z-index: 20;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  padding: 10px 16px;
  color: #6b4b12;
  font-weight: 950;
  background: #fff4bd;
  border: 2px solid #e4b64c;
  border-radius: 999px;
  box-shadow: 0 14px 28px rgba(96, 62, 17, 0.18);
  transform: translateX(-50%);
}

.story-reward-toast i {
  color: #d7902d;
}

.reward-pop-enter-active,
.reward-pop-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.reward-pop-enter-from,
.reward-pop-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}

.safety-story-banner {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 15px;
  padding: 11px 14px;
  border: 1px solid #ead19a;
  border-radius: 13px;
  background: #fff7db;
}

.safety-story-banner img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.safety-story-banner div {
  display: grid;
  gap: 3px;
}

.safety-story-banner strong {
  color: #8d5c1a;
  font-size: 13px;
}

.safety-story-banner span {
  color: var(--muted);
  font-size: 11px;
  line-height: 1.5;
}

.reward-panel {
  padding: 22px;
}

.reward-hero {
  position: relative;
  display: grid;
  justify-items: center;
  padding: 5px 0 18px;
  text-align: center;
}

.reward-hero__stars {
  position: relative;
  z-index: 1;
}

.reward-hero__stars i {
  margin: 0 4px;
  color: #e5a42e;
  font-size: 27px;
  filter: drop-shadow(0 3px 3px rgba(201, 128, 22, 0.22));
}

.reward-hero img {
  width: 142px;
  height: 142px;
  object-fit: contain;
  filter: drop-shadow(0 13px 9px rgba(92, 52, 14, 0.22));
}

.reward-hero__stamp {
  padding: 5px 10px;
  color: #fff9e8;
  border-radius: 999px;
  background: var(--green);
  font-size: 10px;
  font-weight: 950;
  letter-spacing: 0.12em;
}

.reward-hero h3 {
  margin: 10px 0 0;
  color: var(--green-deep);
  font-size: 28px;
  font-weight: 950;
}

.reward-hero p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.reward-list {
  display: grid;
  gap: 8px;
  max-width: 520px;
  margin: 0 auto;
}

.reward-row {
  display: grid;
  grid-template-columns: 46px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 9px 12px;
  border: 1px solid #e8d7b8;
  border-radius: 12px;
  background: #fffdf5;
}

.reward-row img {
  width: 42px;
  height: 42px;
  object-fit: contain;
}

.reward-row span {
  display: grid;
  gap: 2px;
}

.reward-row strong {
  color: var(--green-deep);
  font-size: 13px;
}

.reward-row small {
  color: var(--muted);
  font-size: 10px;
}

.reward-row b {
  color: var(--gold);
  font-size: 17px;
}

.side-card {
  padding: 14px;
}

.side-card__heading h2 {
  margin: 0;
  color: var(--green-deep);
  font-size: 17px;
  font-weight: 950;
}

.side-card__heading strong {
  color: var(--green);
  font-size: 22px;
}

.overall-progress {
  height: 10px;
  margin: 10px 0 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #eadfc7;
}

.overall-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #72ad55, var(--gold-light));
  transition: width 240ms ease;
}

.mission-card {
  display: grid;
  gap: 4px;
}

.mission-item {
  display: grid;
  grid-template-columns: 30px 1fr auto;
  gap: 8px;
  align-items: center;
  min-width: 0;
  padding: 8px 6px;
  border-bottom: 1px solid #eee2cd;
}

.mission-item:last-child {
  border-bottom: 0;
}

.mission-item.complete {
  background: #f3f9e9;
}

.mission-item__icon {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  color: #a46a1d;
  border-radius: 9px;
  background: #fff3cf;
  font-size: 12px;
}

.mission-item.complete .mission-item__icon {
  color: #fff;
  background: #75ad57;
}

.mission-item > div {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.mission-item strong {
  overflow: hidden;
  color: var(--green-deep);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mission-item small {
  overflow: hidden;
  color: var(--muted);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mission-item b {
  color: var(--gold);
  font-size: 12px;
}

.inventory-card {
  display: grid;
  gap: 10px;
}

.clue-inventory {
  display: grid;
  gap: 6px;
}

.inventory-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 7px 6px;
  color: #a18d77;
  border-radius: 9px;
  background: #f8f0df;
  font-size: 11px;
}

.inventory-item.found {
  color: var(--green-deep);
  background: #f0f8e8;
}

.inventory-item > span {
  display: grid;
  width: 22px;
  height: 22px;
  flex: 0 0 auto;
  place-items: center;
  color: #b8a388;
  border-radius: 7px;
  background: #fffaf0;
}

.inventory-item > span img {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.inventory-item.found > span {
  color: #fff;
  background: #75ad57;
}

.inventory-item strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.star-card {
  display: grid;
  gap: 9px;
}

.star-goal {
  display: grid;
  grid-template-columns: 17px 1fr auto;
  gap: 7px;
  align-items: center;
  color: var(--muted);
  font-size: 11px;
}

.star-goal > i {
  color: #e7aa39;
  font-size: 12px;
}

.star-goal b {
  color: #b0936a;
  font-size: 10px;
}

.star-goal b.done {
  color: #62a24a;
}

.safety-note {
  display: flex;
  gap: 9px;
  align-items: center;
  padding: 12px;
  border-color: #ead09b;
  background: #fff6d8;
}

.safety-note img {
  width: 45px;
  height: 45px;
  flex: 0 0 auto;
  object-fit: contain;
}

.safety-note strong {
  color: #92611f;
  font-size: 12px;
}

.safety-note p {
  margin: 4px 0 0;
  color: #856e50;
  font-size: 10px;
  line-height: 1.55;
}

.coming-soon-panel {
  display: grid;
  min-height: 510px;
  justify-items: center;
  align-content: center;
  padding: 40px;
  text-align: center;
}

.coming-soon-panel img {
  width: 150px;
  height: 150px;
  object-fit: contain;
}

.coming-soon-panel h2 {
  margin: 5px 0 0;
  color: var(--green-deep);
  font-size: 28px;
}

.coming-soon-panel p {
  max-width: 380px;
  margin: 8px 0 18px;
  color: var(--muted);
  line-height: 1.7;
}

.reader-overlay {
  --green: #236d5c;
  --green-deep: #184f45;
  --gold: #d7902d;
  --muted: #806d5a;
  --paper: #fffaf0;
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  padding: 22px;
  place-items: center;
  background: rgba(49, 35, 22, 0.64);
  backdrop-filter: blur(5px);
}

.reader-dialog {
  width: min(980px, 96vw);
  max-height: 92vh;
  overflow: auto;
  border: 1px solid #d8b77c;
  border-radius: 20px;
  background: var(--paper);
  box-shadow: 0 28px 70px rgba(40, 25, 10, 0.35);
}

.reader-dialog__header,
.reader-dialog__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 17px 22px;
}

.reader-dialog__header {
  border-bottom: 1px solid #eadbc2;
}

.reader-dialog__header span {
  color: var(--gold);
  font-size: 11px;
  font-weight: 900;
}

.reader-dialog__header h2 {
  margin: 3px 0 0;
  color: var(--green-deep);
  font-size: 24px;
}

.reader-dialog__header button {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: var(--muted);
  border: 1px solid #e2cda8;
  border-radius: 50%;
  background: #fffaf0;
  cursor: pointer;
}

.reader-dialog__body {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 22px;
  padding: 22px;
}

.reader-dialog__illustration {
  position: relative;
  height: 360px;
  overflow: hidden;
  border-radius: 14px;
  background: #e9d8bc;
}

.reader-dialog__illustration img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reader-dialog__page-mark {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 6px 9px;
  color: #fffaf0;
  border-radius: 999px;
  background: rgba(58, 37, 19, 0.7);
  font-size: 11px;
  font-weight: 900;
}

.reader-dialog__story {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.reader-dialog__story > p {
  margin: 0;
  color: #64503d;
  font-size: 16px;
  line-height: 2;
}

.reader-dialog__tip {
  display: grid;
  grid-template-columns: 50px 1fr;
  gap: 9px;
  align-items: center;
  margin-top: 24px;
  padding: 11px;
  border: 1px solid #e3d1ac;
  border-radius: 12px;
  background: #f8f0df;
}

.reader-dialog__tip img {
  width: 46px;
  height: 52px;
  object-fit: contain;
}

.reader-dialog__tip span {
  color: #725e4a;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.55;
}

.reader-dialog__footer {
  border-top: 1px solid #eadbc2;
}

.reader-dots {
  display: flex;
  gap: 8px;
}

.reader-dots button {
  width: 10px;
  height: 10px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: #dbc8a7;
  cursor: pointer;
}

.reader-dots button.read {
  background: #9fc38a;
}

.reader-dots button.active {
  width: 28px;
  border-radius: 999px;
  background: var(--green);
}

.reader-next-btn {
  color: #fffaf0;
  background: var(--green);
}

button:focus-visible {
  outline: 3px solid rgba(35, 109, 92, 0.35);
  outline-offset: 2px;
}

@keyframes hotspot-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(0.94); }
  50% { transform: translate(-50%, -50%) scale(1.06); }
}

.reader-enter-active,
.reader-leave-active {
  transition: opacity 180ms ease;
}

.reader-enter-from,
.reader-leave-to {
  opacity: 0;
}

.reader-enter-active .reader-dialog,
.reader-leave-active .reader-dialog {
  transition: transform 180ms ease;
}

.reader-enter-from .reader-dialog,
.reader-leave-to .reader-dialog {
  transform: translateY(12px) scale(0.98);
}

@media (max-width: 1320px) {
  .story-shell {
    width: min(1180px, calc(100% - 72px));
  }

  .story-workspace {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .mission-rail {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
  }

  .safety-note {
    grid-column: 1 / -1;
  }
}

@media (max-width: 920px) {
  .story-shell {
    width: min(720px, calc(100% - 32px));
    padding-top: 18px;
  }

  .story-workspace {
    grid-template-columns: 1fr;
  }

  .chapter-rail {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .rail-heading,
  .all-stories-btn {
    grid-column: 1 / -1;
  }

  .mission-rail {
    grid-column: auto;
    grid-template-columns: 1fr;
  }

  .safety-note {
    grid-column: auto;
  }

  .reader-dialog__body {
    grid-template-columns: 1fr;
  }

  .reader-dialog__illustration {
    height: 300px;
  }
}

@media (max-width: 640px) {
  .story-shell {
    width: calc(100% - 20px);
    padding: 14px 0 30px;
  }

  .page-heading {
    align-items: flex-start;
    min-height: 82px;
    padding: 0 4px 12px;
  }

  .page-heading__icon {
    width: 62px;
    height: 62px;
  }

  .page-heading h1 {
    font-size: 29px;
  }

  .page-heading > div > p:last-child {
    max-width: 235px;
    font-size: 12px;
    line-height: 1.5;
  }

  .case-mascot {
    display: none;
  }

  .story-stepper {
    gap: 2px;
    padding: 6px;
    overflow-x: auto;
  }

  .step-item {
    min-width: 68px;
    flex: 0 0 auto;
    font-size: 11px;
  }

  .step-item__number {
    width: 19px;
    height: 19px;
  }

  .chapter-rail {
    grid-template-columns: 1fr;
  }

  .rail-heading,
  .all-stories-btn {
    grid-column: auto;
  }

  .chapter-card {
    grid-template-columns: 74px 1fr;
  }

  .case-heading {
    align-items: flex-end;
  }

  .case-heading h2 {
    font-size: 24px;
  }

  .case-heading p {
    max-width: 220px;
    line-height: 1.45;
  }

  .case-score {
    min-width: 88px;
    padding: 8px 7px;
  }

  .case-score i {
    font-size: 17px;
  }

  .case-panel {
    padding: 11px;
    border-radius: 15px;
  }

  .case-hero {
    height: 238px;
  }

  .case-hero__guide {
    width: 108px;
    height: 132px;
    left: -1%;
  }

  .case-hero__caption {
    right: 10px;
    bottom: 10px;
    max-width: 190px;
    padding: 8px 10px;
  }

  .case-hero__caption strong {
    font-size: 13px;
  }

  .case-hero__caption span {
    font-size: 10px;
  }

  .read-intro,
  .stage-title-row,
  .stage-action-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .read-intro {
    gap: 10px;
  }

  .read-goal,
  .mini-counter {
    align-self: stretch;
    text-align: left;
  }

  .read-goal strong,
  .mini-counter strong {
    margin-left: 5px;
  }

  .page-preview-grid,
  .clue-card-grid {
    grid-template-columns: 1fr;
  }

  .page-preview__image {
    height: 138px;
  }

  .stage-action-bar {
    align-items: stretch;
  }

  .primary-action,
  .secondary-action {
    width: 100%;
  }

  .reward-actions {
    width: 100%;
    flex-direction: column-reverse;
  }

    .investigation-scene {
      min-height: 238px;
      aspect-ratio: auto;
    }

  .hotspot {
    width: 45px;
    height: 45px;
  }

  .evidence-board__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 3px;
  }

  .evidence-slots {
    grid-template-columns: 1fr;
  }

  .evidence-slot {
    min-height: 45px;
  }

  .reader-overlay {
    padding: 10px;
  }

  .reader-dialog__header,
  .reader-dialog__body,
  .reader-dialog__footer {
    padding: 14px;
  }

  .reader-dialog__header h2 {
    font-size: 20px;
  }

  .reader-dialog__illustration {
    height: 210px;
  }

  .reader-dialog__story > p {
    font-size: 14px;
    line-height: 1.75;
  }

  .reader-dialog__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .reader-next-btn {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
    animation-duration: 0.01ms !important;
  }
}
</style>
