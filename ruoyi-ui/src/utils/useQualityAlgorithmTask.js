import { onBeforeUnmount } from 'vue'

const DEFAULT_DONE = ['SUCCESS', 'FAILED', 'CANCELED']
const DEFAULT_POLL_MS = 3000
const DEFAULT_MAX_POLLS = 120

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
  const maxPolls = options.maxPolls || DEFAULT_MAX_POLLS
  let timer = null
  let pollCount = 0
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
      clearInterval(timer)
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
    if (!isCurrentRun(id, token)) return
    try {
      const response = await options.query(id, context)
      if (!isCurrentRun(id, token)) return
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
      }
    } catch (error) {
      if (!isCurrentRun(id, token)) return
      stopTimer()
      options.status.value = 'FAILED'
      options.error.value = getErrorMessage(error, '任务状态查询失败')
      if (!silent) options.onError?.(options.error.value, context)
      activeToken += 1
    }
  }

  function startPoll(id, context = {}) {
    const nextPollingKey = makePollingKey(id, context)
    if (timer && pollingKey === nextPollingKey && !doneStatuses.includes(options.status.value)) return
    clearPoll()
    pollingKey = nextPollingKey
    const token = activeToken
    pollCount = 0
    timer = setInterval(() => {
      if (!isCurrentRun(id, token)) {
        clearPoll()
        return
      }
      pollCount += 1
      if (doneStatuses.includes(options.status.value)) {
        clearPoll()
        return
      }
      if (pollCount >= maxPolls) {
        stopTimer()
        if (!isCurrentRun(id, token)) return
        options.status.value = 'FAILED'
        options.error.value = '任务状态查询超时'
        options.onError?.(options.error.value, context)
        activeToken += 1
        return
      }
      queryTask(id, true, token, context)
    }, pollMs)
    queryTask(id, false, token, context)
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
