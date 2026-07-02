// pages/game/game.js
const app = getApp()

Page({
  data: {
    currentFloor: 1,
    todayLife: 5,
    speedCount: 3,
    todaySpeedScore: 0,
    maxFloor: 1
  },

  onShow() {
    this.loadGameData()
  },

  loadGameData() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    if (!openid) return
    wx.request({
      url: `${app.globalData.baseUrl}/game/tower/info`,
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          const d = res.data.data
          this.setData({
            currentFloor: d.currentFloor,
            todayLife: d.todayLife,
            speedCount: d.speedCount,
            todaySpeedScore: d.todaySpeedScore,
            maxFloor: d.maxFloor
          })
        }
      }
    })
  },

  startTower() {
    wx.navigateTo({ url: '/pages/tower/tower' })
  },

  startSpeed() {
    wx.navigateTo({ url: '/pages/speed/speed' })
  },

  goToRank() {
    wx.showToast({ title: '排行榜功能即将上线', icon: 'none' })
  }
})
