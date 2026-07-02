// pages/profile/profile.js
const app = getApp()

Page({
  data: {
    userInfo: {}
  },

  onShow() {
    this.loadUserInfo()
  },

  // 加载用户信息
  loadUserInfo() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    if (!openid) return

    wx.request({
      url: `${app.globalData.baseUrl}/user/info`,
      method: 'GET',
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            userInfo: res.data.data
          })
        }
      }
    })
  },

  // 获取用户信息
  getUserInfo(e) {
    if (e.detail.userInfo) {
      const { nickName, avatarUrl } = e.detail.userInfo
      const openid = app.globalData.openid || wx.getStorageSync('openid')

      // 更新用户信息
      wx.request({
        url: `${app.globalData.baseUrl}/user/update`,
        method: 'POST',
        data: {
          openid,
          nickname: nickName,
          avatar: avatarUrl
        },
        success: () => {
          this.loadUserInfo()
        }
      })
    }
  },

  // 前往错题本
  goToWrong() {
    wx.showToast({ title: '错题本功能开发中', icon: 'none' })
  },

  // 前往收藏
  goToCollect() {
    wx.showToast({ title: '收藏功能开发中', icon: 'none' })
  },

  // 前往会员中心
  goToVip() {
    wx.showToast({ title: '会员功能开发中', icon: 'none' })
  },

  // 意见反馈
  goToFeedback() {
    wx.showToast({ title: '反馈功能开发中', icon: 'none' })
  }
})
