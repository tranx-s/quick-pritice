<template>
  <el-config-provider :locale="zhCn">
    <el-container style="height: 100vh;">
      <el-aside width="200px" style="background: #1d2b3a;">
        <div class="logo">
          <span>⚡ 极速刷题管理</span>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#1d2b3a"
          text-color="#c0cbd8"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/questions">
            <el-icon><List /></el-icon>
            <span>题目管理</span>
          </el-menu-item>
          <el-menu-item index="/spider">
            <el-icon><Connection /></el-icon>
            <span>爬虫控制台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header style="background:#fff; border-bottom:1px solid #e8e8e8; line-height:60px; padding:0 20px; font-size:16px; font-weight:600; color:#303133;">
          {{ pageTitle }}
        </el-header>
        <el-main style="background:#f5f7fa; padding:20px;">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const route = useRoute()
const pageTitle = computed(() => {
  const map = { '/questions': '题目管理', '/spider': '爬虫控制台' }
  return map[route.path] || '极速刷题管理'
})
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border-bottom: 1px solid #2d3f52;
}
</style>
