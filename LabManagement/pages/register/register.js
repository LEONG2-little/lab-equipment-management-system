// pages/register/register.js
const app = getApp()

Page({
  data: {
    user: {
      username: '',
      account: null,
      password: '',
      identity_type: '学生' //默认身份
    },
    confirmPassword: '',
  },

  onInputName(e) {
    this.setData({
      'user.username': e.detail.value
    });
  },
  onInputAccount(e) {
    this.setData({
      'user.account': e.detail.value ? Number(e.detail.value) : null
    });
  },
  onInputPassword(e) {
    this.setData({
      'user.password': e.detail.value
    });
  },
  onInputConfirmPassword(e) {
    this.setData({
      confirmPassword: e.detail.value
    });
  },

  register() {
    const {
      user,
      confirmPassword
    } = this.data;
    if (!user.username.trim()) {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none'
      });
      return;
    }
    if (!user.account) {
      wx.showToast({
        title: '请输入学号',
        icon: 'none'
      });
      return;
    }
    if (user.password.length < 6) {
      wx.showToast({
        title: '密码至少6位',
        icon: 'none'
      });
      return;
    }
    if (user.password !== confirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '注册中...'
    });
    wx.request({
      url: app.getApiUrl('/register'),
      method: 'POST',
      data: this.data.user,
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading();
        const result = res.data;
        if (result.status === 200) {
          wx.showToast({
            title: '注册成功',
            icon: 'success'
          });
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else {
          wx.showToast({
            title: result.message || '注册失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  },

  goToLogin() {
    wx.navigateBack();
  }
});