// pages/index/index.js
const app = getApp()

Page({
  data: {
    identity_type:'',
  },

  onLoad(options) {

    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      identity_type:userData.identity_type
    })

    // 故障上报成功
    if (options.faultReportSuccess === 'true') {
      setTimeout(() => {
        wx.showToast({
          title: options.message || '故障上报成功',
          icon: 'success',
          duration: 2000
        });
      }, 300);
    }

    //预约成功
    if (options.reservationSuccess === 'true') {
      setTimeout(() => {
        wx.showToast({
          title: options.message || '预约成功',
          icon: 'success',
          duration: 2000
        });
      }, 300);
    }

    //登录成功
    if (options.loginSuccess === 'true') {
      setTimeout(() => {
        wx.showToast({
          title: options.message || '登录成功',
          icon: 'success',
          duration: 2000
        });
      }, 300);
    }

  },

  //跳转到所有设备页面（allDevice）
  toAllDevice() {
    wx.navigateTo({
      url: '/pages/allDevice/allDevice',
    })
  },

  //跳转到故障上报页面（faultReport）
  toFaultReport() {

    if(this.data.identity_type == '学生'){
      wx.navigateTo({
        url: '/pages/faultReportSelectDevice/faultReportSelectDevice',
      })
    }else{
      wx.navigateTo({
        url: '/pages/faultReport/faultReport',
      })
    }
  },

  //跳转到预约设备页面（reservation）
  toReservation() {
    wx.navigateTo({
      url: '/pages/reservation/reservation',
    })
  },

  //跳转到预约实验室页面（ScanQRcode）
  toLabReservation() {
    wx.navigateTo({
      url: '/pages/labReservation/labReservation',
    })
  }
})