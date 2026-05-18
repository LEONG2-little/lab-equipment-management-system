// pages/faultReportSelectDevice/faultReportSelectDevice.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户ID
    reservationArr: [], //预约数据
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      user_id: userData.user_id
    })

    //搜索我预约的设备
    this.getMyReseration()
  },

  //搜索我预约的设备
  getMyReseration() {
    wx.request({
      url: app.getApiUrl('/getMyReservation'),
      method: 'POST',
      data: {
        user_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取我的预约信息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          const reservations = result.data

          //如果有预约数据，获取每个设备的详细信息
          if (reservations && reservations.length > 0) {
            this.getDeviceDetail(reservations)
          } else {
            this.setData({ reservationArr: [] })
          }
        }
      }
    })
  },

  //获取每个设备的详细信息
  getDeviceDetail(reservations) {
    const promises = reservations.map(reservation => {
      return new Promise((resolve) => {
        wx.request({
          url: app.getApiUrl('/getDeviceDetail'),
          method: 'POST',
          data: {
            device_id: reservation.device_id
          },
          header: {
            'content-type': 'application/json'
          },
          success: (res) => {
            const result = res.data
            if (result.status == 200) {
              const deviceDetail = result.data
              // 处理图片URL，拼接完整地址
              if (deviceDetail.image_url && !deviceDetail.image_url.startsWith('http://') && !deviceDetail.image_url.startsWith('https://')) {
                deviceDetail.image_url = app.globalData.imageBaseUrl + deviceDetail.image_url
              }
              resolve({
                ...reservation,
                device_detail: deviceDetail
              })
            } else {
              resolve(reservation)
            }
          },
          fail: () => {
            resolve(reservation)
          }
        })
      })
    })

    Promise.all(promises).then(reservationsWithDetail => {
      this.setData({
        reservationArr: reservationsWithDetail
      })
    })
  },

  //点击事件，前往故障信息填写页面（faultReportFillOut）
  toFaultReportFillOut(e) {
    const index = e.currentTarget.dataset.index
    const reservation = this.data.reservationArr[index]
    
    if (reservation) {
      const device_id = reservation.device_detail?.device_id || reservation.device_id
      const reservation_id = reservation.reservation_id

      wx.navigateTo({
        url: `/pages/faultReportFillOut/faultReportFillOut?user_id=${this.data.user_id}&device_id=${device_id}&reservation_id=${reservation_id}`,
      })
    }
  },
})