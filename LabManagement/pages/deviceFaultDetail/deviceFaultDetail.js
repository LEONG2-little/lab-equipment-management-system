// pages/deviceFaultDetail/deviceFaultDetail.js
const app = getApp()

Page({

  data: {
    device_id: '', //设备ID
    faultReportArr: [], //故障上报数据
    deviceArr: [], //设备数据
    userArr: [], //用户数据
    faultImageUrl: '', // 故障图片完整URL
  },

  onLoad(options) {
    this.setData({
      device_id: options.device_id
    })
    //获取故障上报信息
    this.selectByDeviceId()
  },

  //获取故障上报信息
  selectByDeviceId() {
    wx.request({
      url: app.getApiUrl('/selectByDeviceId'),
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
          const faultData = result.data;
          this.setData({
            faultReportArr: faultData
          })
          // 如果有图片，处理图片URL
          if (faultData.images) {
            this.processFaultImage(faultData.images);
          }
          this.getDeviceDetail()
        }
      },
    })
  },

  //处理故障图片
  processFaultImage(imageUrl) {
    if (!imageUrl) return;
    
    // 如果已经是完整URL则保留，否则拼接
    let fullImageUrl = imageUrl;
    if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
      fullImageUrl = app.globalData.faultImageBaseUrl + imageUrl;
    }
    
    this.setData({ faultImageUrl: fullImageUrl });
  },

  //预览图片
  previewFaultImage() {
    const imageUrl = this.data.faultImageUrl;
    if (!imageUrl) return;
    wx.previewImage({
      current: imageUrl,
      urls: [imageUrl]
    });
  },

  //获取设备详细信息
  getDeviceDetail() {
    wx.request({
      url: app.getApiUrl('/getDeviceDetail'),
      method: 'POST',
      data: {
        device_id: this.data.faultReportArr.device_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status == 200) {
          this.setData({ deviceArr: result.data })
          this.getUserDetail()
        }
      },
      fail: (error) => {
        console.error(`设备详情获取失败:`, error)
      }
    })
  },

  //获取用户数据
  getUserDetail() {
    wx.request({
      url: app.getApiUrl('/getUserDetail'),
      method: 'POST',
      data: {
        user_id: this.data.faultReportArr.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status == 200) {
          const userData = result.data && result.data.length > 0 ? result.data[0] : {}
          this.setData({ userArr: userData })
        }
      },
      fail: (error) => {
        console.error(`用户详情获取失败:`, error)
      }
    })
  },

  //点击事件，故障误报
  toCancelFault() {
    wx.showModal({
      title: '故障上报',
      content: '确认要为故障误报吗',
      complete: (res) => {
        if (res.cancel) return
        if (res.confirm) {
          wx.request({
            url: app.getApiUrl('/changeDeviceStatus'),
            method: 'POST',
            data: {
              deviceIds: [parseInt(this.data.faultReportArr.device_id)],
              status: '空闲'
            },
            header: { 'content-type': 'application/json' },
            success: (res) => {
              wx.hideLoading()
              const result = res.data
              if (result.status !== 200) {
                wx.showToast({ title: result.message, icon: 'none' })
                return
              }
              if (result.status === 200) {
                const pages = getCurrentPages();
                const prevPage = pages[pages.length - 2];
                if (prevPage) {
                  prevPage.setData({ toastMessage: '故障取消成功' });
                }
                wx.navigateBack();
              }
            },
            fail: (error) => {
              wx.hideLoading()
              console.error('操作失败:', error)
              wx.showToast({ title: '网络错误，请稍后重试', icon: 'none', duration: 2000 })
            }
          })
        }
      }
    })
  },

  //点击事件，确定故障
  toConfirmFault() {
    wx.showModal({
      title: '故障上报',
      content: '确认该设备发生故障吗',
      complete: (res) => {
        if (res.cancel) return
        if (res.confirm) {
          wx.request({
            url: app.getApiUrl('/changeDeviceStatus'),
            method: 'POST',
            data: {
              deviceIds: [parseInt(this.data.faultReportArr.device_id)],
              status: '故障'
            },
            header: { 'content-type': 'application/json' },
            success: (res) => {
              wx.hideLoading()
              const result = res.data
              if (result.status !== 200) {
                wx.showToast({ title: result.message, icon: 'none' })
                return
              }
              if (result.status === 200) {
                const pages = getCurrentPages();
                const prevPage = pages[pages.length - 2];
                if (prevPage) {
                  prevPage.setData({ toastMessage: '确定故障成功' });
                }
                wx.navigateBack();
              }
            },
            fail: (error) => {
              wx.hideLoading()
              console.error('操作失败:', error)
              wx.showToast({ title: '网络错误，请稍后重试', icon: 'none', duration: 2000 })
            }
          })
        }
      }
    })
  },
})