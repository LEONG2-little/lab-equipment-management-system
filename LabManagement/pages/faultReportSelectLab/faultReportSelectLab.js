// pages/faultReportSelectLab/faultReportSelectLab.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户ID
    labReservationArr: [], //实验室预约数据
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      user_id: userData.user_id
    })

    //搜索我预约的实验室
    this.getLabUseReservation()
  },

  //搜索我预约的实验室
  getLabUseReservation() {
    wx.request({
      url: app.getApiUrl('/getLabUseReservation'),
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

          //如果有预约数据，获取每个实验室的详细信息
          if (reservations && reservations.length > 0) {
            this.getLaboratoryDetail(reservations)
          } else {
            this.setData({ labReservationArr: [] })
          }
        }
      }
    })
  },

  //获取每个实验室的详细信息
  getLaboratoryDetail(reservations) {
    const promises = reservations.map(reservation => {
      return new Promise((resolve) => {
        wx.request({
          url: app.getApiUrl('/getLaboratoryDetail'),
          method: 'POST',
          data: {
            laboratory_id: reservation.laboratory_id
          },
          header: {
            'content-type': 'application/json'
          },
          success: (res) => {
            const result = res.data
            if (result.status == 200) {
              const labDetail = result.data
              // 处理图片URL，拼接完整地址
              if (labDetail.image_url && !labDetail.image_url.startsWith('http://') && !labDetail.image_url.startsWith('https://')) {
                labDetail.image_url = app.globalData.imageBaseUrl + labDetail.image_url
              }
              resolve({
                ...reservation,
                lab_detail: labDetail
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
        labReservationArr: reservationsWithDetail
      })
    })
  },

  //点击事件，前往故障信息填写页面
  toFaultReportLabFillOut(e) {
    const index = e.currentTarget.dataset.index
    const reservation = this.data.labReservationArr[index]
    
    if (reservation) {
      const laboratory_id = reservation.lab_detail?.laboratory_id || reservation.laboratory_id
      const lab_reservation_id = reservation.lab_reservation_id

      wx.navigateTo({
        url: `/pages/faultReportLabFillOut/faultReportLabFillOut?user_id=${this.data.user_id}&laboratory_id=${laboratory_id}&lab_reservation_id=${lab_reservation_id}`,
      })
    }
  },
})