// pages/announcement/announcement.js
const app = getApp()

Page({

  data: {
    announcementArr: [], //公告数据
  },

  onLoad() {
    this.getAnnouncement()
  },

  onShow() {
    this.getAnnouncement()
  },

  //获取公告数据
  getAnnouncement() {
    wx.request({
      url: app.getApiUrl('/getAllAnnouncement'),
      method: 'GET',
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取公告信息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          // 处理图片URL，拼接完整地址
          const announcements = (result.data || []).map(announcement => {
            if (announcement.image_url && !announcement.image_url.startsWith('http://') && !announcement.image_url.startsWith('https://')) {
              announcement.image_url = app.globalData.imageBaseUrl + announcement.image_url
            }
            return announcement
          })

          this.setData({
            announcementArr: announcements
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

  //跳转事件，点击按钮后跳转到公告详情页面（announcementDetail）
  toAnnouncementDetail(e) {
    const index = e.currentTarget.dataset.index
    const announcementData = this.data.announcementArr[index]

    wx.navigateTo({
      url: `/pages/announcementDetail/announcementDetail?announcement_id=${announcementData.announcement_id}&image_url=${encodeURIComponent(announcementData.image_url || '')}`,
    })
  },
})