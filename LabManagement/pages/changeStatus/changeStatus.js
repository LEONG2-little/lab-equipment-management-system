// pages/changeStatus/changeStatus.js
const app = getApp()

Page({
  data: {
    deviceData: [], //设备数据
    deviceIds: [], //设备id数组
    statusOptions: ['闲置', '课题组专用', '故障'], //状态选项
    deviceStatus: '',
    changeStatus: '', //要修改成的状态
    faultReason: '', //故障原因

    //消息数据
    message: {
      is_read: false,
      user_id: '',
      title: '修改设备状态通知',
      content: ''
    }
  },

  onLoad(options) {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      'message.user_id': userData.user_id
    })

    if (options.selectedDevices) {
      const deviceData = JSON.parse(decodeURIComponent(options.selectedDevices))
      const deviceIds = deviceData.map(item => item.device_id)

      this.setData({
        deviceData: deviceData,
        deviceIds: deviceIds,
        deviceStatus: deviceData[0]?.status, // 默认显示当前状态
        changeStatus: deviceData[0]?.status,
      })
    }
  },

  //点击事件，选择要修改为什么状态
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({
      changeStatus: status
    })
  },

  onInputReason(e) {
    this.setData({
      faultReason: e.detail.value
    })
  },

  //点击事件，点击确认修改
  confirmChange() {

    if (this.data.deviceStatus == this.data.changeStatus) {
      wx.showToast({
        title: '要修改的状态与当前状态一致',
        icon: 'none'
      })
      return
    }

    if (this.data.changeStatus == "故障" && !this.data.faultReason) {
      wx.showToast({
        title: '未填写故障原因',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '修改确认',
      content: '确认修改设备状态吗',
      complete: (res) => {
        if (res.cancel) {
          return
        }

        if (res.confirm) {
          wx.request({
            url: app.getApiUrl('/changeDeviceStatus'),
            method: 'POST',
            data: {
              deviceIds: this.data.deviceIds,
              status: this.data.changeStatus,
              faultReason: this.data.faultReason
            },
            header: {
              'content-type': 'application/json'
            },

            success: (res) => {
              const result = res.data

              if (result.status !== 200) {
                wx.showToast({
                  title: result.message,
                  icon: 'none'
                })
                return
              }

              if (result.status == 200) {

                //遍历设备数组拿到所有设备的名称
                const deviceNames = this.data.deviceData.map(item => item.device_name || item.name || '未知设备').join('、')

                this.setData({
                  'message.content': `成功将设备 “${deviceNames}” 从 “${this.data.deviceData[0].status}” 状态修改为 “${this.data.changeStatus}” 状态`
                })

                this.sendMessage()

                const pages = getCurrentPages();
                const prevPage = pages[pages.length - 2]; //上一页实例
                if (prevPage) {
                  prevPage.setData({
                    toastMessage: '修改成功'
                  });
                }
                wx.navigateBack();
              }
            }
          })
        }
      }
    })
  },

  //发送信息
  sendMessage() {
    wx.request({
      url: app.getApiUrl('/sendMessage'),
      method: 'POST',
      data: this.data.message,
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '消息失败',
            icon: 'none'
          })
          return
        }
      }
    })
  },
})