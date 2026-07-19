<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { demoEvents, type DemoEvent, type EventEntry } from './data/events'
import { fetchEventDetail, fetchEvents } from './services/eventApi'

const query = ref('')
const activeCategory = ref('全部')
const events = ref<DemoEvent[]>([])
const selectedEvent = ref<DemoEvent | null>(null)
const isReady = ref(false)
const isDetailLoading = ref(false)
const isApiAvailable = ref(false)
const toastMessage = ref('')
const failedImages = ref<Set<number>>(new Set())

const categories = computed(() => [
  '全部',
  ...Array.from(new Set(events.value.map((event) => event.category))),
])

const filteredEvents = computed(() => {
  const keyword = query.value.trim().toLowerCase()

  return events.value.filter((event) => {
    const matchesCategory =
      activeCategory.value === '全部' || event.category === activeCategory.value
    const matchesKeyword =
      !keyword ||
      [event.title, event.summary, event.location, event.organizer, event.category]
        .join(' ')
        .toLowerCase()
        .includes(keyword)

    return matchesCategory && matchesKeyword
  })
})

const formatDate = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date(value))

const formatTime = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))

const selectCategory = (category: string) => {
  activeCategory.value = category
}

const clearFilters = () => {
  query.value = ''
  activeCategory.value = '全部'
}

const openEvent = async (event: DemoEvent) => {
  selectedEvent.value = event
  document.body.classList.add('sheet-open')

  if (!isApiAvailable.value) return

  isDetailLoading.value = true
  try {
    const detail = await fetchEventDetail(event.id)
    if (selectedEvent.value?.id === event.id) {
      selectedEvent.value = detail
    }
  } catch {
    showToast('详情接口暂时不可用，当前展示活动列表中的基础信息。')
  } finally {
    isDetailLoading.value = false
  }
}

const closeEvent = () => {
  selectedEvent.value = null
  document.body.classList.remove('sheet-open')
}

let toastTimer: number | undefined
const showToast = (message: string) => {
  toastMessage.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
  }, 3200)
}

const markImageFailed = (id: number) => {
  failedImages.value = new Set([...failedImages.value, id])
}

const selectedEntry = computed<EventEntry | null>(() => {
  if (!selectedEvent.value) return null
  if (selectedEvent.value.entries?.length) return selectedEvent.value.entries[0]

  const url = `https://example.com/demo-event/${selectedEvent.value.id}`
  return {
    type: 'LINK',
    label: selectedEvent.value.entryLabel,
    url,
    qrImageUrl: `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(url)}`,
  }
})

onMounted(async () => {
  try {
    events.value = await fetchEvents()
    isApiAvailable.value = true
  } catch {
    events.value = demoEvents
    showToast('后端未启动，已自动切换为前端模拟数据。')
  } finally {
    isReady.value = true
  }
})
</script>

