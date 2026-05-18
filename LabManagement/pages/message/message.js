// pages/message/message.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户id
    messageArr: [], //消息数据
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      user_id: userData.user_id,
    })

    //获取信息
    this.getMessage()
  },

  onShow() {
    this.onLoad()
  },

  //根据用户id获取信息
  getMessage() {
    wx.request({
      url: app.getApiUrl('/getMessage'),
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
            title: result.message || '获取消息失败',
            icon: 'none',
            duration: 1500
          })
          return
        }

        if (result.status === 200) {

          this.setData({
            messageArr: result.data.reverse()
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

  //点击事件，点击某条消息
  toMessageDetail(e) {
    const index = e.currentTarget.dataset.index
    const messageData = this.data.messageArr[index]

    //如果这条消息是未读的
    if (messageData.is_read == false) {

      //设置为已读
      wx.request({
        url: app.getApiUrl('/setRead'),
        method: 'POST',
        data: {
          notification_id: messageData.notification_id,
        },
        header: {
          'content-type': 'application/json'
        },
        success: (res) => {
          wx.hideLoading()

          const result = res.data

          if (result.status !== 200) {
            wx.showToast({
              title: result.message || '已读消息失败',
              icon: 'none',
              duration: 1500
            })
            return
          }

          if (result.status === 200) {

            //刷新Tabbar
            this.updateTabbarUnreadCount();

            //跳转到消息详情页面（messageDetail）
            wx.navigateTo({
              url: `/pages/messageDetail/messageDetail?notification_id=${messageData.notification_id}`,
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
    } else {
      //跳转到消息详情页面（messageDetail）
      wx.navigateTo({
        url: `/pages/messageDetail/messageDetail?notification_id=${messageData.notification_id}`,
      })
    }
  },

  //刷新Tabbar
  updateTabbarUnreadCount() {

    //获取当前页面的tabbar组件实例
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];

    //在当前页面查找 tabbar 组件
    const tabbar = currentPage.selectComponent('.custom-tabbar');
    if (tabbar) {
      tabbar.getUnreadCount();
    } else {

      //如果找不到，尝试在页面中查找
      setTimeout(() => {
        const tabbarComponent = currentPage.selectComponent('.custom-tabbar');
        if (tabbarComponent) {
          tabbarComponent.getUnreadCount();
        }
      }, 100);
    }
  }
})