// pages/historyLabDetail/historyLabDetail.js
const app = getApp()

Page({

  data: {
    lab_reservation_id: '', //预约id
    laboratory_id: '', //实验室id
    reservationDetailArr: {}, //预约记录详情数据（改为对象）
    labDetailArr: {} //实验室详情数据
  },

  onLoad(options) {
    this.setData({
      lab_reservation_id: options.lab_reservation_id,
      laboratory_id: options.laboratory_id
    })

    //获取预约记录详情
    this.getLabReservationDetail()

    //获取实验室详情
    this.getLaboratoryDetail()
  },

  //获取预约记录详情
  getLabReservationDetail() {
    wx.request({
      url: app.getApiUrl('/getLabReservationDetail'),
      method: 'POST',
      data: {
        lab_reservation_id: this.data.lab_reservation_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '未获取到预约详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          //后端返回的是数组，取第一个元素
          const reservationData = Array.isArray(result.data) ? result.data[0] : result.data
          this.setData({
            reservationDetailArr: reservationData || {}
          })
        }
      },

      fail: (error) => {
        wx.hideLoading()
        console.error('获取预约信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },

  //获取实验室详情
  getLaboratoryDetail() {
    wx.request({
      url: app.getApiUrl('/getLaboratoryDetail'),
      method: 'POST',
      data: {
        laboratory_id: this.data.laboratory_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '未获取到实验室详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            labDetailArr: result.data
          })
        }
      },

      fail: (error) => {
        wx.hideLoading()
        console.error('获取实验室信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },
})