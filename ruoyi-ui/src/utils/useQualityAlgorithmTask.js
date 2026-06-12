import { onBeforeUnmount } from 'vue'

const DEFAULT_DONE = ['SUCCESS', 'FAILED', 'CANCELED']
const DEFAULT_POLL_MS = 3000
const DEFAULT_MAX_BACKOFF_MS = 30000

export function getApiPayload(response) {
  if (!response || typeof response !== 'object') return {}
  return response.data && typeof response.data === 'object' ? response.data : response
}

export function getErrorMessage(error, fallback = '请求失败') {
  const data = error?.response?.data
  return data?.msg || data?.message || data?.error || error?.msg || error?.message || fallback
}

export function normalizeAlgorithmResult(data) {
  if (!data || typeof data !== 'object') return {}
  return data.result && typeof data.result === 'object' ? data.result : data
}

export function useQualityAlgorithmTask(options) {
  const doneStatuses = options.doneStatuses || DEFAULT_DONE
  const pollMs = options.pollMs || DEFAULT_POLL_MS
  const maxBackoffMs = options.maxBackoffMs || DEFAULT_MAX_BACKOFF_MS
  let timer = null
  let pollFailures = 0
  let activeToken = 0
  let pollingKey = ''

  function makePollingKey(id, context = {}) {
    try {
      return `${id}:${JSON.stringify(context || {})}`
    } catch (e) {
      return `${id}:`
    }
  }

  function stopTimer() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    pollingKey = ''
  }

  function clearPoll() {
    activeToken += 1
    stopTimer()
  }

  function applyResult(data) {
    const ret = (options.normalizeResult || normalizeAlgorithmResult)(data)
    if (ret && typeof ret === 'object' && Object.keys(ret).length) {
      options.result.value = ret
    }
  }

  function acceptTask(payload, context = {}) {
    const data = getApiPayload(payload)
    options.taskId.value = data.taskId || data.task_id || ''
    options.status.value = data.status || options.status.value || 'RUNNING'
    options.error.value = data.errorMessage || data.error_message || data.errMsg || data.err_msg || ''
    applyResult(data.result)
    if (!options.taskId.value) {
      options.onMissingTaskId?.()
      return
    }
    startPoll(options.taskId.value, context)
  }

  function isCurrentRun(id, token) {
    return token === activeToken && options.taskId.value === id
  }

  async function queryTask(id, silent = false, token = activeToken, context = {}) {
    if (!isCurrentRun(id, token)) return { done: true, failed: false }
    try {
      const response = await options.query(id, context)
      if (!isCurrentRun(id, token)) return { done: true, failed: false }
      pollFailures = 0
      const data = getApiPayload(response)
      options.status.value = data.status || options.status.value
      options.error.value = data.errorMessage || data.error_message || data.errMsg || data.err_msg || ''
      applyResult(data.result)
      if (doneStatuses.includes(options.status.value)) {
        stopTimer()
        options.onDone?.(options.status.value, data, context)
        if (options.status.value === 'FAILED' && options.error.value && !silent) {
          options.onError?.(options.error.value, context)
        }
        activeToken += 1
        return { done: true, failed: false }
      }
      return { done: false, failed: false }
    } catch (error) {
      if (!isCurrentRun(id, token)) return { done: true, failed: false }
      pollFailures += 1
      options.onPollError?.(getErrorMessage(error, '任务状态查询失败'), context, pollFailures)
      return { done: false, failed: true }
    }
  }

  function startPoll(id, context = {}) {
    const nextPollingKey = makePollingKey(id, context)
    if (timer && pollingKey === nextPollingKey && !doneStatuses.includes(options.status.value)) return
    clearPoll()
    pollingKey = nextPollingKey
    const token = activeToken
    pollFailures = 0
    const poll = async (silent) => {
      if (!isCurrentRun(id, token) || doneStatuses.includes(options.status.value)) return
      const state = await queryTask(id, silent, token, context)
      if (!isCurrentRun(id, token) || state?.done || doneStatuses.includes(options.status.value)) return
      const delay = state?.failed
        ? Math.min(maxBackoffMs, pollMs * (2 ** Math.min(pollFailures, 4)))
        : pollMs
      timer = setTimeout(() => poll(true), delay)
    }
    poll(false)
  }

  onBeforeUnmount(clearPoll)

  return {
    acceptTask,
    applyResult,
    clearPoll,
    getApiPayload,
    getErrorMessage,
    queryTask,
    startPoll
  }
}
