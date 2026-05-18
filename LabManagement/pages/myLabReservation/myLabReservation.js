// pages/myLabReservation/myLabReservation.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户id
    reservationArr: [], //预约信息数据
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      user_id: userData.user_id,
    })

    //获取未结束使用的预约信息
    this.getLabUseReservation()
  },

  //获取未结束使用的预约信息
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
            title: '获取我的场地预约信息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          const reservations = result.data

          //如果有预约数据
          if (reservations && reservations.length > 0) {
            //获取实验室详细信息
            this.getLabDetail(reservations)
          } else {
            this.setData({
              reservationArr: []
            })
          }
        }
      },
      fail: (error) => {
        console.error('获取场地预约信息失败:', error)
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },

  //获取实验室详细信息
  getLabDetail(reservations) {
    //遍历预约信息列表
    const promises = reservations.map(reservation => {
      //为每个预约创建一个Promise，分别获取每个预约实验室的详细信息
      return new Promise((resolve) => {
        wx.request({
          url: app.getApiUrl('/getLaboratoryDetail'), // 修改接口
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
              //合并预约信息和实验室详情
              resolve({
                ...reservation,
                lab_detail: result.data
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

    //等待所有实验室详情请求完成
    Promise.all(promises).then(reservationsWithDetail => {
      this.setData({
        reservationArr: reservationsWithDetail
      })
    })
  },

  //点击事件，归还场地
  returnLab(e) {
    const index = e.currentTarget.dataset.index
    const reservation = this.data.reservationArr[index];
    const lab_reservation_id = reservation.lab_reservation_id;

    wx.showModal({
      title: '取消预约',
      content: '确认要取消预约吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '提交中...',
          })
          
          wx.request({
            url: app.getApiUrl('/returnLab'),
            method: 'POST',
            data: {
              lab_reservation_id: lab_reservation_id
            },
            header: {
              'content-type': 'application/json'
            },
            success: (res) => {
              wx.hideLoading()
              const result = res.data

              if (result.status == 200) {
                wx.showToast({
                  title: '取消预约成功',
                  icon: 'success',
                  duration: 1500
                })
                //刷新页面
                setTimeout(() => {
                  this.onLoad()
                }, 1500)
              } else {
                wx.showToast({
                  title: result.message || '取消预约失败',
                  icon: 'error',
                  duration: 1500
                })
              }
            },
            fail: (error) => {
              wx.hideLoading()
              console.error('取消预约失败:', error)
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  //点击事件，跳转到预约详情页面
  toReservationDetail(e) {
    const index = e.currentTarget.dataset.index
    const reservation = this.data.reservationArr[index]
    const lab_reservation_id = reservation.lab_reservation_id
    const laboratory_id = reservation.lab_detail?.laboratory_id

    wx.navigateTo({
      url: `/pages/labReservationDetail/labReservationDetail?lab_reservation_id=${lab_reservation_id}&laboratory_id=${laboratory_id}`,
    })
  },
})