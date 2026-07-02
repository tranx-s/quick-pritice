// pages/wrong/wrong.js
const app = getApp()

Page({
  data: {
    wrongList: [],
    loading: false,
    expandedId: null,   // 展开显示答案解析的错题 wrongId
  },

  onShow() {
    this.loadWrongList()
  },

  loadWrongList() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    if (!openid) return
    this.setData({ loading: true })
    wx.request({
      url: `${app.globalData.baseUrl}/question/wrong/list`,
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ wrongList: res.data.data || [] })
        }
      },
      complete: () => this.setData({ loading: false })
    })
  },

  toggleExpand(e) {
    const wrongId = e.currentTarget.dataset.wrongid
    this.setData({
      expandedId: this.data.expandedId === wrongId ? null : wrongId
    })
  },

  deleteWrong(e) {
    const questionId = e.currentTarget.dataset.questionid
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.showModal({
      title: '确认从错题本移除？',
      content: '移除后不影响历史答题记录',
      success: (res) => {
        if (!res.confirm) return
        wx.request({
          url: `${app.globalData.baseUrl}/question/wrong/${questionId}`,
          method: 'DELETE',
          data: { openid },
          success: (r) => {
            if (r.data.code === 200) {
              wx.showToast({ title: '已移除', icon: 'success' })
              this.loadWrongList()
            }
          }
        })
      }
    })
  },

  practiceWrong() {
    const { wrongList } = this.data
    if (!wrongList.length) {
      wx.showToast({ title: '错题本为空', icon: 'none' })
      return
    }
    // 取前20题错题ID随机练习，复用 practice 页
    wx.navigateTo({
      url: '/pages/practice/practice?mode=wrong'
    })
  }
})
