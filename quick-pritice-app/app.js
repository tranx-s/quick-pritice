// app.js
App({
  globalData: {
    openid: '',
    userInfo: null,
    baseUrl: 'http://localhost:8080/api'
  },

  onLaunch() {
    // 小程序启动时自动登录
    this.login()
  },

  // 微信登录
  login() {
    wx.login({
      success: (res) => {
        if (res.code) {
          // 发送 code 到后端换取 openid
          wx.request({
            url: `${this.globalData.baseUrl}/user/login`,
            method: 'POST',
            data: {
              code: res.code
            },
            success: (response) => {
              if (response.data.code === 200) {
                this.globalData.openid = response.data.data.openid
                this.globalData.userInfo = response.data.data
                wx.setStorageSync('openid', response.data.data.openid)
              }
            }
          })
        }
      }
    })
  }
})
