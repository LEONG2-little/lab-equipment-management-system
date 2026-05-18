// pages/allDevice/allDevice.js
const app = getApp()

Page({
  data: {
    devicesArr: [],           // 所有设备数据
    filteredDevicesArr: [],   // 搜索后的设备数据
    showFiltered: false,      // 是否显示搜索结果
    searchKeyword: '',        // 搜索关键字
  },

  onLoad() {
    this.getAllDevices()
  },

  onShow() {
    this.getAllDevices()
  },

  // 获取所有设备数据
  getAllDevices() {
    wx.request({
      url: app.getApiUrl('/getAllDevices'),
      method: 'POST',
      header: { 'content-type': 'application/json' },
      success: (res) => {
        const result = res.data
        if (result.status !== 200) {
          wx.showToast({ title: '获取设备信息失败', icon: 'none' })
          return
        }

        // 处理图片URL，拼接完整地址
        const devices = result.data.map(device => {
          if (device.image_url) {
            // 如果已经是完整URL则保留，否则拼接
            if (!device.image_url.startsWith('http://') && !device.image_url.startsWith('https://')) {
              device.image_url = app.globalData.imageBaseUrl + device.image_url
            }
          }
          return device
        })

        this.setData({
          devicesArr: devices,
          filteredDevicesArr: devices,
          showFiltered: false,
        })

        console.log(this.data.devicesArr)
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  // 点击跳转详情
  toAllDeviceDetail(e) {
    const index = e.currentTarget.dataset.index
    const devices = this.data.showFiltered ? this.data.filteredDevicesArr : this.data.devicesArr
    const device = devices[index]
    if (device && device.device_id) {
      wx.navigateTo({
        url: `/pages/allDeviceDetail/allDeviceDetail?device_id=${device.device_id}`,
      })
    }
  },

  // 搜索
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({ searchKeyword: keyword })
    this.filterDevices(keyword)
  },

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