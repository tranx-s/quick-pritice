// pages/speed/speed.js
const app = getApp()

Page({
  data: {
    phase: 'ready',      // ready | playing | finished
    speedCount: 3,
    todayBest: 0,
    questions: [],
    currentIndex: 0,
    timeLeft: 60,
    score: 0,
    correctCount: 0,
    totalAnswered: 0,
    lastCorrect: null,   // true/false/null 上一题反馈动画
    loading: false
  },

  _timer: null,

  onLoad() {
    this.loadInfo()
  },

  onUnload() {
    this._clearTimer()
  },

  loadInfo() {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/tower/info`,
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            speedCount: res.data.data.speedCount,
            todayBest: res.data.data.todaySpeedScore
          })
        }
      }
    })
  },

  startGame() {
    if (this.data.speedCount <= 0) {
      wx.showToast({ title: '今日次数已用完', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/speed/start`,
      method: 'POST',
      data: { openid },
      success: (res) => {
        if (res.data.code === 200) {
          const d = res.data.data
          this.setData({
            questions: d.questions,
            currentIndex: 0,
            speedCount: d.speedCount,
            todayBest: d.todayBest,
            timeLeft: 60,
            score: 0,
            correctCount: 0,
            totalAnswered: 0,
            lastCorrect: null,
            phase: 'playing'
          })
          this._startTimer()
        } else {
          wx.showToast({ title: res.data.message, icon: 'none' })
        }
      },
      complete: () => this.setData({ loading: false })
    })
  },

  _startTimer() {
    this._clearTimer()
    this._timer = setInterval(() => {
      const left = this.data.timeLeft - 1
      if (left <= 0) {
        this._clearTimer()
        this.setData({ timeLeft: 0 })
        this._finishGame()
      } else {
        this.setData({ timeLeft: left })
      }
    }, 1000)
  },

  _clearTimer() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  selectAnswer(e) {
    if (this.data.phase !== 'playing') return
    const answer = e.currentTarget.dataset.answer
    const { currentIndex, questions, score, correctCount, totalAnswered } = this.data
    const q = questions[currentIndex]
    const isCorrect = answer === q.correctAnswer

    const newScore = isCorrect ? score + 10 : score
    const newCorrect = isCorrect ? correctCount + 1 : correctCount
    const newTotal = totalAnswered + 1

    this.setData({
      score: newScore,
      correctCount: newCorrect,
      totalAnswered: newTotal,
      lastCorrect: isCorrect
    })

    // 短暂显示对错反馈后跳下一题
    setTimeout(() => {
      const nextIndex = currentIndex + 1
      if (nextIndex >= questions.length) {
        // 拉下一批题目
        this._fetchNextBatch(newScore, newCorrect, newTotal)
      } else {
        this.setData({ currentIndex: nextIndex, lastCorrect: null })
      }
    }, 350)
  },

  _fetchNextBatch(currentScore, currentCorrect, currentTotal) {
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/speed/next`,
      data: { openid },
      success: (res) => {
        if (res.data.code === 200 && this.data.phase === 'playing') {
          this.setData({
            questions: res.data.data,
            currentIndex: 0,
            lastCorrect: null,
            score: currentScore,
            correctCount: currentCorrect,
            totalAnswered: currentTotal
          })
        }
      }
    })
  },

  _finishGame() {
    this._clearTimer()
    const { score, correctCount, totalAnswered } = this.data
    this.setData({ phase: 'finished' })

    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/game/speed/finish`,
      method: 'POST',
      data: { openid, score, correctCount, totalAnswered },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ todayBest: res.data.data.todayBest })
        }
      }
    })
  },

  earlyFinish() {
    wx.showModal({
      title: '确认结束？',
      content: '提前结束将以当前得分结算',
      success: (res) => {
        if (res.confirm) this._finishGame()
      }
    })
  },

  goBack() { wx.navigateBack() },

  retry() {
    this.setData({ phase: 'ready' })
    this.loadInfo()
  }
})
