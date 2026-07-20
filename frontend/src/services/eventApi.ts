import type { DemoEvent, EventEntry } from '../data/events'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? '/api/public'

interface EventSummaryPayload {
  id: number
  title: string
  summary: string
  category: string
  categorySlug: string
  startTime: string
  endTime: string
  location: string
  organizer: string
  coverUrl: string
  demo: boolean
}

interface EventDetailPayload extends EventSummaryPayload {
  content: string
  sourceUrl: string | null
  entries: EventEntry[]
}

const request = async <T>(path: string): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { Accept: 'application/json' },
  })

  if (!response.ok) {
    throw new Error(`接口请求失败：${response.status}`)
  }

  return response.json() as Promise<T>
}

const toEvent = (payload: EventSummaryPayload | EventDetailPayload): DemoEvent => {
  const entries = 'entries' in payload ? payload.entries : undefined

  return {
    id: payload.id,
    title: payload.title,
    summary: payload.summary,
    category: payload.category,
    categorySlug: payload.categorySlug,
    startTime: payload.startTime,
    endTime: payload.endTime,
    location: payload.location,
    organizer: payload.organizer,
    coverUrl: payload.coverUrl,
    content: 'content' in payload ? payload.content : undefined,
    sourceUrl: 'sourceUrl' in payload ? payload.sourceUrl : undefined,
    entries,
    entryLabel: entries?.[0]?.label ?? '查看官方入口',
    isDemo: payload.demo,
  }
}

export const fetchEvents = async (): Promise<DemoEvent[]> => {
  const payload = await request<EventSummaryPayload[]>('/events')
  return payload.map(toEvent)
}

export const fetchEventDetail = async (id: number): Promise<DemoEvent> => {
  const payload = await request<EventDetailPayload>(`/events/${id}`)
  return toEvent(payload)
}
