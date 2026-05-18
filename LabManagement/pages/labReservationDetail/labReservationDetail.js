// pages/labReservationDetail/labReservationDetail.js
const app = getApp()

Page({

  data: {
    lab_reservation_id: '', //预约id
    laboratory_id: '', //实验室id
    labReservationDetailArr: {}, //预约详情数据
    labDetailArr: {}, //实验室详情数据
    deviceNames: '' //该实验室下的设备名称，用顿号隔开
  },

  onLoad(options) {
    this.setData({
      lab_reservation_id: options.lab_reservation_id,
      laboratory_id: options.laboratory_id
    })

    //获取预约详情数据
    this.getLabReservationDetail()

    //获取实验室详情数据
    this.getLaboratoryDetail()
  },

  //获取预约详情数据
  getLabReservationDetail() {
    wx.request({
      url: app.getApiUrl('/getLabReservationDetail'),
      method: 'POST',
      data: {
        lab_reservation_id: parseInt(this.data.lab_reservation_id)
      },
      header: {
        'content-type': 'application/json'
      },

      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '未获取到预约详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          //后端返回的是数组，取第一个元素
          const reservationData = Array.isArray(result.data) ? result.data[0] : result.data
          this.setData({
            labReservationDetailArr: reservationData || {}
          })
        }
      },

      fail: (error) => {
        console.error('获取预约详情失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },

  //获取实验室详情数据
  getLaboratoryDetail() {
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

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '未获取到实验室详情',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            labDetailArr: result.data
          })
          //获取实验室详情后，获取该实验室下的设备列表
          this.getDeviceByLab()
        }
      },

      fail: (error) => {
        console.error('获取实验室详情失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      },
    })
  },

  //获取该实验室下的设备
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
  }
})