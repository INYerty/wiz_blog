<!--
  IntroAnimation.vue — 首页"剧场式"入场动画组件

  实现效果:
  1. 文字逐字浮现 (GSAP stagger + rotateX)
  2. 个人标签交错弹入 (back.out 弹性缓动)
  3. 副标题淡入 + 打字机光标
  4. 整体 2-3 秒, 使用 GSAP Timeline 精确编排

  使用方式:
  <IntroAnimation @complete="onIntroComplete" />
-->
<template>
  <section class="intro-container" :class="{ 'intro-done': isComplete }">
    <!-- 主标题 -->
    <h1 ref="titleRef" class="intro-title">
      <span
        v-for="(char, i) in titleChars"
        :key="i"
        class="char"
        :style="{ display: char === ' ' ? 'inline' : 'inline-block' }"
      >{{ char === ' ' ? '&nbsp;' : char }}</span>
    </h1>

    <!-- 个人标签 -->
    <div ref="tagsRef" class="intro-tags">
      <span v-for="tag in tags" :key="tag" class="tag-chip">
        {{ tag }}
      </span>
    </div>

    <!-- 副标题 (打字机效果) -->
    <p ref="subtitleRef" class="intro-subtitle">
      {{ displayText }}<span class="cursor" :class="{ blink: showCursor }">|</span>
    </p>

    <!-- 向下滚动提示 -->
    <div ref="scrollHintRef" class="scroll-hint">
      <svg class="scroll-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M7 13l5 5 5-5M7 6l5 5 5-5" />
      </svg>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTimeline, gsap } from '@/composables/useGsap'

const emit = defineEmits<{ complete: [] }>()

// ===== 数据 =====
const title = "Hi, I'm WizV"
const titleChars = computed(() => title.split(''))
const tags = ['Vue 3', 'Spring Boot', 'Redis', 'MySQL', 'GSAP']
const subtitle = 'Full-Stack Developer & Open Source Enthusiast'

// ===== Refs =====
const titleRef = ref<HTMLElement | null>(null)
const tagsRef = ref<HTMLElement | null>(null)
const subtitleRef = ref<HTMLElement | null>(null)
const scrollHintRef = ref<HTMLElement | null>(null)
const isComplete = ref(false)

// ===== 打字机效果 =====
const displayText = ref('')
const showCursor = ref(false)

function typeWriter(text: string, speed = 50): Promise<void> {
  return new Promise((resolve) => {
    let i = 0
    showCursor.value = true
    const timer = setInterval(() => {
      displayText.value += text[i]
      i++
      if (i >= text.length) {
        clearInterval(timer)
        resolve()
      }
    }, speed)
  })
}

// ===== GSAP Timeline 编排 =====
onMounted(() => {
  const tl = gsap.timeline({
    defaults: { ease: 'power3.out' },
    onComplete: async () => {
      // Timeline 结束后启动打字机
      await typeWriter(subtitle, 40)
      // 显示滚动提示
      gsap.to(scrollHintRef.value, { opacity: 1, y: 0, duration: 0.5 })
      // 发射完成事件
      isComplete.value = true
      emit('complete')
    },
  })

  // 第1段: 文字逐字浮现 (0s ~ 1s)
  tl.from(titleRef.value!.querySelectorAll('.char'), {
    y: 80,
    opacity: 0,
    stagger: 0.05,
    duration: 0.8,
    rotateX: -90,
    transformOrigin: 'center bottom',
    ease: 'power3.out',
  })

  // 第2段: 标签交错弹入 (0.7s ~ 1.5s, 与上段有 0.3s 重叠)
  tl.from(
    tagsRef.value!.querySelectorAll('.tag-chip'),
    {
      y: 40,
      opacity: 0,
      stagger: 0.12,
      duration: 0.6,
      scale: 0.5,
      ease: 'back.out(2)', // 弹性回弹, 数值越大弹跳越大
    },
    '-=0.3' // 提前 0.3s 开始, 形成连贯感
  )

  // 初始隐藏副标题和滚动提示
  gsap.set(subtitleRef.value!, { opacity: 0 })
  gsap.set(scrollHintRef.value!, { opacity: 0, y: 20 })

  // 副标题容器淡入 (打字机效果在 onComplete 中触发)
  tl.to(subtitleRef.value!, { opacity: 1, duration: 0.3 }, '-=0.2')
})
</script>

<style scoped>
.intro-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  color: #fff;
  overflow: hidden;
  perspective: 1000px;
}

.intro-title {
  font-size: clamp(2.5rem, 6vw, 5rem);
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-bottom: 1.5rem;
}

.char {
  will-change: transform, opacity;
}

.intro-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
  margin-bottom: 2rem;
}

.tag-chip {
  padding: 0.4rem 1.2rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 0.9rem;
  font-weight: 500;
  will-change: transform, opacity;
  transition: background 0.3s ease;
}

.tag-chip:hover {
  background: rgba(255, 255, 255, 0.2);
}

.intro-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.7);
  min-height: 1.5em;
}

.cursor {
  color: #60a5fa;
  font-weight: 100;
  margin-left: 2px;
}

.cursor.blink {
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.scroll-hint {
  position: absolute;
  bottom: 2rem;
}

.scroll-arrow {
  width: 2rem;
  height: 2rem;
  color: rgba(255, 255, 255, 0.4);
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-8px); }
  60% { transform: translateY(-4px); }
}

/* 入场完成后允许页面滚动 */
.intro-done {
  /* 可添加后续过渡 */
}
</style>
