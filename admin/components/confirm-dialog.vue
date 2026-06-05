<script>
export default {
  name: 'ConfirmDialog',
  methods: {
    confirm(content) {
      return new Promise(resolve => {
        uni.showModal({
          title: '二次确认',
          content,
          confirmColor: '#b42318',
          success: res => resolve(res.confirm)
        })
      })
    }
  }
}
</script>
