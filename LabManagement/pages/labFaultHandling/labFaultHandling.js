// pages/labFaultHandling/labFaultHandling.js
const app = getApp()

Page({

  data: {
    user_id: '',
    labFaultArr: [],
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
      url: app.getApiUrl('/getLabFault'),
      method: 'POST',
      data: {
        user_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        if (res.data.status == 200) {
          this.setData({
            labFaultArr: res.data.data
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
  toLabFaultDetail(e) {

    const index = e.currentTarget.dataset.index
    const laboratory_id = this.data.labFaultArr[index].laboratory_id

    wx.navigateTo({
      url: `/pages/labFaultDetail/labFaultDetail?laboratory_id=${laboratory_id}`,
    })
  }
})