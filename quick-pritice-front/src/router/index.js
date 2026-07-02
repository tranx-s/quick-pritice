import { createRouter, createWebHistory } from 'vue-router'
import QuestionList from '@/views/QuestionList.vue'
import SpiderControl from '@/views/SpiderControl.vue'

const routes = [
  { path: '/', redirect: '/questions' },
  { path: '/questions', component: QuestionList },
  { path: '/spider', component: SpiderControl }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
