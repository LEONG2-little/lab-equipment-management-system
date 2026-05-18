// pages/login/login.js

const app = getApp()

Page({
  data: {
    user: {
      account: '', //账号
      password: '' //密码
    }
  },

  onLoad(options){
    //退出登录成功
    if (options.logoutSuccess === 'true') {
      setTimeout(() => {
        wx.showToast({
          title: options.message || '退出登录成功',
          icon: 'success',
          duration: 2000
        });
      }, 300);
    }
  },

  //输入账号
  onInputAccount(e) {
    this.setData({
      'user.account': e.detail.value
    })
  },

  //输入密码
  onInputPassword(e) {
    this.setData({
      'user.password': e.detail.value
    })
  },

  //登录
  login() {
    const {
      account,
      password
    } = this.data.user
    if (!account || !password) {
      wx.showToast({
        title: '账号或密码不能为空',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '登录中...',
    })

    wx.request({
      url: app.getApiUrl('/login'),
      method: 'POST',
      data: this.data.user,
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()

        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '登录失败',
            icon: 'none',
            duration: 2000
          })
          return
        }

        if (result.status === 200) {
          //存储登录用户数据
          wx.setStorageSync('UserData', JSON.stringify(result.data))

          wx.reLaunch({
            url: '/pages/index/index?loginSuccess=true&message=登录成功'
          });
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

  //用户注册
  goToRegister() {
    wx.navigateTo({
      url: '/pages/register/register',
    });
  }
})