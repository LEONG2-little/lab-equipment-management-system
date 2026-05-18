// pages/deviceDetail/deviceDetail.js
const app = getApp()

Page({

  data: {
    username: '', //用户名
    device_id: '', //设备id
    reservation_id: '', //预约id
    deviceArr: [], //设备信息数据
    reservationArr: [], //预约信息数据
    showCancelDialog: false,
    cancelReason: ''
  },

  onLoad(options) {
    this.setData({
      device_id: options.device_id,
      reservation_id: options.reservation_id,
      username: options.username
    })

    //获取设备详细信息
    this.getDeviceDetail()
  },

  //获取设备详细信息
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

        if (result.status == 200) {

          this.setData({
            deviceArr: result.data
          })

          //获取预约详细信息
          this.getReservationDetail()
        }
      },
      fail: (error) => {
        console.error(`设备详情获取失败:`, error)
      }
    })
  },

  //获取预约详细信息
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

        if (result.status == 200) {

          this.setData({
            reservationArr: result.data
          })
        }
      },
      fail: (error) => {
        console.error(`设备详情获取失败:`, error)
      }
    })
  },

  //点击事件，取消该预约
  cancelReservation() {
    //显示弹窗，让用户输入取消理由
    wx.showModal({
      title: '强制取消预约',
      editable: true,
      placeholderText: '请输入取消理由',
      confirmText: '确认取消',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          const reason = res.content
          if (!reason || reason.trim() === '') {
            wx.showToast({
              title: '请填写取消理由',
              icon: 'none',
              duration: 1500
            })
            return
          }

          //二次确认
          wx.showModal({
            title: '确认取消',
            content: '强制取消后将通知用户，是否确认？',
            success: (modalRes) => {
              if (modalRes.confirm) {
                this.submitCancelReservation(reason)
              }
            }
          })
        }
      }
    })
  },

  //提交取消预约
  submitCancelReservation(reason) {
    wx.showLoading({
      title: '提交中...',
    })

    wx.request({
      url: app.getApiUrl('/cancelReservation'),
      method: 'POST',
      data: {
        reservation_id: parseInt(this.data.reservation_id),
        reason: reason
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        const result = res.data

        if (result.status == 200) {

          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2]; //上一页实例
          if (prevPage) {
            prevPage.setData({
              toastMessage: '取消成功'
            });

          }
          wx.navigateBack();
        } else {
          wx.showToast({
            title: result.message || '取消失败',
            icon: 'error',
            duration: 1500
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('取消预约失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },
})