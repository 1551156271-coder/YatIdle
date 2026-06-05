<template>
  <view class="table-wrap">
    <slot />
  </view>
</template>

<script>
export default { name: 'DataTable' }
</script>

<style scoped>
.table-wrap { background: #fff; border: 1px solid #dfe5ec; border-radius: 8px; overflow: hidden; }
</style>
