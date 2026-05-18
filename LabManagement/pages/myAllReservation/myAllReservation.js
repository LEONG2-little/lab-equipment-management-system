// pages/myAllReservation/myAllReservation.js
Page({

  data: {

  },

  //点击事件，跳转到我的预约设备页面（myReservation）
  toMyReservation() {
    wx.navigateTo({
      url: '/pages/myReservation/myReservation',
    })
  },

  //点击事件，跳转到我的预约场地页面（myLabReservation）
  toMyLabReservation() {
    wx.navigateTo({
      url: '/pages/myLabReservation/myLabReservation',
    })
  },
})