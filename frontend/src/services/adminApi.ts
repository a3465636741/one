import type { EventEntry } from '../data/events'

const ADMIN_API_BASE_URL =
  import.meta.env.VITE_ADMIN_API_BASE_URL ?? '/api/admin'

let csrfToken = ''

export class AdminApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

export type EventStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'REJECTED'
  | 'PUBLISHED'
  | 'CANCELLED'
  | 'ARCHIVED'

export interface AdminCategory {
  id: number
  name: string
  slug: string
  sortOrder: number
  active: boolean
  eventCount: number
}

export interface AdminCategoryInput {
  name: string
  slug: string
  sortOrder: number
  active: boolean
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
  reviewNote: string | null
  status: EventStatus
  expired: boolean
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

export interface AdminAccount {
  id: number
  username: string
  displayName: string
  role: string
  passwordChangeRequired: boolean
  lastLoginAt: string | null
}

interface CsrfTokenPayload {
  headerName: string
  token: string
}

const refreshCsrfToken = async () => {
  const response = await fetch(`${ADMIN_API_BASE_URL}/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) {
    throw new AdminApiError('安全令牌获取失败，请刷新页面重试。', response.status)
  }
  const payload = (await response.json()) as CsrfTokenPayload
  csrfToken = payload.token
}

const request = async <T>(
  path: string,
  init?: RequestInit,
  canRetryCsrf = true,
): Promise<T> => {
  const method = init?.method?.toUpperCase() ?? 'GET'
  const mutating = !['GET', 'HEAD', 'OPTIONS'].includes(method)
  if (mutating && !csrfToken) await refreshCsrfToken()

  const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
    ...init,
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...(mutating ? { 'X-XSRF-TOKEN': csrfToken } : {}),
      ...init?.headers,
    },
  })

  if (response.status === 403 && mutating && canRetryCsrf) {
    csrfToken = ''
    await refreshCsrfToken()
    return request<T>(path, init, false)
  }

  if (!response.ok) {
    let message = `管理接口请求失败：${response.status}`
    try {
      const payload = (await response.json()) as { detail?: string; message?: string }
      message = payload.detail ?? payload.message ?? message
    } catch {
      // Keep the status-based fallback when the response is not JSON.
    }
    throw new AdminApiError(message, response.status)
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const fetchAdminAccount = () => request<AdminAccount>('/account')

export const loginAdmin = async (username: string, password: string) => {
  const account = await request<AdminAccount>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  csrfToken = ''
  return account
}

export const logoutAdmin = async () => {
  await request<void>('/auth/logout', { method: 'POST' })
  csrfToken = ''
}

export const changeAdminPassword = async (currentPassword: string, newPassword: string) => {
  const account = await request<AdminAccount>('/account/password', {
    method: 'PATCH',
    body: JSON.stringify({ currentPassword, newPassword }),
  })
  csrfToken = ''
  return account
}

export const fetchAdminEvents = () => request<AdminEvent[]>('/events')

export const fetchAdminCategories = () => request<AdminCategory[]>('/categories')

export const createAdminCategory = (input: AdminCategoryInput) =>
  request<AdminCategory>('/categories', {
    method: 'POST',
    body: JSON.stringify(input),
  })

export const updateAdminCategory = (id: number, input: AdminCategoryInput) =>
  request<AdminCategory>(`/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  })

export const changeAdminCategoryActive = (id: number, active: boolean) =>
  request<AdminCategory>(`/categories/${id}/active`, {
    method: 'PATCH',
    body: JSON.stringify({ active }),
  })

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

export const changeAdminEventStatus = (id: number, status: EventStatus, reviewNote?: string) =>
  request<AdminEvent>(`/events/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, reviewNote }),
  })
