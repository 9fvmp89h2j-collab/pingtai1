<template>
  <div class="user-workbench">
    <section class="page-intro">
      <div>
        <div class="eyebrow">
          <span class="eyebrow-dot" /> 杏林运营台 · 用户管理
        </div>
        <h1>用户数据管理</h1>
        <p>从账号状态到学习档案，集中维护每一位小侦探的成长轨迹。</p>
      </div>
      <div class="intro-stamp" aria-hidden="true">
        <span class="stamp-ring">杏</span>
        <span class="stamp-caption">杏林档案</span>
      </div>
    </section>

    <AdminPageError :message="loadError" :loading="loading" @retry="refreshAll" />

    <section class="summary-grid" aria-label="用户统计">
      <div class="summary-card summary-card--ink">
        <div class="summary-icon"><TeamOutlined /></div>
        <div>
          <span class="summary-label">全部用户</span>
          <strong>{{ summary.totalUsers }}</strong>
          <small>当前系统账号总量</small>
        </div>
      </div>
      <div class="summary-card summary-card--green">
        <div class="summary-icon"><CheckCircleOutlined /></div>
        <div>
          <span class="summary-label">正常账号</span>
          <strong>{{ summary.activeUsers }}</strong>
          <small>可以正常进入杏林探险</small>
        </div>
      </div>
      <div class="summary-card summary-card--apricot">
        <div class="summary-icon"><ClockCircleOutlined /></div>
        <div>
          <span class="summary-label">已禁用</span>
          <strong>{{ summary.disabledUsers }}</strong>
          <small>学习数据仍然完整保留</small>
        </div>
      </div>
      <div class="summary-card summary-card--gold">
        <div class="summary-icon"><SafetyCertificateOutlined /></div>
        <div>
          <span class="summary-label">管理员</span>
          <strong>{{ summary.adminUsers }}</strong>
          <small>拥有后台管理权限</small>
        </div>
      </div>
    </section>

    <a-card class="workspace-card" :bordered="false">
      <div class="workspace-heading">
        <div>
          <span class="section-kicker">用户目录</span>
          <h2>账号档案库</h2>
        </div>
        <a-button class="create-button" type="primary" @click="openCreate">
          <template #icon><UserAddOutlined /></template>
          新增普通用户
        </a-button>
      </div>

      <a-form class="filter-bar" layout="inline" @submit.prevent="handleSearch">
        <a-form-item class="filter-keyword">
          <a-input
            v-model:value="filters.keyword"
            allow-clear
            placeholder="搜索用户名、姓名或邮箱"
            @press-enter="handleSearch"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="filters.userType"
            allow-clear
            placeholder="用户类型"
            class="filter-select"
          >
            <a-select-option value="USER">普通用户</a-select-option>
            <a-select-option value="ADMIN">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-select
            v-model:value="filters.status"
            allow-clear
            placeholder="账号状态"
            class="filter-select"
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">已禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="filter-actions">
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="table-toolbar">
        <div class="result-caption">
          <span class="result-mark" />
          <span>共 {{ pagination.total }} 条用户记录</span>
          <span v-if="filters.keyword" class="query-caption">匹配“{{ filters.keyword }}”</span>
        </div>
        <a-button type="text" class="refresh-button" :loading="loading" @click="refreshAll">
          <template #icon><ReloadOutlined /></template>
          刷新数据
        </a-button>
      </div>

      <a-table
        class="user-table"
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        :row-key="record => record.id"
        :scroll="{ x: 1120 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'account'">
            <div class="account-cell">
              <a-avatar :src="getAvatarUrl(record.avatar)" :size="42">
                {{ initials(record) }}
              </a-avatar>
              <div class="account-copy">
                <strong>{{ record.displayName || record.name || record.username }}</strong>
                <span>@{{ record.username }}</span>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'contact'">
            <div class="contact-cell">
              <span>{{ record.email || '未填写邮箱' }}</span>
              <small>{{ record.phone || '未填写手机号' }}</small>
            </div>
          </template>

          <template v-else-if="column.key === 'identity'">
            <div class="identity-cell">
              <a-tag :class="['role-tag', record.userType === 'ADMIN' ? 'role-tag--admin' : 'role-tag--user']">
                {{ getUserTypeLabel(record.userType) }}
              </a-tag>
              <span class="minor-meta">{{ record.age ? `${record.age}岁` : '年龄未填' }} · {{ record.sex || '性别未填' }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'status'">
            <span :class="['status-pill', record.status === 1 ? 'status-pill--active' : 'status-pill--disabled']">
              <span class="status-dot" /> {{ getStatusLabel(record.status) }}
            </span>
          </template>

          <template v-else-if="column.key === 'progress'">
            <div class="progress-cell">
              <div><strong>{{ record.score ?? '—' }}</strong><span> 能量</span></div>
              <small>Lv.{{ record.level || 1 }} · {{ record.levelName || '杏林见习侦探' }}</small>
            </div>
          </template>

          <template v-else-if="column.key === 'updatedAt'">
            <span class="date-cell">{{ formatDate(record.updatedAt || record.createdAt) }}</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space class="row-actions" :size="4">
              <a-button type="link" size="small" @click="openDetail(record)">
                <EyeOutlined /> 档案
              </a-button>
              <a-dropdown :trigger="['click']">
                <a-button type="text" size="small" aria-label="更多操作">
                  <MoreOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="edit" @click="openEdit(record)">
                      <EditOutlined /> 编辑资料
                    </a-menu-item>
                    <a-menu-item
                      key="status"
                      :disabled="record.id === currentUserId && record.status === 1"
                      @click="toggleStatus(record)"
                    >
                      <UnlockOutlined v-if="record.status !== 1" />
                      <LockOutlined v-else />
                      {{ record.status === 1 ? '禁用账号' : '启用账号' }}
                    </a-menu-item>
                    <a-menu-item key="password" @click="openPasswordReset(record)">
                      <KeyOutlined /> 重置密码
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="detailOpen"
      class="user-drawer"
      :width="drawerWidth"
      placement="right"
      :destroy-on-close="true"
      @close="closeDetail"
    >
      <template #title>
        <div v-if="detailUser" class="drawer-title">
          <span class="drawer-title-mark" />
          <span>用户成长档案</span>
        </div>
      </template>

      <a-spin :spinning="detailLoading && !detailUser">
        <template v-if="detailUser">
          <div class="drawer-profile">
            <a-avatar :src="getAvatarUrl(detailUser.avatar)" :size="64">
              {{ initials(detailUser) }}
            </a-avatar>
            <div class="drawer-profile-copy">
              <div class="drawer-profile-name">{{ detailUser.displayName || detailUser.name || detailUser.username }}</div>
              <div class="drawer-profile-handle">@{{ detailUser.username }} · ID {{ detailUser.id }}</div>
              <div class="drawer-profile-tags">
                <a-tag :class="['role-tag', detailUser.userType === 'ADMIN' ? 'role-tag--admin' : 'role-tag--user']">
                  {{ getUserTypeLabel(detailUser.userType) }}
                </a-tag>
                <span :class="['status-pill', detailUser.status === 1 ? 'status-pill--active' : 'status-pill--disabled']">
                  <span class="status-dot" /> {{ getStatusLabel(detailUser.status) }}
                </span>
              </div>
            </div>
            <div class="drawer-profile-actions">
              <a-button size="small" @click="openEdit(detailUser)"><EditOutlined /> 编辑</a-button>
              <a-button size="small" @click="toggleStatus(detailUser)">
                <UnlockOutlined v-if="detailUser.status !== 1" />
                <LockOutlined v-else />
                {{ detailUser.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-button size="small" @click="openPasswordReset(detailUser)"><KeyOutlined /> 重置密码</a-button>
            </div>
          </div>

          <div class="drawer-meta-line">
            <span><CalendarOutlined /> 注册于 {{ formatDate(detailUser.createdAt) }}</span>
            <span><ClockCircleOutlined /> 更新于 {{ formatDate(detailUser.updatedAt) }}</span>
          </div>

          <a-tabs v-model:activeKey="activeTab" class="profile-tabs" @change="handleTabChange">
            <a-tab-pane key="profile" tab="基本资料">
              <div class="detail-section-heading">
                <div>
              <span class="section-kicker">账号档案</span>
                  <h3>身份与联系方式</h3>
                </div>
                <a-button type="link" @click="openEdit(detailUser)"><EditOutlined /> 编辑</a-button>
              </div>
              <div class="detail-grid">
                <div class="detail-field"><span>姓名</span><strong>{{ detailUser.name || '未填写' }}</strong></div>
                <div class="detail-field"><span>年龄</span><strong>{{ detailUser.age ? `${detailUser.age} 岁` : '未填写' }}</strong></div>
                <div class="detail-field"><span>性别</span><strong>{{ detailUser.sex || '未填写' }}</strong></div>
                <div class="detail-field"><span>用户类型</span><strong>{{ getUserTypeLabel(detailUser.userType) }}</strong></div>
                <div class="detail-field detail-field--wide"><span>邮箱</span><strong>{{ detailUser.email || '未填写' }}</strong></div>
                <div class="detail-field detail-field--wide"><span>手机号</span><strong>{{ detailUser.phone || '未填写' }}</strong></div>
              </div>

              <div class="readonly-notice">
                <SafetyCertificateOutlined /> 积分、等级、徽章和学习记录由学习行为自动生成，管理员只能查看，不能在此页面直接修改。
              </div>
            </a-tab-pane>

            <a-tab-pane key="learning" tab="学习概览">
              <a-spin :spinning="detailLoading">
                <template v-if="overview">
                  <div class="level-banner">
                    <div class="level-emblem">{{ overview.level?.level || 1 }}</div>
                    <div class="level-copy">
                      <span>当前成长等级</span>
                      <strong>{{ overview.level?.levelName || '杏林见习侦探' }}</strong>
                      <small>{{ overview.level?.description || '尚未开始记录学习能量' }}</small>
                    </div>
                    <div class="level-score"><strong>{{ detailUser.score ?? '—' }}</strong><span>能量</span></div>
                  </div>
                  <a-progress
                    class="level-progress"
                    :percent="Math.min(100, Math.max(0, Math.round(overview.level?.progress || 0)))"
                    :show-info="false"
                    stroke-color="#6C9076"
                  />

                  <div class="learning-stat-grid">
                    <div class="learning-stat"><span>签到天数</span><strong>{{ overview.learning?.checkinDays || 0 }}</strong><small>累计签到</small></div>
                    <div class="learning-stat"><span>答题正确率</span><strong>{{ formatPercent(overview.learning?.quizAccuracy) }}</strong><small>{{ overview.learning?.quizTotalCount || 0 }} 次答题</small></div>
                    <div class="learning-stat"><span>徽章</span><strong>{{ overview.learning?.badgeCount || 0 }}</strong><small>成长纪念</small></div>
                    <div class="learning-stat"><span>铜人案件</span><strong>{{ overview.learning?.completedCases || 0 }}</strong><small>完成案件</small></div>
                  </div>

                  <div class="learning-panels">
                    <div class="learning-panel">
                      <div class="panel-title"><CompassOutlined /> 探险地图</div>
                      <div class="map-current">当前：{{ currentMapLabel }}</div>
                      <div class="map-track">
                        <span
                          v-for="level in overview.adventureMap?.levels || []"
                          :key="level.id"
                          :class="['map-node', { 'map-node--current': level.current, 'map-node--done': level.completed, 'map-node--locked': !level.unlocked }]"
                          :title="level.label"
                        >{{ level.order }}</span>
                      </div>
                    </div>
                    <div class="learning-panel">
                      <div class="panel-title"><BookOutlined /> 学习资产</div>
                      <div class="asset-lines">
                        <span>技能背包 <strong>{{ overview.learning?.backpackCount || 0 }}</strong></span>
                        <span>技能收藏 <strong>{{ overview.learning?.collectCount || 0 }}</strong></span>
                        <span>身体星点 <strong>{{ overview.learning?.acupointDiscoveredCount || 0 }}</strong></span>
                        <span>错题记录 <strong>{{ overview.learning?.mistakeCount || 0 }}</strong></span>
                      </div>
                    </div>
                    <div class="learning-panel learning-panel--copper">
                      <div class="panel-title"><span class="copper-symbol">铜</span> 小铜人档案</div>
                      <div class="copper-assets">
                        <span><strong>{{ overview.learning?.copperTokens || 0 }}</strong> 铜片</span>
                        <span><strong>{{ overview.learning?.starSand || 0 }}</strong> 星砂</span>
                        <span>最近案件 {{ overview.learning?.lastCompletedCaseDate || '暂无' }}</span>
                      </div>
                    </div>
                  </div>
                </template>
              </a-spin>
            </a-tab-pane>

            <a-tab-pane key="records" tab="学习记录">
              <a-spin :spinning="recordsLoading">
                <div class="records-section">
                  <div class="detail-section-heading">
                    <div>
              <span class="section-kicker">签到日历</span>
                      <h3>签到日历</h3>
                    </div>
                    <span class="last-record">最近签到：{{ overview?.learning?.lastCheckinDate || '暂无' }}</span>
                  </div>
                  <a-calendar v-model:value="calendarValue" :fullscreen="false" @panel-change="handleCalendarChange">
                    <template #dateCellRender="{ current }">
                      <div v-if="isCheckinDate(current)" class="checkin-mark">签</div>
                    </template>
                  </a-calendar>
                </div>

                <div class="records-section records-section--quiz">
                  <div class="detail-section-heading">
                    <div>
              <span class="section-kicker">答题记录</span>
                      <h3>答题记录</h3>
                    </div>
                    <span class="last-record">{{ overview?.learning?.quizTotalCount || 0 }} 次作答</span>
                  </div>
                  <a-table
                    size="small"
                    :columns="quizColumns"
                    :data-source="quizHistory.records"
                    :pagination="quizPagination"
                    :row-key="record => record.id"
                    @change="handleQuizTableChange"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'correct'">
                        <a-tag :color="record.correct ? 'green' : 'red'">{{ record.correct ? '答对' : '答错' }}</a-tag>
                      </template>
                      <template v-else-if="column.key === 'createTime'">
                        {{ formatDate(record.createTime) }}
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-spin>
            </a-tab-pane>

            <a-tab-pane key="honor" tab="徽章与荣誉">
              <div class="detail-section-heading">
                <div>
                  <span class="section-kicker">BADGES & HONOR</span>
                  <h3>成长纪念</h3>
                </div>
              </div>
              <div v-if="overview?.badges?.length" class="badge-grid">
                <div v-for="badge in overview.badges" :key="badge.id" class="badge-item">
                  <a-avatar :src="getAvatarUrl(badge.badgePath)" :size="52">
                    <TrophyOutlined />
                  </a-avatar>
                  <span>{{ badge.badgeName || '成长徽章' }}</span>
                </div>
              </div>
              <a-empty v-else description="还没有获得徽章" />

              <div class="honor-card">
                <div class="honor-icon"><SafetyCertificateOutlined /></div>
                <div>
                  <strong>九级荣誉证书</strong>
                  <p>{{ detailUser.honor ? '该用户已提交荣誉证书。' : '达到九级后可在前台提交荣誉证书。' }}</p>
                </div>
                <a :href="detailUser.honor ? getAvatarUrl(detailUser.honor) : undefined" target="_blank" rel="noreferrer" :class="{ disabled: !detailUser.honor }">
                  {{ detailUser.honor ? '预览' : '暂无' }}
                </a>
              </div>
            </a-tab-pane>
          </a-tabs>
        </template>
        <a-empty v-else description="暂无用户档案" />
      </a-spin>
    </a-drawer>

    <a-modal v-model:open="createOpen" title="新增普通用户" :confirm-loading="createSaving" destroy-on-close @ok="submitCreate" @cancel="resetCreateForm">
      <a-alert class="modal-note" type="info" show-icon message="新账号默认创建为普通用户，初始状态为正常，初始能量为10。" />
      <a-form ref="createFormRef" :model="createForm" :rules="createRules" layout="vertical">
        <div class="form-row">
          <a-form-item label="用户名" name="username">
            <a-input v-model:value="createForm.username" placeholder="3-50位字母、数字或下划线" />
          </a-form-item>
          <a-form-item label="姓名" name="name">
            <a-input v-model:value="createForm.name" placeholder="可选" />
          </a-form-item>
        </div>
        <a-form-item label="邮箱" name="email"><a-input v-model:value="createForm.email" /></a-form-item>
        <div class="form-row">
          <a-form-item label="手机号" name="phone"><a-input v-model:value="createForm.phone" placeholder="可选" /></a-form-item>
          <a-form-item label="年龄" name="age"><a-input-number v-model:value="createForm.age" :min="0" :max="150" style="width: 100%" placeholder="可选" /></a-form-item>
        </div>
        <a-form-item label="性别" name="sex">
          <a-select v-model:value="createForm.sex" allow-clear placeholder="可选">
            <a-select-option value="男">男</a-select-option>
            <a-select-option value="女">女</a-select-option>
          </a-select>
        </a-form-item>
        <div class="form-row">
          <a-form-item label="登录密码" name="password"><a-input-password v-model:value="createForm.password" /></a-form-item>
          <a-form-item label="确认密码" name="confirmPassword"><a-input-password v-model:value="createForm.confirmPassword" /></a-form-item>
        </div>
      </a-form>
    </a-modal>

    <a-modal v-model:open="editOpen" title="编辑用户资料" :confirm-loading="profileSaving" destroy-on-close @ok="submitProfile" @cancel="editOpen = false">
      <a-form ref="profileFormRef" :model="profileForm" :rules="profileRules" layout="vertical">
        <div class="form-row">
          <a-form-item label="姓名" name="name"><a-input v-model:value="profileForm.name" /></a-form-item>
          <a-form-item label="年龄" name="age"><a-input-number v-model:value="profileForm.age" :min="0" :max="150" style="width: 100%" /></a-form-item>
        </div>
        <div class="form-row">
          <a-form-item label="性别" name="sex">
            <a-select v-model:value="profileForm.sex" allow-clear>
              <a-select-option value="男">男</a-select-option>
              <a-select-option value="女">女</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="用户类型"><a-input :value="getUserTypeLabel(profileTarget?.userType)" disabled /></a-form-item>
        </div>
        <a-form-item label="邮箱" name="email"><a-input v-model:value="profileForm.email" /></a-form-item>
        <a-form-item label="手机号" name="phone"><a-input v-model:value="profileForm.phone" placeholder="可选" /></a-form-item>
        <a-form-item label="头像路径" name="avatar"><a-input v-model:value="profileForm.avatar" placeholder="可填写 /files/... 路径" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="passwordOpen" title="重置用户密码" :confirm-loading="passwordSaving" destroy-on-close @ok="submitPasswordReset" @cancel="passwordOpen = false">
      <a-alert class="modal-note" type="warning" show-icon message="新密码只会写入账号，不会在操作日志中记录。" />
      <p class="password-target">正在重置：<strong>{{ passwordTarget?.username }}</strong></p>
      <a-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" layout="vertical">
        <a-form-item label="新密码" name="newPassword"><a-input-password v-model:value="passwordForm.newPassword" /></a-form-item>
        <a-form-item label="确认新密码" name="confirmPassword"><a-input-password v-model:value="passwordForm.confirmPassword" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import {
  BookOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CompassOutlined,
  EditOutlined,
  EyeOutlined,
  KeyOutlined,
  LockOutlined,
  MoreOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
  SearchOutlined,
  TeamOutlined,
  TrophyOutlined,
  UnlockOutlined,
  UserAddOutlined
} from '@ant-design/icons-vue'
import {
  createAdminUser,
  getAdminUserCheckins,
  getAdminUserOverview,
  getAdminUserQuizHistory,
  getAdminUserSummary,
  getUserPage,
  resetAdminUserPassword,
  updateAdminUserProfile,
  updateAdminUserStatus
} from '@/api/user'
import { useUserStore } from '@/store/user'
import { resolveMediaUrl } from '@/utils/resolveMediaUrl'
import AdminPageError from '@/components/backend/AdminPageError.vue'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userId)

const filters = reactive({ keyword: '', userType: undefined, status: undefined })
const summary = reactive({ totalUsers: 0, activeUsers: 0, disabledUsers: 0, adminUsers: 0 })
const userList = ref([])
const loading = ref(false)
const loadError = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: total => `共 ${total} 条`
})

