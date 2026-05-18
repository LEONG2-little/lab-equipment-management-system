// pages/deviceBorrow/deviceBorrow.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户id
    deviceBorrowArr: [], //设备借出的数据
    filteredDevicesArr: [], //搜索后的设备数据
    showFiltered: false, //是否显示搜索后的设备
    searchKeyword: '', //搜索关键字
    statusOptions: ['全部', '已归还', '使用中'], //选项列表
    selectedStatus: '全部', //当前选中的状态
    toastMessage: '',
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      user_id: userData.user_id
    })

    //获取设备借出的信息
    this.getDeviceBorrow()
  },

  onShow() {

    if (this.data.toastMessage) {
      wx.showToast({
        title: this.data.toastMessage,
        icon: 'success'
      });
      this.setData({
        toastMessage: ''
      });
    }

    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      user_id: userData.user_id
    })

    //获取设备借出的信息
    this.getDeviceBorrow()
  },

  //获取设备借出的信息
  getDeviceBorrow() {
    wx.request({
      url: app.getApiUrl('/getDeviceBorrow'),
      method: 'POST',
      data: {
        manager_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {

        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message,
            icon: 'none',
          })
          return
        }

        if (result.status === 200) {
          this.setData({
            deviceBorrowArr: result.data,
          })

          //从索引0开始获取设备详细信息
          this.getDeviceDetail(0)
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //从索引0开始获取设备详细信息
  getDeviceDetail(index) {

    const deviceBorrowArr = this.data.deviceBorrowArr

    //如果索引大于等于数组长度
    if (index >= deviceBorrowArr.length) {

      //所有设备详情获取完成，开始获取借用人信息
      this.getBorrowerDetail(0)
      return
    }

    //获取当前索引的设备
    const device = deviceBorrowArr[index]
    const device_id = device.device_id

    //获取设备详细信息
    wx.request({
      url: app.getApiUrl('/getDeviceDetail'),
      method: 'POST',
      data: {
        device_id: device_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {

          //将设备详情合并到当前设备对象中
          const key = `deviceBorrowArr[${index}].deviceDetail`
          this.setData({
            [key]: result.data
          })
        } else {
          console.warn(`设备 ${device_id} 详情获取失败:`, result.message)
        }

        //继续获取下一个设备的详情
        this.getDeviceDetail(index + 1)
      },
      fail: (error) => {
        console.error(`设备 ${device_id} 详情请求失败:`, error)

        //即使失败也继续获取下一个设备的详情
        this.getDeviceDetail(index + 1)
      }
    })
  },

  //从索引0开始获取设备借用人详细信息
  getBorrowerDetail(index) {

    const deviceBorrowArr = this.data.deviceBorrowArr

    //如果索引大于等于数组长度
    if (index >= deviceBorrowArr.length) {

      //所有设备借用人详情获取完成
      this.setData({
        filteredDevicesArr: this.data.deviceBorrowArr
      })
      return
    }

    //获取当前索引的设备
    const device = deviceBorrowArr[index]
    const user_id = device.user_id

    //获取设备借用人详细信息
    wx.request({
      url: app.getApiUrl('/getUserDetail'),
      method: 'POST',
      data: {
        user_id: user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {

          //取数组的第一个元素
          const borrowerData = result.data[0]

          //将设备借用人详情合并到当前设备对象中
          const key = `deviceBorrowArr[${index}].borrowerDetail`
          this.setData({
            [key]: borrowerData
          })
        } else {
          console.warn(`借用人 ${user_id} 详情获取失败:`, result.message)
        }

        //继续获取下一个设备借用人的详情
        this.getBorrowerDetail(index + 1)
      },
      fail: (error) => {
        console.error(`借用人 ${user_id} 详情请求失败:`, error)

        //即使失败也继续获取下一个设备借用人的详情
        this.getBorrowerDetail(index + 1)
      }
    })
  },

  //点击事件，进行搜索
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({
      searchKeyword: keyword
    })

    //搜索对应的设备借出记录
    this.filterDevices(keyword, this.data.selectedStatus)
  },

  //搜索对应的设备借出记录
  filterDevices(keyword, status) {

    const sourceArray = this.data.deviceBorrowArr;

    //如果关键字为空并且status为全部
    if (!keyword && status === "全部") {
      this.setData({
        filteredDevicesArr: sourceArray,
        showFiltered: false
      });
      return;
    }

    //获取数组中包含搜索关键字并且状态一样的设备借出记录
    const filtered = sourceArray.filter(device => {
      let nameMatch = true;
      if (keyword) {

        //从deviceDetail中获取设备名称
        const deviceName = device.deviceDetail?.device_name || '';
        nameMatch = deviceName.toLowerCase().includes(keyword.toLowerCase());
      }

      let statusMatch = true;

      //如果status不为全部
      if (status && status !== '全部') {
        statusMatch = device.status === status;
      }
      return nameMatch && statusMatch;
    });

    //显示搜索后的设备借出记录
    this.setData({
      filteredDevicesArr: filtered,
      showFiltered: true
    });
  },

  //点击事件，状态下拉框选择
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({
      selectedStatus: status
    })

    //搜索相同状态的设备借出记录
    this.filterDevices(this.data.searchKeyword, status)
  },

  //点击事件，跳转到设备借出详情页面（deviceDetail）
  toDeviceDetail(e) {
    const index = e.currentTarget.dataset.index
    const devices = this.data.showFiltered ? this.data.filteredDevicesArr : this.data.deviceBorrowArr
    const device = devices[index]

    wx.navigateTo({
      url: `/pages/deviceDetail/deviceDetail?device_id=${device.device_id}&reservation_id=${device.reservation_id}&username=${device.borrowerDetail.username}`,
    })
  },
})