// pages/historySelect/historySelect.js
Page({

  data: {

  },

  //点击事件，跳转到设备历史记录页面（history）
  toHistory() {
    wx.navigateTo({
      url: '/pages/history/history',
    })
  },

  //点击事件，跳转到场地历史记录页面（historyLab）
  toHistoryLab() {
    wx.navigateTo({
      url: '/pages/historyLab/historyLab',
    })
  },
})