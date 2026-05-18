// pages/historyDetail/historyDetail.js
const app = getApp()

Page({

  data: {
    reservation_id: '', //预约id
    device_id: '', //设备id
    reservationDetailArr: [], //预约记录详情数据
    deviceDetailArr: [] //设备详情数据
  },

  onLoad(options) {
    this.setData({
      reservation_id: options.reservation_id,
      device_id: options.device_id
    })

    //获取预约记录详情
    this.getReservationDetail()

    //获取设备详情
    this.getDeviceDetail()
  },

  //获取预约记录详情
  getReservationDetail() {
    wx.request({
      url: app.getApiUrl('/getReservationDetail'),
      method: 'POST',
      data: {
        reservation_id: this.data.reservation_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '未获取到预约详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            reservationDetailArr: result.data
          })
        }
      },

      fail: (error) => {
        wx.hideLoading()
        console.error('获取预订信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },

  //获取设备详情
  getDeviceDetail() {
    wx.request({
      url: app.getApiUrl('/getDeviceDetail'),
      method: 'POST',
      data: {
        device_id: this.data.device_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '未获取到设备详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            deviceDetailArr: result.data
          })
        }
      },

      fail: (error) => {
        wx.hideLoading()
        console.error('获取设备信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },
})