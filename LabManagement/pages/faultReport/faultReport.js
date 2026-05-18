// pages/faultReport/faultReport.js
Page({

  data: {

  },

  onLoad() {

  },

  //跳转到设备故障上报页面
  toFaultReportDevice() {
    wx.navigateTo({
      url: '/pages/faultReportSelectDevice/faultReportSelectDevice',
    })
  },

  //跳转到场地故障上报页面
  toFaultReportLab() {
    wx.navigateTo({
      url: '/pages/faultReportSelectLab/faultReportSelectLab',
    })
  },

})