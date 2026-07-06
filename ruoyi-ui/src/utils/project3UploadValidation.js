const DEFAULT_ALLOWED_EXTENSIONS = ['csv', 'txt']

export function validateUploadFile(file, options = {}) {
  const allowedExtensions = normalizeList(options.allowedExtensions || DEFAULT_ALLOWED_EXTENSIONS)
  const allowedMimeTypes = normalizeList(options.allowedMimeTypes || [])
  const maxSize = Number(options.maxSize || 0)
  if (!file) return { valid: false, reason: 'EMPTY_FILE' }
  if (!Number.isFinite(Number(file.size)) || Number(file.size) <= 0) {
    return { valid: false, reason: 'EMPTY_FILE' }
  }
  if (maxSize > 0 && Number(file.size) > maxSize) {
    return { valid: false, reason: 'FILE_TOO_LARGE' }
  }
  const extension = String(file.name || '').split('.').pop().toLowerCase()
  if (!allowedExtensions.includes(extension)) {
    return { valid: false, reason: 'INVALID_EXTENSION' }
  }
  if (allowedMimeTypes.length && file.type && !allowedMimeTypes.includes(String(file.type).toLowerCase())) {
    return { valid: false, reason: 'MIME_MISMATCH' }
  }
  return { valid: true, reason: '' }
}

export function validateUploadFiles(files, options = {}) {
  return (Array.isArray(files) ? files : []).map(file => ({
    file,
    ...validateUploadFile(file, options)
  }))
}

export function summarizeUploadValidation(files, options = {}) {
  const results = validateUploadFiles(files, options)
  return {
    valid: results.every(item => item.valid),
    total: results.length,
    failed: results.filter(item => !item.valid),
    results
  }
}

export function validateChunkMeta(meta = {}) {
  const chunkIndex = Number(meta.chunkIndex)
  const chunkCount = Number(meta.chunkCount)
  const fileSize = Number(meta.fileSize)
  const hash = String(meta.hash || meta.fileHash || '').trim()
  if (!hash) return { valid: false, reason: 'MISSING_HASH' }
  if (!Number.isFinite(fileSize) || fileSize <= 0) return { valid: false, reason: 'INVALID_SIZE' }
  if (!Number.isInteger(chunkIndex) || !Number.isInteger(chunkCount) || chunkCount <= 0) {
    return { valid: false, reason: 'INVALID_CHUNK_INDEX' }
  }
  if (chunkIndex < 0 || chunkIndex >= chunkCount) {
    return { valid: false, reason: 'CHUNK_INDEX_OUT_OF_RANGE' }
  }
  return { valid: true, reason: '' }
}

function normalizeList(values) {
  return (Array.isArray(values) ? values : [])
    .map(value => String(value || '').toLowerCase())
    .filter(Boolean)
}
