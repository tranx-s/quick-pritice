// pages/module/module.js
const app = getApp()

const MODULE_CONFIG = {
  '言语理解': { icon: '📖', color: '#3b82f6', lightColor: '#eff6ff' },
  '数量关系': { icon: '🔢', color: '#8b5cf6', lightColor: '#f5f3ff' },
  '判断推理': { icon: '🧠', color: '#10b981', lightColor: '#ecfdf5' },
  '政治理论': { icon: '⚖️', color: '#f59e0b', lightColor: '#fffbeb' }
}

Page({
  data: {
    phase: 'select',      // select | answering | result
    modules: [],
    selectedModule: null,
    questions: [],
    currentIndex: 0,
    userAnswers: [],
    showAnalysis: false,
    isCorrect: false,
    result: null,
    loading: false
  },

  onLoad() {
    this.loadModules()
  },

  loadModules() {
    wx.showLoading({ title: '加载中...' })
    wx.request({
      url: `${app.globalData.baseUrl}/question/module/list`,
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          const modules = res.data.data.map(m => ({
            ...m,
            ...MODULE_CONFIG[m.moduleType]
          }))
          this.setData({ modules })
        }
      },
      fail: () => wx.hideLoading()
    })
  },

  selectModule(e) {
    const moduleType = e.currentTarget.dataset.type
    const count = e.currentTarget.dataset.count
    if (count === 0) {
      wx.showToast({ title: '该模块暂无题目', icon: 'none' })
      return
    }
    this.setData({ selectedModule: moduleType, loading: true })
    wx.request({
      url: `${app.globalData.baseUrl}/question/module`,
      data: { moduleType, count: 20 },
      success: (res) => {
        if (res.data.code === 200) {
          const questions = res.data.data
          this.setData({
            questions,
            currentIndex: 0,
            userAnswers: new Array(questions.length).fill(''),
            showAnalysis: false,
            phase: 'answering',
            result: null
          })
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

  submitPractice() {
    const { userAnswers, questions } = this.data
    const unanswered = userAnswers.filter(a => !a).length
    if (unanswered > 0) {
      wx.showModal({
        title: '还有题目未作答',
        content: `还有 ${unanswered} 题未作答，确认交卷？`,
        success: (res) => { if (res.confirm) this._doSubmit() }
      })
      return
    }
    this._doSubmit()
  },

  _doSubmit() {
    const { userAnswers, questions, selectedModule } = this.data
    const openid = app.globalData.openid || wx.getStorageSync('openid')

    let correctCount = 0
    questions.forEach((q, i) => {
      const ua = userAnswers[i] || ''
      if (ua === q.correctAnswer) {
        correctCount++
      } else {
        // 提交错题（答过的才记录）
        if (ua) {
          wx.request({
            url: `${app.globalData.baseUrl}/question/submit`,
            method: 'POST',
            data: { openid, questionId: q.id, userAnswer: ua }
          })
        }
      }
    })

    const total = questions.length
    const answered = userAnswers.filter(a => a).length
    const accuracy = answered > 0 ? ((correctCount / answered) * 100).toFixed(1) : 0

    this.setData({
      phase: 'result',
      result: { correctCount, total, answered, accuracy, selectedModule }
    })
  },

  practiceAgain() {
    this.setData({ phase: 'select' })
    this.loadModules()
  },

  goBack() { wx.navigateBack() }
})
