const PIPELINE_CURRENT_KEY = "topic4_current_pipeline_id"
const PIPELINE_DATA_PREFIX = "topic4_pipeline_"

export function createTopic4Pipeline(datasetInfo = {}) {
  const pipelineId = "P-" + Date.now()

  const pipeline = {
    pipelineId,
    pipelineCode: pipelineId,
    datasetId: datasetInfo.datasetId || 1,
    datasetName: datasetInfo.datasetName || "CWRU轴承故障数据集",
    currentStage: "PREPROCESSED",
    status: "运行中",
    createTime: formatDateTime(new Date()),
    processedSamples: [],
    augmentResults: [],
    fusionResults: [],
    diagnosisResults: [],
    rootCauseResults: []
  }

  sessionStorage.setItem(PIPELINE_CURRENT_KEY, pipelineId)
  saveTopic4Pipeline(pipeline)

  return pipeline
}

export function getCurrentTopic4PipelineId() {
  return sessionStorage.getItem(PIPELINE_CURRENT_KEY)
}

export function saveTopic4Pipeline(pipeline) {
  if (!pipeline || !pipeline.pipelineId) {
    return
  }

  sessionStorage.setItem(
      PIPELINE_DATA_PREFIX + pipeline.pipelineId,
      JSON.stringify(pipeline)
  )

  sessionStorage.setItem(PIPELINE_CURRENT_KEY, pipeline.pipelineId)
}

export function getTopic4Pipeline(pipelineId) {
  const id = pipelineId || getCurrentTopic4PipelineId()

  if (!id) {
    return null
  }

  const raw = sessionStorage.getItem(PIPELINE_DATA_PREFIX + id)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function updateTopic4Pipeline(pipelineId, patch = {}) {
  const pipeline = getTopic4Pipeline(pipelineId)

  if (!pipeline) {
    return null
  }

  const nextPipeline = {
    ...pipeline,
    ...patch,
    updateTime: formatDateTime(new Date())
  }

  saveTopic4Pipeline(nextPipeline)

  return nextPipeline
}

export function getPipelineStageData(pipelineId, stageKey) {
  const pipeline = getTopic4Pipeline(pipelineId)

  if (!pipeline) {
    return []
  }

  return pipeline[stageKey] || []
}

export function formatDateTime(date) {
  const pad = value => String(value).padStart(2, "0")

  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}