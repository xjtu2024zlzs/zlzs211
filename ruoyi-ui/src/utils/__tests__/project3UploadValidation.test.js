import { describe, expect, it } from 'vitest'
import {
  summarizeUploadValidation,
  validateChunkMeta,
  validateUploadFile,
  validateUploadFiles
} from '../project3UploadValidation'

describe('project3 upload validation utility', () => {
  const options = {
    allowedExtensions: ['csv', 'txt'],
    allowedMimeTypes: ['text/csv', 'text/plain'],
    maxSize: 1024
  }

  it('rejects empty, oversized, fake MIME and invalid extension files', () => {
    expect(validateUploadFile({ name: 'a.csv', size: 0, type: 'text/csv' }, options).reason).toBe('EMPTY_FILE')
    expect(validateUploadFile({ name: 'a.csv', size: 2048, type: 'text/csv' }, options).reason).toBe('FILE_TOO_LARGE')
    expect(validateUploadFile({ name: 'a.csv', size: 10, type: 'application/pdf' }, options).reason).toBe('MIME_MISMATCH')
    expect(validateUploadFile({ name: 'a.exe', size: 10, type: 'text/plain' }, options).reason).toBe('INVALID_EXTENSION')
  })

  it('reports partial failures for multiple files', () => {
    const results = validateUploadFiles([
      { name: 'a.csv', size: 10, type: 'text/csv' },
      { name: 'b.txt', size: 0, type: 'text/plain' },
      { name: 'c.csv', size: 10, type: 'application/pdf' }
    ], options)

    expect(results.map(item => item.valid)).toEqual([true, false, false])
    expect(summarizeUploadValidation(results.map(item => item.file), options).failed).toHaveLength(2)
  })

  it('validates chunk index, hash and file size metadata', () => {
    expect(validateChunkMeta({ hash: 'abc', fileSize: 100, chunkIndex: 0, chunkCount: 2 }).valid).toBe(true)
    expect(validateChunkMeta({ fileSize: 100, chunkIndex: 0, chunkCount: 2 }).reason).toBe('MISSING_HASH')
    expect(validateChunkMeta({ hash: 'abc', fileSize: 0, chunkIndex: 0, chunkCount: 2 }).reason).toBe('INVALID_SIZE')
    expect(validateChunkMeta({ hash: 'abc', fileSize: 100, chunkIndex: 2, chunkCount: 2 }).reason).toBe('CHUNK_INDEX_OUT_OF_RANGE')
    expect(validateChunkMeta({ hash: 'abc', fileSize: 100, chunkIndex: 0.5, chunkCount: 2 }).reason).toBe('INVALID_CHUNK_INDEX')
  })
})
