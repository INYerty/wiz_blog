import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArticlesView from '../views/ArticlesView.vue'
import ArticleDetailView from '../views/ArticleDetailView.vue'
import CategoriesView from '../views/CategoriesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/articles',
      name: 'ArticlesList',
      component: ArticlesView,
    },
    {
      path: '/articles/:id',
      name: 'ArticleDetail',
      component: ArticleDetailView,
    },
    {
      path: '/categories',
      name: 'Categories',
      component: CategoriesView,
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router