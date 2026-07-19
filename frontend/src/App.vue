<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { demoEvents, type DemoEvent, type EventEntry } from './data/events'
import { fetchEventDetail, fetchEvents } from './services/eventApi'

type ViewName = 'hall' | 'favorites' | 'profile'

const query = ref('')
const activeCategory = ref('全部')
const activeView = ref<ViewName>('hall')
const events = ref<DemoEvent[]>([])
const selectedEvent = ref<DemoEvent | null>(null)
const categoryScroller = ref<HTMLElement | null>(null)
const favoriteIds = ref<Set<number>>(new Set())
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

const favoriteEvents = computed(() =>
  events.value.filter((event) => favoriteIds.value.has(event.id)),
)

const visibleEvents = computed(() =>
  activeView.value === 'hall' ? filteredEvents.value : favoriteEvents.value,
)

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

const scrollCategories = (direction: -1 | 1) => {
  const scroller = categoryScroller.value
  if (!scroller) return

  scroller.scrollBy({
    left: direction * Math.min(280, scroller.clientWidth * 0.78),
    behavior: 'smooth',
  })
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

const switchView = (view: ViewName) => {
  activeView.value = view
  closeEvent()
  window.scrollTo({ top: 0, behavior: 'auto' })
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

const persistFavoriteEvents = () => {
  window.localStorage.setItem(
    'campus-event-hub-favorites',
    JSON.stringify([...favoriteIds.value]),
  )
}

const toggleFavorite = (id: number) => {
  const next = new Set(favoriteIds.value)
  if (next.has(id)) {
    next.delete(id)
    showToast('已取消收藏。')
  } else {
    next.add(id)
    showToast('已收藏，记录保存在当前浏览器。')
  }
  favoriteIds.value = next
  persistFavoriteEvents()
}

const isSelectedFavorite = computed(() =>
  selectedEvent.value ? favoriteIds.value.has(selectedEvent.value.id) : false,
)

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
    const currentFavorites = window.localStorage.getItem('campus-event-hub-favorites')
    const legacyJoined = window.localStorage.getItem('campus-event-hub-joined')
    const storedIds = JSON.parse(currentFavorites ?? legacyJoined ?? '[]')
    if (Array.isArray(storedIds)) {
      favoriteIds.value = new Set(storedIds.filter((id): id is number => Number.isInteger(id)))
      persistFavoriteEvents()
      window.localStorage.removeItem('campus-event-hub-joined')
    }
  } catch {
    window.localStorage.removeItem('campus-event-hub-favorites')
    window.localStorage.removeItem('campus-event-hub-joined')
  }

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
        <a
          class="brand"
          href="#top"
          aria-label="返回校园活动中心首页"
          @click.prevent="switchView('hall')"
        >
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
      <template v-if="activeView === 'hall'">
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

        <div class="category-scroll-shell">
          <button
            type="button"
            class="category-scroll-button"
            aria-label="查看前面的活动分类"
            @click="scrollCategories(-1)"
          >
            ‹
          </button>
          <div ref="categoryScroller" class="category-list" role="list" aria-label="活动分类">
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
          <button
            type="button"
            class="category-scroll-button"
            aria-label="查看更多活动分类"
            @click="scrollCategories(1)"
          >
            ›
          </button>
        </div>
      </section>
      </template>

      <section v-if="activeView !== 'profile'" class="events-section" aria-labelledby="events-title">
        <div class="section-heading">
          <div>
            <p class="section-label">{{ activeView === 'hall' ? '近期活动' : '你的收藏' }}</p>
            <h2 id="events-title">
              {{ activeView === 'hall' ? '接下来可以去哪里' : '收藏的活动' }}
            </h2>
          </div>
          <p>{{ activeView === 'hall' ? '按开始时间排序' : '记录保存在当前浏览器' }}</p>
        </div>

        <div v-if="!isReady" class="event-grid" aria-label="活动加载中">
          <div v-for="item in 4" :key="item" class="skeleton-card" aria-hidden="true">
            <div class="skeleton-cover"></div>
            <div class="skeleton-line wide"></div>
            <div class="skeleton-line"></div>
          </div>
        </div>

        <div v-else-if="visibleEvents.length" class="event-grid">
          <article v-for="event in visibleEvents" :key="event.id" class="event-card">
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
          <strong>{{ activeView === 'hall' ? '暂时没有匹配的活动' : '还没有收藏活动' }}</strong>
          <p>
            {{
              activeView === 'hall'
                ? '换一个关键词或清除分类后再试试。'
                : '打开活动详情后，可以收藏感兴趣的活动。'
            }}
          </p>
          <button
            type="button"
            @click="activeView === 'hall' ? clearFilters() : switchView('hall')"
          >
            {{ activeView === 'hall' ? '清除筛选' : '去大厅看看' }}
          </button>
        </div>
      </section>

      <section v-else class="profile-section" aria-labelledby="profile-title">
        <p class="section-label">个人</p>
        <h1 id="profile-title">访客模式</h1>
        <p class="profile-lead">无需登录也可以浏览活动，当前只在这台设备上保存你的活动记录。</p>

        <div class="profile-summary">
          <div class="profile-identity">
            <span class="profile-mark" aria-hidden="true">集</span>
            <div>
              <strong>校园活动访客</strong>
              <span>本地演示账户</span>
            </div>
          </div>
          <dl class="profile-stats">
            <div>
              <dt>收藏</dt>
              <dd>{{ favoriteEvents.length }} 项</dd>
            </div>
            <div>
              <dt>数据位置</dt>
              <dd>当前浏览器</dd>
            </div>
          </dl>
          <button type="button" class="profile-action" @click="switchView('favorites')">
            查看收藏的活动
          </button>
        </div>

        <div class="profile-note">
          <strong>后续计划</strong>
          <p>学生账号上线后，可将活动记录同步到后端，实现跨设备查看。当前版本不收集姓名、学号或报名信息。</p>
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

            <button
              type="button"
              class="favorite-toggle"
              :class="{ active: isSelectedFavorite }"
              @click="toggleFavorite(selectedEvent.id)"
            >
              {{ isSelectedFavorite ? '取消收藏' : '收藏活动' }}
            </button>
            <p class="favorite-help">收藏只表示感兴趣，不代表已经完成报名。</p>

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

    <nav class="bottom-nav" aria-label="主要页面">
      <button
        type="button"
        :class="{ active: activeView === 'hall' }"
        :aria-current="activeView === 'hall' ? 'page' : undefined"
        @click="switchView('hall')"
      >
        <strong>大厅</strong>
        <span>发现活动</span>
      </button>
      <button
        type="button"
        :class="{ active: activeView === 'favorites' }"
        :aria-current="activeView === 'favorites' ? 'page' : undefined"
        @click="switchView('favorites')"
      >
        <strong>收藏</strong>
        <span>{{ favoriteEvents.length }} 项活动</span>
      </button>
      <button
        type="button"
        :class="{ active: activeView === 'profile' }"
        :aria-current="activeView === 'profile' ? 'page' : undefined"
        @click="switchView('profile')"
      >
        <strong>个人</strong>
        <span>访客模式</span>
      </button>
    </nav>

    <div v-if="toastMessage" class="toast" role="status">{{ toastMessage }}</div>
  </div>
</template>
