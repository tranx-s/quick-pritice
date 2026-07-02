// pages/tower/tower.js
const app = getApp()

Page({
  data: {
    phase: 'ready',        // ready | answering | result
    currentFloor: 1,
    todayLife: 5,
    maxFloor: 1,
    questions: [],
    currentIndex: 0,
    userAnswers: [],
    showAnalysis: false,
    isCorrect: false,
    floorResult: null,     // 本层结算结果
    loading: false
  },

  onLoad() {
    this.loadInfo()
  },

  loadInfo() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/tower/info`,
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          const d = res.data.data
          this.setData({
            currentFloor: d.currentFloor,
            todayLife: d.todayLife,
            maxFloor: d.maxFloor
          })
        }
      }
    })
  },

  startFloor() {
    if (this.data.todayLife <= 0) {
      wx.showToast({ title: '今日生命值已耗尽', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/tower/start`,
      method: 'POST',
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          const questions = res.data.data
          this.setData({
            questions,
            currentIndex: 0,
            userAnswers: new Array(questions.length).fill(''),
            showAnalysis: false,
            phase: 'answering',
            floorResult: null
          })
        } else {
          wx.showToast({ title: res.data.message, icon: 'none' })
        }
      },
      complete: () => this.setData({ loading: false })
    })
  },

  selectAnswer(e) {
    const answer = e.currentTarget.dataset.answer
    const { currentIndex, userAnswers } = this.data
    userAnswers[currentIndex] = answer
    this.setData({ userAnswers, showAnalysis: false })
  },

  toggleAnalysis() {
    const { currentIndex, userAnswers, questions } = this.data
    if (!userAnswers[currentIndex]) {
      wx.showToast({ title: '请先选择答案', icon: 'none' })
      return
    }
    const isCorrect = userAnswers[currentIndex] === questions[currentIndex].correctAnswer
    this.setData({ showAnalysis: !this.data.showAnalysis, isCorrect })
  },

  prevQuestion() {
    if (this.data.currentIndex > 0) {
      this.setData({ currentIndex: this.data.currentIndex - 1, showAnalysis: false })
    }
  },

  nextQuestion() {
    const { currentIndex, questions } = this.data
    if (currentIndex < questions.length - 1) {
      this.setData({ currentIndex: currentIndex + 1, showAnalysis: false })
    }
  },

  submitFloor() {
    const { userAnswers, questions } = this.data
    const unanswered = userAnswers.filter(a => !a).length
    if (unanswered > 0) {
      wx.showModal({
        title: '还有题目未作答',
        content: `还有 ${unanswered} 题未作答，未答题目将计为错误，确认提交？`,
        success: (res) => { if (res.confirm) this._doSubmit() }
      })
      return
    }
    this._doSubmit()
  },

  _doSubmit() {
    const { userAnswers, questions } = this.data
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    const answers = questions.map((q, i) => ({
      questionId: q.id,
      userAnswer: userAnswers[i] || 'X'
    }))
    wx.showLoading({ title: '结算中...' })
    wx.request({
      url: `${app.globalData.baseUrl}/game/tower/submit`,
      method: 'POST',
      data: { openid, answers },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          const d = res.data.data
          this.setData({
            phase: 'result',
            floorResult: d,
            currentFloor: d.currentFloor,
            todayLife: d.todayLife,
            maxFloor: d.maxFloor
          })
        }
      },
      fail: () => wx.hideLoading()
    })
  },

  nextFloor() {
    if (this.data.todayLife <= 0) {
      wx.showToast({ title: '今日生命值已耗尽，明日再战', icon: 'none' })
      this.setData({ phase: 'ready' })
      return
    }
    this.startFloor()
  },

  goBack() {
    wx.navigateBack()
  }
})
