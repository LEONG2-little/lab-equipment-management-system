// pages/announcementDetail/announcementDetail.js
const app = getApp()

Page({

  data: {
    announcement_id: '', //公告ID
    image_url: '', //图片完整URL
    announcementDetailArr: [], //公告数据
  },

  onLoad(options) {
    this.setData({
      announcement_id: options.announcement_id,
      image_url: decodeURIComponent(options.image_url || '')
    })

    this.getAnnouncementDetail()
  },

  //获取公告数据
  getAnnouncementDetail() {
    wx.request({
      url: app.getApiUrl('/getAnnouncementDetail'),
      method: 'POST',
      data: {
        announcement_id: this.data.announcement_id,
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取公告详细信息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            announcementDetailArr: result.data
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('获取数据失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //预览图片
  previewImage() {
    const imageUrl = this.data.image_url
    if (!imageUrl) return
    wx.previewImage({
      current: imageUrl,
      urls: [imageUrl]
    })
  }
})