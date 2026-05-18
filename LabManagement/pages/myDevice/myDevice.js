// pages/myDevice/myDevice.js
const app = getApp()

Page({
  data: {
    manager_id: '', //管理人id
    deviceArr: [], //设备信息数据
    searchKeyword: '', //搜索关键字
    filteredDevicesArr: [], //搜索后的设备信息数据
    showFiltered: false, //是否显示搜索后的设备
    statusOptions: ['全部', '闲置', '课题组专用', '维修中', '空闲'], //选项列表
    selectedStatus: '全部', //当前选中的状态
    selectDataArr: [], //选择的设备
    operation: '批量操作', //按钮名称
    isOperation: false, //是否选中
    selectedDevices: [], //选择的设备id
    toastMessage: '',
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    const userId = userData.user_id
    this.setData({
      manager_id: userId,
      isOperation: false,
      selectedDevices: [],
      operation: '批量操作',
    })

    //获取自己管理的的设备信息
    this.getMyDevice()
  },

  onShow() {
    if (this.data.toastMessage) {
      wx.showToast({
        title: this.data.toastMessage,
        icon: 'success'
      });
      this.setData({ toastMessage: '' });
    }

    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    const userId = userData.user_id

    this.setData({
      manager_id: userId,
      isOperation: false,
      selectedDevices: [],
      operation: '批量操作',
      selectedStatus: '全部',
      searchKeyword: '',
      showFiltered: false,
      deviceArr: [],
      filteredDevicesArr: []
    })

    this.getMyDevice()
  },

  //获取自己管理的的设备信息
  getMyDevice() {
    wx.request({
      url: app.getApiUrl('/getMyDevices'),
      method: 'POST',
      data: {
        manager_id: this.data.manager_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '获取设备信息失败',
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
            deviceArr: devices,
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

  //点击事件，跳转到设备详情页面（myDeviceDetails）
  toMyDeviceDetails(e) {
    if (this.data.isOperation) return

    const index = e.currentTarget.dataset.index
    const devices = this.data.showFiltered ? this.data.filteredDevicesArr : this.data.deviceArr
    const device = devices[index]

    if (device && device.device_id) {
      wx.navigateTo({
        url: `/pages/myDeviceDetails/myDeviceDetails?device_id=${device.device_id}`,
      })
    }
  },

  //输入事件，输入搜索关键字
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({ searchKeyword: keyword })
    this.filterDevices(keyword, this.data.selectedStatus)
  },

  //搜索对应的设备
  filterDevices(keyword, status) {
    if (!keyword && status === "全部") {
      const deviceArrWithSelected = this.data.deviceArr.map(item => ({
        ...item,
        selected: this.data.selectedDevices.includes(item.device_id)
      }))
      this.setData({
        filteredDevicesArr: deviceArrWithSelected,
        showFiltered: false
      })
      return
    }

    const filtered = this.data.deviceArr.filter(device => {
      let nameMatch = true
      if (keyword) {
        nameMatch = device.device_name && device.device_name.toLowerCase().includes(keyword.toLowerCase())
      }
      let statusMatch = true
      if (status && status !== '全部') {
        statusMatch = device.status === status
      }
      return nameMatch && statusMatch
    }).map(item => ({
      ...item,
      selected: this.data.selectedDevices.includes(item.device_id)
    }))

    this.setData({
      filteredDevicesArr: filtered,
      showFiltered: true
    })
  },

  //清空搜索
  clearSearch() {
    this.setData({
      searchKeyword: '',
      selectedStatus: '全部',
      showFiltered: false,
      filteredDevicesArr: this.data.deviceArr.map(item => ({
        ...item,
        selected: this.data.selectedDevices.includes(item.device_id)
      }))
    })
  },

  //点击事件，选择要搜索的设备状态
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({ selectedStatus: status })
    this.filterDevices(this.data.searchKeyword, status)
  },

  //点击事件，点击批量操作按钮
  onBatchSelect() {
    if (!this.data.isOperation) {
      this.setData({
        selectedDevices: [],
        operation: '取消',
        isOperation: true,
      })
      return
    }

    if (this.data.isOperation) {
      const resetFilteredDevicesArr = this.data.filteredDevicesArr.map(item => ({
        ...item,
        selected: false
      }))
      this.setData({
        selectedDevices: [],
        filteredDevicesArr: resetFilteredDevicesArr,
        operation: '批量操作',
        isOperation: false,
      })
    }
  },

  //点击事件，选择要操作的设备
  toggleDeviceSelection(e) {
    if (!this.data.isOperation) return

    const index = e.currentTarget.dataset.index
    const devices = this.data.showFiltered ? this.data.filteredDevicesArr : this.data.deviceArr
    const device = devices[index]
    const deviceId = device.device_id

    const currentSelected = this.data.selectedDevices || []
    const selectedDevices = [...currentSelected]

    const deviceIndex = selectedDevices.indexOf(deviceId)
    if (deviceIndex === -1) {
      selectedDevices.push(deviceId)
    } else {
      selectedDevices.splice(deviceIndex, 1)
    }

    const newFilteredDevicesArr = this.data.filteredDevicesArr.map(item => ({
      ...item,
      selected: selectedDevices.includes(item.device_id)
    }))

    this.setData({
      selectedDevices: selectedDevices,
      filteredDevicesArr: newFilteredDevicesArr
    })
  },

  //点击事件，点击修改状态按钮
  toChangeStatus() {
    if (this.data.selectedDevices.length === 0) {
      wx.showToast({ title: '请选择设备', icon: 'none' })
      return
    }

    const selectedDevicesInfo = this.data.deviceArr.filter(device => this.data.selectedDevices.includes(device.device_id))
    const selectedFromFiltered = this.data.filteredDevicesArr.filter(device => this.data.selectedDevices.includes(device.device_id))

    const deviceMap = new Map();
    [...selectedDevicesInfo, ...selectedFromFiltered].forEach(device => {
      deviceMap.set(device.device_id, device)
    })

    const allSelectedDevices = Array.from(deviceMap.values())
    const firstStatus = allSelectedDevices[0].status
    const allSameStatus = allSelectedDevices.every(device => device.status === firstStatus)

    if (!allSameStatus) {
      wx.showToast({ title: '请选择相同状态的设备', icon: 'none' })
      return
    }
    if (firstStatus == '空闲') {
      wx.showToast({ title: '无法修改状态为空闲的设备', icon: 'none' })
      return
    }
    if (firstStatus == '可能发生故障') {
      wx.showToast({ title: '无法修改状态为可能发生故障的设备', icon: 'none' })
      return
    }

    const selectedDevicesStr = JSON.stringify(allSelectedDevices)
    wx.navigateTo({
      url: `/pages/changeStatus/changeStatus?selectedDevices=${encodeURIComponent(selectedDevicesStr)}`,
    })
  },

  //点击事件，点击开放机时按钮
  toSelectOpenTime() {
    if (this.data.selectedDevices.length === 0) {
      wx.showToast({ title: '请选择设备', icon: 'none' })
      return
    }

    const selectedDevicesInfo = this.data.deviceArr.filter(device => this.data.selectedDevices.includes(device.device_id))
    const selectedFromFiltered = this.data.filteredDevicesArr.filter(device => this.data.selectedDevices.includes(device.device_id))

    const deviceMap = new Map();
    [...selectedDevicesInfo, ...selectedFromFiltered].forEach(device => {
      deviceMap.set(device.device_id, device)
    })

    const allSelectedDevices = Array.from(deviceMap.values())
    const firstStatus = allSelectedDevices[0].status
    const allSameStatus = allSelectedDevices.every(device => device.status === firstStatus)

    if (firstStatus !== "闲置") {
      wx.showToast({ title: '只有设备闲置时才可开放机时', icon: 'none' })
      return
    }
    if (!allSameStatus) {
      wx.showToast({ title: '当前选择的设备未处于闲置状态', icon: 'none' })
      return
    }

    const selectedDevicesStr = JSON.stringify(allSelectedDevices)
    wx.navigateTo({
      url: `/pages/selectOpenTime/selectOpenTime?selectedDevices=${encodeURIComponent(selectedDevicesStr)}`,
    })
  }
})