// pages/messageDetail/messageDetail.js
const app = getApp()

Page({

  data: {
    notification_id: '', //消息id
    messageDetailArr: [], //消息详情数据
  },

  onLoad(options) {
    this.setData({
      notification_id: options.notification_id,
    })

    //获取消息详情
    this.getMessage()
  },

  //获取消息详情
  getMessage() {
    wx.request({
      url: app.getApiUrl('/getMessageDetail'),
      method: 'POST',
      data: {
        notification_id: this.data.notification_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {

        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '获取消息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status === 200) {

          this.setData({
            messageDetailArr: result.data
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('登录失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },
})