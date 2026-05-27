<template>
  <div class="categories-view">
    <canvas ref="canvasRef"></canvas>
    <div class="title">Categories</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Engine, Render, Runner, World, Bodies, Composite, Mouse, MouseConstraint } from 'matter-js'
import axios from 'axios'

interface Category {
  id: number;
  name: string;
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
const categories = ref<Category[]>([])

let engine: Engine;
let render: Render;
let runner: Runner;
let observer: MutationObserver;

// 动态色相值，用于生成适应背景的颜色
const hues = [220, 280, 320, 0, 160, 200, 40, 250, 100, 30];

async function fetchCategories() {
  try {
    const response = await axios.get('/api/categories');
    if (response.data.code === 200) {
      categories.value = response.data.data;
      initMatter();
    }
  } catch (error) {
    console.error('Failed to fetch categories:', error);
  }
}

function initMatter() {
  if (!canvasRef.value) return;

  const canvas = canvasRef.value;
  const width = window.innerWidth;
  const height = window.innerHeight;

  engine = Engine.create({
    gravity: { y: 0.2 },
  });

  render = Render.create({
    canvas: canvas,
    engine: engine,
    options: {
      width: width,
      height: height,
      wireframes: false,
      background: 'transparent',
    }
  });

  const wallOptions = {
    isStatic: true,
    render: {
      fillStyle: 'transparent',
    }
  };

  const ground = Bodies.rectangle(width / 2, height + 30, width, 60, wallOptions);
  const wallLeft = Bodies.rectangle(-30, height / 2, 60, height, wallOptions);
  const wallRight = Bodies.rectangle(width + 30, height / 2, 60, height, wallOptions);
  const ceiling = Bodies.rectangle(width / 2, -30, width, 60, wallOptions);
  World.add(engine.world, [ground, wallLeft, wallRight, ceiling]);

  const isLight = document.documentElement.classList.contains('light');

  categories.value.forEach((category, index) => {
    const radius = Math.max(40, category.name.length * 8 + 20);
    const x = Math.random() * (width - radius * 2) + radius;
    const y = Math.random() * (height / 2);
    const hue = hues[index % hues.length];

    const circle = Bodies.circle(x, y, radius, {
      label: 'CategoryCircle',
      restitution: 0.8,
      friction: 0.1,
      render: {
        sprite: {
          texture: createTextTexture(category.name, radius, hue, isLight),
        }
      }
    });
    // 附加自定义数据，方便后续主题切换时重绘
    (circle as any).customData = { text: category.name, radius, hue };
    World.add(engine.world, circle);
  });

  const mouse = Mouse.create(render.canvas);
  const mouseConstraint = MouseConstraint.create(engine, {
    mouse: mouse,
    constraint: {
      stiffness: 0.2,
      render: {
        visible: false
      }
    }
  });
  World.add(engine.world, mouseConstraint);
  render.mouse = mouse;

  runner = Runner.create();
  Runner.run(runner, engine);
  Render.run(render);

  window.addEventListener('resize', handleResize);
}

function updateTextures() {
  if (!engine || !render) return;
  const isLight = document.documentElement.classList.contains('light');

  Composite.allBodies(engine.world).forEach(body => {
    if (body.label === 'CategoryCircle') {
      const data = (body as any).customData;
      if (data) {
        // 清理旧材质缓存避免内存泄漏
        if (body.render.sprite && body.render.sprite.texture && render.textures) {
          delete render.textures[body.render.sprite.texture];
        }
        // 生成并应用新主题的材质
        if (body.render.sprite) {
          body.render.sprite.texture = createTextTexture(data.text, data.radius, data.hue, isLight);
        }
      }
    }
  });
}

function createTextTexture(text: string, radius: number, hue: number, isLight: boolean): string {
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  const size = radius * 2;
  canvas.width = size;
  canvas.height = size;

  if (context) {
    // 动态颜色：根据主题推断
    // 浅色主题使用更透明、清新的背景和较深的文字
    // 深色主题使用有幽幽发光感的半透明背景和明亮的文字
    const fillAlpha = isLight ? 0.15 : 0.2;
    const strokeAlpha = isLight ? 0.3 : 0.5;
    const textColor = isLight ? `hsl(${hue}, 70%, 30%)` : `hsl(${hue}, 70%, 80%)`;

    // 绘制半透明融入背景的圆
    context.fillStyle = `hsla(${hue}, 70%, 50%, ${fillAlpha})`;
    context.beginPath();
    context.arc(radius, radius, radius - 2, 0, 2 * Math.PI);
    context.fill();

    // 绘制柔和边框
    context.lineWidth = 3;
    context.strokeStyle = `hsla(${hue}, 70%, 50%, ${strokeAlpha})`;
    context.stroke();

    // 绘制文字
    context.fillStyle = textColor;
    context.font = `bold ${radius / 2.8}px Inter, sans-serif`;
    context.textAlign = 'center';
    context.textBaseline = 'middle';
    context.fillText(text, radius, radius);
  }
  return canvas.toDataURL();
}

function handleResize() {
  if (!render) return;
  const width = window.innerWidth;
  const height = window.innerHeight;
  render.canvas.width = width;
  render.canvas.height = height;
  render.options.width = width;
  render.options.height = height;

  // Update wall positions
  const ground = Composite.allBodies(engine.world).find(body => body.label === 'Rectangle Body' && body.position.y > height - 60);
  if (ground) {
    // Matter.js does not directly support resizing bodies, so we remove and re-add
  }
  // This part is tricky with Matter.js, a simpler approach is to restart on resize
  cleanup();
  initMatter();
}

function cleanup() {
  if (render) {
    Render.stop(render);
    World.clear(engine.world, false);
    Engine.clear(engine);
    render.canvas.remove();
    render.textures = {};
  }
  if (runner) {
    Runner.stop(runner);
  }
  window.removeEventListener('resize', handleResize);
}

onMounted(() => {
  fetchCategories();

  // 监听 <html> 的 class 变化来实现无缝主题切换
  observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === 'class') {
        updateTextures();
      }
    });
  });
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
  cleanup();
});
</script>

<style scoped>
.categories-view {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: var(--bg-primary);
  transition: background-color 0.3s ease;
}

canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.title {
  position: absolute;
  top: 40px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 3rem;
  font-weight: bold;
  color: var(--text-primary);
  pointer-events: none;
  transition: color 0.3s ease;
}
</style>