// pages/myManage/myManage.js
Page({

  data: {

  },

  //点击事件，跳转到我的管理设备页面（myDevice）
  toMyDevice() {
    wx.navigateTo({
      url: '/pages/myDevice/myDevice',
    })
  },

  //点击事件，跳转到我的管理场地页面（myLab）
  toMyLab() {
    wx.navigateTo({
      url: '/pages/myLab/myLab',
    })
  },

  //点击事件，跳转到设备借出情况页面（deviceBorrow）
  toDeviceBorrow() {
    wx.navigateTo({
      url: '/pages/deviceBorrow/deviceBorrow',
    })
  },

  //点击事件，跳转到场地借出情况页面（labBorrow）
  toLabBorrow() {
    wx.navigateTo({
      url: '/pages/labBorrow/labBorrow',
    })
  },

  //点击事件，跳转到设备故障处理页面（deviceFaultHandling）
  toDeviceFaultHandling() {
    wx.navigateTo({
      url: '/pages/deviceFaultHandling/deviceFaultHandling',
    })
  },

  //点击事件，跳转到场地故障处理页面（labFaultHandling）
  toLabFaultHandling(){
    wx.navigateTo({
      url: '/pages/labFaultHandling/labFaultHandling',
    })
  },
})