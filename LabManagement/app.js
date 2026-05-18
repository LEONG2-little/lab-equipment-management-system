// app.js
App({
  // 全局配置
  globalData: {
    userInfo: null,
    // 添加 API 基础地址配置
    //apiBaseUrl: 'http://localhost:8080',
    apiBaseUrl: 'https://c6f84486ee80eb86-113-105-10-60.serveousercontent.com',  // 开发环境
  },

  // 添加获取完整 API 地址的方法
  getApiUrl(endpoint) {
    return `${this.globalData.apiBaseUrl}${endpoint}`
  },

  onLaunch() {

    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      }
    })
  },
})