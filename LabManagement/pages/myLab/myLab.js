// pages/myLab/myLab.js
const app = getApp()

Page({
  data: {
    user_id: '', //管理人id
    labArr: [], //实验室信息数据
    searchKeyword: '', //搜索关键字
    filteredLabsArr: [], //搜索后的实验室信息数据
    showFiltered: false, //是否显示搜索后的实验室
    statusOptions: ['全部', '维修', '正常', '停用'], //选项列表
    selectedStatus: '全部', //当前选中的状态
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    const userId = userData.user_id
    this.setData({ user_id: userId })

    //获取自己管理的实验室信息
    this.getMyLab()
  },

  onShow() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)
    const userId = userData.user_id
    this.setData({ user_id: userId })

    //获取自己管理的实验室信息
    this.getMyLab()
  },

  //获取自己管理的实验室信息
  getMyLab() {
    wx.request({
      url: app.getApiUrl('/getMyLab'),
      method: 'POST',
      data: {
        manager_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '获取实验室信息失败',
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
            labArr: labs,
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

  //点击事件，跳转到实验室详情页面
  toMyLabDetails(e) {
    const index = e.currentTarget.dataset.index
    const labs = this.data.showFiltered ? this.data.filteredLabsArr : this.data.labArr
    const lab = labs[index]

    if (lab && lab.laboratory_id) {
      wx.navigateTo({
        url: `/pages/myLabDetails/myLabDetails?laboratory_id=${lab.laboratory_id}`,
      })
    }
  },

  //输入事件，输入搜索关键字
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({ searchKeyword: keyword })
    this.filterLabs(keyword, this.data.selectedStatus)
  },

  //搜索对应的实验室
  filterLabs(keyword, status) {
    if (!keyword && status === "全部") {
      this.setData({
        filteredLabsArr: this.data.labArr,
        showFiltered: false
      })
      return
    }

    const filtered = this.data.labArr.filter(lab => {
      let nameMatch = true
      if (keyword) {
        nameMatch = lab.location && lab.location.toLowerCase().includes(keyword.toLowerCase())
      }
      let statusMatch = true
      if (status && status !== '全部') {
        statusMatch = lab.status === status
      }
      return nameMatch && statusMatch
    })

    this.setData({
      filteredLabsArr: filtered,
      showFiltered: true
    })
  },

  //点击事件，选择要搜索的实验室状态
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({ selectedStatus: status })
    this.filterLabs(this.data.searchKeyword, status)
  },

  //清空搜索
  clearSearch() {
    this.setData({
      searchKeyword: '',
      selectedStatus: '全部',
      showFiltered: false,
      filteredLabsArr: this.data.labArr
    })
  }
})