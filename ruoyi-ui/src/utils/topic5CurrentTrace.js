const TOPIC5_CURRENT_TRACE_KEY = 'topic5_current_trace'

export function setTopic5CurrentTrace(trace) {
  if (!trace || !trace.id) {
    return
  }

  localStorage.setItem(TOPIC5_CURRENT_TRACE_KEY, JSON.stringify({
    id: trace.id,
    traceNo: trace.traceNo,
    eventTime: trace.eventTime,
    aircraftNo: trace.aircraftNo,
    partName: trace.partName,
    problemType: trace.problemType,
    severityLevel: trace.severityLevel,
    status: trace.status,
    workflowStage: trace.workflowStage
  }))
}

export function getTopic5CurrentTrace() {
  const value = localStorage.getItem(TOPIC5_CURRENT_TRACE_KEY)

  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch (e) {
    return null
  }
}

export function getTopic5CurrentTraceId() {
  const trace = getTopic5CurrentTrace()
  return trace && trace.id ? trace.id : null
}

export function clearTopic5CurrentTrace() {
  localStorage.removeItem(TOPIC5_CURRENT_TRACE_KEY)
}