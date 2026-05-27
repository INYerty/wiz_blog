<template>
  <div class="article-detail" v-if="article">
    <!-- 顶部封面 -->
    <div class="article-hero" ref="heroRef">
      <img :src="article.cover || defaultCover" class="hero-image" :alt="article.title" />
      <div class="hero-overlay">
        <div class="hero-content">
          <h1 ref="titleRef" class="article-title">{{ article.title }}</h1>
          <div ref="metaRef" class="article-meta">
            <img :src="defaultAvatar" class="meta-avatar" alt="avatar" />
            <span class="meta-author">{{ article.authorName || 'WizV' }}</span>
            <span class="meta-divider">·</span>
            <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
            <span class="meta-divider">·</span>
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
            <span class="meta-divider">·</span>
            <span class="meta-stat">
              <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              {{ article.commentCount || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="article-body">
      <div class="content-wrapper">
        <!-- 文章正文 -->
        <article class="article-content" ref="contentRef" v-html="renderedContent" />

        <!-- 侧边目录 -->
        <TocHighlight :content="article.content" />
      </div>
    </div>

    <!-- 底部互动区 -->
    <div class="article-footer">
      <div class="action-buttons">
        <button class="action-btn" :class="{ active: liked }" @click="handleLike">
          <svg class="icon" viewBox="0 0 24 24" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
          <span>{{ article.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" :class="{ active: collected }" @click="handleCollect">
          <svg class="icon" viewBox="0 0 24 24" :fill="collected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
          </svg>
          <span>{{ article.collectCount || 0 }}</span>
        </button>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <h3 class="section-title">评论 ({{ commentCount }})</h3>
      
      <!-- 评论输入框 -->
      <div class="comment-input-wrapper">
        <img :src="defaultAvatar" class="form-avatar" alt="avatar" />
        <div class="form-body">
          <div v-if="replyTo" class="reply-hint">
            回复 @{{ replyTo.username }}
            <button class="cancel-reply" @click="cancelReply">×</button>
          </div>
          <textarea
            v-model="commentContent"
            :placeholder="replyTo ? `回复 @${replyTo.username}...` : '写下你的想法...'"
            class="comment-input"
            rows="3"
          />
          <button class="submit-btn" @click="submitComment" :disabled="!commentContent.trim()">
            发表评论
          </button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <img :src="comment.userAvatar || defaultAvatar" class="comment-avatar" alt="avatar" />
            <div class="comment-info">
              <span class="comment-user">{{ comment.username || '匿名用户' }}</span>
              <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
            </div>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
          <button class="reply-btn" @click="setReplyTo(comment)">回复</button>
          
          <!-- 子评论 -->
          <div v-if="comment.children && comment.children.length > 0" class="sub-comments">
            <div v-for="sub in comment.children" :key="sub.id" class="sub-comment">
              <div class="comment-header">
                <img :src="sub.userAvatar || defaultAvatar" class="comment-avatar small" alt="avatar" />
                <div class="comment-info">
                  <span class="comment-user">{{ sub.username || '匿名用户' }}</span>
                  <span v-if="sub.replyToUsername" class="reply-to"> @{{ sub.replyToUsername }}</span>
                  <span class="comment-date">{{ formatDate(sub.createdAt) }}</span>
                </div>
              </div>
              <p class="comment-text">{{ sub.content }}</p>
              <button class="reply-btn" @click="setReplyTo(sub)">回复</button>
            </div>
          </div>
        </div>
        <p v-if="comments.length === 0" class="empty-comments">暂无评论，来说两句吧</p>
      </div>
    </div>
  </div>

  <div v-else class="loading">
    <p>加载中...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { gsap } from '@/composables/useGsap'
import TocHighlight from '@/components/TocHighlight.vue'

const route = useRoute()
const article = ref<any>(null)
const liked = ref(false)
const collected = ref(false)
const heroRef = ref<HTMLElement | null>(null)
const titleRef = ref<HTMLElement | null>(null)
const metaRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)

// 评论相关
const comments = ref<any[]>([])
const commentContent = ref('')
const replyTo = ref<any>(null)
const commentCount = computed(() => article.value?.commentCount || 0)

const defaultCover = 'https://picsum.photos/1200/600'
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=wizv'

// 使用后端渲染好的 HTML
const renderedContent = computed(() => {
  return article.value?.contentHtml || ''
})

// 获取文章详情
async function fetchArticle() {
  const id = route.params.id
  try {
    const res = await axios.get(`/api/articles/${id}`)
    if (res.data.code === 200) {
      article.value = res.data.data
      animateEntrance()
    }
  } catch (err) {
    console.error('Failed to load article:', err)
  }
}

// GSAP 入场动画
function animateEntrance() {
  const tl = gsap.timeline({ defaults: { ease: 'power3.out' } })
  
  tl.from(heroRef.value, {
    y: 60,
    opacity: 0,
    duration: 0.8,
  })
  
  tl.from(titleRef.value, {
    y: 40,
    opacity: 0,
    duration: 0.6,
  }, '-=0.4')
  
  tl.from(metaRef.value, {
    y: 20,
    opacity: 0,
    duration: 0.5,
  }, '-=0.3')
  
  tl.from(contentRef.value, {
    y: 40,
    opacity: 0,
    duration: 0.7,
  }, '-=0.2')
}

// 获取评论
async function fetchComments() {
  if (!article.value) return
  try {
    const res = await axios.get(`/api/comments/article/${article.value.id}`)
    if (res.data.code === 200) {
      comments.value = res.data.data || []
    }
  } catch (err) {
    console.error('Failed to load comments:', err)
  }
}

// 发表评论
async function submitComment() {
  if (!commentContent.value.trim() || !article.value) return
  try {
    const body: any = {
      articleId: article.value.id,
      userId: 1,
      content: commentContent.value.trim(),
    }
    if (replyTo.value) {
      body.parentId = replyTo.value.parentId && replyTo.value.parentId > 0
        ? replyTo.value.parentId
        : replyTo.value.id
      body.replyToUserId = replyTo.value.userId
    }
    const res = await axios.post('/api/comments', body)
    if (res.data.code === 200) {
      commentContent.value = ''
      replyTo.value = null
      article.value.commentCount = (article.value.commentCount || 0) + 1
      await fetchComments()
    }
  } catch (err) {
    console.error('Failed to submit comment:', err)
  }
}

function setReplyTo(comment: any) {
  replyTo.value = comment
  window.scrollTo({ top: document.querySelector('.comment-input-wrapper')?.getBoundingClientRect().top! + window.scrollY - 100, behavior: 'smooth' })
}

function cancelReply() {
  replyTo.value = null
}

function handleLike() {
  if (!article.value) return
  axios.post(`/api/articles/${article.value.id}/like`, null, {
    params: { userId: 1 }
  }).then(res => {
    if (res.data.code === 200) {
      liked.value = res.data.data
      article.value.likeCount = liked.value 
        ? (article.value.likeCount || 0) + 1 
        : Math.max(0, (article.value.likeCount || 0) - 1)
      gsap.fromTo('.action-btn:first-child', { scale: 1.2 }, { duration: 0.3, ease: 'back.out(2)' })
    }
  })
}

function handleCollect() {
  if (!article.value) return
  axios.post(`/api/articles/${article.value.id}/collect`, null, {
    params: { userId: 1 }
  }).then(res => {
    if (res.data.code === 200) {
      collected.value = res.data.data
      article.value.collectCount = collected.value 
        ? (article.value.collectCount || 0) + 1 
        : Math.max(0, (article.value.collectCount || 0) - 1)
      gsap.fromTo('.action-btn:nth-child(2)', { scale: 1.2 }, { duration: 0.3, ease: 'back.out(2)' })
    }
  })
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchArticle().then(() => fetchComments())
})
</script>

<style scoped>
.article-detail {
  min-height: 100vh;
  background: var(--bg-primary);
}

.article-hero {
  position: relative;
  height: 500px;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.4);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  padding: 4rem 2rem;
  background: linear-gradient(transparent 0%, color-mix(in srgb, var(--bg-primary) 95%, black) 100%);
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.article-title {
  font-size: clamp(2rem, 5vw, 3.5rem);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.2;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.meta-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.meta-divider {
  color: color-mix(in srgb, var(--text-secondary) 50%, transparent);
}

.meta-stat {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.icon {
  width: 16px;
  height: 16px;
}

.article-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 2rem;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 250px;
  gap: 3rem;
}

.article-content {
  color: var(--text-primary);
  line-height: 1.8;
  font-size: 1.05rem;
}

.article-content :deep(h1) {
  font-size: 2rem;
  font-weight: 700;
  margin: 2rem 0 1rem;
  color: var(--text-primary);
}

.article-content :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1.5rem 0 0.75rem;
  color: var(--text-primary);
}

.article-content :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 1.25rem 0 0.5rem;
  color: var(--text-primary);
}

.article-content :deep(p) {
  margin-bottom: 1rem;
}

.article-content :deep(code) {
  background: var(--bg-secondary);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
  color: var(--accent-secondary);
}

.article-content :deep(pre) {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.5rem 0;
}

.article-content :deep(blockquote) {
  border-left: 4px solid var(--accent-primary);
  padding-left: 1rem;
  margin: 1.5rem 0;
  color: var(--text-secondary);
  font-style: italic;
}

.article-footer {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  border-top: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: color-mix(in srgb, var(--accent-primary) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--accent-primary) 30%, transparent);
  border-radius: 8px;
  color: var(--accent-primary);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: color-mix(in srgb, var(--accent-primary) 20%, transparent);
  border-color: color-mix(in srgb, var(--accent-primary) 50%, transparent);
  transform: translateY(-2px);
}

.loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 1.2rem;
}

.action-btn.active {
  color: #ec4899;
  border-color: rgba(236, 72, 153, 0.5);
  background: rgba(236, 72, 153, 0.1);
}

/* ===== 评论区 ===== */
.comment-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 2rem;
}

.comment-input-wrapper {
  display: flex;
  gap: 1rem;
  margin-bottom: 2.5rem;
  padding: 1.5rem;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.form-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.form-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.reply-hint {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--accent-primary);
}

.cancel-reply {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 0.25rem;
  line-height: 1;
}

.cancel-reply:hover {
  color: #ef4444;
}

.comment-input {
  width: 100%;
  padding: 0.75rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.comment-input:focus {
  border-color: var(--accent-primary);
}

.submit-btn {
  align-self: flex-end;
  padding: 0.6rem 1.5rem;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  padding: 1.25rem;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-avatar.small {
  width: 28px;
  height: 28px;
}

.comment-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.comment-user {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.reply-to {
  font-size: 0.85rem;
  color: var(--accent-primary);
}

.comment-date {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.comment-text {
  color: var(--text-primary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 0.5rem;
}

.reply-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 0.8rem;
  cursor: pointer;
  padding: 0.2rem 0;
  transition: color 0.2s;
}

.reply-btn:hover {
  color: var(--accent-primary);
}

.sub-comments {
  margin-top: 1rem;
  padding-left: 1rem;
  border-left: 2px solid color-mix(in srgb, var(--accent-primary) 20%, transparent);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sub-comment {
  background: color-mix(in srgb, var(--bg-primary) 50%, transparent);
  padding: 1rem;
  border-radius: 8px;
}

.empty-comments {
  text-align: center;
  color: var(--text-secondary);
  padding: 3rem 0;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .article-hero {
    height: 300px;
  }
}
</style>