<!--
  NavBar.vue — 导航栏组件
  磨砂玻璃效果 + GSAP 入场动画
-->
<template>
  <header ref="navRef" class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="nav-logo">
        <span class="logo-icon">W</span>
        <span class="logo-text">Wiz_Blog</span>
      </router-link>

      <nav class="nav-links">
        <router-link
          v-for="link in navLinks"
          :key="link.path"
          :to="link.path"
          class="nav-link"
          active-class="nav-link-active"
        >
          {{ link.label }}
        </router-link>
      </nav>

      <div class="nav-actions">
        <button class="btn-theme" @click="toggleTheme">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
          </svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import gsap from 'gsap'

const navRef = ref<HTMLElement | null>(null)

const navLinks = [
  { path: '/', label: '首页' },
  { path: '/articles', label: '文章' },
  { path: '/categories', label: '分类' },
  // { path: '/about', label: '关于' },
]

function toggleTheme() {
  const currentTheme = document.documentElement.classList.contains('light') ? 'light' : 'dark'
  const newTheme = currentTheme === 'light' ? 'dark' : 'light'
  document.documentElement.classList.remove(currentTheme)
  if (newTheme === 'light') {
    document.documentElement.classList.add('light')
  }
  localStorage.setItem('theme', newTheme)
}

onMounted(() => {
  gsap.from(navRef.value, {
    y: -60,
    opacity: 0,
    duration: 0.6,
    ease: 'power2.out',
    delay: 0.2,
  })
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: color-mix(in srgb, var(--bg-primary) 80%, transparent);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 1.1rem;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 800;
  color: #fff;
}

.nav-links {
  display: flex;
  gap: 0.5rem;
}

.nav-link {
  padding: 0.4rem 1rem;
  border-radius: 8px;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: var(--text-primary);
  background: color-mix(in srgb, var(--text-primary) 5%, transparent);
}

.nav-link-active {
  color: var(--accent-primary);
  background: color-mix(in srgb, var(--accent-primary) 10%, transparent);
}

.btn-theme {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.4rem;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.btn-theme:hover {
  background: color-mix(in srgb, var(--text-primary) 5%, transparent);
  color: var(--text-primary);
}

.icon {
  width: 18px;
  height: 18px;
}
</style>
