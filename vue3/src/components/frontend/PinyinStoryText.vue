<template>
  <span class="pinyin-story-text">
    <template
      v-for="segment in segments"
      :key="segment.key"
    >
      <span v-if="segment.type === 'text'">{{ segment.text }}</span>
      <button
        v-else-if="segment.type === 'term'"
        type="button"
        class="pinyin-term"
        :class="{ 'is-active': activeTermId === segment.entry.id }"
        @click="$emit('select-term', segment.entry)"
      >
        <ruby>
          <span>{{ segment.text }}</span>
          <rt>{{ segment.pinyin }}</rt>
        </ruby>
      </button>
      <ruby
        v-else-if="segment.pinyin"
        class="pinyin-char"
      >
        <span>{{ segment.text }}</span>
        <rt>{{ segment.pinyin }}</rt>
      </ruby>
      <span v-else>{{ segment.text }}</span>
    </template>
  </span>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { buildStoryReadingSegments, ensureStoryPinyinReady } from '@/utils/storyPinyin'

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  glossary: {
    type: Array,
    default: () => []
  },
  fullPinyin: {
    type: Boolean,
    default: false
  },
  activeTermId: {
    type: String,
    default: ''
  }
})

defineEmits(['select-term'])

const pinyinReady = ref(false)

watch(
  () => props.fullPinyin,
  async (enabled) => {
    if (!enabled) {
      pinyinReady.value = false
      return
    }

    await ensureStoryPinyinReady()
    pinyinReady.value = true
  },
  { immediate: true }
)

const segments = computed(() => buildStoryReadingSegments(props.text, props.glossary, props.fullPinyin && pinyinReady.value))
</script>

<style scoped>
.pinyin-story-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.pinyin-story-text ruby {
  ruby-align: center;
}

.pinyin-story-text rt {
  font-size: 0.56em;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: 0.02em;
  color: #c7831c;
  user-select: none;
}

.pinyin-term {
  display: inline;
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  color: #2757c6;
  font: inherit;
  line-height: inherit;
  cursor: pointer;
  border-bottom: 1px dashed rgba(39, 87, 198, 0.45);
}

.pinyin-term:hover {
  color: #183b90;
}

.pinyin-term.is-active {
  background: rgba(39, 87, 198, 0.12);
  border-radius: 6px;
}

.pinyin-char {
  color: inherit;
}
</style>
