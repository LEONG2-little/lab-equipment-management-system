// pages/selectLabOpenTime/selectLabOpenTime.js
const app = getApp()

Page({

  data: {
    //实验室数据
    labData: {},

    //设置数据
    setData: {
      laboratory_id: '',
      manager_id: '',
    },

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

    //选中的星期（数组，存储value值）
    selectedWeeks: [],
    selectedWeeksStr: '',

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
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      'setData.laboratory_id': options.laboratory_id,
      'setData.manager_id': userData.user_id,
    })

    //获取实验室详细信息
    this.getLabDetail()
  },

  //获取实验室详细信息
  getLabDetail() {
    wx.request({
      url: app.getApiUrl('/getLaboratoryDetail'),
      method: 'POST',
      data: {
        laboratory_id: parseInt(this.data.setData.laboratory_id)
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status == 200) {
          this.setData({
            labData: result.data
          })
        } else {
          wx.showToast({
            title: result.message || '获取实验室信息失败',
            icon: 'none'
          })
        }
      },
      fail: (error) => {
        console.error('获取实验室信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      }
    })
  },

  //切换星期选择
  toggleWeek(e) {
    const index = e.currentTarget.dataset.index
    const weekOptions = this.data.weekOptions
    const week = weekOptions[index]

    week.selected = !week.selected

    //更新选中的星期列表
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

    //更新选中的时间段列表
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

  //点击确认开放机时
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

    //构建开放机时数据
    //一条记录对应：一个实验室 + 一个时间段 + 多个星期（用逗号分隔）
    const openTimeList = []

    for (const timeSlot of this.data.selectedTimeObjects) {
      openTimeList.push({
        laboratory_id: parseInt(this.data.setData.laboratory_id),
        manager_id: parseInt(this.data.setData.manager_id),
        week: this.data.selectedWeeksStr, // 如 "1,2,5"
        start_time: timeSlot.start,
        end_time: timeSlot.end,
        status: '空闲'
      })
    }

    console.log('提交数据:', openTimeList)

    wx.showLoading({
      title: '提交中...',
    })

    //设置实验室的开放机时
    wx.request({
      url: app.getApiUrl('/setOpenLabTime'),
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
            title: result.message || '设置失败',
            icon: 'none'
          })
          return
        }

        if (result.status == 200) {
          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2]; //上一页实例
          if (prevPage) {
            prevPage.setData({
              toastMessage: '设置开放机时成功'
            });
          }
          wx.navigateBack();
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('设置开放机时失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },
})