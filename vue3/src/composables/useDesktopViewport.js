import { onBeforeUnmount, onMounted, ref } from 'vue'

export function useDesktopViewport() {
  const mediaQuery = window.matchMedia('(min-width: 1024px)')
  const isDesktopViewport = ref(mediaQuery.matches)

  const updateViewport = (event) => {
    isDesktopViewport.value = event.matches
  }

  onMounted(() => mediaQuery.addEventListener('change', updateViewport))
  onBeforeUnmount(() => mediaQuery.removeEventListener('change', updateViewport))

  return isDesktopViewport
}
