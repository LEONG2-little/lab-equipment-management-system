// pages/myDeviceDetails/myDeviceDetails.js
const app = getApp()

Page({

  data: {
    device_id: '', //设备id
    deviceDetailArr: null, //设备详细信息数据
    openTimesText: '暂无', //开放机时文本
    openTimeArr: null,
    user_id: '',
    toastMessage: '',
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      device_id: options.device_id,
      user_id: userData.user_id
    })

    //获取设备详细信息
    this.getDeviceDetail()
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

    if (this.data.device_id) {
      this.getDeviceDetail()
    }
  },

  //获取设备详细信息
  getDeviceDetail() {
    wx.request({
      url: app.getApiUrl('/getDeviceDetail'),
      method: 'POST',
      data: {
        device_id: this.data.device_id
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取设备详情失败',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            deviceDetailArr: result.data
          })

          //获取设备开放时间
          this.getDeviceOpenTimes()
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

  //获取设备开放时间
  getDeviceOpenTimes() {
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
        const result = res.data;
        if (result.status === 200) {
          const openTimes = result.data || [];
          this.setData({ openTimeArr: openTimes }); 
          if (openTimes.length === 0) {
            this.setData({
              openTimesText: '暂无'
            });
            return;
          }

          //收集所有涉及的时间段（去重）
          const timeSlotSet = new Set();

          //收集所有涉及的星期（去重并排序）
          const weekSet = new Set();

          for (const openTime of openTimes) {
            //处理时间格式，提取 HH:MM
            let startTime = openTime.start_time;
            let endTime = openTime.end_time;
            if (startTime && startTime.includes('T')) {
              const startDate = new Date(startTime);
              const endDate = new Date(endTime);
              startTime = `${startDate.getHours().toString().padStart(2, '0')}:${startDate.getMinutes().toString().padStart(2, '0')}`;
              endTime = `${endDate.getHours().toString().padStart(2, '0')}:${endDate.getMinutes().toString().padStart(2, '0')}`;
            } else if (startTime && startTime.includes(':')) {
              startTime = startTime.substring(0, 5);
              endTime = endTime.substring(0, 5);
            }
            const timeSlot = `${startTime}-${endTime}`;
            timeSlotSet.add(timeSlot);

            //解析星期字段，如 "1,2,5"
            const weeks = openTime.week.split(',').map(w => parseInt(w));
            weeks.forEach(w => weekSet.add(w));
          }

          //将星期数字转为中文，并按周一至周日排序
          const weekMap = {
            1: '周一',
            2: '周二',
            3: '周三',
            4: '周四',
            5: '周五',
            6: '周六',
            7: '周日'
          };
          const sortedWeeks = Array.from(weekSet).sort((a, b) => a - b);
          const weekStr = sortedWeeks.map(w => weekMap[w]).join('、');

          //时间段列表转为字符串
          const timeSlotsStr = Array.from(timeSlotSet).sort().join('、');

          //组合最终显示文本
          const displayText = `每周${weekStr} ${timeSlotsStr}`;

          this.setData({
            openTimesText: displayText
          });
        }
      },
      fail: (error) => {
        console.error('获取设备开放时间失败:', error);
      }
    });
  },

  //点击事件，点击修改状态按钮
  toChangeStatus() {
    const deviceArray = [this.data.deviceDetailArr];
    const deviceDataStr = JSON.stringify(deviceArray)

    //跳转到修改设备状态页面（changeStatus）
    wx.navigateTo({
      url: `/pages/changeStatus/changeStatus?selectedDevices=${encodeURIComponent(deviceDataStr)}`,
    })
  },

  //点击事件，点击开放机时按钮
  toSelectOpenTime() {
    const deviceArray = [this.data.deviceDetailArr];
    const deviceDataStr = JSON.stringify(deviceArray)

    //跳转到选择开放机时页面（selectOpenTime）
    wx.navigateTo({
      url: `/pages/selectOpenTime/selectOpenTime?selectedDevices=${encodeURIComponent(deviceDataStr)}`,
    })
  },

  //点击事件，点击开放机时按钮
  toUpdataDeviceOpenTime() {
    const deviceArray = [this.data.deviceDetailArr];
    const deviceDataStr = JSON.stringify(deviceArray)

    //跳转到修改开放机时页面（selectOpenTime）
    wx.navigateTo({
      url: `/pages/updataDeviceOpenTime/updataDeviceOpenTime?selectedDevices=${encodeURIComponent(deviceDataStr)}`,
    })
  },

  //停止开放机时
  disableDeviceOpenTime() {
    //获取当前设备的所有开放时间记录
    const openTimeArr = this.data.openTimeArr

    if (!openTimeArr || openTimeArr.length === 0) {
      wx.showToast({
        title: '暂无开放机时设置',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '停止确认',
      content: '确认要停止开放机时吗？停止后用户将无法预约该设备。',
      complete: (res) => {
        if (res.cancel) {
          return
        }

        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          })

          // 获取第一条开放时间记录（因为只有一条）
          const openTime = openTimeArr[0]

          wx.request({
            url: app.getApiUrl('/disableDeviceOpenTime'),
            method: 'POST',
            data: {
              device_id: this.data.device_id
            },
            header: {
              'content-type': 'application/json'
            },
            success: (res) => {
              wx.hideLoading()
              if (res.data.status == 200) {
                wx.showToast({
                  title: '停止成功',
                  icon: 'success',
                  duration: 1500
                })
                //刷新页面数据
                setTimeout(() => {
                  this.getDeviceDetail()
                }, 1500)
              } else {
                wx.showToast({
                  title: res.data.message || '停止失败',
                  icon: 'none'
                })
              }
            },
            fail: (error) => {
              wx.hideLoading()
              console.error('停止开放机时失败:', error)
              wx.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  Scrap() {
    //显示弹窗，让用户输入取消理由
    wx.showModal({
      title: '设备报废',
      editable: true,
      placeholderText: '请输入报废理由',
      confirmText: '确认报废',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          const reason = res.content
          if (!reason || reason.trim() === '') {
            wx.showToast({
              title: '请填写报废理由',
              icon: 'none',
              duration: 1500
            })
            return
          }

          this.submitScrap(reason)
        }
      }
    })
  },

  //提交报废申请
  submitScrap(reason) {
    wx.showLoading({
      title: '提交中...',
    })

    wx.request({
      url: app.getApiUrl('/scrap'),
      method: 'POST',
      data: {
        device_id: parseInt(this.data.device_id),
        manager_id: parseInt(this.data.user_id),
        reason: reason
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        const result = res.data

        if (result.status == 200) {
          wx.showToast({
            title: '报废成功',
            icon: 'success',
            duration: 1500
          })
          this.getDeviceDetail()

        } else {
          wx.showToast({
            title: result.message || '取消失败',
            icon: 'error',
            duration: 1500
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('取消预约失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },
})