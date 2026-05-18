// pages/myLabDetails/myLabDetails.js
const app = getApp()

Page({
  data: {
    laboratory_id: null, //实验室ID
    labData: {}, //实验室数据
    labDescription: '', //实验室描述
    statusOptions: ['闲置', '维修', '停用', '故障'], //状态选项
    changeStatus: '', //要修改成的状态
    faultReason: '', //故障原因
    deviceNames: '', //该实验室下的设备名称（顿号隔开）

    //开放机时显示
    openTimesText: '暂无',

    //消息数据
    message: {
      is_read: false,
      user_id: '',
      title: '修改实验室信息通知',
      content: ''
    },

    toastMessage: ''
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      laboratory_id: options.laboratory_id,
      'message.user_id': userData.user_id
    })

    //获取实验室详情
    this.getLabDetail()
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

    this.getLabDetail()
  },

  //获取实验室详情
  getLabDetail() {
    wx.request({
      url: app.getApiUrl('/getLaboratoryDetail'),
      method: 'POST',
      data: {
        laboratory_id: parseInt(this.data.laboratory_id)
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {
          const labData = result.data
          this.setData({
            labData: labData,
            labDescription: labData.description || '',
            changeStatus: labData.status
          })

          //获取开放时间
          this.getLabOpenTimes()
          //新增：获取该实验室下的设备
          this.getDeviceByLab()
        } else {
          wx.showToast({
            title: result.message || '获取实验室详情失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },

  //新增：获取该实验室下的设备
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

  //获取实验室开放时间
  getLabOpenTimes() {
    wx.request({
      url: app.getApiUrl('/getLabOpenTimes'),
      method: 'POST',
      data: {
        laboratory_id: parseInt(this.data.laboratory_id)
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data;
        if (result.status === 200) {
          const openTimes = result.data || [];
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

  //输入描述
  onDescriptionInput(e) {
    this.setData({
      labDescription: e.detail.value
    })
  },

  //输入故障原因
  onFaultReasonInput(e) {
    this.setData({
      faultReason: e.detail.value
    })
  },

  //点击事件，选择要修改为什么状态
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({
      changeStatus: status
    })
  },

  //点击事件，跳转到修改信息页面
  toChangeLab() {
    wx.navigateTo({
      url: `/pages/changeLab/changeLab?laboratory_id=${this.data.laboratory_id}`,
    })
  },

  //点击事件，点击开放机时按钮
  toSelectLabOpenTime() {
    wx.navigateTo({
      url: `/pages/selectLabOpenTime/selectLabOpenTime?laboratory_id=${this.data.laboratory_id}`,
    })
  },

  //点击事件，点击修改开放机时按钮
  toUpdataLabOpenTime() {
    wx.navigateTo({
      url: `/pages/updataLabOpenTime/updataLabOpenTime?laboratory_id=${this.data.laboratory_id}`,
    })
  },

  //点击事件，停止开放机时
  disableLabOpenTime() {
    wx.showLoading({
      title: '提交中...'
    });

    wx.request({
      url: app.getApiUrl('/disableLabOpenTime'),
      method: 'POST',
      data: {
        laboratory_id: parseInt(this.data.laboratory_id)
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data;
        if (result.status === 200) {

          wx.showToast({
            title: '停止开放机时成功',
            icon: 'success'
          });

          this.getLabDetail()

        } else {

          wx.hideLoading();
          wx.showToast({
            title: result.message || '停用旧机时失败',
            icon: 'none'
          });
        }
      },
      fail: (error) => {
        wx.hideLoading();
        console.error('停用请求失败:', error);
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      }
    });
  },
})