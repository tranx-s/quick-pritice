// pages/game/game.js
const app = getApp()

Page({
  data: {
    currentFloor: 1,
    todayLife: 5,
    speedCount: 3,
    todaySpeedScore: 0
  },

  onShow() {
    // TODO: 加载游戏数据
    this.loadGameData()
  },

  loadGameData() {
    // 暂时使用模拟数据，后续对接后端
    this.setData({
      currentFloor: 1,
      todayLife: 5,
      speedCount: 3,
      todaySpeedScore: 0
    })
  },

  // 开始闯关爬塔
  startTower() {
    wx.showToast({
      title: '闯关爬塔功能开发中',
      icon: 'none'
    })
  },

  // 开始60秒竞速
  startSpeed() {
    wx.showToast({
      title: '竞速模式开发中',
      icon: 'none'
    })
  },

  // 前往排行榜
  goToRank() {
    wx.showToast({
      title: '排行榜功能开发中',
      icon: 'none'
    })
  }
})
