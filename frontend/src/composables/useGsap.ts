/**
 * useGsap.ts — GSAP 动画 Composable 工具集
 *
 * 封装 GSAP 常用操作, 让组件中调用动画像调用函数一样简单
 * 包含: timeline 管理、ScrollTrigger、文字逐字动画、弹性入场等
 */
import { onMounted, onUnmounted, type Ref } from 'vue'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { Flip } from 'gsap/Flip'

// 注册插件 (全局只需注册一次)
gsap.registerPlugin(ScrollTrigger, Flip)

/**
 * 使用 GSAP Timeline, 组件卸载时自动 kill
 */
export function useTimeline(
  setupFn: (tl: gsap.core.Timeline) => void,
  options?: gsap.TimelineVars
) {
  let tl: gsap.core.Timeline | null = null

  onMounted(() => {
    tl = gsap.timeline(options)
    setupFn(tl)
  })

  onUnmounted(() => {
    tl?.kill()
  })

  return { getTimeline: () => tl }
}

/**
 * 文字逐字浮现动画
 * @param target 包含 <span> 子元素的容器 ref
 * @param options 自定义参数
 */
export function useCharAnimation(
  target: Ref<HTMLElement | null>,
  options = {
    charDelay: 0.04,    // 每个字的延迟
    yDistance: 60,       // Y轴起始偏移
    duration: 0.7,       // 单字动画时长
    ease: 'power3.out' as gsap.EaseString,
  }
) {
  onMounted(() => {
    if (!target.value) return
    const chars = target.value.querySelectorAll('.char')
    gsap.from(chars, {
      y: options.yDistance,
      opacity: 0,
      stagger: options.charDelay,
      duration: options.duration,
      ease: options.ease,
      rotateX: -80,
      transformOrigin: 'center bottom',
    })
  })
}

/**
 * 交错入场动画 (适用于标签、卡片列表)
 */
export function useStaggerIn(
  target: Ref<HTMLElement | null>,
  selector: string,
  options = {
    y: 40,
    opacity: 0,
    scale: 0.6,
    stagger: 0.1,
    duration: 0.6,
    ease: 'back.out(1.7)' as gsap.EaseString,
  }
) {
  onMounted(() => {
    if (!target.value) return
    gsap.from(target.value.querySelectorAll(selector), {
      y: options.y,
      opacity: options.opacity,
      scale: options.scale,
      stagger: options.stagger,
      duration: options.duration,
      ease: options.ease,
    })
  })
}

/**
 * 滚动触发动画 (进入视口时淡入上移)
 */
export function useScrollReveal(
  target: Ref<HTMLElement | null>,
  options = {
    y: 50,
    opacity: 0,
    duration: 0.8,
    ease: 'power2.out' as gsap.EaseString,
    start: 'top 85%',
  }
) {
  let trigger: ScrollTrigger | null = null

  onMounted(() => {
    if (!target.value) return
    const el = target.value
    gsap.set(el, { y: options.y, opacity: options.opacity })
    trigger = ScrollTrigger.create({
      trigger: el,
      start: options.start,
      onEnter: () => {
        gsap.to(el, {
          y: 0,
          opacity: 1,
          duration: options.duration,
          ease: options.ease,
        })
      },
      once: true,
    })
  })

  onUnmounted(() => {
    trigger?.kill()
  })
}

export { gsap, ScrollTrigger, Flip }
