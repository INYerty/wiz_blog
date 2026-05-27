<template>
  <div class="home-view">
    <IntroAnimation @complete="showArticles = true" />

    <div v-if="showArticles" class="articles-section">
      <h2 class="section-title">最新文章</h2>
      <div class="articles-grid">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @click="viewArticle(article.id)"
        />
      </div>
      <p v-if="articles.length === 0" class="empty-text">暂无文章</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import IntroAnimation from '@/components/IntroAnimation.vue'
import ArticleCard from '@/components/ArticleCard.vue'

const router = useRouter()

const showArticles = ref(false)
const articles = ref<any[]>([])

// 获取文章列表
axios.get('/api/articles?pageNum=1&pageSize=10').then(res => {
  if (res.data.code === 200) {
    articles.value = res.data.data?.records || []
  }
}).catch(err => {
  console.error('Failed to load articles:', err)
})

function viewArticle(id: number) {
  router.push({ name: 'ArticleDetail', params: { id } })
}
</script>

<style scoped>
.home-view {
  padding-top: 80px;
}

.articles-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 4rem 2rem;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
}

.empty-text {
  text-align: center;
  color: #94a3b8;
  padding: 4rem 0;
}
</style>