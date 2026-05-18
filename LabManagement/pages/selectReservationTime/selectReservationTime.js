// pages/selectReservationTime/selectReservationTime.js
const app = getApp()

Page({
  data: {
    user_id: '',
    device_id: '', //设备id
    device_name: '', //设备名称
    manager_id: '', //设备负责人id
    purpose: '', //设备使用用途
    max_reserve_days: '', //可提前预约天数
    laboratory_id: '', // 实验室id

    //设备每周开放时间列表
    deviceOpenTimes: [],

    //权限数据
    permissionData: {
      permission_level: '',
      max_reserve_days: '',
      max_concurrent_devices: '',
    },

    //用户数据
    user: {
      user_id: '',
      permission_id: ''
    },

    //预约数据
    deviceReservationArr: [], //该设备所有的预约信息
    labReservationArr: [], // 该设备所在实验室的预约信息

    dateList: [], //日期方块列表
    selectedDate: '', //选中的日期 (YYYY-MM-DD)

    ExecutionTimePeriod: [], //禁止预约时间段

    timeSlots: [{
        label: '08:30-09:15',
        start: '08:30',
        end: '09:15',
        selected: false,
        disabled: false
      },
      {
        label: '09:25-10:10',
        start: '09:25',
        end: '10:10',
        selected: false,
        disabled: false
      },
      {
        label: '10:30-11:15',
        start: '10:30',
        end: '11:15',
        selected: false,
        disabled: false
      },
      {
        label: '11:25-12:10',
        start: '11:25',
        end: '12:10',
        selected: false,
        disabled: false
      },
      {
        label: '13:05-13:50',
        start: '13:05',
        end: '13:50',
        selected: false,
        disabled: false
      },
      {
        label: '14:00-14:45',
        start: '14:00',
        end: '14:45',
        selected: false,
        disabled: false
      },
      {
        label: '14:55-15:40',
        start: '14:55',
        end: '15:40',
        selected: false,
        disabled: false
      },
      {
        label: '15:50-16:35',
        start: '15:50',
        end: '16:35',
        selected: false,
        disabled: false
      },
      {
        label: '16:45-17:30',
        start: '16:45',
        end: '17:30',
        selected: false,
        disabled: false
      },
      {
        label: '18:30-19:15',
        start: '18:30',
        end: '19:15',
        selected: false,
        disabled: false
      },
      {
        label: '19:25-20:10',
        start: '19:25',
        end: '20:10',
        selected: false,
        disabled: false
      },
      {
        label: '20:20-21:05',
        start: '20:20',
        end: '21:05',
        selected: false,
        disabled: false
      }
    ],
    selectedTimeObject: null, //存储选中的时间段完整信息

    //消息数据
    message: {
      is_read: false,
      user_id: '',
      title: '预约设备通知',
      content: ''
    }
  },

  onLoad(options) {
    this.setData({
      laboratory_id: options.laboratory_id,
      device_id: options.device_id,
      device_name: options.device_name,
      manager_id: options.manager_id,
      charge: options.charge,
    })

    //获取用户数据
    this.getUserData()
  },

  //获取用户数据
  getUserData() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      'user.user_id': userData.user_id,
      'user.permission_id': userData.permission_id,
      'message.user_id': userData.user_id,
      user_id: userData.user_id
    })

    //获取权限数据
    this.getPermissionData()
  },

  //获取权限数据
  getPermissionData() {
    wx.request({
      url: app.getApiUrl('/getPermission'),
      method: 'POST',
      data: {
        permission_id: this.data.user.permission_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status == 200) {
          this.setData({
            'permissionData.permission_level': result.data.permission_level,
            'permissionData.max_reserve_days': result.data.max_reserve_days,
            'permissionData.max_concurrent_devices': result.data.max_concurrent_devices,
            max_reserve_days: result.data.max_reserve_days
          })

          //获取设备的开放时间设置
          Promise.all([
            this.getDeviceOpenTimes(),
            this.getDeviceReservation(),
            this.getLabReservation(),
            this.selectByExecute()
          ]).then(() => {
            //生成日期列表
            this.generateDateList()
          })
        }

        if (result.status !== 200) {
          wx.showToast({
            title: '获取权限数据出错',
            icon: 'error',
            duration: 1500
          })
        }
      },

      fail: (error) => {
        wx.hideLoading()
        console.error('获取信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },

  //获取设备的每周开放时间设置
  getDeviceOpenTimes() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: app.getApiUrl('/getDeviceOpenTimes'),
        method: 'POST',
        data: {
          device_id: parseInt(this.data.device_id)
        },
        header: {
          'content-type': 'application/json'
        },
        success: (res) => {
          const result = res.data
          if (result.status == 200) {
            console.log('获取的设备开放时间：', result.data)
            this.setData({
              deviceOpenTimes: result.data || []
            })
            resolve()
          } else {
            reject()
          }
        },
        fail: (error) => {
          console.error('获取设备开放时间失败:', error)
          reject(error)
        }
      })
    })
  },

  //根据设备id获取该设备的预约数据
  getDeviceReservation() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: app.getApiUrl('/getDeviceReservation'),
        method: 'POST',
        data: {
          device_id: this.data.device_id
        },
        header: {
          'content-type': 'application/json'
        },

        success: (res) => {
          const result = res.data

          if (result.status == 200) {
            console.log('获取的设备预约数据：', result.data)
            this.setData({
              deviceReservationArr: result.data
            })
            resolve()
          } else {
            reject()
          }
        },

        fail: (error) => {
          wx.hideLoading()
          console.error('获取设备预约数据失败:', error)
          reject(error)
        },
      })
    })
  },

  //获取该设备所在实验室的预约信息
  getLabReservation() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: app.getApiUrl('/getLabReservation'),
        method: 'POST',
        data: {
          laboratory_id: parseInt(this.data.laboratory_id)
        },
        header: {
          'content-type': 'application/json'
        },
        success: (res) => {
          const result = res.data
          if (result.status == 200) {
            console.log('获取的实验室预约数据：', result.data)
            this.setData({
              labReservationArr: result.data
            })
            resolve()
          } else {
            reject()
          }
        },
        fail: (error) => {
          console.error('获取实验室预约数据失败:', error)
          reject(error)
        }
      })
    })
  },

  selectByExecute() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: app.getApiUrl('/selectByExecute'),
        method: 'POST',
        header: {
          'content-type': 'application/json'
        },
        success: (res) => {
          const result = res.data
          if (result.status == 200) {
            console.log('获取的执行时段数据：', result.data)
            this.setData({
              ExecutionTimePeriod: result.data
            })
            resolve()
          } else {
            reject()
          }
        },
        fail: (error) => {
          console.error('获取执行时段数据失败:', error)
          reject(error)
        }
      })
    })
  },

  //判断某一天是否是可预约的（根据每周开放时间）
  isDateAvailable(date) {
    const deviceOpenTimes = this.data.deviceOpenTimes

    //如果没有设置开放时间，则不可预约
    if (!deviceOpenTimes || deviceOpenTimes.length === 0) {
      return false
    }

    //获取该日期是星期几（1-7，周一为1，周日为7）
    const weekday = date.getDay()
    const weekNum = weekday === 0 ? 7 : weekday

    //检查是否有开放时间包含这个星期
    const hasOpenTime = deviceOpenTimes.some(openTime => {
      const weeks = openTime.week.split(',').map(w => parseInt(w))
      return weeks.includes(weekNum)
    })

    return hasOpenTime
  },

  //生成日期方块列表
  generateDateList() {
    const today = new Date()
    const maxDays = parseInt(this.data.max_reserve_days) || 7 // 默认7天
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const dateList = []

    //获取执行中的禁用时段数据
    const disabledPeriods = this.data.ExecutionTimePeriod || []

    //将禁用时段转换为日期范围列表
    const disabledDateRanges = disabledPeriods.map(period => {
      const startDateStr = period.start_time.split('T')[0]
      const endDateStr = period.end_time.split('T')[0]
      return {
        start: startDateStr,
        end: endDateStr
      }
    })

    for (let i = 0; i <= maxDays; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() + i)

      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const dateStr = `${year}-${month}-${day}`

      const weekIndex = date.getDay()
      const week = weekdays[weekIndex]

      //判断是否可预约
      let disabled = i === 0 //今天不可预约

      //检查该日期是否符合每周开放时间
      if (!disabled && this.data.deviceOpenTimes.length > 0) {
        if (!this.isDateAvailable(date)) {
          disabled = true
        }
      }

      //检查是否在禁用时段内
      if (!disabled) {
        const isInDisabledPeriod = disabledDateRanges.some(range => {
          return dateStr >= range.start && dateStr <= range.end
        })
        if (isInDisabledPeriod) {
          disabled = true
        }
      }

      dateList.push({
        date: dateStr,
        display: `${month}月${day}日 ${week}`,
        year: year,
        month: month,
        day: day,
        week: week,
        weekNum: weekIndex === 0 ? 7 : weekIndex,
        disabled: disabled
      })
    }

    this.setData({
      dateList: dateList
    })
  },

  //选择日期
  onDateSelect(e) {
    const dataset = e.currentTarget.dataset
    const date = dataset.date

    this.setData({
      selectedDate: date,
      selectedTimeObject: null, //清空已选时间段
      timeSlots: this.data.timeSlots.map(item => ({ //重置时间段选中状态
        ...item,
        selected: false,
        disabled: false
      }))
    }, () => {
      //选中日期后，先标记不在开放时间内的时段，再标记被占用的时段
      this.markAvailableTimeSlots()
    })
  },

  //标记哪些时间段是可预约的（根据每周开放时间）
  markAvailableTimeSlots() {
    const {
      deviceOpenTimes,
      selectedDate,
      timeSlots
    } = this.data

    if (!selectedDate || deviceOpenTimes.length === 0) {
      //如果没有设置开放时间，所有时间段都不可选
      const newTimeSlots = timeSlots.map(slot => ({
        ...slot,
        disabled: true,
        selected: false
      }))
      this.setData({
        timeSlots: newTimeSlots
      })
      return
    }

    //获取选中日期是星期几
    const date = new Date(selectedDate)
    const weekday = date.getDay()
    const weekNum = weekday === 0 ? 7 : weekday

    //找出该星期几对应的开放时间段
    const availableTimeRanges = []
    for (const openTime of deviceOpenTimes) {
      const weeks = openTime.week.split(',').map(w => parseInt(w))
      if (weeks.includes(weekNum)) {
        //处理时间，从UTC时间字符串中提取本地时间
        let startTime = openTime.start_time
        let endTime = openTime.end_time

        //如果是ISO格式字符串，直接解析并提取本地时间
        if (startTime && startTime.includes('T')) {
          //创建Date对象，自动处理时区
          const startDate = new Date(startTime)
          const endDate = new Date(endTime)

          //获取本地时间的小时和分钟
          const startHour = startDate.getHours().toString().padStart(2, '0')
          const startMin = startDate.getMinutes().toString().padStart(2, '0')
          const endHour = endDate.getHours().toString().padStart(2, '0')
          const endMin = endDate.getMinutes().toString().padStart(2, '0')

          startTime = `${startHour}:${startMin}`
          endTime = `${endHour}:${endMin}`
        } else if (startTime && startTime.includes(':')) {
          //如果是 "08:30:00" 格式，取前5位
          startTime = startTime.substring(0, 5)
          endTime = endTime.substring(0, 5)
        }

        availableTimeRanges.push({
          start: startTime,
          end: endTime
        })
      }
    }

    console.log('该星期可预约时间段：', availableTimeRanges)
    console.log('所有时间段：', timeSlots.map(s => ({
      label: s.label,
      start: s.start,
      end: s.end
    })))

    //标记时间段是否在开放时间内
    const newTimeSlots = timeSlots.map(slot => {
      let isAvailable = false

      //检查当前时间段是否在任一开放时间段内
      for (const range of availableTimeRanges) {
        //判断时间段是否有重叠（只要部分重叠即可预约）
        if (slot.start < range.end && slot.end > range.start) {
          isAvailable = true
          break
        }
      }

      return {
        ...slot,
        disabled: !isAvailable,
        selected: false
      }
    })

    this.setData({
      timeSlots: newTimeSlots
    }, () => {
      //然后再标记被占用的时段（在已开放的时段基础上标记占用）
      this.markOccupiedTimeSlots()
    })
  },

  //标记被占用的时间段（合并设备和实验室的预约）
  markOccupiedTimeSlots() {
    const {
      deviceReservationArr,
      labReservationArr,
      timeSlots,
      selectedDate
    } = this.data

    if (!selectedDate) {
      return
    }

    //合并所有预约数据（设备预约 + 实验室预约）
    const allReservations = [...(deviceReservationArr || []), ...(labReservationArr || [])]

    if (allReservations.length === 0) {
      return
    }

    console.log('所有预约数据：', allReservations)

    //创建新的timeSlots数组
    const newTimeSlots = timeSlots.map(slot => {
      //如果时间段已经因为不在开放时间内而禁用，则保持不变
      if (slot.disabled) {
        return slot
      }

      //检查当前时间段在选中的日期是否被占用
      const isOccupied = allReservations.some(reservation => {
        const startTime = new Date(reservation.start_time)
        const endTime = new Date(reservation.end_time)

        //获取预约日期的年月日
        const resYear = startTime.getFullYear()
        const resMonth = String(startTime.getMonth() + 1).padStart(2, '0')
        const resDay = String(startTime.getDate()).padStart(2, '0')
        const resDateStr = `${resYear}-${resMonth}-${resDay}`

        //如果预约日期不等于当前选中的日期，则不冲突
        if (resDateStr !== selectedDate) return false

        //获取预约时间的小时分钟部分
        const resStartHour = String(startTime.getHours()).padStart(2, '0')
        const resStartMin = String(startTime.getMinutes()).padStart(2, '0')
        const resEndHour = String(endTime.getHours()).padStart(2, '0')
        const resEndMin = String(endTime.getMinutes()).padStart(2, '0')

        const resStart = `${resStartHour}:${resStartMin}`
        const resEnd = `${resEndHour}:${resEndMin}`

        //判断时间段是否重叠
        return (slot.start < resEnd && slot.end > resStart)
      })

      return {
        ...slot,
        disabled: isOccupied,
        selected: slot.selected && !isOccupied
      }
    })

    this.setData({
      timeSlots: newTimeSlots
    })
  },

  //选择时间段
  onTimeSelect(e) {
    const index = e.currentTarget.dataset.index
    const timeSlots = this.data.timeSlots

    //如果该时间段已被占用或不在开放时间内，则不能选择
    if (timeSlots[index].disabled) {
      wx.showToast({
        title: '该时间段不可预约',
        icon: 'none',
        duration: 1500
      })
      return
    }

    //如果已经选中了其他时间段，先取消选中
    const newTimeSlots = timeSlots.map((slot, i) => ({
      ...slot,
      selected: i === index ? !slot.selected : false
    }))

    //获取选中的时间段
    const selectedTimeObject = newTimeSlots.find(item => item.selected)

    this.setData({
      timeSlots: newTimeSlots,
      selectedTimeObject: selectedTimeObject || null
    })
  },

  //输入事件，输入设备使用用途
  onInputPurpose(e) {
    this.setData({
      purpose: e.detail.value
    })
  },

  //点击事件，确认预约
  checkReservation() {
    const {
      selectedDate,
      selectedTimeObject,
      purpose,
      device_id,
      device_name,
      user_id,
      manager_id,
    } = this.data

    if (!selectedDate) {
      wx.showToast({
        title: '请先选择预约日期',
        icon: 'none',
        duration: 1500
      })
      return
    }

    if (!selectedTimeObject) {
      wx.showToast({
        title: '请选择一个时间段',
        icon: 'none',
        duration: 1500
      })
      return
    }

    if (!purpose || purpose.trim() === '') {
      wx.showToast({
        title: '请输入使用用途',
        icon: 'none',
        duration: 1500
      })
      return
    }

    //构建完整的时间数据
    const startTime = selectedDate + ' ' + selectedTimeObject.start + ':00'
    const endTime = selectedDate + ' ' + selectedTimeObject.end + ':00'

    const contentDevice = `预约设备：${device_name}`
    const contentTime = `预约时间：${selectedDate} ${selectedTimeObject.label} , 使用用途：${purpose}`

    wx.showModal({
      title: contentDevice,
      content: contentTime,
      complete: (res) => {
        if (res.cancel) {
          return
        }

        if (res.confirm) {
          wx.showLoading({
            title: '提交中...',
          })

          //添加设备预约数据
          wx.request({
            url: app.getApiUrl('/checkReservation'),
            method: 'POST',
            data: {
              user_id: parseInt(user_id),
              device_id: parseInt(device_id),
              start_time: startTime,
              end_time: endTime,
              purpose: purpose,
              manager_id: parseInt(manager_id),
              status: '使用中'
            },
            header: {
              'content-type': 'application/json'
            },

            success: (res) => {
              wx.hideLoading()
              const result = res.data

              if (result.status == 200) {

                wx.reLaunch({
                  url: '/pages/index/index?reservationSuccess=true&message=预约成功'
                });

              } else if (result.status == 409) {

                //刷新页面
                this.getUserData()

                wx.showToast({
                  title: '预约时间冲突，请重新选择预约时间',
                  icon: 'error',
                  duration: 1500
                })
              } else {
                wx.showToast({
                  title: result.message || '预约失败',
                  icon: 'error',
                  duration: 1500
                })
              }
            },
            fail: (error) => {
              wx.hideLoading()
              console.error('预约失败:', error)
              wx.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none',
                duration: 2000
              })
            },
          })
        }
      }
    })
  },
})