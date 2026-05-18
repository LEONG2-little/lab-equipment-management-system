// pages/updataDeviceOpenTime/updataDeviceOpenTime.js
const app = getApp()

Page({

  data: {
    deviceData: [], //设备数据
    deviceIds: [], //设备id数组
    manager_id: '', //管理员id

    //每周选项
    weekOptions: [{
        name: '周一',
        value: 1,
        selected: false
      },
      {
        name: '周二',
        value: 2,
        selected: false
      },
      {
        name: '周三',
        value: 3,
        selected: false
      },
      {
        name: '周四',
        value: 4,
        selected: false
      },
      {
        name: '周五',
        value: 5,
        selected: false
      },
      {
        name: '周六',
        value: 6,
        selected: false
      },
      {
        name: '周日',
        value: 7,
        selected: false
      }
    ],

    //选中的星期
    selectedWeeks: [],
    selectedWeeksStr: '',
    selectedWeeksNames: '',

    //时间段选项
    timeSlots: [{
        label: '08:30-09:15',
        start: '08:30',
        end: '09:15',
        selected: false
      },
      {
        label: '09:25-10:10',
        start: '09:25',
        end: '10:10',
        selected: false
      },
      {
        label: '10:30-11:15',
        start: '10:30',
        end: '11:15',
        selected: false
      },
      {
        label: '11:25-12:10',
        start: '11:25',
        end: '12:10',
        selected: false
      },
      {
        label: '13:05-13:50',
        start: '13:05',
        end: '13:50',
        selected: false
      },
      {
        label: '14:00-14:45',
        start: '14:00',
        end: '14:45',
        selected: false
      },
      {
        label: '14:55-15:40',
        start: '14:55',
        end: '15:40',
        selected: false
      },
      {
        label: '15:50-16:35',
        start: '15:50',
        end: '16:35',
        selected: false
      },
      {
        label: '16:45-17:30',
        start: '16:45',
        end: '17:30',
        selected: false
      },
      {
        label: '18:30-19:15',
        start: '18:30',
        end: '19:15',
        selected: false
      },
      {
        label: '19:25-20:10',
        start: '19:25',
        end: '20:10',
        selected: false
      },
      {
        label: '20:20-21:05',
        start: '20:20',
        end: '21:05',
        selected: false
      }
    ],

    //选中的时间段
    selectedTimeObjects: [],

    //原有的开放时间数据
    oldOpenTimes: []
  },

  onLoad(options) {
    const deviceData = JSON.parse(decodeURIComponent(options.selectedDevices))
    const deviceIds = deviceData.map(item => item.device_id)
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      deviceData: deviceData,
      deviceIds: deviceIds,
      manager_id: userData.user_id
    })

    //获取已有开放时间（取第一个设备）
    if (deviceIds.length > 0) {
      this.getDeviceOpenTimes(deviceIds[0])
    }
  },

  //获取设备已有的开放时间
  getDeviceOpenTimes(deviceId) {
    wx.request({
      url: app.getApiUrl('/getDeviceOpenTimes'),
      method: 'POST',
      data: {
        device_id: parseInt(deviceId)
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {
          const openTimes = result.data || []
          this.setData({
            oldOpenTimes: openTimes
          })
          //如果有已设置的开放时间，显示在页面上
          if (openTimes.length > 0) {
            this.displayExistingOpenTimes(openTimes)
          }
        }
      },
      fail: (error) => {
        console.error('获取开放时间失败:', error)
      }
    })
  },

  //显示已有的开放时间
  displayExistingOpenTimes(openTimes) {
    //收集所有已选中的星期
    const selectedWeeksSet = new Set()
    //收集所有已选中的时间段
    const selectedTimeSlotsSet = new Set()

    for (const openTime of openTimes) {
      //解析星期
      const weeks = openTime.week.split(',').map(w => parseInt(w))
      weeks.forEach(week => selectedWeeksSet.add(week))

      //解析时间
      let startTime = openTime.start_time
      let endTime = openTime.end_time

      //处理时间格式，提取 HH:MM
      if (startTime && startTime.includes('T')) {
        const startDate = new Date(startTime)
        const endDate = new Date(endTime)
        startTime = `${startDate.getHours().toString().padStart(2, '0')}:${startDate.getMinutes().toString().padStart(2, '0')}`
        endTime = `${endDate.getHours().toString().padStart(2, '0')}:${endDate.getMinutes().toString().padStart(2, '0')}`
      } else if (startTime && startTime.includes(':')) {
        startTime = startTime.substring(0, 5)
        endTime = endTime.substring(0, 5)
      }

      const timeSlotKey = `${startTime}-${endTime}`
      selectedTimeSlotsSet.add(timeSlotKey)
    }

    //更新星期选择
    const weekOptions = this.data.weekOptions.map(week => ({
      ...week,
      selected: selectedWeeksSet.has(week.value)
    }))

    //更新时间段选择
    const timeSlots = this.data.timeSlots.map(slot => {
      const slotKey = `${slot.start}-${slot.end}`
      return {
        ...slot,
        selected: selectedTimeSlotsSet.has(slotKey)
      }
    })

    //收集选中的星期
    const selectedWeeks = weekOptions.filter(w => w.selected).map(w => w.value)
    const selectedWeeksStr = selectedWeeks.join(',')
    const selectedWeeksNames = weekOptions.filter(w => w.selected).map(w => w.name).join('、')

    //收集选中的时间段
    const selectedTimeObjects = timeSlots.filter(t => t.selected).map(t => ({
      label: t.label,
      start: t.start,
      end: t.end
    }))

    this.setData({
      weekOptions: weekOptions,
      selectedWeeks: selectedWeeks,
      selectedWeeksStr: selectedWeeksStr,
      selectedWeeksNames: selectedWeeksNames,
      timeSlots: timeSlots,
      selectedTimeObjects: selectedTimeObjects
    })
  },

  //切换星期选择
  toggleWeek(e) {
    const index = e.currentTarget.dataset.index
    const weekOptions = this.data.weekOptions
    const week = weekOptions[index]

    week.selected = !week.selected

    const selectedWeeks = weekOptions.filter(w => w.selected).map(w => w.value)
    const selectedWeeksStr = selectedWeeks.join(',')
    const selectedWeeksNames = weekOptions.filter(w => w.selected).map(w => w.name).join('、')

    this.setData({
      weekOptions: weekOptions,
      selectedWeeks: selectedWeeks,
      selectedWeeksStr: selectedWeeksStr,
      selectedWeeksNames: selectedWeeksNames
    })
  },

  //切换时间段选择
  toggleTimeSlot(e) {
    const index = e.currentTarget.dataset.index
    const timeSlots = this.data.timeSlots
    const timeSlot = timeSlots[index]

    timeSlot.selected = !timeSlot.selected

    const selectedTimeObjects = timeSlots.filter(t => t.selected).map(t => ({
      label: t.label,
      start: t.start,
      end: t.end
    }))

    this.setData({
      timeSlots: timeSlots,
      selectedTimeObjects: selectedTimeObjects
    })
  },

  //确认修改开放机时
  setOpenTime() {
    //验证是否选择了星期
    if (this.data.selectedWeeks.length === 0) {
      wx.showToast({
        title: '请至少选择一个星期',
        icon: 'none'
      })
      return
    }

    //验证是否选择了时间段
    if (this.data.selectedTimeObjects.length === 0) {
      wx.showToast({
        title: '请至少选择一个时间段',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认修改',
      content: '确定要修改开放机时吗？原有的开放时间将被停用。',
      success: (res) => {
        if (res.confirm) {
          this.submitUpdate()
        }
      }
    })
  },

  //提交修改
  submitUpdate() {
    wx.showLoading({
      title: '提交中...',
    })

    wx.request({
      url: app.getApiUrl('/disableDeviceOpenTime'),
      method: 'POST',
      data: {
        device_id: this.data.deviceIds[0]
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        if (res.data.status == 200) {
          //停用成功后，插入新的开放时间
          this.insertNewOpenTimes()
        } else {
          wx.hideLoading()
          wx.showToast({
            title: res.data.message || '停用失败',
            icon: 'none'
          })
        }
      },
      fail: (error) => {
        reject(error)
      }
    })
  },

  //插入新的开放时间
  insertNewOpenTimes() {
    //构建新的开放机时数据
    const openTimeList = []
    const deviceIds = this.data.deviceIds

    for (const deviceId of deviceIds) {
      for (const timeSlot of this.data.selectedTimeObjects) {
        openTimeList.push({
          device_id: deviceId,
          manager_id: parseInt(this.data.manager_id),
          week: this.data.selectedWeeksStr,
          start_time: timeSlot.start,
          end_time: timeSlot.end,
          status: '空闲'
        })
      }
    }

    //设置设备的开放机时
    wx.request({
      url: app.getApiUrl('/setOpenDeviceTime'),
      method: 'POST',
      data: openTimeList,
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '修改失败',
            icon: 'none'
          })
          return
        }

        if (result.status == 200) {
          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2]; //上一页实例
          if (prevPage) {
            prevPage.setData({
              toastMessage: '修改开放机时成功'
            });
          }
          wx.navigateBack();
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('修改开放机时失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  }
})