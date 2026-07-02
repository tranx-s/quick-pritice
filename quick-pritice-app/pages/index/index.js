// pages/index/index.js
const app = getApp()

Page({
  data: {
    todayQuestions: 0,
    totalQuestions: 0
  },

  onShow() {
    this.loadUserData()
  },

  // 加载用户数据
  loadUserData() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    if (!openid) return

    wx.request({
      url: `${app.globalData.baseUrl}/user/info`,
      method: 'GET',
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            todayQuestions: res.data.data.todayQuestions || 0,
            totalQuestions: res.data.data.totalQuestions || 0
          })
        }
      }
    })
  },

  // 前往极速速刷
  goToPractice() {
    wx.navigateTo({
      url: '/pages/practice/practice'
    })
  },

  // 前往刷题闯关
  goToGame() {
    wx.switchTab({
      url: '/pages/game/game'
    })
  },

  // 前往专项刷题
  goToModule() {
    wx.navigateTo({
      url: '/pages/module/module'
    })
  },

  // 前往错题本
  goToWrong() {
    wx.navigateTo({
      url: '/pages/wrong/wrong'
    })
  }
})
