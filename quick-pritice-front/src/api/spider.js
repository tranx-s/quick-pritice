import axios from 'axios'

const http = axios.create({ baseURL: '/', timeout: 30000 })
http.interceptors.response.use(res => res.data, err => Promise.reject(err))

export const spiderApi = {
  start: (data) => http.post('/spider/scrape/start', data),
  status: (taskId) => http.get(`/spider/scrape/status/${taskId}`),
  tasks: () => http.get('/spider/scrape/tasks'),
  logs: (taskId) => http.get(`/spider/scrape/logs/${taskId}`)
}
