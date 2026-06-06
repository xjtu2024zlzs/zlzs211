import axios from 'axios'

const pythonRequest = axios.create({
  baseURL: 'http://127.0.0.1:8090',
  timeout: 180000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 调用 Python FastAPI 溯源算法
export function runPythonTraceAlgorithm(data) {
  return pythonRequest({
    url: '/api/v1/trace/run',
    method: 'post',
    data
  })
}