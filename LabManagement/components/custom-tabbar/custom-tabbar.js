// components/custom-tabbar/custom-tabbar.js
Component({
  properties: {
    current: {
      type: Number,
      value: 0
    }
  },

  data: {
    tabList: [{
        pagePath: '/pages/index/index',
        text: '首页',
        icon: 'home'
      },
      {
        pagePath: '/pages/message/message',
        text: '消息',
        icon: 'message'
      },
      {
        pagePath: '/pages/announcement/announcement',
        text: '公告',
        icon: 'announcement'
      },
      {
        pagePath: '/pages/personal/personal',
        text: '我的',
        icon: 'personal'
      }
    ],
    unreadCount: 0,
  },

  // 在页面显示时刷新未读数量（可选）
  pageLifetimes: {
    show() {
      this.getUnreadCount();
    }
  },

  methods: {

    lifetimes: {
      attached() {
        this.getUnreadCount();
      }
    },

    // 获取未读消息数量的方法
    getUnreadCount() {
      const app = getApp();
      const userData = wx.getStorageSync('UserData');

      if (userData) {
        const user_id = JSON.parse(userData).user_id;

        wx.request({
          url: app.getApiUrl('/getUnreadMessage'), // 需要后端提供这个接口
          method: 'POST',
          data: {
            user_id: user_id
          },
          success: (res) => {
            if (res.data.status === 200) {
              const unreadList = res.data.data || [];
              this.setData({
                unreadCount: unreadList.length // ✅ 获取列表的长度
              });
            }
          }
        });
      }
    },


    switchTab(e) {
      console.log('点击了底部导航栏')
      const index = e.currentTarget.dataset.index
      const tab = this.data.tabList[index]

      console.log('点击索引:', index, '当前索引:', this.data.current)
      console.log('跳转页面:', tab.pagePath)

      if (index === this.data.current) {
        console.log('当前页面，不跳转')
        return
      }

      // 跳转到对应页面
      wx.reLaunch({
        url: tab.pagePath,
        success: () => {
          console.log('跳转成功')
        },
        fail: (err) => {
          console.error('跳转失败:', err)

          wx.switchTab({
            url: tab.pagePath
          })
        }
      })
    }
  }
})