const columns = [
  { title: '账号档案', key: 'account', width: 220 },
  { title: '联系方式', key: 'contact', width: 230 },
  { title: '身份信息', key: 'identity', width: 160 },
  { title: '状态', key: 'status', width: 110 },
  { title: '成长进度', key: 'progress', width: 180 },
  { title: '最近更新', key: 'updatedAt', width: 170 },
  { title: '操作', key: 'action', fixed: 'right', width: 130 }
]

const detailOpen = ref(false)
const detailLoading = ref(false)
const detailOverview = ref(null)
const activeTab = ref('profile')
const drawerWidth = computed(() => (window.innerWidth < 768 ? '100%' : 820))
const detailUser = computed(() => detailOverview.value?.user || null)
const overview = computed(() => detailOverview.value)
const currentMapLabel = computed(() => {
  const levels = overview.value?.adventureMap?.levels || []
  return levels.find(level => level.current)?.label || '报到处'
})

const calendarValue = ref(dayjs())
const checkinDates = ref([])
const recordsLoading = ref(false)
const quizHistory = reactive({ records: [], total: 0 })
const quizPagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
  showSizeChanger: false,
  showTotal: total => `共 ${total} 条`
})
const quizColumns = [
  { title: '题目编号', dataIndex: 'questionId', key: 'questionId', width: 100 },
  { title: '作答', dataIndex: 'userAnswer', key: 'userAnswer', width: 80 },
  { title: '结果', key: 'correct', width: 90 },
  { title: '时间', key: 'createTime' }
]

