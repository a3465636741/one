<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  AdminApiError,
  changeAdminEventStatus,
  changeAdminPassword,
  createAdminEvent,
  fetchAdminAccount,
  fetchAdminCategories,
  fetchAdminEvents,
  fetchAdminOrganizers,
  loginAdmin,
  logoutAdmin,
  updateAdminEvent,
  type AdminAccount,
  type AdminCategory,
  type AdminEvent,
  type AdminEventInput,
  type AdminOrganizer,
  type EventStatus,
} from './services/adminApi'

type StatusFilter = 'ALL' | EventStatus
type AdminSection = 'events' | 'account'

interface EventForm {
  title: string
  summary: string
  content: string
  categoryId: number
  organizerId: number
  startTime: string
  endTime: string
  location: string
  coverUrl: string
  sourceUrl: string
  demo: boolean
  entryLabel: string
  entryUrl: string
  qrImageUrl: string
}

const events = ref<AdminEvent[]>([])
const categories = ref<AdminCategory[]>([])
const organizers = ref<AdminOrganizer[]>([])
const account = ref<AdminAccount | null>(null)
const activeSection = ref<AdminSection>('events')
const statusFilter = ref<StatusFilter>('ALL')
const searchQuery = ref('')
const isAuthLoading = ref(true)
const isLoading = ref(false)
const isSaving = ref(false)
const isLoggingIn = ref(false)
const isChangingPassword = ref(false)
const isLoggingOut = ref(false)
const editorOpen = ref(false)
const editingId = ref<number | null>(null)
const errorMessage = ref('')
const authError = ref('')
const accountError = ref('')
const successMessage = ref('')

const loginForm = reactive({ username: 'admin', password: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const statusLabels: Record<EventStatus, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审核',
  PUBLISHED: '已发布',
  CANCELLED: '已取消',
  ARCHIVED: '已下架',
}

const filters: Array<{ value: StatusFilter; label: string }> = [
  { value: 'ALL', label: '全部' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING_REVIEW', label: '待审核' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'ARCHIVED', label: '已下架' },
]

const blankForm = (): EventForm => ({
  title: '',
  summary: '',
  content: '',
  categoryId: categories.value[0]?.id ?? 0,
  organizerId: organizers.value[0]?.id ?? 0,
  startTime: '',
  endTime: '',
  location: '',
  coverUrl: '',
  sourceUrl: '',
  demo: true,
  entryLabel: '前往官方入口',
  entryUrl: '',
  qrImageUrl: '',
})

const form = reactive<EventForm>(blankForm())

const filteredEvents = computed(() => {
  const keyword = searchQuery.value.trim().toLocaleLowerCase('zh-CN')
  return events.value.filter((event) => {
    const matchesStatus = statusFilter.value === 'ALL' || event.status === statusFilter.value
    const matchesKeyword =
      !keyword ||
      [event.title, event.category, event.organizer, event.location].some((value) =>
        value.toLocaleLowerCase('zh-CN').includes(keyword),
      )
    return matchesStatus && matchesKeyword
  })
})

const resultDescription = computed(() => {
  if (searchQuery.value.trim()) return `找到 ${filteredEvents.value.length} 条匹配活动`
  return `共 ${filteredEvents.value.length} 条符合当前状态的活动`
})

const statusCount = (status: EventStatus) =>
  events.value.filter((event) => event.status === status).length

const formatDateTime = (value: string | null) => {
  if (!value) return '本次会话首次登录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

const formatEventDateTime = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))

const toLocalInput = (value: string) => value.slice(0, 16)

const showSuccess = (message: string) => {
  successMessage.value = message
  window.setTimeout(() => {
    successMessage.value = ''
  }, 2600)
}

const expireSession = () => {
  account.value = null
  events.value = []
  authError.value = '登录已过期，请重新登录。'
}

const loadData = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [eventData, categoryData, organizerData] = await Promise.all([
      fetchAdminEvents(),
      fetchAdminCategories(),
      fetchAdminOrganizers(),
    ])
    events.value = eventData
    categories.value = categoryData
    organizers.value = organizerData
  } catch (error) {
    if (error instanceof AdminApiError && error.status === 401) {
      expireSession()
    } else if (error instanceof AdminApiError && error.status === 428) {
      if (account.value) account.value.passwordChangeRequired = true
      activeSection.value = 'account'
      accountError.value = error.message
    } else {
      errorMessage.value = error instanceof Error ? error.message : '管理数据读取失败。'
    }
  } finally {
    isLoading.value = false
  }
}

