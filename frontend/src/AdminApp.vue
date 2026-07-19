<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  changeAdminEventStatus,
  createAdminEvent,
  fetchAdminCategories,
  fetchAdminEvents,
  fetchAdminOrganizers,
  updateAdminEvent,
  type AdminCategory,
  type AdminEvent,
  type AdminEventInput,
  type AdminOrganizer,
  type EventStatus,
} from './services/adminApi'

type StatusFilter = 'ALL' | EventStatus

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
const statusFilter = ref<StatusFilter>('ALL')
const isLoading = ref(true)
const isSaving = ref(false)
const editorOpen = ref(false)
const editingId = ref<number | null>(null)
const errorMessage = ref('')
const successMessage = ref('')

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

const filteredEvents = computed(() =>
  statusFilter.value === 'ALL'
    ? events.value
    : events.value.filter((event) => event.status === statusFilter.value),
)

const statusCount = (status: EventStatus) =>
  events.value.filter((event) => event.status === status).length

const formatDateTime = (value: string) =>
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
    errorMessage.value = error instanceof Error ? error.message : '管理数据读取失败。'
  } finally {
    isLoading.value = false
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

onMounted(loadData)
</script>

<template>
  <div class="admin-shell">
    <header class="admin-topbar">
      <a class="admin-brand" href="/admin" aria-label="校园活动管理台首页">
        <span>管</span>
        <div>
          <strong>校园活动管理台</strong>
          <small>内容录入与发布</small>
        </div>
      </a>
      <a class="admin-student-link" href="/">查看学生端</a>
    </header>

    <main class="admin-main">
      <section class="admin-intro">
        <div>
          <p>活动运营</p>
          <h1>管理活动内容</h1>
          <span>创建草稿、补充官方入口，并控制活动是否出现在学生端。</span>
        </div>
        <button type="button" class="admin-primary" @click="openCreate">新增活动</button>
      </section>

      <div class="admin-security-note" role="note">
        当前是本地演示管理台，尚未接入管理员登录。完成权限保护前请勿公开部署管理接口。
      </div>

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
            <p>共 {{ filteredEvents.length }} 条符合当前状态的活动</p>
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
              <div><dt>开始时间</dt><dd>{{ formatDateTime(event.startTime) }}</dd></div>
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
        <div v-else class="admin-empty">当前状态下没有活动。</div>
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