const createOpen = ref(false)
const createSaving = ref(false)
const createFormRef = ref()
const createForm = reactive({ username: '', email: '', password: '', confirmPassword: '', name: '', phone: '', sex: undefined, age: null })
const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, message: '密码至少8位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const editOpen = ref(false)
const profileSaving = ref(false)
const profileFormRef = ref()
const profileTarget = ref(null)
const profileForm = reactive({ name: '', email: '', phone: null, sex: null, age: null, avatar: null })
const profileRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const passwordOpen = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref()
const passwordTarget = ref(null)
const passwordForm = reactive({ newPassword: '', confirmPassword: '' })
const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 8, message: '密码至少8位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
}

const initials = record => (record?.displayName || record?.name || record?.username || '用').slice(0, 1).toUpperCase()
const getAvatarUrl = path => resolveMediaUrl(path)
const getUserTypeLabel = type => (type === 'ADMIN' ? '管理员' : type === 'USER' ? '普通用户' : '未知类型')
const getStatusLabel = status => (status === 1 ? '正常' : status === 0 ? '已禁用' : '未知状态')
const formatPercent = value => `${Number(value || 0).toFixed(1)}%`

function formatDate(value) {
  if (!value) return '—'
  const text = String(value)
  if (/^\d{4}-\d{2}-\d{2}$/.test(text)) return text
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return text
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function fetchUserList() {
  loading.value = true
  try {
    const result = await getUserPage({
      currentPage: pagination.current,
      size: pagination.pageSize,
      keyword: filters.keyword || undefined,
      userType: filters.userType || undefined,
      status: filters.status
    }, { showDefaultMsg: false })
    userList.value = result?.records || []
    pagination.total = result?.total || 0
  } catch (error) {
    loadError.value = error?.message || '获取用户列表失败'
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  try {
    const result = await getAdminUserSummary({ showDefaultMsg: false })
    Object.assign(summary, result || {})
  } catch (error) {
    loadError.value = error?.message || '获取用户统计失败'
  }
}

async function refreshAll() {
  loadError.value = ''
  await Promise.all([fetchUserList(), fetchSummary()])
}

function handleSearch() {
  pagination.current = 1
  fetchUserList()
}

function handleReset() {
  filters.keyword = ''
  filters.userType = undefined
  filters.status = undefined
  pagination.current = 1
  fetchUserList()
}

function handleTableChange(page) {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchUserList()
}

async function openDetail(record) {
  detailOpen.value = true
  activeTab.value = 'profile'
  detailOverview.value = null
  quizHistory.records = []
  quizHistory.total = 0
  quizPagination.current = 1
  detailLoading.value = true
  try {
    detailOverview.value = await getAdminUserOverview(record.id, { showDefaultMsg: false })
  } catch (error) {
    message.error(error?.message || '获取用户档案失败')
    detailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  detailOverview.value = null
  checkinDates.value = []
  quizHistory.records = []
}

async function reloadDetail() {
  if (!detailUser.value?.id) return
  const currentId = detailUser.value.id
  detailLoading.value = true
  try {
    detailOverview.value = await getAdminUserOverview(currentId, { showDefaultMsg: false })
  } catch (error) {
    message.error(error?.message || '刷新用户档案失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleTabChange(key) {
  if (key !== 'records' || !detailUser.value) return
  await Promise.all([loadCheckins(), loadQuizHistory()])
}

async function loadCheckins(value = calendarValue.value) {
  if (!detailUser.value?.id) return
  calendarValue.value = value
  recordsLoading.value = true
  try {
    const result = await getAdminUserCheckins(detailUser.value.id, { year: value.year(), month: value.month() + 1 }, { showDefaultMsg: false })
    checkinDates.value = (result || []).map(date => String(date).slice(0, 10))
  } catch (error) {
    message.error(error?.message || '获取签到记录失败')
  } finally {
    recordsLoading.value = false
  }
}

function handleCalendarChange(value) {
  loadCheckins(value)
}

async function loadQuizHistory() {
  if (!detailUser.value?.id) return
  recordsLoading.value = true
  try {
    const result = await getAdminUserQuizHistory(detailUser.value.id, { current: quizPagination.current, size: quizPagination.pageSize }, { showDefaultMsg: false })
    quizHistory.records = result?.records || []
    quizHistory.total = result?.total || 0
    quizPagination.total = result?.total || 0
  } catch (error) {
    message.error(error?.message || '获取答题记录失败')
  } finally {
    recordsLoading.value = false
  }
}

function handleQuizTableChange(page) {
  quizPagination.current = page.current
  loadQuizHistory()
}

function isCheckinDate(value) {
  return checkinDates.value.includes(value.format('YYYY-MM-DD'))
}

function openCreate() {
  resetCreateForm()
  createOpen.value = true
}

function resetCreateForm() {
  Object.assign(createForm, { username: '', email: '', password: '', confirmPassword: '', name: '', phone: '', sex: undefined, age: null })
  nextTick(() => createFormRef.value?.clearValidate())
}

async function submitCreate() {
  try {
    await createFormRef.value.validate()
    if (createForm.password !== createForm.confirmPassword) {
      message.error('两次输入的密码不一致')
      return
    }
    createSaving.value = true
    await createAdminUser({
      ...createForm,
      phone: createForm.phone || null,
      sex: createForm.sex || null,
      age: createForm.age ?? null
    }, { showDefaultMsg: false })
    message.success('普通用户创建成功')
    createOpen.value = false
    await refreshAll()
  } catch (error) {
    if (!error?.errorFields) message.error(error?.message || '创建用户失败')
  } finally {
    createSaving.value = false
  }
}

function openEdit(record) {
  profileTarget.value = record
  Object.assign(profileForm, {
    name: record.name || record.displayName || '',
    email: record.email || '',
    phone: record.phone || null,
    sex: record.sex || null,
    age: record.age ?? null,
    avatar: record.avatar || null
  })
  editOpen.value = true
}

async function submitProfile() {
  try {
    await profileFormRef.value.validate()
    profileSaving.value = true
    await updateAdminUserProfile(profileTarget.value.id, {
      ...profileForm,
      phone: profileForm.phone || null,
      sex: profileForm.sex || null,
      avatar: profileForm.avatar || null,
      age: profileForm.age ?? null
    }, { showDefaultMsg: false })
    message.success('用户资料已更新')
    editOpen.value = false
    await Promise.all([fetchUserList(), fetchSummary(), reloadDetail()])
  } catch (error) {
    if (!error?.errorFields) message.error(error?.message || '更新用户资料失败')
  } finally {
    profileSaving.value = false
  }
}

function toggleStatus(record) {
  const nextStatus = record.status === 1 ? 0 : 1
  if (record.id === currentUserId.value && nextStatus === 0) {
    message.warning('不能禁用当前登录管理员账号')
    return
  }
  Modal.confirm({
    title: nextStatus === 1 ? '启用这个账号？' : '禁用这个账号？',
    content: nextStatus === 1 ? '用户将可以重新登录并继续学习。' : '账号将无法登录，但所有学习记录会完整保留。',
    okText: nextStatus === 1 ? '确认启用' : '确认禁用',
    cancelText: '取消',
    okButtonProps: nextStatus === 1 ? {} : { danger: true },
    onOk: async () => {
      try {
        await updateAdminUserStatus(record.id, { status: nextStatus }, { showDefaultMsg: false })
        message.success(nextStatus === 1 ? '账号已启用' : '账号已禁用')
        await Promise.all([fetchUserList(), fetchSummary(), reloadDetail()])
      } catch (error) {
        message.error(error?.message || '账号状态更新失败')
      }
    }
  })
}

function openPasswordReset(record) {
  passwordTarget.value = record
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordOpen.value = true
}

async function submitPasswordReset() {
  try {
    await passwordFormRef.value.validate()
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      message.error('两次输入的密码不一致')
      return
    }
    passwordSaving.value = true
    await resetAdminUserPassword(passwordTarget.value.id, { ...passwordForm }, { showDefaultMsg: false })
    message.success('用户密码已重置')
    passwordOpen.value = false
  } catch (error) {
    if (!error?.errorFields) message.error(error?.message || '重置密码失败')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(refreshAll)
</script>

<style lang="scss" scoped>
.user-workbench {
  --ink: #1d403c;
  --ink-soft: #52706a;
  --green: #6c9076;
  --apricot: #e9b18e;
  --gold: #d5ac60;
  --paper: #f6f2ea;
  --line: #e4ded3;
  --muted: #7b8782;
  min-height: 100%;
  padding: 28px 30px 44px;
  color: var(--ink);
  background: var(--paper);
  font-family: 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.page-intro {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  max-width: 1480px;
  margin: 0 auto 24px;
}

.eyebrow,
.section-kicker {
  color: var(--green);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .16em;
}

.eyebrow {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.eyebrow-dot,
.result-mark {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--apricot);
  box-shadow: 0 0 0 4px rgba(233, 177, 142, .18);
}

h1,
h2,
h3,
p {
  margin: 0;
}

.page-intro h1 {
  color: var(--ink);
  font-size: clamp(28px, 3vw, 40px);
  font-weight: 700;
  letter-spacing: -.04em;
}

.page-intro p {
  margin-top: 8px;
  color: var(--muted);
  font-size: 14px;
}

.intro-stamp {
  display: flex;
  align-items: center;
  gap: 10px;
  transform: rotate(-4deg);
  opacity: .82;
}

.stamp-ring {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border: 1px solid rgba(213, 172, 96, .8);
  border-radius: 50%;
  color: var(--gold);
  font-size: 25px;
  font-weight: 700;
}

.stamp-caption {
  color: var(--gold);
  font-size: 12px;
  writing-mode: vertical-rl;
  letter-spacing: .18em;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  max-width: 1480px;
  margin: 0 auto 20px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 112px;
  padding: 18px 20px;
  border: 1px solid transparent;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgba(29, 64, 60, .06);
}

.summary-card--ink { background: var(--ink); color: #fff; }
.summary-card--green { background: #e7eee7; border-color: #d3e2d5; }
.summary-card--apricot { background: #f8e3d6; border-color: #f0cbb8; }
.summary-card--gold { background: #f4ead5; border-color: #ead8ae; }

.summary-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, .17);
  font-size: 20px;
}

.summary-card--green .summary-icon { color: var(--green); background: rgba(108, 144, 118, .12); }
.summary-card--apricot .summary-icon { color: #bd7957; background: rgba(189, 121, 87, .12); }
.summary-card--gold .summary-icon { color: #aa812e; background: rgba(170, 129, 46, .12); }

.summary-card > div:last-child { display: grid; gap: 2px; }
.summary-label { font-size: 12px; opacity: .72; }
.summary-card strong { font-size: 28px; line-height: 1.1; letter-spacing: -.04em; }
.summary-card small { color: inherit; font-size: 11px; opacity: .64; }

.workspace-card {
  max-width: 1480px;
  margin: 0 auto;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 255, 255, .78);
  box-shadow: 0 18px 45px rgba(29, 64, 60, .07);
}

:deep(.workspace-card .ant-card-body) { padding: 24px; }

.workspace-heading,
.table-toolbar,
.detail-section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.workspace-heading { margin-bottom: 22px; }
.workspace-heading h2 { margin-top: 5px; font-size: 22px; letter-spacing: -.03em; }
.create-button { height: 40px; border: 0; border-radius: 10px; background: var(--ink); box-shadow: none; }
.create-button:hover { background: #2c5a54 !important; }

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fbfaf6;
}

.filter-bar :deep(.ant-form-item) { margin: 0; }
.filter-keyword { flex: 1 1 300px; }
.filter-keyword :deep(.ant-input-affix-wrapper) { min-width: 280px; }
.filter-select { width: 150px; }
.filter-actions { margin-left: auto !important; }

.table-toolbar { padding: 20px 2px 14px; }
.result-caption { display: flex; align-items: center; gap: 9px; color: var(--muted); font-size: 13px; }
.query-caption { padding-left: 8px; color: var(--ink-soft); }
.refresh-button { color: var(--green); }

.user-table :deep(.ant-table) { background: transparent; }
.user-table :deep(.ant-table-thead > tr > th) {
  padding: 13px 12px;
  color: var(--muted);
  background: #f4f1e9;
  border-bottom: 1px solid var(--line);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .04em;
}
.user-table :deep(.ant-table-tbody > tr > td) { padding: 16px 12px; border-bottom: 1px solid #eeeae2; }
.user-table :deep(.ant-table-tbody > tr:hover > td) { background: #faf7f0 !important; }

.account-cell { display: flex; align-items: center; gap: 11px; min-width: 180px; }
.account-cell :deep(.ant-avatar) { flex: 0 0 auto; color: var(--ink); background: #dfe9df; font-weight: 700; }
.account-copy { display: grid; gap: 3px; min-width: 0; }
.account-copy strong { overflow: hidden; color: var(--ink); font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.account-copy span,
.contact-cell small,
.minor-meta,
.progress-cell small { color: var(--muted); font-size: 11px; }
.contact-cell { display: grid; gap: 4px; color: var(--ink-soft); font-size: 12px; }
.contact-cell span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.identity-cell { display: grid; gap: 5px; }
.role-tag { width: fit-content; margin: 0; border: 0; border-radius: 5px; font-size: 11px; }
.role-tag--admin { color: #9c5f45; background: #f8e3d6; }
.role-tag--user { color: #52735b; background: #e7eee7; }
.status-pill { display: inline-flex; align-items: center; gap: 6px; width: fit-content; font-size: 12px; font-weight: 600; }
.status-pill--active { color: #5d8566; }
.status-pill--disabled { color: #b26e55; }
.status-dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
.progress-cell { display: grid; gap: 4px; color: var(--ink); }
.progress-cell strong { font-size: 17px; }
.progress-cell span { color: var(--muted); font-size: 11px; }
.date-cell { color: var(--muted); font-size: 12px; }
.row-actions :deep(.ant-btn-link) { padding-inline: 3px; color: var(--ink-soft); }
.row-actions :deep(.ant-btn-link:hover) { color: var(--green); }

.drawer-title { display: flex; align-items: center; gap: 10px; color: var(--ink); font-weight: 700; }
.drawer-title-mark { width: 10px; height: 22px; border-radius: 3px; background: var(--apricot); }
.drawer-profile { display: flex; align-items: center; gap: 14px; padding: 4px 0 18px; border-bottom: 1px solid var(--line); }
.drawer-profile :deep(.ant-avatar) { flex: 0 0 auto; color: var(--ink); background: #dfe9df; font-size: 24px; font-weight: 700; }
.drawer-profile-copy { min-width: 0; flex: 1; }
.drawer-profile-name { overflow: hidden; color: var(--ink); font-size: 22px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.drawer-profile-handle { margin-top: 4px; color: var(--muted); font-size: 12px; }
.drawer-profile-tags { display: flex; align-items: center; gap: 8px; margin-top: 9px; }
.drawer-profile-actions { display: flex; flex-wrap: wrap; gap: 7px; }
.drawer-profile-actions :deep(.ant-btn) { border-color: var(--line); color: var(--ink-soft); border-radius: 7px; }
.drawer-meta-line { display: flex; flex-wrap: wrap; gap: 16px; padding: 13px 0 5px; color: var(--muted); font-size: 11px; }
.drawer-meta-line span { display: inline-flex; align-items: center; gap: 5px; }
.profile-tabs :deep(.ant-tabs-nav) { margin-bottom: 24px; }
.profile-tabs :deep(.ant-tabs-tab) { color: var(--muted); }
.profile-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) { color: var(--ink); font-weight: 700; }
.profile-tabs :deep(.ant-tabs-ink-bar) { background: var(--apricot); }
.detail-section-heading { margin-bottom: 16px; }
.detail-section-heading h3 { margin-top: 4px; color: var(--ink); font-size: 18px; }
.detail-section-heading :deep(.ant-btn-link) { color: var(--green); }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); border-top: 1px solid var(--line); border-left: 1px solid var(--line); }
.detail-field { display: grid; gap: 7px; min-height: 74px; padding: 14px 16px; border-right: 1px solid var(--line); border-bottom: 1px solid var(--line); }
.detail-field--wide { grid-column: span 2; }
.detail-field span { color: var(--muted); font-size: 11px; }
.detail-field strong { overflow: hidden; color: var(--ink); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.readonly-notice { display: flex; gap: 8px; margin-top: 18px; padding: 12px 14px; border-radius: 9px; color: #6c705f; background: #f5eedf; font-size: 12px; line-height: 1.6; }

.level-banner { display: flex; align-items: center; gap: 14px; padding: 18px; border-radius: 14px; color: #fff; background: var(--ink); }
.level-emblem { display: grid; place-items: center; width: 54px; height: 54px; border: 1px solid rgba(255,255,255,.3); border-radius: 50%; color: #f1d7a0; font-size: 24px; font-weight: 700; }
.level-copy { display: grid; flex: 1; gap: 3px; }
.level-copy span { font-size: 11px; opacity: .65; }
.level-copy strong { font-size: 16px; }
.level-copy small { opacity: .72; font-size: 11px; }
.level-score { display: grid; justify-items: end; gap: 2px; }
.level-score strong { color: #f1d7a0; font-size: 28px; }
.level-score span { font-size: 11px; opacity: .65; }
.level-progress { margin: 13px 2px 20px; }
.level-progress :deep(.ant-progress-inner) { background: #e7eee7; }
.learning-stat-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 9px; }
.learning-stat { display: grid; gap: 3px; padding: 13px; border: 1px solid var(--line); border-radius: 10px; background: #fbfaf6; }
.learning-stat span, .learning-stat small { color: var(--muted); font-size: 11px; }
.learning-stat strong { color: var(--ink); font-size: 21px; }
.learning-panels { display: grid; gap: 10px; margin-top: 18px; }
.learning-panel { padding: 16px; border: 1px solid var(--line); border-radius: 12px; background: #fbfaf6; }
.learning-panel--copper { background: #f8eee5; border-color: #efd9c7; }
.panel-title { display: flex; align-items: center; gap: 7px; color: var(--ink); font-size: 13px; font-weight: 700; }
.map-current { margin-top: 14px; color: var(--muted); font-size: 12px; }
.map-track { display: flex; align-items: center; gap: 0; margin-top: 17px; }
.map-node { position: relative; display: grid; place-items: center; width: 27px; height: 27px; border: 2px solid #d7ddd5; border-radius: 50%; color: #aeb9ae; background: #fff; font-size: 10px; z-index: 1; }
.map-node:not(:last-child)::after { position: absolute; left: 25px; width: 26px; height: 2px; background: #d7ddd5; content: ''; z-index: -1; }
.map-node--done { border-color: var(--green); color: #fff; background: var(--green); }
.map-node--done:not(:last-child)::after { background: var(--green); }
.map-node--current { border-color: var(--apricot); color: #9f694b; background: #f8e3d6; box-shadow: 0 0 0 4px rgba(233, 177, 142, .18); }
.asset-lines { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-top: 14px; color: var(--muted); font-size: 12px; }
.asset-lines span { display: flex; justify-content: space-between; gap: 8px; padding-bottom: 7px; border-bottom: 1px dashed var(--line); }
.asset-lines strong { color: var(--ink); }
.copper-symbol { display: inline-grid; place-items: center; width: 20px; height: 20px; border-radius: 6px; color: #a9714d; background: #efd0b7; font-size: 11px; }
.copper-assets { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 14px; color: #906746; font-size: 12px; }
.copper-assets strong { color: #704b35; font-size: 16px; }
.records-section { margin-bottom: 24px; }
.records-section :deep(.ant-picker-calendar) { border: 1px solid var(--line); border-radius: 10px; overflow: hidden; }
.records-section :deep(.ant-picker-calendar-full .ant-picker-panel) { border-top: 0; }
.checkin-mark { display: grid; place-items: center; width: 18px; height: 18px; margin: 3px auto 0; border-radius: 50%; color: #fff; background: var(--green); font-size: 9px; }
.last-record { color: var(--muted); font-size: 11px; }
.records-section--quiz { padding-top: 4px; }
.records-section--quiz :deep(.ant-table-thead > tr > th) { background: #f4f1e9; color: var(--muted); font-size: 11px; }
.records-section--quiz :deep(.ant-table-tbody > tr > td) { color: var(--ink-soft); font-size: 12px; }
.badge-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.badge-item { display: grid; justify-items: center; gap: 8px; padding: 14px 8px; border: 1px solid var(--line); border-radius: 11px; text-align: center; }
.badge-item :deep(.ant-avatar) { color: #a27632; background: #f5ead2; }
.badge-item span { color: var(--ink-soft); font-size: 11px; line-height: 1.4; }
.honor-card { display: flex; align-items: center; gap: 12px; margin-top: 20px; padding: 15px; border: 1px solid #ead8ae; border-radius: 12px; background: #f8f0dd; }
.honor-icon { display: grid; place-items: center; width: 38px; height: 38px; border-radius: 10px; color: #a27632; background: #f3dfad; font-size: 19px; }
.honor-card > div:nth-child(2) { flex: 1; }
.honor-card strong { color: #765a28; font-size: 13px; }
.honor-card p { margin-top: 4px; color: #9b8454; font-size: 11px; line-height: 1.5; }
.honor-card a { color: #a27632; font-size: 12px; }
.honor-card a.disabled { color: #b5a580; pointer-events: none; }

.modal-note { margin-bottom: 18px; }
.form-row { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.password-target { margin: -2px 0 15px; color: var(--muted); font-size: 13px; }
.password-target strong { color: var(--ink); }

@media (min-width: 1024px) {
  .user-workbench {
    --ink: #17211e;
    --ink-soft: #52615c;
    --green: #1f5a50;
    --paper: transparent;
    --line: #dde4e1;
    --muted: #66736f;
    min-height: calc(100vh - 112px);
    padding: 0;
    background: transparent;
  }

  .page-intro,
  .summary-grid,
  .workspace-card {
    max-width: none;
  }

  .page-intro {
    align-items: flex-start;
    margin-bottom: 18px;
  }

  .page-intro h1 {
    font-size: 24px;
    font-weight: 680;
    letter-spacing: -.02em;
  }

  .page-intro p {
    margin-top: 6px;
    color: #66736f;
  }

  .eyebrow,
  .intro-stamp {
    display: none;
  }

  .summary-grid {
    gap: 16px;
    margin-bottom: 18px;
  }

  .summary-card,
  .summary-card--ink,
  .summary-card--green,
  .summary-card--apricot,
  .summary-card--gold {
    min-height: 116px;
    padding: 18px;
    color: #17211e;
    background: #ffffff;
    border: 1px solid #dde4e1;
    border-radius: 8px;
    box-shadow: none;
  }

  .summary-icon,
  .summary-card--green .summary-icon,
  .summary-card--apricot .summary-icon,
  .summary-card--gold .summary-icon {
    color: #1f5a50;
    background: #edf3f1;
    border-radius: 8px;
  }

  .summary-label,
  .summary-card small {
    color: #66736f;
    opacity: 1;
  }

  .summary-card strong {
    font-size: 26px;
  }

  .workspace-card {
    border-radius: 8px;
    background: #ffffff;
    box-shadow: none;
  }

  :deep(.workspace-card .ant-card-body) {
    padding: 20px;
  }

  .workspace-heading h2 {
    font-size: 18px;
    font-weight: 680;
  }

  .section-kicker {
    color: #66736f;
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 0;
  }

  .filter-bar {
    margin-top: 18px;
    padding: 16px 0 4px;
    border-top: 1px solid #dde4e1;
  }

  .table-toolbar {
    min-height: 48px;
    border-top: 1px solid #dde4e1;
  }

  .result-mark {
    width: 4px;
    height: 14px;
    border-radius: 2px;
    background: #1f5a50;
    box-shadow: none;
  }
}

@media (max-width: 1100px) {
  .summary-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .user-workbench { padding: 22px 20px 34px; }
}

@media (max-width: 768px) {
  .user-workbench { padding: 18px 12px 28px; }
  .page-intro { align-items: flex-start; }
  .page-intro p { max-width: 260px; line-height: 1.6; }
  .intro-stamp { display: none; }
  .summary-card { min-height: 94px; padding: 14px; }
  .summary-card strong { font-size: 24px; }
  :deep(.workspace-card .ant-card-body) { padding: 15px 12px; }
  .workspace-heading { align-items: flex-start; flex-direction: column; }
  .create-button { width: 100%; }
  .filter-bar { display: grid; grid-template-columns: 1fr 1fr; }
  .filter-keyword, .filter-actions { grid-column: span 2; }
  .filter-keyword :deep(.ant-input-affix-wrapper), .filter-select { width: 100%; min-width: 0; }
  .filter-actions { margin-left: 0 !important; }
  .filter-actions :deep(.ant-space), .filter-actions :deep(.ant-btn) { width: 100%; }
  .filter-actions :deep(.ant-space-item) { flex: 1; }
  .filter-actions :deep(.ant-btn) { min-width: 100px; }
  .drawer-profile { align-items: flex-start; flex-wrap: wrap; }
  .drawer-profile-copy { min-width: calc(100% - 82px); }
  .drawer-profile-actions { width: 100%; padding-left: 78px; }
  .learning-stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .badge-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 520px) {
  .summary-grid { gap: 8px; }
  .summary-card { align-items: flex-start; flex-direction: column; gap: 8px; }
  .summary-card small { display: none; }
  .summary-card strong { font-size: 21px; }
  .summary-icon { width: 32px; height: 32px; font-size: 16px; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-field--wide { grid-column: span 1; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
  .level-banner { align-items: flex-start; flex-wrap: wrap; }
  .level-score { margin-left: 68px; justify-items: start; }
  .asset-lines { grid-template-columns: 1fr; }
  .map-track { transform: scale(.88); transform-origin: left center; }
}

@media (min-width: 1024px) and (max-width: 1100px) {
  .user-workbench { padding: 0; }
}
</style>