const initialize = async () => {
  isAuthLoading.value = true
  try {
    account.value = await fetchAdminAccount()
    if (account.value.passwordChangeRequired) {
      activeSection.value = 'account'
    } else {
      await loadData()
    }
  } catch (error) {
    if (!(error instanceof AdminApiError && error.status === 401)) {
      authError.value = error instanceof Error ? error.message : '管理端初始化失败。'
    }
  } finally {
    isAuthLoading.value = false
  }
}

const submitLogin = async () => {
  isLoggingIn.value = true
  authError.value = ''
  try {
    account.value = await loginAdmin(loginForm.username.trim(), loginForm.password)
    loginForm.password = ''
    if (account.value.passwordChangeRequired) {
      activeSection.value = 'account'
    } else {
      activeSection.value = 'events'
      await loadData()
    }
  } catch (error) {
    authError.value = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  } finally {
    isLoggingIn.value = false
  }
}

const openEvents = async () => {
  if (account.value?.passwordChangeRequired) {
    activeSection.value = 'account'
    accountError.value = '修改初始密码后才能管理活动。'
    return
  }
  activeSection.value = 'events'
  if (!events.value.length) await loadData()
}

const submitPasswordChange = async () => {
  accountError.value = ''
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    accountError.value = '两次输入的新密码不一致。'
    return
  }
  isChangingPassword.value = true
  try {
    account.value = await changeAdminPassword(
      passwordForm.currentPassword,
      passwordForm.newPassword,
    )
    Object.assign(passwordForm, { currentPassword: '', newPassword: '', confirmPassword: '' })
    showSuccess('密码已更新，活动管理权限已启用。')
    await loadData()
  } catch (error) {
    accountError.value = error instanceof Error ? error.message : '密码修改失败。'
  } finally {
    isChangingPassword.value = false
  }
}

const submitLogout = async () => {
  isLoggingOut.value = true
  accountError.value = ''
  try {
    await logoutAdmin()
    account.value = null
    events.value = []
    activeSection.value = 'events'
    authError.value = ''
  } catch (error) {
    accountError.value = error instanceof Error ? error.message : '退出登录失败。'
  } finally {
    isLoggingOut.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, blankForm())
  editorOpen.value = true
}

const openEdit = (event: AdminEvent) => {
  const entry = event.entries[0]
  editingId.value = event.id
  Object.assign(form, {
    title: event.title,
    summary: event.summary,
    content: event.content,
    categoryId: event.categoryId,
    organizerId: event.organizerId,
    startTime: toLocalInput(event.startTime),
    endTime: toLocalInput(event.endTime),
    location: event.location,
    coverUrl: event.coverUrl ?? '',
    sourceUrl: event.sourceUrl ?? '',
    demo: event.demo,
    entryLabel: entry?.label ?? '前往官方入口',
    entryUrl: entry?.url ?? '',
    qrImageUrl: entry?.qrImageUrl ?? '',
  })
  editorOpen.value = true
}

const closeEditor = () => {
  if (isSaving.value) return
  editorOpen.value = false
}

const toInput = (): AdminEventInput => {
  const hasEntry = Boolean(form.entryUrl.trim() || form.qrImageUrl.trim())
  return {
    title: form.title.trim(),
    summary: form.summary.trim(),
    content: form.content.trim(),
    categoryId: Number(form.categoryId),
    organizerId: Number(form.organizerId),
    startTime: form.startTime,
    endTime: form.endTime,
    location: form.location.trim(),
    coverUrl: form.coverUrl.trim() || null,
    sourceUrl: form.sourceUrl.trim() || null,
    demo: form.demo,
    entries: hasEntry
      ? [
          {
            type: 'LINK',
            label: form.entryLabel.trim() || '前往官方入口',
            url: form.entryUrl.trim() || null,
            qrImageUrl: form.qrImageUrl.trim() || null,
            sortOrder: 10,
          },
        ]
      : [],
  }
}

