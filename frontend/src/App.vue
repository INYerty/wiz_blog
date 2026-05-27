<!--
  App.vue — 根组件

  集成 PageTransition 实现全局页面切换动画,
  确保路由切换时淡入淡出, 永不白屏
-->
<template>
  <div id="app">
    <!-- 导航栏 -->
    <NavBar />

    <!-- 页面内容 (带切换动画) -->
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <PageTransition>
          <component :is="Component" :key="route.path" />
        </PageTransition>
      </router-view>
    </main>

    <!-- 全局滚动进度条 -->
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import PageTransition from '@/components/PageTransition.vue'
import NavBar from '@/components/NavBar.vue'

// ===== 主题初始化 =====
function initializeTheme() {
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme === 'light') {
    document.documentElement.classList.add('light');
  }
}
initializeTheme();

// ===== 滚动进度条 =====
const scrollProgress = ref(0)

function updateProgress() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  scrollProgress.value = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0
}

onMounted(() => {
  window.addEventListener('scroll', updateProgress, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateProgress)
})
</script>

<style>
/* 全局样式重置 */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  overflow-x: hidden;
  transition: background 0.3s, color 0.3s;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--accent-primary);
  opacity: 0.3;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  opacity: 0.5;
}
</style>

<style scoped>
.main-content {
  position: relative;
  min-height: 100vh;
}

/* 顶部滚动进度条 */
.scroll-progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--accent-primary), var(--accent-secondary));
  z-index: 9999;
  transition: width 0.1s linear;
  border-radius: 0 2px 2px 0;
}
</style>
