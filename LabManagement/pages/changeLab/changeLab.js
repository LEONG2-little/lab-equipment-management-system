// pages/changeLab/changeLab.js
const app = getApp()

Page({
  data: {
    laboratory_id: null, //实验室ID
    labData: {}, //实验室数据
    labDescription: '', //实验室描述（可编辑）
    statusOptions: ['闲置', '维修', '停用', '故障'], //状态选项
    changeStatus: '', //要修改成的状态
    faultReason: '', //故障原因

    //消息数据
    message: {
      is_read: false,
      user_id: '',
      title: '修改实验室信息通知',
      content: ''
    }
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
            labDescription: labData.description || '', //初始化描述
            changeStatus: labData.status //默认显示当前状态
          })
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

  //点击事件，点击确认修改
  confirmChange() {
    //检查是否有修改
    const statusChanged = this.data.labData.status !== this.data.changeStatus
    const descriptionChanged = this.data.labData.description !== this.data.labDescription

    if (!statusChanged && !descriptionChanged) {
      wx.showToast({
        title: '没有内容被修改',
        icon: 'none'
      })
      return
    }

    //如果要修改为故障，判断是否有填写故障原因
    if (this.data.changeStatus === "故障" && this.data.labData.status !== "故障" && !this.data.faultReason) {
      wx.showToast({
        title: '请填写故障原因',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '修改确认',
      content: '确认修改实验室信息吗？',
      complete: (res) => {
        if (res.cancel) {
          return
        }

        if (res.confirm) {
          wx.request({
            url: app.getApiUrl('/updateLaboratory'),
            method: 'POST',
            data: {
              laboratory_id: parseInt(this.data.laboratory_id),
              status: this.data.changeStatus,
              description: this.data.labDescription
            },
            header: {
              'content-type': 'application/json'
            },
            success: (res) => {
              const result = res.data

              if (result.status !== 200) {
                wx.showToast({
                  title: result.message || '修改失败',
                  icon: 'none'
                })
                return
              }

              if (result.status === 200) {

                //构建修改内容的消息
                let changeContent = ''
                if (statusChanged && descriptionChanged) {
                  changeContent = `状态从 “${this.data.labData.status}” 修改为 “${this.data.changeStatus}”，描述已更新`
                  if (this.data.changeStatus === "故障") {
                    changeContent += `，故障原因：${this.data.faultReason}`
                  }
                } else if (statusChanged) {
                  changeContent = `状态从 “${this.data.labData.status}” 修改为 “${this.data.changeStatus}”`
                  if (this.data.changeStatus === "故障") {
                    changeContent += `，故障原因：${this.data.faultReason}`
                  }
                } else if (descriptionChanged) {
                  changeContent = `实验室描述已更新`
                }

                //发送消息通知
                this.setData({
                  'message.content': `成功将实验室 “${this.data.labData.location}” 的信息修改成功。${changeContent}`
                })
                this.sendMessage()
              }
            },
            fail: () => {
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              })
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

        if (result.status == 200) {
          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2]; //上一页实例
          if (prevPage) {
            prevPage.setData({
              toastMessage: '修改成功'
            });
          }
          wx.navigateBack();
        }

        if (result.status !== 200) {
          console.error('消息发送失败', result.message)
        }
      },
      fail: (err) => {
        console.error('消息发送失败', err)
      }
    })
  },
})