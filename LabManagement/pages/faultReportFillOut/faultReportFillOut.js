// pages/faultReportFillOut/faultReportFillOut.js
const app = getApp()

Page({

  data: {
    deviceArr: [], //设备信息

    //故障上报数据
    reportData: {
      user_id: '', //用户id
      device_id: '', //设备id
      description: '', //故障描述
      reservation_id: '',
      status: '可能发生故障', //要修改的设备状态
    },

    faultImagePath: '', // 临时图片路径
    hasImage: false, // 是否有图片
  },

  onLoad(options) {
    this.setData({
      'reportData.user_id': options.user_id,
      'message.user_id': options.user_id,
      'reportData.device_id': options.device_id,
      'reportData.reservation_id': options.reservation_id,
    })
    this.getDeviceDetail()
  },

  //根据设备id获得设备信息
  getDeviceDetail() {

    wx.request({
      url: app.getApiUrl('/getDeviceDetail'),
      method: 'POST',
      data: {
        device_id: this.data.reportData.device_id
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
            deviceArr: result.data
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('上报失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //输入事件，输入故障描述
  onInputDescription(e) {
    this.setData({
      'reportData.description': e.detail.value
    })
  },

  //选择图片（拍照或从相册）
  chooseImage() {
    const that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['original'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        that.setData({
          faultImagePath: tempFilePath,
          hasImage: true
        });
      },
      fail(err) {
        console.error('选择图片失败', err);
      }
    });
  },

  //删除图片
  deleteImage() {
    this.setData({
      faultImagePath: '',
      hasImage: false
    });
  },

  //预览图片
  previewImage() {
    const current = this.data.faultImagePath;
    if (!current) return;
    wx.previewImage({
      current: current,
      urls: [current]
    });
  },

  faultReport() {
    if (!this.data.reportData.description) {
      wx.showToast({
        title: '请填写故障描述',
        icon: 'none'
      });
      return;
    }

    if (!this.data.hasImage) {
      wx.showToast({
        title: '请拍摄故障图片',
        icon: 'none'
      })
      return
    } 

    wx.showModal({
      title: '故障上报',
      content: '确认要进行故障上报吗',
      complete: (res) => {
        if (res.cancel) return;
        if (res.confirm) {
          //如果有图片，先上传图片
          if (this.data.hasImage) {
            this.uploadImageAndReport();
          } else {
            this.submitFaultReport('');
          }
        }
      }
    });
  },

  //上传图片
  uploadImageAndReport() {
    wx.showLoading({
      title: '上传图片中...'
    });
    wx.uploadFile({
      url: app.getApiUrl('/uploadFaultImage'),
      filePath: this.data.faultImagePath,
      name: 'file',
      success: (res) => {
        wx.hideLoading();
        const data = JSON.parse(res.data);
        if (data.status === 200) {
          const imageUrl = data.data; //后端返回的相对路径
          this.submitFaultReport(imageUrl);
        } else {
          wx.showToast({
            title: '图片上传失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  },

  //提交故障上报
  submitFaultReport(imageUrl) {
    wx.showLoading({
      title: '提交中...'
    });
    wx.request({
      url: app.getApiUrl('/changeDeviceStatus'),
      method: 'POST',
      data: {
        deviceIds: [parseInt(this.data.reportData.device_id)],
        status: this.data.reportData.status,
        faultReason: this.data.reportData.description,
        reservation_id: parseInt(this.data.reportData.reservation_id),
        images: imageUrl
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading();
        const result = res.data;
        if (result.status === 200) {
          wx.reLaunch({
            url: '/pages/index/index?faultReportSuccess=true&message=故障上报成功'
          });
        } else {
          wx.showToast({
            title: result.message,
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  },
})