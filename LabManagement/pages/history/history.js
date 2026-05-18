// pages/history/history.js
const app = getApp()

Page({

  data: {
    user_id: '',//用户id
    reservationArr: []//预约历史记录数据
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      user_id: userData.user_id
    })

    //获取预约历史记录
    this.getMyReseration()
  },

  //获取预约历史记录
  getMyReseration() {
    wx.request({
      url: app.getApiUrl('/getMyReservationHistory'),
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
          }, 1500)
          return
        }

        if (result.status == 200) {
          const reservations = result.data

          //如果有预约数据
          if (reservations && reservations.length > 0) {

            //获取设备详细信息
            this.getDeviceDetail(reservations)
          } else {
            this.setData({
              reservationArr: []
            })
          }
        }
      }
    })
  },

  //获取设备详细信息
  getDeviceDetail(reservations) {

    //遍历所有预约历史记录
    const promises = reservations.map(reservation => {

      //为每个预约历史创建一个Promise，分别获取每个预约设备的详细信息
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

              //合并预约历史信息和设备详情
              resolve({
                ...reservation,
                device_detail: result.data
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

    //等待所有设备详情请求完成
    Promise.all(promises).then(reservationsWithDetail => {
      this.setData({
        reservationArr: reservationsWithDetail
      })
    })
  },

  //点击事件，跳转到预订历史的详情页面（historyDetail）
  toHistoryDetail(e){
    const index = e.currentTarget.dataset.index
    const reservation_id = this.data.reservationArr[index].reservation_id
    const device_id = this.data.reservationArr[index].device_detail.device_id

    wx.navigateTo({
      url: `/pages/historyDetail/historyDetail?reservation_id=${reservation_id}&device_id=${device_id}`,
    })
  },
})