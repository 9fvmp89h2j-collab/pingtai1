<template>
  <div class="meridian-compass">
    <svg
      class="meridian-compass__svg"
      viewBox="0 0 260 260"
      role="img"
      aria-label="经络星图罗盘"
    >
      <circle
        cx="130"
        cy="130"
        r="106"
        class="ring"
      />
      <circle
        cx="130"
        cy="130"
        r="70"
        class="inner-ring"
      />
      <path
        d="M130 42 L194 92 L170 170 L90 170 L66 92 Z"
        class="route"
      />
      <g
        v-for="node in nodes"
        :key="node.id"
        class="node"
        :class="{ completed: node.completed }"
        @click="$emit('select', node)"
      >
        <circle
          :cx="node.x"
          :cy="node.y"
          r="13"
        />
        <text
          :x="node.x"
          :y="node.y + 4"
          text-anchor="middle"
        >
          {{ node.shortName }}
        </text>
      </g>
    </svg>
  </div>
</template>

<script setup>
defineEmits(['select'])

defineProps({
  nodes: {
    type: Array,
    default: () => [
      { id: 'story', shortName: '故', name: '故事竹林', x: 130, y: 42, completed: true },
      { id: 'body', shortName: '身', name: '身体山谷', x: 194, y: 92, completed: false },
      { id: 'meridian', shortName: '络', name: '经络星河', x: 170, y: 170, completed: false },
      { id: 'copper', shortName: '铜', name: '小铜人馆', x: 90, y: 170, completed: false },
      { id: 'safety', shortName: '安', name: '艾草安全屋', x: 66, y: 92, completed: true }
    ]
  }
})
</script>

<style scoped>
.meridian-compass {
  width: min(100%, 360px);
  aspect-ratio: 1;
}

.meridian-compass__svg {
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, #fffdf3 0%, #f5efe0 72%, #e8d7b0 100%);
  border-radius: 50%;
}

.ring,
.inner-ring {
  fill: none;
  stroke: #ad8b00;
  stroke-width: 2;
}

.route {
  fill: none;
  stroke: #91caff;
  stroke-linejoin: round;
  stroke-width: 4;
}

.node {
  cursor: pointer;
}

.node circle {
  fill: #fff;
  stroke: #8c8c8c;
  stroke-width: 2;
}

.node text {
  fill: #4a3217;
  font-size: 12px;
  font-weight: 700;
  pointer-events: none;
}

.node.completed circle {
  fill: #fff7e6;
  stroke: #faad14;
  filter: drop-shadow(0 0 6px rgba(250, 173, 20, 0.75));
}
</style>
