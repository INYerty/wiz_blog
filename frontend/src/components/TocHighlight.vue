<!--
  TocHighlight.vue — 文章目录 (TOC) 组件

  实现效果:
  1. 根据 Markdown 标题自动生成目录树
  2. 滚动时高亮当前章节, 高亮指示器带 GSAP 弹性滑动
  3. 点击目录项平滑滚动到对应位置

  Props:
    headings: 从 Markdown 解析出的标题数组
-->
<template>
  <nav class="toc" ref="tocRef">
    <h4 class="toc-title">目录</h4>

    <!-- 高亮指示器 (弹性滑动) -->
    <div ref="indicatorRef" class="toc-indicator" />

    <ul class="toc-list">
      <li
        v-for="(heading, index) in headings"
        :key="heading.id"
        :ref="(el) => setItemRef(el as HTMLElement, index)"
        class="toc-item"
        :class="[
          `toc-level-${heading.level}`,
          { active: activeIndex === index }
        ]"
        @click="scrollToHeading(heading.id, index)"
      >
        <span class="toc-text">{{ heading.text }}</span>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import gsap from 'gsap'

interface Heading {
  id: string
  text: string
  level: number // 1-6, 通常用 2-3
}

const props = defineProps<{ headings: Heading[] }>()

const tocRef = ref<HTMLElement | null>(null)
const indicatorRef = ref<HTMLElement | null>(null)
const itemRefs = ref<(HTMLElement | null)[]>([])
const activeIndex = ref(0)

let scrollHandler: (() => void) | null = null
let resizeObserver: ResizeObserver | null = null

function setItemRef(el: HTMLElement | null, index: number) {
  itemRefs.value[index] = el
}

// ===== 弹性滑动指示器 =====
function moveIndicator(index: number) {
  const item = itemRefs.value[index]
  const indicator = indicatorRef.value
  if (!item || !indicator || !tocRef.value) return

  const tocRect = tocRef.value.getBoundingClientRect()
  const itemRect = item.getBoundingClientRect()

  gsap.to(indicator, {
    y: itemRect.top - tocRect.top,
    height: itemRect.height,
    duration: 0.4,
    ease: 'elastic.out(1, 0.6)', // 橡皮筋弹性效果
  })
}

// ===== 滚动监听: 判断当前活跃标题 =====
function setupScrollSpy() {
  scrollHandler = () => {
    let currentIndex = 0
    for (let i = 0; i < props.headings.length; i++) {
      const el = document.getElementById(props.headings[i].id)
      if (el) {
        const rect = el.getBoundingClientRect()
        if (rect.top <= 100) {
          currentIndex = i
        }
      }
    }
    if (activeIndex.value !== currentIndex) {
      activeIndex.value = currentIndex
      moveIndicator(currentIndex)
    }
  }

  window.addEventListener('scroll', scrollHandler, { passive: true })
}

// ===== 点击目录项: 平滑滚动 =====
function scrollToHeading(id: string, index: number) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeIndex.value = index
    moveIndicator(index)
  }
}

// ===== 生命周期 =====
onMounted(async () => {
  await nextTick()
  setupScrollSpy()
  // 初始化指示器位置
  if (itemRefs.value[0]) {
    moveIndicator(0)
  }
})

onUnmounted(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler)
  }
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.toc {
  position: sticky;
  top: 100px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 1rem;
  border-left: 2px solid rgba(99, 102, 241, 0.1);
}

.toc::-webkit-scrollbar {
  width: 3px;
}

.toc::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 10px;
}

.toc-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 1rem;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
}

.toc-item {
  padding: 0.4rem 0 0.4rem 1rem;
  cursor: pointer;
  border-radius: 6px;
  transition: color 0.2s ease;
  position: relative;
}

.toc-level-2 { padding-left: 1rem; }
.toc-level-3 { padding-left: 2rem; font-size: 0.85rem; }
.toc-level-4 { padding-left: 3rem; font-size: 0.8rem; }

.toc-text {
  color: #94a3b8;
  font-size: 0.85rem;
  line-height: 1.5;
  transition: color 0.2s ease;
}

.toc-item:hover .toc-text {
  color: #e2e8f0;
}

.toc-item.active .toc-text {
  color: #818cf8;
  font-weight: 600;
}

/* 弹性高亮指示器 */
.toc-indicator {
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  background: linear-gradient(180deg, #818cf8, #6366f1);
  border-radius: 999px;
  pointer-events: none;
  will-change: transform, height;
}
</style>
