// pages/changePassword/changePassword.js
const app = getApp()

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    user_id: ''
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    if (userDataStr) {
      const userData = JSON.parse(userDataStr)
      this.setData({
        user_id: userData.user_id
      })
    }
  },

  // 输入原密码
  onOldPasswordInput(e) {
    this.setData({
      oldPassword: e.detail.value
    })
  },

  // 输入新密码
  onNewPasswordInput(e) {
    this.setData({
      newPassword: e.detail.value
    })
  },

  // 输入确认密码
  onConfirmPasswordInput(e) {
    this.setData({
      confirmPassword: e.detail.value
    })
  },

  // 提交修改密码
  submitChangePassword() {
    const { oldPassword, newPassword, confirmPassword, user_id } = this.data

    // 验证原密码不能为空
    if (!oldPassword) {
      wx.showToast({
        title: '请输入原密码',
        icon: 'none'
      })
      return
    }

    // 验证新密码长度
    if (!newPassword || newPassword.length < 6) {
      wx.showToast({
        title: '新密码至少6位',
        icon: 'none'
      })
      return
    }

    // 验证两次密码是否一致
    if (newPassword !== confirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      })
      return
    }

    // 验证新旧密码不能相同
    if (oldPassword === newPassword) {
      wx.showToast({
        title: '新密码不能与原密码相同',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认修改',
      content: '确定要修改密码吗？修改后需要重新登录。',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '修改中...'
          })

          wx.request({
            url: app.getApiUrl('/changePassword'),
            method: 'POST',
            data: {
              user_id: user_id,
              oldPassword: oldPassword,
              newPassword: newPassword
            },
            header: {
              'content-type': 'application/json'
            },
            success: (res) => {
              wx.hideLoading()
              const result = res.data

              if (result.status == 200) {
                wx.showToast({
                  title: '密码修改成功，请重新登录',
                  icon: 'success',
                  duration: 2000
                })

                // 清除缓存并跳转到登录页
                setTimeout(() => {
                  wx.removeStorageSync('UserData')
                  wx.reLaunch({
                    url: '/pages/login/login'
                  })
                }, 2000)
              } else if (result.status == 201) {
                wx.showToast({
                  title: '原密码错误',
                  icon: 'none'
                })
              } else {
                wx.showToast({
                  title: result.message || '修改失败',
                  icon: 'none'
                })
              }
            },
            fail: () => {
              wx.hideLoading()
              wx.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  }
})