<template>
  <div class="site-shell">
    <header class="topbar">
      <div class="topbar-inner">
        <a class="brand" href="#top" aria-label="返回校园活动中心首页">
          <span class="brand-mark" aria-hidden="true">集</span>
          <span>
            <strong>校园活动中心</strong>
            <small>发现值得参加的校园活动</small>
          </span>
        </a>
        <span class="demo-badge">演示数据</span>
      </div>
    </header>

    <main id="top" class="page-content">
      <section class="intro" aria-labelledby="page-title">
        <div class="intro-copy">
          <p class="section-label">校园活动导航</p>
          <h1 id="page-title">好活动，不该藏在消息列表里</h1>
          <p>集中查看讲座、社团、比赛和公益活动，找到后直接进入主办方的官方入口。</p>
        </div>

        <label class="search-box">
          <span>搜索活动</span>
          <input
            v-model="query"
            type="search"
            placeholder="试试搜索“讲座”“摄影”或地点"
            autocomplete="off"
          />
        </label>
      </section>

      <section class="category-section" aria-labelledby="category-title">
        <div class="section-heading compact">
          <div>
            <p class="section-label">快速筛选</p>
            <h2 id="category-title">你想参加什么</h2>
          </div>
          <span>{{ filteredEvents.length }} 条活动</span>
        </div>

        <div class="category-list" role="list" aria-label="活动分类">
          <button
            v-for="category in categories"
            :key="category"
            type="button"
            class="category-button"
            :class="{ active: activeCategory === category }"
            :aria-pressed="activeCategory === category"
            @click="selectCategory(category)"
          >
            {{ category }}
          </button>
        </div>
      </section>

      <section class="events-section" aria-labelledby="events-title">
        <div class="section-heading">
          <div>
            <p class="section-label">近期活动</p>
            <h2 id="events-title">接下来可以去哪里</h2>
          </div>
          <p>按开始时间排序</p>
        </div>

        <div v-if="!isReady" class="event-grid" aria-label="活动加载中">
          <div v-for="item in 4" :key="item" class="skeleton-card" aria-hidden="true">
            <div class="skeleton-cover"></div>
            <div class="skeleton-line wide"></div>
            <div class="skeleton-line"></div>
          </div>
        </div>

        <div v-else-if="filteredEvents.length" class="event-grid">
          <article v-for="event in filteredEvents" :key="event.id" class="event-card">
            <div class="event-cover">
              <img
                v-if="!failedImages.has(event.id)"
                :src="event.coverUrl"
                :alt="`${event.title}的演示封面`"
                loading="lazy"
                width="960"
                height="640"
                @error="markImageFailed(event.id)"
              />
              <div v-else class="cover-fallback">活动海报待上传</div>
            </div>

            <div class="event-body">
              <div class="event-meta">
                <span class="category-name">{{ event.category }}</span>
                <time :datetime="event.startTime">{{ formatDate(event.startTime) }}</time>
              </div>
              <h3>{{ event.title }}</h3>
              <p class="event-summary">{{ event.summary }}</p>
              <dl class="event-facts">
                <div>
                  <dt>时间</dt>
                  <dd>{{ formatTime(event.startTime) }}</dd>
                </div>
                <div>
                  <dt>地点</dt>
                  <dd>{{ event.location }}</dd>
                </div>
              </dl>
              <button type="button" class="detail-button" @click="openEvent(event)">
                查看活动详情
              </button>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <strong>暂时没有匹配的活动</strong>
          <p>换一个关键词或清除分类后再试试。</p>
          <button type="button" @click="clearFilters">清除筛选</button>
        </div>
      </section>
    </main>

    <footer class="footer">
      <strong>校园活动中心</strong>
      <span>当前页面使用模拟数据，正式内容由运营方维护。</span>
    </footer>

    <div
      v-if="selectedEvent"
      class="sheet-backdrop"
      role="presentation"
      @click.self="closeEvent"
    >
      <section class="detail-sheet" role="dialog" aria-modal="true" aria-labelledby="detail-title">
        <div class="sheet-toolbar">
          <span>活动详情</span>
          <button type="button" @click="closeEvent">关闭</button>
        </div>

        <div class="sheet-scroll">
          <div class="detail-cover">
            <img
              v-if="!failedImages.has(selectedEvent.id)"
              :src="selectedEvent.coverUrl"
              :alt="`${selectedEvent.title}的演示封面`"
              width="960"
              height="640"
              @error="markImageFailed(selectedEvent.id)"
            />
            <div v-else class="cover-fallback">活动海报待上传</div>
          </div>

          <div class="detail-content">
            <span class="category-name">{{ selectedEvent.category }}</span>
            <h2 id="detail-title">{{ selectedEvent.title }}</h2>
            <p>{{ selectedEvent.summary }}</p>
            <p v-if="isDetailLoading" class="demo-note">正在读取完整活动信息……</p>

            <dl class="detail-list">
              <div>
                <dt>活动时间</dt>
                <dd>
                  {{ formatDate(selectedEvent.startTime) }}
                  {{ formatTime(selectedEvent.startTime) }}-{{ formatTime(selectedEvent.endTime) }}
                </dd>
              </div>
              <div>
                <dt>活动地点</dt>
                <dd>{{ selectedEvent.location }}</dd>
              </div>
              <div>
                <dt>主办方</dt>
                <dd>{{ selectedEvent.organizer }}</dd>
              </div>
            </dl>

            <div v-if="selectedEntry" class="entry-panel">
              <div>
                <h3>主办方官方入口</h3>
                <p>手机端可点击按钮，电脑端可使用手机扫码。</p>
                <a
                  class="primary-button"
                  :href="selectedEntry.url"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {{ selectedEntry.label }}
                </a>
              </div>
              <img
                :src="selectedEntry.qrImageUrl"
                alt="模拟官方入口二维码"
                width="120"
                height="120"
              />
            </div>

            <p class="demo-note">本活动为虚构演示内容，不代表任何学校或组织的真实通知。</p>
          </div>
        </div>
      </section>
    </div>

    <div v-if="toastMessage" class="toast" role="status">{{ toastMessage }}</div>
  </div>
</template>
