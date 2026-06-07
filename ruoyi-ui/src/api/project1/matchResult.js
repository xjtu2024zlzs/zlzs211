import request from '@/utils/request'

export function listMatchResult(query) {
  return request({
    url: '/project1/matchResult/list',
    method: 'get',
    params: query
  })
}

export function getMatchResult(resultSetId) {
  return request({
    url: `/project1/matchResult/${resultSetId}`,
    method: 'get'
  })
}

export function setDefaultMatchResult(resultSetId) {
  return request({
    url: `/project1/matchResult/default/${resultSetId}`,
    method: 'post'
  })
}

export function getMatchResultDetail(resultSetId, query) {
  return request({
    url: `/project1/matchResult/detail/${resultSetId}`,
    method: 'get',
    params: query
  })
}

export function getMatchResultMetrics(resultSetId) {
  return request({
    url: `/project1/matchResult/metrics/${resultSetId}`,
    method: 'get'
  })
}

export function approveMatchResult(data) {
  return request({
    url: '/project1/matchResult/review/approve',
    method: 'post',
    data
  })
}

export function rejectMatchResult(data) {
  return request({
    url: '/project1/matchResult/review/reject',
    method: 'post',
    data
  })
}

export function autoApproveMatchResult(resultSetId) {
  return request({
    url: `/project1/matchResult/review/auto/${resultSetId}`,
    method: 'post'
  })
}

export function getReviewHistory(reviewId) {
  return request({
    url: `/project1/matchResult/review/history/${reviewId}`,
    method: 'get'
  })
}
