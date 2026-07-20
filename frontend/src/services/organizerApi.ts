import type { EventEntry } from '../data/events'
import type { AdminCategory, EventStatus } from './adminApi'

const ORGANIZER_API_BASE_URL =
  import.meta.env.VITE_ORGANIZER_API_BASE_URL ?? '/api/organizer'

let csrfToken = ''

export class OrganizerApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

export interface OrganizerAccount {
  id: number
  username: string
  displayName: string
  organizerId: number
  organizerName: string
  passwordChangeRequired: boolean
  lastLoginAt: string | null
}

export interface OrganizerEvent {
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

export interface OrganizerEventInput {
  title: string
  summary: string
  content: string
  categoryId: number
  startTime: string
  endTime: string
  location: string
  coverUrl: string | null
  sourceUrl: string | null
  demo: boolean
  entries: Array<{
    type: string
    label: string
    url: string | null
    qrImageUrl: string | null
    sortOrder: number
  }>
}

interface CsrfTokenPayload {
  headerName: string
  token: string
}

const refreshCsrfToken = async () => {
  const response = await fetch(`${ORGANIZER_API_BASE_URL}/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
  if (!response.ok) {
    throw new OrganizerApiError('安全令牌获取失败，请刷新页面重试。', response.status)
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

  const response = await fetch(`${ORGANIZER_API_BASE_URL}${path}`, {
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
    let message = `活动方接口请求失败：${response.status}`
    try {
      const payload = (await response.json()) as { detail?: string; message?: string }
      message = payload.detail ?? payload.message ?? message
    } catch {
      // Keep the status-based fallback when the response is not JSON.
    }
    throw new OrganizerApiError(message, response.status)
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const fetchOrganizerAccount = () => request<OrganizerAccount>('/account')

export const loginOrganizer = async (username: string, password: string) => {
  const account = await request<OrganizerAccount>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  csrfToken = ''
  return account
}

export const logoutOrganizer = async () => {
  await request<void>('/auth/logout', { method: 'POST' })
  csrfToken = ''
}

export const changeOrganizerPassword = async (
  currentPassword: string,
  newPassword: string,
) => {
  const account = await request<OrganizerAccount>('/account/password', {
    method: 'PATCH',
    body: JSON.stringify({ currentPassword, newPassword }),
  })
  csrfToken = ''
  return account
}

export const fetchOrganizerEvents = () => request<OrganizerEvent[]>('/events')
export const fetchOrganizerCategories = () => request<AdminCategory[]>('/categories')

export const createOrganizerEvent = (input: OrganizerEventInput) =>
  request<OrganizerEvent>('/events', {
    method: 'POST',
    body: JSON.stringify(input),
  })

export const updateOrganizerEvent = (id: number, input: OrganizerEventInput) =>
  request<OrganizerEvent>(`/events/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  })

export const submitOrganizerEvent = (id: number) =>
  request<OrganizerEvent>(`/events/${id}/submit`, { method: 'PATCH' })

export const cancelOrganizerEvent = (id: number) =>
  request<OrganizerEvent>(`/events/${id}/cancel`, { method: 'PATCH' })
