<template>
  <aside class="safety-bubble" :aria-label="label">
    <span class="moxa-guide"><i>{{ mark }}</i></span>
    <p><strong>{{ title }}</strong>{{ text }}</p>
  </aside>
</template>

<script setup>
const label = '\u5b89\u5168\u63d0\u9192'
const mark = '\u827e'
const title = '\u827e\u8349\u5c0f\u7cbe\u7075\u63d0\u9192\uff1a'
const text = '\u53ea\u5b66\u4e60\u6587\u5316\u548c\u8eab\u4f53\u8ba4\u77e5\uff0c\u4e0d\u53ef\u4ee5\u81ea\u5df1\u9488\u523a\uff0c\u4e0d\u53ef\u4ee5\u6a21\u4eff\u6cbb\u7597\u3002\u8eab\u4f53\u4e0d\u8212\u670d\u8981\u544a\u8bc9\u5bb6\u957f\u548c\u533b\u751f\u3002'
</script>
