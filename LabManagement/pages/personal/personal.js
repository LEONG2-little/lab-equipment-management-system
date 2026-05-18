// pages/personal/personal.js
const app = getApp()

Page({
  data: {
    user: {
      username: '', //用户姓名
      identity_type: '', //身份
      permission_id: '', //权限id
      user_id: '', //用户id
      identity_type: '' //用户身份
    },
    permission_level: '', //权限等级
  },

  onLoad() {
    this.loadUserData()
    this.getPermission_level()
  },

  onShow() {
    this.loadUserData()
    this.getPermission_level()
  },

  //获取用户信息
  loadUserData() {
    try {
      const userDataStr = wx.getStorageSync('UserData')
      if (userDataStr) {
        const userData = JSON.parse(userDataStr)

        this.setData({
          'user.username': userData.username,
          'user.identity_type': userData.identity_type,
          'user.permission_id': userData.permission_id,
          'user.user_id': userData.user_id,
          'user.identity_type': userData.identity_type
        })
      } else {
        wx.showToast({
          title: '系统错误，请重新登录',
          duration: 1500,
        })

        setTimeout(() => {
          wx.navigateTo({
            url: '/pages/login/login',
          })
        }, 1600)
      }

    } catch (error) {
      console.error('加载用户数据失败:', error)
    }
  },

  //根据权限id获取权限等级
  getPermission_level() {
    wx.request({
      url: app.getApiUrl('/getPermission'),
      method: 'POST',
      data: {
        permission_id: this.data.user.permission_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {

        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取权限信息失败',
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            permission_level: result.data.permission_level
          })
        }
      }
    })
  },

  //点击事件，跳转到历史记录选择页面（historySelect）
  toHistorySelect() {

    if (this.data.user.identity_type == '学生') {
      wx.navigateTo({
        url: '/pages/history/history',
      })
    } else {
      wx.navigateTo({
        url: '/pages/historySelect/historySelect',
      })
    }
  },

  //点击事件，跳转到我的预约选择页面（myAllReservation）
  toMyAllReservation() {

    if (this.data.user.identity_type == '学生') {
      wx.navigateTo({
        url: '/pages/myReservation/myReservation',
      })
    } else {
      wx.navigateTo({
        url: '/pages/myAllReservation/myAllReservation',
      })
    }
  },

  //点击事件，跳转到我的管理页面（myManage）
  toMyManage() {
    wx.navigateTo({
      url: '/pages/myManage/myManage',
    })
  },

  //点击事件，跳转到个人中心页面（personalCenter）
  toPersonalCenter(){
    wx.navigateTo({
      url: '/pages/personalCenter/personalCenter',
    })
  },

  //点击事件，退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('UserData')

          wx.reLaunch({
            url: '/pages/login/login?logoutSuccess=true&message=退出登录成功'
          });

        } else if (res.cancel) {
          return
        }
      }
    })
  },
})