<template>
  <text :class="['tag', tone]">{{ text || '-' }}</text>
</template>

<script>
export default {
  name: 'StatusTag',
  props: { text: String },
  computed: {
    tone() {
      const value = this.text || ''
      if (['active', 'ON_SALE', 'COMPLETED', 'HANDLED', '1'].includes(String(value))) return 'ok'
      if (['inactive', 'REMOVED', 'CANCELLED', 'REJECTED', '0'].includes(String(value))) return 'bad'
      return 'warn'
    }
  }
}
</script>

<style scoped>
.tag { display: inline-block; min-width: 58px; padding: 4px 8px; border-radius: 999px; text-align: center; font-size: 12px; }
.ok { background: #e7f6ee; color: #247247; }
.bad { background: #fdeceb; color: #b42318; }
.warn { background: #fff4da; color: #9a6700; }
</style>
