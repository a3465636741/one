import type { EventEntry } from '../data/events'

const ADMIN_API_BASE_URL =
  import.meta.env.VITE_ADMIN_API_BASE_URL ?? 'http://localhost:8080/api/admin'

export type EventStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'PUBLISHED'
  | 'CANCELLED'
  | 'ARCHIVED'

export interface AdminCategory {
  id: number
  name: string
  slug: string
  sortOrder: number
}

export interface AdminOrganizer {
  id: number
  name: string
  description: string | null
  contact: string | null
}

export interface AdminEvent {
  id: number
  title: string
  summary: string
  content: string
  categoryId: number
  category: string
  categorySlug: string
  organizerId: number
  organizer: string
  startTime: string
  endTime: string
  location: string
  coverUrl: string | null
  sourceUrl: string | null
  status: EventStatus
  demo: boolean
  entries: EventEntry[]
}

export interface AdminEventEntryInput {
  type: string
  label: string
  url: string | null
  qrImageUrl: string | null
  sortOrder: number
}

export interface AdminEventInput {
  title: string
  summary: string
  content: string
  categoryId: number
  organizerId: number
  startTime: string
  endTime: string
  location: string
  coverUrl: string | null
  sourceUrl: string | null
  demo: boolean
  entries: AdminEventEntryInput[]
}

const request = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
  })

  if (!response.ok) {
    let message = `管理接口请求失败：${response.status}`
    try {
      const payload = (await response.json()) as { detail?: string; message?: string }
      message = payload.detail ?? payload.message ?? message
    } catch {
      // Keep the status-based fallback when the response is not JSON.
    }
    throw new Error(message)
  }

  return response.json() as Promise<T>
}

export const fetchAdminEvents = () => request<AdminEvent[]>('/events')

export const fetchAdminCategories = () => request<AdminCategory[]>('/categories')

export const fetchAdminOrganizers = () => request<AdminOrganizer[]>('/organizers')

export const createAdminEvent = (input: AdminEventInput) =>
  request<AdminEvent>('/events', {
    method: 'POST',
    body: JSON.stringify(input),
  })

export const updateAdminEvent = (id: number, input: AdminEventInput) =>
  request<AdminEvent>(`/events/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  })

export const changeAdminEventStatus = (id: number, status: EventStatus) =>
  request<AdminEvent>(`/events/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
