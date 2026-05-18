// pages/personalCenter/personalCenter.js
const app = getApp()

Page({
  data: {
    userData: {
      user_id: '',
      account: '',
      username: '',
      identity_type: '',
      phone: '',
      email: '',
      permission_id: ''
    },
    // 编辑表单数据（编辑时使用，取消时丢弃）
    editForm: {
      username: '',
      phone: '',
      email: ''
    },
    // 原始数据备份（用于取消编辑时恢复）
    originalUserData: null,
    permission_level: '',
    isEditing: false, // 是否正在编辑
  },

  onLoad() {
    this.loadUserData()
  },

  onShow() {
    this.loadUserData()
  },

  // 加载用户完整信息
  loadUserData() {
    const userDataStr = wx.getStorageSync('UserData')
    if (!userDataStr) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/login/login',
        })
      }, 1500)
      return
    }

    const cachedUser = JSON.parse(userDataStr)
    
    // 从后端获取最新的用户信息
    wx.request({
      url: app.getApiUrl('/getUserDetail'),
      method: 'POST',
      data: {
        user_id: cachedUser.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status == 200 && result.data && result.data.length > 0) {
          const userDetail = result.data[0]
          // 不显示密码
          delete userDetail.password
          
          this.setData({
            userData: userDetail,
            originalUserData: { ...userDetail }  // 备份原始数据
          })
          
          // 更新本地缓存
          wx.setStorageSync('UserData', JSON.stringify(userDetail))
          
          // 获取权限等级
          this.getPermissionLevel(userDetail.permission_id)
        }
      },
      fail: () => {
        // 网络错误时使用缓存数据
        this.setData({
          userData: cachedUser,
          originalUserData: { ...cachedUser }
        })
        if (cachedUser.permission_id) {
          this.getPermissionLevel(cachedUser.permission_id)
        }
      }
    })
  },

  // 获取权限等级
  getPermissionLevel(permission_id) {
    wx.request({
      url: app.getApiUrl('/getPermission'),
      method: 'POST',
      data: {
        permission_id: permission_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status == 200) {
          this.setData({
            permission_level: result.data.permission_level
          })
        }
      }
    })
  },

  // 输入事件
  onUsernameInput(e) {
    this.setData({
      'editForm.username': e.detail.value
    })
  },

  onPhoneInput(e) {
    this.setData({
      'editForm.phone': e.detail.value
    })
  },

  onEmailInput(e) {
    this.setData({
      'editForm.email': e.detail.value
    })
  },

  // 点击修改信息按钮 - 进入编辑状态
  startEdit() {
    // 用当前用户数据初始化编辑表单
    this.setData({
      isEditing: true,
      editForm: {
        username: this.data.userData.username || '',
        phone: this.data.userData.phone || '',
        email: this.data.userData.email || ''
      }
    })
  },

  // 点击取消按钮 - 退出编辑状态，丢弃修改
  cancelEdit() {
    // 如果有修改，弹出确认提示
    const userData = this.data.userData
    const editForm = this.data.editForm
    
    const hasChanged = 
      editForm.username !== userData.username ||
      editForm.phone !== (userData.phone || '') ||
      editForm.email !== (userData.email || '')
    
    if (hasChanged) {
      wx.showModal({
        title: '提示',
        content: '放弃修改的内容？',
        success: (res) => {
          if (res.confirm) {
            this.exitEditMode()
          }
        }
      })
    } else {
      this.exitEditMode()
    }
  },

  // 退出编辑模式
  exitEditMode() {
    this.setData({
      isEditing: false,
      editForm: {
        username: '',
        phone: '',
        email: ''
      }
    })
  },

  // 点击确认修改按钮
  confirmUpdate() {
    const { userData, editForm } = this.data

    if (!editForm.username || !editForm.username.trim()) {
      wx.showToast({
        title: '姓名不能为空',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认修改',
      content: '确定要修改个人信息吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '修改中...'
          })

          wx.request({
            url: app.getApiUrl('/updateUserInfo'),
            method: 'POST',
            data: {
              user_id: userData.user_id,
              username: editForm.username,
              phone: editForm.phone || '',
              email: editForm.email || ''
            },
            header: {
              'content-type': 'application/json'
            },
            success: (res) => {
              wx.hideLoading()
              const result = res.data
              
              if (result.status == 200) {
                // 更新本地缓存
                wx.setStorageSync('UserData', JSON.stringify(result.data))
                
                wx.showToast({
                  title: '修改成功',
                  icon: 'success'
                })
                
                // 退出编辑模式
                this.exitEditMode()
                
                // 重新加载数据
                this.loadUserData()
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
                title: '网络错误',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  // 跳转到修改密码页面
  toChangePassword() {
    wx.navigateTo({
      url: '/pages/changePassword/changePassword',
    })
  }
})