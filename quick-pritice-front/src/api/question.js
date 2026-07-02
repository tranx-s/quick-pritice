import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/', timeout: 15000 })

http.interceptors.response.use(
  res => res.data,
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)

export const questionApi = {
  page: (params) => http.get('/api/admin/question/page', { params }),
  getById: (id) => http.get(`/api/admin/question/${id}`),
  add: (data) => http.post('/api/admin/question', data),
  update: (id, data) => http.put(`/api/admin/question/${id}`, data),
  delete: (id) => http.delete(`/api/admin/question/${id}`),
  batchDelete: (ids) => http.delete('/api/admin/question/batch', { data: { ids } }),
  toggle: (id) => http.put(`/api/admin/question/${id}/toggle`),
  moduleTypes: () => http.get('/api/admin/question/module-types'),
  sources: () => http.get('/api/admin/question/sources'),
  stats: () => http.get('/api/admin/question/stats')
}
