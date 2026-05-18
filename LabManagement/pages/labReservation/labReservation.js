// pages/labReservation/labReservation.js
const app = getApp()

Page({

  data: {
    permission_id: '', //权限id
    max_reserve_days: '', //最多预约实验室数量（复用字段）
    permission_level: '', //权限等级
    labsArr: [], //可预约的实验室列表
    filteredLabsArr: [], //搜索后的实验室列表
    showFiltered: false, //是否显示搜索后的实验室列表
    searchKeyword: '', //搜索关键字
  },

  onLoad() {
    this.loadUserData()
  },

  onShow() {
    this.loadUserData()
  },

  //获取用户信息
  loadUserData() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    this.setData({
      permission_id: userData.permission_id
    })

    //获取权限等级对应的信息
    this.getPermission_level()
  },

  //根据权限id获取权限等级对应的信息
  getPermission_level() {
    wx.request({
      url: app.getApiUrl('/getPermission'),
      method: 'POST',
      data: {
        permission_id: this.data.permission_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取权限信息失败',
            icon: 'none'
          })
          return
        }

        if (result.status == 200) {
          this.setData({
            permission_level: result.data.permission_level,
            max_reserve_days: result.data.max_reserve_days
          })

          //获取所有实验室
          this.getAllLaboratory()
        }
      }
    })
  },

  //获取所有实验室
  getAllLaboratory() {
    wx.request({
      url: app.getApiUrl('/getCanBorrowLab'),
      method: 'GET',
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data

        if (result.status !== 200) {
          wx.showToast({
            title: '获取实验室信息失败',
            icon: 'error',
            duration: 1500
          })
          return
        }

        if (result.status == 200) {
          // 处理图片URL，拼接完整地址
          const labs = (result.data || []).map(lab => {
            if (lab.image_url && !lab.image_url.startsWith('http://') && !lab.image_url.startsWith('https://')) {
              lab.image_url = app.globalData.imageBaseUrl + lab.image_url
            }
            return lab
          })

          this.setData({
            labsArr: labs,
            filteredLabsArr: labs,
            showFiltered: false
          })
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('获取实验室信息失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //点击事件，选择要预约的实验室
  toLabReservationData(e) {
    const index = e.currentTarget.dataset.index
    const labs = this.data.showFiltered ? this.data.filteredLabsArr : this.data.labsArr
    const lab = labs[index]

    if (lab && lab.laboratory_id) {
      //跳转到实验室预约页面（labReservationData）
      wx.navigateTo({
        url: `/pages/labReservationData/labReservationData?laboratory_id=${lab.laboratory_id}&max_reserve_days=${this.data.max_reserve_days}`,
      })
    }
  },

  //输入事件，输入搜索关键字
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({
      searchKeyword: keyword
    })
    this.filterLabs(keyword)
  },

  //根据关键字搜索实验室
  filterLabs(keyword) {
    if (!keyword) {
      this.setData({
        filteredLabsArr: this.data.labsArr,
        showFiltered: false
      })
      return
    }

    const filtered = this.data.labsArr.filter(lab => {
      const name = lab.laboratory_name ? lab.laboratory_name.toLowerCase() : ''
      const location = lab.location ? lab.location.toLowerCase() : ''
      const kw = keyword.toLowerCase()
      return name.includes(kw) || location.includes(kw)
    })

    this.setData({
      filteredLabsArr: filtered,
      showFiltered: true
    })
  }
})