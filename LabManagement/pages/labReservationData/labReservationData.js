// pages/labReservationData/labReservationData.js
const app = getApp()

Page({

  data: {
    laboratory_id: '', //实验室id
    user_id: '', //用户id
    max_reserve_days: '', //最大可预约天数
    permission_level: '', //权限等级
    labDetail: null, //实验室详细信息
    deviceNames: '', //该实验室下的设备名称（顿号隔开）
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      laboratory_id: options.laboratory_id,
      user_id: userData.user_id,
      permission_id: userData.permission_id,
      max_reserve_days: parseInt(options.max_reserve_days) || 0
    })

    //获取用户权限信息
    this.getUserPermission()
  },

  //获取用户权限信息
  getUserPermission() {
    wx.request({
      url: app.getApiUrl('/getPermission'),
      method: 'POST',
      data: {
        permission_id: this.data.permission_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status === 200) {
          this.setData({
            permission_level: result.data.permission_level,
          })

          //获取实验室详细信息
          this.getLaboratoryDetail()
        } else {
          wx.showToast({
            title: '获取权限信息失败',
            icon: 'error'
          })
        }
      },
      fail: (error) => {
        console.error('获取权限信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      }
    })
  },

  //获取实验室详细信息
  getLaboratoryDetail() {
    wx.request({
      url: app.getApiUrl('/getLaboratoryDetail'),
      method: 'POST',
      data: {
        laboratory_id: this.data.laboratory_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取实验室详情失败',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status === 200) {
          this.setData({
            labDetail: result.data
          })
          //获取实验室详情后，获取该实验室下的设备
          this.getDeviceByLab()
        }
      },
      fail: (error) => {
        console.error('获取实验室信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //获取该实验室下的设备
  getDeviceByLab() {
    wx.request({
      url: app.getApiUrl('/getDeviceByLab'),
      method: 'POST',
      data: {
        laboratory_id: this.data.laboratory_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status === 200 && result.data && result.data.length > 0) {
          //提取所有设备名称，用顿号隔开
          const names = result.data.map(device => device.device_name).join('、')
          this.setData({
            deviceNames: names
          })
        } else {
          this.setData({
            deviceNames: '暂无设备'
          })
        }
      },
      fail: (error) => {
        console.error('获取设备列表失败:', error)
        this.setData({
          deviceNames: '获取失败'
        })
      }
    })
  },

  //点击事件，点击预约按钮
  toSelectReservationTime() {
    //检查实验室状态
    if (this.data.labDetail && this.data.labDetail.status !== '空闲') {
      wx.showToast({
        title: '该实验室当前不可预约',
        icon: 'error',
        duration: 2000
      })
      return
    }

    //跳转到选择预约时间页面
    wx.navigateTo({
      url: `/pages/labSelectTime/labSelectTime?laboratory_id=${this.data.laboratory_id}&max_reserve_days=${this.data.max_reserve_days}`,
    })
  }
})