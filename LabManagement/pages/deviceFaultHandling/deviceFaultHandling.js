// pages/deviceFaultHandling/deviceFaultHandling.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户ID
    deviceFaultArr: [], //设备故障数据
    toastMessage: '',
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      user_id: userData.user_id
    })

    this.getDeviceFault()
  },

  onShow() {
    if (this.data.toastMessage) {
      wx.showToast({
        title: this.data.toastMessage,
        icon: 'success'
      });
      this.setData({
        toastMessage: ''
      });
    }

    this.getDeviceFault()
  },

  //获取可能发生故障的设备
  getDeviceFault() {
    wx.request({
      url: app.getApiUrl('/getDeviceFault'),
      method: 'POST',
      data: {
        manager_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        if (res.data.status == 200) {
          this.setData({
            deviceFaultArr: res.data.data
          })
        } else {
          wx.showToast({
            title: '获取故障信息失败',
            icon: 'error'
          })
        }
      }
    })
  },

  //点击事件，前往设备故障详情页面
  toDeviceFaultDetail(e) {

    const index = e.currentTarget.dataset.index
    const device_id = this.data.deviceFaultArr[index].device_id

    wx.navigateTo({
      url: `/pages/deviceFaultDetail/deviceFaultDetail?device_id=${device_id}`,
    })
  }
})