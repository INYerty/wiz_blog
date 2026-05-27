<!--
  ArticleCard.vue — 文章卡片组件 (含 3D 微交互)

  实现效果:
  1. 悬浮时 3D 倾斜 (Vanilla Tilt)
  2. 封面图缩放 + 光晕效果
  3. 点击时 GSAP Flip 共享元素过渡到详情页

  Props:
    article: 文章数据对象
-->
<template>
  <div
    ref="cardRef"
    class="article-card"
    :data-article-id="article.id"
    @click="handleClick"
    @mouseenter="onHover"
    @mouseleave="onLeave"
  >
    <!-- 封面图 -->
    <div class="card-cover-wrapper">
      <img
        :src="article.cover || defaultCover"
        :alt="article.title"
        class="card-cover"
        :data-flip-id="`cover-${article.id}`"
        loading="lazy"
      />
      <div class="card-overlay">
        <span class="card-category">{{ article.categoryName || '未分类' }}</span>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="card-body">
      <h3 class="card-title" :data-flip-id="`title-${article.id}`">
        {{ article.title }}
      </h3>
      <p class="card-summary">{{ article.summary }}</p>

      <!-- 底部信息栏 -->
      <div class="card-meta">
        <div class="meta-left">
          <img :src="article.authorAvatar || defaultAvatar" class="meta-avatar" alt="avatar" />
          <span class="meta-author">{{ article.authorName || '匿名' }}</span>
          <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
        </div>
        <div class="meta-right">
          <span class="meta-stat">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            {{ article.viewCount || 0 }}
          </span>
          <span class="meta-stat">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
            </svg>
            {{ article.likeCount || 0 }}
          </span>
          <span class="meta-stat">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            {{ article.commentCount || 0 }}
          </span>
        </div>
      </div>
    </div>

    <!-- 悬浮光效 -->
    <div ref="glowRef" class="card-glow" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { gsap, Flip } from '@/composables/useGsap'
import { useRouter } from 'vue-router'

interface Article {
  id: number
  title: string
  summary: string
  cover: string
  categoryName: string
  authorName: string
  authorAvatar: string
  createdAt: string
  viewCount: number
  likeCount: number
  commentCount: number
}

const props = defineProps<{ article: Article }>()
const emit = defineEmits<{ flip: [state: any] }>()

const router = useRouter()
const cardRef = ref<HTMLElement | null>(null)
const glowRef = ref<HTMLElement | null>(null)

const defaultCover = 'https://picsum.photos/600/400?random=' + props.article.id
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// ===== Vanilla Tilt 3D 效果 (手动实现, 无需额外库) =====
function onHover(e: MouseEvent) {
  if (!cardRef.value) return
  const card = cardRef.value
  const rect = card.getBoundingClientRect()

  // 鼠标跟踪光效
  card.addEventListener('mousemove', handleMouseMove)

  gsap.to(card, {
    rotateY: 5,
    rotateX: -5,
    scale: 1.02,
    duration: 0.4,
    ease: 'power2.out',
    transformPerspective: 1000,
  })
}

function handleMouseMove(e: MouseEvent) {
  if (!cardRef.value || !glowRef.value) return
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  // 光效跟随鼠标
  gsap.to(glowRef.value, {
    x: x - rect.width / 2,
    y: y - rect.height / 2,
    opacity: 0.15,
    duration: 0.3,
  })

  // 动态倾斜角度
  const rotateY = ((x / rect.width) - 0.5) * 10
  const rotateX = ((y / rect.height) - 0.5) * -10
  gsap.to(cardRef.value, {
    rotateY,
    rotateX,
    duration: 0.3,
    ease: 'power1.out',
  })
}

function onLeave() {
  if (!cardRef.value) return
  cardRef.value.removeEventListener('mousemove', handleMouseMove)

  gsap.to(cardRef.value, {
    rotateY: 0,
    rotateX: 0,
    scale: 1,
    duration: 0.5,
    ease: 'elastic.out(1, 0.5)', // 弹性回弹
  })

  if (glowRef.value) {
    gsap.to(glowRef.value, { opacity: 0, duration: 0.3 })
  }
}

// ===== 点击: 共享元素过渡 =====
function handleClick() {
  if (!cardRef.value) return
  // 捕获 Flip 状态
  const state = Flip.getState('[data-flip-id]')
  emit('flip', state)
  router.push({ name: 'ArticleDetail', params: { id: props.article.id } })
}

// ===== 工具函数 =====
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onUnmounted(() => {
  cardRef.value?.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.article-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: var(--bg-secondary);
  cursor: pointer;
  transform-style: preserve-3d;
  will-change: transform;
  transition: box-shadow 0.3s ease;
}

.article-card:hover {
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 60px color-mix(in srgb, var(--accent-primary) 10%, transparent);
}

.card-cover-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 16 / 9;
}

.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .card-cover {
  transform: scale(1.08);
}

.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 1rem;
  background: linear-gradient(transparent, color-mix(in srgb, var(--bg-secondary) 70%, black));
}

.card-category {
  padding: 0.2rem 0.8rem;
  background: color-mix(in srgb, var(--accent-primary) 80%, black);
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.card-body {
  padding: 1.25rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meta-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.meta-author {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.meta-date {
  font-size: 0.75rem;
  color: color-mix(in srgb, var(--text-secondary) 70%, transparent);
}

.meta-right {
  display: flex;
  gap: 1rem;
}

.meta-stat {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: color-mix(in srgb, var(--text-secondary) 70%, transparent);
}

.icon {
  width: 14px;
  height: 14px;
}

/* 鼠标跟踪光效 */
.card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--accent-primary) 40%, transparent), transparent 70%);
  pointer-events: none;
  opacity: 0;
  transform: translate(-50%, -50%);
  filter: blur(40px);
}
</style>
