<!--
  PageTransition.vue — 页面切换动画包装器

  使用 GSAP JavaScript 钩子替代 Vue 原生 CSS Transition,
  实现更细腻的页面间淡入淡出 + 位移动画, 杜绝白屏闪烁。

  使用方式:
  <router-view v-slot="{ Component, route }">
    <PageTransition>
      <component :is="Component" :key="route.path" />
    </PageTransition>
  </router-view>
-->
<template>
  <Transition
    :css="false"
    mode="out-in"
    @before-leave="onBeforeLeave"
    @leave="onLeave"
    @before-enter="onBeforeEnter"
    @enter="onEnter"
    @after-enter="onAfterEnter"
  >
    <slot />
  </Transition>
</template>

<script setup lang="ts">
import gsap from 'gsap'

// ===== 离开动画: 淡出 + 上移 =====
function onBeforeLeave(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.willChange = 'opacity, transform'
  // 固定宽度防止水平滚动条出现导致页面压缩
  htmlEl.style.width = htmlEl.offsetWidth + 'px'
  document.body.style.overflowX = 'hidden'
}

function onLeave(el: Element, done: gsap.Callback) {
  gsap.to(el, {
    opacity: 0,
    y: -30,
    duration: 0.35,
    ease: 'power2.in',
    onComplete: done,
  })
}

// ===== 进入动画: 淡入 + 下移 =====
function onBeforeEnter(el: Element) {
  gsap.set(el, { opacity: 0, y: 30 })
}

function onEnter(el: Element, done: gsap.Callback) {
  gsap.to(el, {
    opacity: 1,
    y: 0,
    duration: 0.45,
    ease: 'power2.out',
    onComplete: done,
  })
}

function onAfterEnter(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.willChange = ''
  htmlEl.style.width = ''
  document.body.style.overflowX = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>
