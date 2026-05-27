<template>
  <div class="articles-view">
    <div class="header">
      <h1 class="page-title">全部文章</h1>
      <div class="filter-bar">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索文章..."
          class="search-input"
          @input="fetchArticles"
        />
        <select v-model="categoryId" class="category-select" @change="fetchArticles">
          <option :value="null">全部分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </option>
        </select>
      </div>
    </div>

    <div class="articles-grid">
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
        @click="viewArticle(article.id)"
      />
    </div>

    <div v-if="articles.length === 0" class="empty-state">
      <p>暂无文章</p>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <button
        :disabled="pageNum <= 1"
        @click="changePage(pageNum - 1)"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button
        :disabled="pageNum >= totalPages"
        @click="changePage(pageNum + 1)"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import ArticleCard from '@/components/ArticleCard.vue'

const router = useRouter()
const articles = ref<any[]>([])
const categories = ref<any[]>([])
const keyword = ref('')
const categoryId = ref<number | null>(null)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

async function fetchArticles() {
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value

    const res = await axios.get('/api/articles', { params })
    if (res.data.code === 200) {
      articles.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
    }
  } catch (err) {
    console.error('Failed to load articles:', err)
  }
}

async function fetchCategories() {
  try {
    const res = await axios.get('/api/categories')
    if (res.data.code === 200) {
      categories.value = res.data.data || []
    }
  } catch (err) {
    console.error('Failed to load categories:', err)
  }
}

function changePage(page: number) {
  pageNum.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function viewArticle(id: number) {
  router.push({ name: 'ArticleDetail', params: { id } })
}

onMounted(() => {
  fetchArticles()
  fetchCategories()
})
</script>

<style scoped>
.articles-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 6rem 2rem 4rem;
  min-height: 100vh;
}

.header {
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1.5rem;
}

.filter-bar {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  padding: 0.75rem 1rem;
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #6366f1;
}

.category-select {
  padding: 0.75rem 1rem;
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 0.95rem;
  outline: none;
  cursor: pointer;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 0;
  color: #94a3b8;
  font-size: 1.1rem;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.page-btn {
  padding: 0.6rem 1.5rem;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 8px;
  color: #818cf8;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.5);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  color: #94a3b8;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
}
</style>