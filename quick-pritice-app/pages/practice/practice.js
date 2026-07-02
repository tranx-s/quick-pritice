// pages/practice/practice.js
const app = getApp()

Page({
  data: {
    questions: [],
    currentIndex: 0,
    userAnswers: [],
    showAnalysis: false,
    isCorrect: false,
    showTimer: false,
    timeLeft: 600, // 10分钟倒计时
    progress: 0
  },

  onLoad() {
    this.loadQuestions()
  },

  // 加载题目
  loadQuestions() {
    wx.showLoading({ title: '加载中...' })

    wx.request({
      url: `${app.globalData.baseUrl}/question/random`,
      method: 'GET',
      data: {
        count: 10
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          const questions = res.data.data
          this.setData({
            questions,
            userAnswers: new Array(questions.length).fill('')
          })
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },

  // 选择答案
  selectAnswer(e) {
    const answer = e.currentTarget.dataset.answer
    const { currentIndex, userAnswers } = this.data

    userAnswers[currentIndex] = answer
    this.setData({
      userAnswers,
      showAnalysis: false
    })

    this.updateProgress()
  },

  // 更新进度
  updateProgress() {
    const { userAnswers } = this.data
    const answeredCount = userAnswers.filter(a => a).length
    const progress = (answeredCount / userAnswers.length) * 100
    this.setData({ progress })
  },

  // 查看解析
  showAnswerAnalysis() {
    const { currentIndex, userAnswers, questions } = this.data
    const userAnswer = userAnswers[currentIndex]
    const correctAnswer = questions[currentIndex].correctAnswer
    const isCorrect = userAnswer === correctAnswer

    this.setData({
      showAnalysis: true,
      isCorrect
    })

    // 提交答案到后端
    const openid = app.globalData.openid || wx.getStorageSync('openid')
    wx.request({
      url: `${app.globalData.baseUrl}/question/submit`,
      method: 'POST',
      data: {
        openid,
        questionId: questions[currentIndex].id,
        userAnswer
      }
    })
  },

  // 上一题
  prevQuestion() {
    const { currentIndex } = this.data
    if (currentIndex > 0) {
      this.setData({
        currentIndex: currentIndex - 1,
        showAnalysis: false
      })
    }
  },

  // 下一题
  nextQuestion() {
    const { currentIndex, questions } = this.data
    if (currentIndex < questions.length - 1) {
      this.setData({
        currentIndex: currentIndex + 1,
        showAnalysis: false
      })
    }
  },

  // 交卷
  submitPractice() {
    const { userAnswers, questions } = this.data

    // 统计正确率
    let correctCount = 0
    questions.forEach((q, index) => {
      if (userAnswers[index] === q.correctAnswer) {
        correctCount++
      }
    })

    const accuracy = ((correctCount / questions.length) * 100).toFixed(1)

    wx.showModal({
      title: '本次练习完成',
      content: `正确率: ${accuracy}%\n正确: ${correctCount}题\n错误: ${questions.length - correctCount}题`,
      confirmText: '返回首页',
      success: (res) => {
        if (res.confirm) {
          wx.switchTab({
            url: '/pages/index/index'
          })
        }
      }
    })
  },

  // 格式化时间
  formatTime(seconds) {
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    return `${min}:${sec < 10 ? '0' : ''}${sec}`
  }
})
