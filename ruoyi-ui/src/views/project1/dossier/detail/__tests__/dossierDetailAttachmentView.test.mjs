import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import assert from 'node:assert/strict'
import test from 'node:test'

const __dirname = dirname(fileURLToPath(import.meta.url))
const component = readFileSync(resolve(__dirname, '../index.vue'), 'utf8')

function extractFunction(name) {
  const start = component.indexOf(`function ${name}`)
  assert.notEqual(start, -1, `function ${name} should exist`)
  const braceStart = component.indexOf('{', start)
  let depth = 0
  for (let i = braceStart; i < component.length; i += 1) {
    const char = component[i]
    if (char === '{') {
      depth += 1
    } else if (char === '}') {
      depth -= 1
      if (depth === 0) {
        return component.slice(start, i + 1)
      }
    }
  }
  throw new Error(`function ${name} should be complete`)
}

const dateHelpers = Function(`
${extractFunction('hasPresentValue')}
${extractFunction('formatDateOnly')}
return { formatDateOnly }
`)()

test('attachment directory is presented as attachment materials', () => {
  assert.match(component, /function displayDirectoryLabel/)
  assert.doesNotMatch(component, /DOSSIER_ATTACHMENT:\s*'证明附件'/)
  assert.doesNotMatch(component, /return\s+'证明附件'/)
  assert.match(component, /return\s+'附件材料'/)
})

test('attachment summary cards are removed and the table is paginated', () => {
  assert.match(component, /const pagedDirectoryDocuments\s*=\s*computed/)
  assert.match(component, /<el-table\s+:data="pagedDirectoryDocuments"/)
  assert.match(component, /<el-pagination[\s\S]*:total="sortedDirectoryDocuments\.length"/)
  assert.match(component, /if\s*\(category\s*===\s*'documents'\)\s*\{\s*return\s+\[\]/)
})

test('attachment list can be filtered by relation and lifecycle stage', () => {
  assert.match(component, /const fileRelationOptions\s*=\s*computed/)
  assert.match(component, /const fileStageOptions\s*=\s*computed/)
  assert.match(component, /v-model:visible="fileRelationFilterVisible"/)
  assert.match(component, /v-model:visible="fileStageFilterVisible"/)
  assert.match(component, /column-filter-select/)
  assert.match(component, /\{\{\s*fileRelationFilter\s*\|\|\s*'与节点关系'\s*\}\}/)
  assert.match(component, /\{\{\s*fileStageFilter\s*\|\|\s*'业务阶段'\s*\}\}/)
  assert.doesNotMatch(component, /\{\{\s*fileRelationFilter\s*\|\|\s*'全部'\s*\}\}/)
  assert.doesNotMatch(component, /\{\{\s*fileStageFilter\s*\|\|\s*'全部'\s*\}\}/)
  assert.doesNotMatch(component, /filter-column-head/)
  assert.match(component, /function selectFileRelationFilter/)
  assert.match(component, /function selectFileStageFilter/)
  assert.match(component, /'设计定义'[\s\S]*'组成装配'[\s\S]*'制造工艺'[\s\S]*'检验验证'[\s\S]*'材料追溯'[\s\S]*'适航放行'[\s\S]*'变更影响'[\s\S]*'状态记录'[\s\S]*'故障维护'/)
  assert.match(component, /'设计'[\s\S]*'制造'[\s\S]*'检验'[\s\S]*'交付'[\s\S]*'服役'[\s\S]*'故障'[\s\S]*'维护'/)
  assert.doesNotMatch(component, /selectFileStageFilter\(documentStageLabel\(row\)\)/)
})

test('attachment list can be sorted by date in both directions', () => {
  assert.match(component, /const fileDateSort\s*=\s*ref/)
  assert.match(component, /function toggleFileDateSort/)
  assert.match(component, /function compareDocumentDate/)
  assert.match(component, /file-date-sort-button/)
  assert.match(component, /sort-triangle up/)
  assert.match(component, /sort-triangle down/)
  assert.doesNotMatch(component, /fileDateSortLabel/)
  assert.match(component, /filteredDirectoryDocuments/)
  assert.match(component, /sortedDirectoryDocuments/)
})

test('timeline can be collapsed and expanded without removing the timeline directory', () => {
  assert.match(component, /const timelineCollapsed\s*=\s*ref\(true\)/)
  assert.match(component, /function toggleTimelineCollapsed/)
  assert.match(component, /class="timeline-toggle-button"/)
  assert.match(component, /<el-timeline\s+v-show="!timelineCollapsed"/)
  assert.match(component, /timelineCollapsed\.value\s*=\s*true/)
})

test('attachment date displays epoch millisecond values as dates', () => {
  assert.equal(dateHelpers.formatDateOnly(1746979200000), '2025-05-12')
  assert.equal(dateHelpers.formatDateOnly('2026-01-22 09:00'), '2026-01-22')
})
