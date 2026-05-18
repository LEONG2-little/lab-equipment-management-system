// pages/allDeviceDetail/allDeviceDetail.js
const app = getApp()

Page({

  data: {
    device_id: '',
    deviceDetailArr: null //设备数据
  },

  onLoad(options) {
    this.setData({
      device_id: options.device_id
    })
    this.getDeviceDetail()
  },

  //根据device_id查询设备数据
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
})