const saveEvent = async () => {
  isSaving.value = true
  errorMessage.value = ''
  try {
    if (editingId.value === null) {
      await createAdminEvent(toInput())
      showSuccess('活动草稿已创建。')
    } else {
      await updateAdminEvent(editingId.value, toInput())
      showSuccess('活动内容已保存。')
    }
    editorOpen.value = false
    await loadData()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '活动保存失败。'
  } finally {
    isSaving.value = false
  }
}

const setStatus = async (event: AdminEvent, status: EventStatus) => {
  errorMessage.value = ''
  try {
    await changeAdminEventStatus(event.id, status)
    showSuccess(`“${event.title}”已更新为${statusLabels[status]}。`)
    await loadData()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '活动状态更新失败。'
  }
}

onMounted(initialize)
</script>

<template>
  <div v-if="isAuthLoading" class="admin-auth-shell">
    <div class="admin-auth-loading" role="status">正在检查登录状态……</div>
  </div>

  <div v-else-if="!account" class="admin-auth-shell">
    <main class="admin-login-panel">
      <a class="admin-brand" href="/admin" aria-label="校园活动管理台首页">
        <span>管</span>
        <div>
          <strong>校园活动管理台</strong>
          <small>仅限授权管理员</small>
        </div>
      </a>
      <div class="admin-login-copy">
        <p>管理员登录</p>
        <h1>安全管理校园活动</h1>
        <span>登录后可以录入、审核、发布和下架活动。</span>
      </div>
      <form class="admin-login-form" @submit.prevent="submitLogin">
        <label class="admin-field">
          <span>管理员账号</span>
          <input v-model="loginForm.username" autocomplete="username" required maxlength="80" />
        </label>
        <label class="admin-field">
          <span>密码</span>
          <input
            v-model="loginForm.password"
            type="password"
            autocomplete="current-password"
            required
          />
        </label>
        <p v-if="authError" class="admin-message error" role="alert">{{ authError }}</p>
        <button type="submit" class="admin-primary" :disabled="isLoggingIn">
          {{ isLoggingIn ? '正在登录……' : '登录管理台' }}
        </button>
      </form>
      <p class="admin-login-help">
        本地演示初始账号为 admin，初始密码为 campus-admin-2026。首次登录后必须修改密码。
      </p>
      <a class="admin-back-link" href="/">返回学生端</a>
    </main>
  </div>

  <div v-else class="admin-shell">
    <header class="admin-topbar">
      <a class="admin-brand" href="/admin" aria-label="校园活动管理台首页">
        <span>管</span>
        <div>
          <strong>校园活动管理台</strong>
          <small>内容录入与发布</small>
        </div>
      </a>
      <nav class="admin-topbar-actions" aria-label="管理台导航">
        <button
          type="button"
          :class="{ active: activeSection === 'events' }"
          :aria-current="activeSection === 'events' ? 'page' : undefined"
          @click="openEvents"
        >
          活动管理
        </button>
        <button
          type="button"
          :class="{ active: activeSection === 'account' }"
          :aria-current="activeSection === 'account' ? 'page' : undefined"
          @click="activeSection = 'account'"
        >
          账号
        </button>
        <a class="admin-student-link" href="/">学生端</a>
      </nav>
    </header>

    <main v-if="activeSection === 'events'" class="admin-main">
      <section class="admin-intro">
        <div>
          <p>活动运营</p>
          <h1>管理活动内容</h1>
          <span>创建草稿、补充官方入口，并控制活动是否出现在学生端。</span>
        </div>
        <button type="button" class="admin-primary" @click="openCreate">新增活动</button>
      </section>

      <section class="admin-stats" aria-label="活动状态统计">
        <div><span>全部活动</span><strong>{{ events.length }}</strong></div>
        <div><span>草稿</span><strong>{{ statusCount('DRAFT') }}</strong></div>
        <div><span>待审核</span><strong>{{ statusCount('PENDING_REVIEW') }}</strong></div>
        <div><span>已发布</span><strong>{{ statusCount('PUBLISHED') }}</strong></div>
      </section>

      <section class="admin-list-section" aria-labelledby="admin-list-title">
        <div class="admin-list-heading">
          <div>
            <h2 id="admin-list-title">活动列表</h2>
            <p>{{ resultDescription }}</p>
          </div>
          <div class="admin-filters" aria-label="按活动状态筛选">
            <button
              v-for="filter in filters"
              :key="filter.value"
              type="button"
              :class="{ active: statusFilter === filter.value }"
              :aria-pressed="statusFilter === filter.value"
              @click="statusFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>

        <div class="admin-search">
          <label for="admin-event-search">搜索活动</label>
          <div>
            <input
              id="admin-event-search"
              v-model="searchQuery"
              type="search"
              placeholder="搜索标题、分类、主办方或地点"
            />
            <button v-if="searchQuery" type="button" @click="searchQuery = ''">清空</button>
          </div>
        </div>

        <p v-if="errorMessage" class="admin-message error" role="alert">{{ errorMessage }}</p>
        <p v-if="successMessage" class="admin-message success" role="status">
          {{ successMessage }}
        </p>

        <div v-if="isLoading" class="admin-loading">正在读取活动数据……</div>
        <div v-else-if="filteredEvents.length" class="admin-event-list">
          <article v-for="event in filteredEvents" :key="event.id" class="admin-event-row">
            <div class="admin-event-main">
              <div class="admin-event-status" :data-status="event.status">
                {{ statusLabels[event.status] }}
              </div>
              <div>
                <h3>{{ event.title }}</h3>
                <p>{{ event.category }} · {{ event.organizer }}</p>
              </div>
            </div>
            <dl class="admin-event-meta">
              <div><dt>开始时间</dt><dd>{{ formatEventDateTime(event.startTime) }}</dd></div>
              <div><dt>地点</dt><dd>{{ event.location }}</dd></div>
              <div><dt>官方入口</dt><dd>{{ event.entries.length ? '已配置' : '未配置' }}</dd></div>
            </dl>
            <div class="admin-event-actions">
              <button type="button" @click="openEdit(event)">编辑</button>
              <button
                v-if="event.status === 'DRAFT'"
                type="button"
                @click="setStatus(event, 'PENDING_REVIEW')"
              >
                送审
              </button>
              <button
                v-if="event.status !== 'PUBLISHED' && event.status !== 'ARCHIVED'"
                type="button"
                class="publish"
                @click="setStatus(event, 'PUBLISHED')"
              >
                发布
              </button>
              <button
                v-if="event.status === 'PUBLISHED' || event.status === 'PENDING_REVIEW'"
                type="button"
                class="archive"
                @click="setStatus(event, 'ARCHIVED')"
              >
                下架
              </button>
              <button
                v-if="event.status === 'ARCHIVED'"
                type="button"
                @click="setStatus(event, 'DRAFT')"
              >
                恢复为草稿
              </button>
            </div>
          </article>
        </div>
        <div v-else class="admin-empty">
          {{ searchQuery ? '没有找到匹配活动，请更换关键词。' : '当前状态下没有活动。' }}
        </div>
      </section>
    </main>

    <main v-else class="admin-main admin-account-main">
      <section class="admin-intro">
        <div>
          <p>账号与安全</p>
          <h1>管理员账号</h1>
          <span>查看当前登录身份、修改密码或安全退出管理台。</span>
        </div>
      </section>

      <div v-if="account.passwordChangeRequired" class="admin-security-note" role="alert">
        当前仍在使用初始密码。修改密码前，活动新增、编辑、发布和下架功能会保持锁定。
      </div>
      <p v-if="accountError" class="admin-message error" role="alert">{{ accountError }}</p>
      <p v-if="successMessage" class="admin-message success" role="status">
        {{ successMessage }}
      </p>

      <section class="admin-account-grid">
        <div class="admin-account-card">
          <div class="admin-account-title">
            <span>管</span>
            <div>
              <h2>{{ account.displayName }}</h2>
              <p>拥有活动内容管理权限</p>
            </div>
          </div>
          <dl class="admin-account-details">
            <div><dt>登录账号</dt><dd>{{ account.username }}</dd></div>
            <div><dt>账号角色</dt><dd>管理员</dd></div>
            <div><dt>最近登录</dt><dd>{{ formatDateTime(account.lastLoginAt) }}</dd></div>
            <div>
              <dt>安全状态</dt>
              <dd>{{ account.passwordChangeRequired ? '需要修改初始密码' : '密码已更新' }}</dd>
            </div>
          </dl>
        </div>

        <form class="admin-password-card" @submit.prevent="submitPasswordChange">
          <div>
            <h2>修改密码</h2>
            <p>新密码至少 10 位，建议同时包含字母、数字和符号。</p>
          </div>
          <label class="admin-field">
            <span>当前密码</span>
            <input
              v-model="passwordForm.currentPassword"
              type="password"
              autocomplete="current-password"
              required
            />
          </label>
          <label class="admin-field">
            <span>新密码</span>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              autocomplete="new-password"
              minlength="10"
              maxlength="72"
              required
            />
          </label>
          <label class="admin-field">
            <span>再次输入新密码</span>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              autocomplete="new-password"
              minlength="10"
              maxlength="72"
              required
            />
          </label>
          <button type="submit" class="admin-primary" :disabled="isChangingPassword">
            {{ isChangingPassword ? '正在更新……' : '更新密码' }}
          </button>
        </form>
      </section>

      <section class="admin-signout">
        <div>
          <h2>退出当前账号</h2>
          <p>退出后必须重新输入账号和密码，才能进入管理台。</p>
        </div>
        <button type="button" :disabled="isLoggingOut" @click="submitLogout">
          {{ isLoggingOut ? '正在退出……' : '退出登录' }}
        </button>
      </section>
    </main>

    <div v-if="editorOpen" class="admin-editor-backdrop" @click.self="closeEditor">
      <section class="admin-editor" role="dialog" aria-modal="true" aria-labelledby="editor-title">
        <div class="admin-editor-head">
          <div>
            <span>{{ editingId === null ? '新建草稿' : `编辑活动 #${editingId}` }}</span>
            <h2 id="editor-title">{{ editingId === null ? '新增活动' : '编辑活动' }}</h2>
          </div>
          <button type="button" @click="closeEditor">关闭</button>
        </div>

        <form class="admin-form" @submit.prevent="saveEvent">
          <label class="admin-field wide">
            <span>活动标题</span>
            <input v-model="form.title" required maxlength="200" />
          </label>
          <label class="admin-field wide">
            <span>活动摘要</span>
            <textarea v-model="form.summary" required maxlength="500" rows="3"></textarea>
          </label>
          <label class="admin-field wide">
            <span>活动详情</span>
            <textarea v-model="form.content" required rows="5"></textarea>
          </label>
          <label class="admin-field">
            <span>活动分类</span>
            <select v-model.number="form.categoryId" required>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>
          <label class="admin-field">
            <span>主办方</span>
            <select v-model.number="form.organizerId" required>
              <option v-for="organizer in organizers" :key="organizer.id" :value="organizer.id">
                {{ organizer.name }}
              </option>
            </select>
          </label>
          <label class="admin-field">
            <span>开始时间</span>
            <input v-model="form.startTime" type="datetime-local" required />
          </label>
          <label class="admin-field">
            <span>结束时间</span>
            <input v-model="form.endTime" type="datetime-local" required />
          </label>
          <label class="admin-field wide">
            <span>活动地点</span>
            <input v-model="form.location" required maxlength="200" />
          </label>
          <label class="admin-field wide">
            <span>封面图片地址</span>
            <input v-model="form.coverUrl" type="url" placeholder="https://..." />
          </label>
          <label class="admin-field wide">
            <span>信息来源地址</span>
            <input v-model="form.sourceUrl" type="url" placeholder="https://..." />
            <small>正式活动应保留原始通知或主办方来源。</small>
          </label>

          <div class="admin-form-section wide">
            <h3>主办方官方入口</h3>
            <p>草稿可以暂时不填；发布前必须至少配置链接或二维码图片。</p>
          </div>
          <label class="admin-field wide">
            <span>入口按钮文字</span>
            <input v-model="form.entryLabel" maxlength="100" />
          </label>
          <label class="admin-field wide">
            <span>官方链接</span>
            <input v-model="form.entryUrl" type="url" placeholder="https://..." />
          </label>
          <label class="admin-field wide">
            <span>二维码图片地址</span>
            <input v-model="form.qrImageUrl" type="url" placeholder="https://..." />
          </label>
          <label class="admin-check wide">
            <input v-model="form.demo" type="checkbox" />
            <span>这是虚构演示活动</span>
          </label>

          <div class="admin-form-actions wide">
            <button type="button" @click="closeEditor">取消</button>
            <button type="submit" class="admin-primary" :disabled="isSaving">
              {{ isSaving ? '正在保存……' : editingId === null ? '创建草稿' : '保存修改' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>
