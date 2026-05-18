// pages/reservation/reservation.js
const app = getApp()

Page({

  data: {
    permission_id: '', //权限id
    max_concurrent_devices: '', //最多预约设备数量
    permission_level: '', //权限等级
    devicesArr: [], //可预约的设备列表
    filteredDevicesArr: [], //搜索后的设备列表
    showFiltered: false, //是否显示搜索后的设备列表
    searchKeyword: '', //搜索关键字
  },

  onLoad() {
    this.loadUserData()
  },

  onShow() {
    this.loadUserData()
  },

  //获取用户信息
  loadUserData() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      permission_id: userData.permission_id
    })

    //获取权限等级对应的信息
    this.getPermission_level()
  },

  //根据权限id获取权限等级对应的信息
  getPermission_level() {
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

        if (result.status !== 200) {
          wx.showToast({
            title: '获取权限信息失败',
            icon: 'none'
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            permission_level: result.data.permission_level,
            max_concurrent_devices: result.data.max_concurrent_devices
          })

          //获取用户权限可以预约的设备
          this.getCanBorrowDevices()
        }
      }
    })
  },

  //获取用户权限可以预约的设备
  getCanBorrowDevices() {
    wx.request({
      url: app.getApiUrl('/getCanBorrowDevices'),
      method: 'POST',
      data: {
        required_permission: this.data.permission_level
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取设备信息失败',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          // 处理图片URL，拼接完整地址
          const devices = (result.data || []).map(device => {
            if (device.image_url && !device.image_url.startsWith('http://') && !device.image_url.startsWith('https://')) {
              device.image_url = app.globalData.imageBaseUrl + device.image_url
            }
            return device
          })

          this.setData({
            devicesArr: devices,
            filteredDevicesArr: devices,
            showFiltered: false
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('获取设备信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //点击事件，选择要预约的设备
  toReservationDevice(e) {
    const index = e.currentTarget.dataset.index
    const devices = this.data.showFiltered ? this.data.filteredDevicesArr : this.data.devicesArr
    const device = devices[index]

    if (device && device.device_id) {
      //跳转到设备预约页面（reservationDevice）
      wx.navigateTo({
        url: `/pages/reservationDevice/reservationDevice?device_id=${device.device_id}&max_concurrent_devices=${this.data.max_concurrent_devices}`,
      })
    }
  },

  //输入事件，输入搜索关键字
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({ searchKeyword: keyword })
    this.filterDevices(keyword)
  },

  //根据关键字搜索设备
  filterDevices(keyword) {
    if (!keyword) {
      this.setData({
        filteredDevicesArr: this.data.devicesArr,
        showFiltered: false
      })
      return
    }

    const filtered = this.data.devicesArr.filter(device => {
      return device.device_name && device.device_name.toLowerCase().includes(keyword.toLowerCase())
    })

    this.setData({
      filteredDevicesArr: filtered,
      showFiltered: true
    })
  },
})