// pages/reservationDevice/reservationDevice.js
const app = getApp()

Page({

  data: {
    device_id: '', //设备id
    user_id: '', //用户id
    max_concurrent_devices: '', //最大可预约的设备数量
    deviceDetailArr: null, //设备详细信息
    reservationCount: '' //已预约的设备数量
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      device_id: options.device_id,
      user_id: userData.user_id,
      max_concurrent_devices: parseInt(options.max_concurrent_devices) || 0
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

        if (result.status !== 200) {
          wx.showToast({
            title: '获取设备详情失败',
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
      }
    })
  },

  //点击事件，点击预约按钮
  toSelectReservationTime() {

    //判断是否可以预约
    this.checkReservation()

  },

  //判断是否可以预约
  checkReservation() {

    //根据用户id获取已经预约的设备
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

        if (result.status == 200) {

          //保存已预约的设备数量
          this.setData({
            reservationCount: result.data ? result.data.length : 0
          })

          //如果已预约的设备数量大于等于用户权限最大可预约的设备数量
          if (this.data.reservationCount >= this.data.max_concurrent_devices) {
            wx.showToast({
              title: '您可预约的设备数量已满，不可继续预约',
              icon: 'error',
              duratio: 2000
            })
            return
          }

          if (this.data.max_concurrent_devices > this.data.reservationCount) {

            //跳转到选择预约时间页面（selectReservationTime）
            wx.navigateTo({
              url: `/pages/selectReservationTime/selectReservationTime?device_id=${this.data.device_id}&device_name=${this.data.deviceDetailArr.device_name}&start_time=${this.data.deviceDetailArr.start_time}&end_time=${this.data.deviceDetailArr.end_time}&manager_id=${this.data.deviceDetailArr.manager_id}&laboratory_id=${this.data.deviceDetailArr.laboratory_id}`,
            })
          }
        }

        if (result.status !== 200) {
          wx.showToast({
            title: '获取预约信息失败',
            icon: 'error',
            duration: 1500
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
  }
})