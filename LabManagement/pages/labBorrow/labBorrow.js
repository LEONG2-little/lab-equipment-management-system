// pages/labBorrow/labBorrow.js
const app = getApp()

Page({

  data: {
    user_id: '', //用户id
    labBorrowArr: [], //场地借出的数据
    filteredLabArr: [], //搜索后的场地数据
    searchKeyword: '', //搜索关键字
    showFiltered: false, //是否显示搜索结果
    statusOptions: ['全部', '正常', '已取消', '系统自动归还'], //状态选项
    selectedStatus: '全部', //当前选中的状态
    toastMessage: '',
  },

  onLoad() {
    const userDataStr = wx.getStorageSync('UserData')
    const userData = JSON.parse(userDataStr)

    this.setData({
      user_id: userData.user_id
    })

    //获取场地借出的信息
    this.getLabBorrow()
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

    //重新获取数据
    this.getLabBorrow()
  },

  //获取场地借出列表
  getLabBorrow() {
    wx.showLoading({
      title: '加载中...',
    })

    wx.request({
      url: app.getApiUrl('/getLabBorrow'),
      method: 'POST',
      data: {
        manager_id: this.data.user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        const result = res.data

        console.log('getLabBorrow 返回结果:', result)

        if (result.status !== 200) {
          wx.showToast({
            title: result.message || '获取失败',
            icon: 'none',
          })
          return
        }

        if (result.status === 200) {
          this.setData({
            labBorrowArr: result.data || [],
          })

          //从索引0开始获取实验室详细信息
          this.getLabDetail(0)
        }
      },
      fail: (error) => {
        wx.hideLoading()
        console.error('获取场地借出列表失败:', error)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
          duration: 2000
        })
      }
    })
  },

  //从索引0开始获取实验室详细信息
  getLabDetail(index) {
    const labBorrowArr = this.data.labBorrowArr

    //如果索引大于等于数组长度
    if (index >= labBorrowArr.length) {
      //所有实验室详情获取完成，开始获取借用人信息
      this.getBorrowerDetail(0)
      return
    }

    //获取当前索引的实验室
    const lab = labBorrowArr[index]
    const laboratory_id = lab.laboratory_id

    //获取实验室详细信息
    wx.request({
      url: app.getApiUrl('/getLaboratoryDetail'),
      method: 'POST',
      data: {
        laboratory_id: laboratory_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {
          //将实验室详情合并到当前实验室对象中
          const key = `labBorrowArr[${index}].labDetail`
          this.setData({
            [key]: result.data
          })
        } else {
          console.warn(`实验室 ${laboratory_id} 详情获取失败:`, result.message)
        }

        //继续获取下一个实验室的详情
        this.getLabDetail(index + 1)
      },
      fail: (error) => {
        console.error(`实验室 ${laboratory_id} 详情请求失败:`, error)

        //即使失败也继续获取下一个实验室的详情
        this.getLabDetail(index + 1)
      }
    })
  },

  //从索引0开始获取实验室借用人详细信息
  getBorrowerDetail(index) {
    const labBorrowArr = this.data.labBorrowArr

    //如果索引大于等于数组长度
    if (index >= labBorrowArr.length) {
      //所有实验室借用人详情获取完成
      this.setData({
        filteredLabArr: this.data.labBorrowArr
      })
      console.log('实验室借出数据加载完成:', this.data.filteredLabArr)
      return
    }

    //获取当前索引的实验室
    const lab = labBorrowArr[index]
    const user_id = lab.user_id

    //获取实验室借用人详细信息
    wx.request({
      url: app.getApiUrl('/getUserDetail'),
      method: 'POST',
      data: {
        user_id: user_id
      },
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        const result = res.data
        if (result.status === 200) {
          //取数组的第一个元素
          const borrowerData = result.data[0]

          //将实验室借用人详情合并到当前实验室对象中
          const key = `labBorrowArr[${index}].borrowerDetail`
          this.setData({
            [key]: borrowerData
          })
        } else {
          console.warn(`借用人 ${user_id} 详情获取失败:`, result.message)
        }

        //继续获取下一个实验室借用人的详情
        this.getBorrowerDetail(index + 1)
      },
      fail: (error) => {
        console.error(`借用人 ${user_id} 详情请求失败:`, error)

        //即使失败也继续获取下一个实验室借用人的详情
        this.getBorrowerDetail(index + 1)
      }
    })
  },

  //输入事件，输入搜索关键字
  onSearchInput(e) {
    const keyword = e.detail.value.trim()
    this.setData({
      searchKeyword: keyword
    })
    this.filterLabs(keyword, this.data.selectedStatus)
  },

  //搜索对应的实验室
  filterLabs(keyword, status) {
    const labBorrowArr = this.data.labBorrowArr

    //如果没有搜索关键字且状态为全部
    if (!keyword && status === "全部") {
      this.setData({
        filteredLabArr: labBorrowArr,
        showFiltered: false
      })
      return
    }

    //获取搜索到的实验室
    const filtered = labBorrowArr.filter(lab => {
      //名称匹配（实验室位置）
      let nameMatch = true
      if (keyword) {
        nameMatch = lab.labDetail && lab.labDetail.location &&
          lab.labDetail.location.toLowerCase().includes(keyword.toLowerCase())
      }
      //状态匹配
      let statusMatch = true
      if (status && status !== '全部') {
        statusMatch = lab.status === status
      }
      return nameMatch && statusMatch
    })

    this.setData({
      filteredLabArr: filtered,
      showFiltered: true
    })
  },

  //选择要搜索的状态
  onStatusChange(e) {
    const status = this.data.statusOptions[e.detail.value]
    this.setData({
      selectedStatus: status
    })
    this.filterLabs(this.data.searchKeyword, status)
  },

  //点击事件，跳转到实验室详情页面
  toLabDetail(e) {
    const index = e.currentTarget.dataset.index
    const labs = this.data.showFiltered ? this.data.filteredLabArr : this.data.labBorrowArr
    const lab = labs[index]

    console.log('跳转传递参数:', {
      laboratory_id: lab.laboratory_id,
      reservation_id: lab.lab_reservation_id,
      username: lab.borrowerDetail?.username
    })

    if (lab && lab.labDetail) {
      wx.navigateTo({
        url: `/pages/labDetail/labDetail?laboratory_id=${lab.laboratory_id}&reservation_id=${lab.lab_reservation_id}&username=${lab.borrowerDetail?.username || ''}`,
      })
    }
  }
})