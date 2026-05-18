<template>
    <!-- 简约设备管理面板：一行四卡，左图右文字 -->
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">设备预约管理</div>

                <button @click="exportToExcel" class="export-button">导出为Excel文件</button>
            </div>

            <div class="search-container">
                <div class="search-box">
                    <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索设备名称或预约人"
                        class="search-input">
                    <input type="date" v-model="startDate" @change="handleSearch" class="date-input" placeholder="开始日期">
                    <span class="date-separator">至</span>
                    <input type="date" v-model="endDate" @change="handleSearch" class="date-input" placeholder="结束日期">
                    <select v-model="searchStatus" @change="handleSearch" class="status-select">
                        <option value="">全部</option>
                        <option value="未完成">未完成</option>
                        <option value="已完成">已完成</option>
                        <option value="设备发生故障">设备发生故障</option>
                        <option value="处理中">处理中</option>
                    </select>
                    <button @click="reset">重置</button>
                </div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="device-grid">
                <div v-for="reservation in filteredReservations" :key="reservation.reservation_id" class="device-card"
                    @click="toReservationDetail(reservation)">

                    <div class="device-info">
                        <div class="info-item"><span class="label">预约ID:</span> {{ reservation.reservation_id }}</div>
                        <div class="info-item"><span class="label">设备名称:</span> {{ reservation.deviceInfo.device_name }}
                        </div>
                        <div class="info-item"><span class="label">预约人:</span> {{ reservation.userInfo.username }}</div>
                        <div class="info-item"><span class="label">预约时间:</span> {{
                            formatDateTime(reservation.start_time)}} 到 {{ formatDateTime(reservation.end_time) }}</div>
                        <div class="info-item"><span class="label">状态:</span> {{ reservation.status }}</div>
                    </div>
                </div>
            </div>

            <div v-if="filteredReservations.length === 0 && (searchKeyword || startDate || endDate)" class="no-result">
                没有找到匹配的预约
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import axios from 'axios';
import { reactive, toRefs } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as XLSX from 'xlsx';

export default {
    setup() {
        const router = useRouter()
        const state = reactive({
            reservationsArr: [],
            filteredReservations: [],
            searchKeyword: '',
            searchStatus: '',
            startDate: '',
            endDate: '',
        })

        function handleSearch() {
            const keyword = state.searchKeyword.trim().toLowerCase()
            const status = state.searchStatus
            const start = state.startDate
            const end = state.endDate

            state.filteredReservations = state.reservationsArr.filter(reservation => {
                // 关键词匹配
                let keywordMatch = true
                if (keyword) {
                    const deviceName = reservation.deviceInfo?.device_name?.toLowerCase() || ''
                    const userName = reservation.userInfo?.username?.toLowerCase() || ''
                    keywordMatch = deviceName.includes(keyword) || userName.includes(keyword)
                }

                let statusMatch = true
                if (status) {
                    statusMatch = reservation.status === status
                }

                // 日期匹配
                let dateMatch = true
                const reservationStart = reservation.start_time.split('T')[0]
                const reservationEnd = reservation.end_time.split('T')[0]

                if (start && end) {
                    // 两个日期都输入：开始时间 >= start 且 结束时间 <= end
                    dateMatch = reservationStart >= start && reservationEnd <= end
                } else if (start) {
                    // 只输入开始日期：开始时间 >= start
                    dateMatch = reservationStart >= start
                } else if (end) {
                    // 只输入结束日期：结束时间 <= end
                    dateMatch = reservationEnd <= end
                }

                return keywordMatch && statusMatch && dateMatch
            })
        }

        function reset() {
            state.startDate = ''
            state.endDate = ''
            state.searchKeyword = ''
            state.searchStatus = ''
            state.filteredReservations = state.reservationsArr
        }

        function getUserData(user_id) {
            return axios.post('/api/getUserDetail', {
                user_id: user_id
            })
                .then((userResult) => {
                    if (userResult.data.status == 200) {
                        return userResult.data.data[0]
                    } else {
                        return null
                    }
                })
                .catch(function (error) {
                    console.log(error)
                    return null
                })
        }

        function getDeviceData(device_id) {
            return axios.post('/api/getDeviceDetail', {
                device_id: device_id
            })
                .then((deviceResult) => {
                    if (deviceResult.data.status == 200) {
                        return deviceResult.data.data
                    } else {
                        return null
                    }
                })
                .catch(function (error) {
                    console.log(error)
                    return null
                })
        }

        function getAllReservation() {
            return axios.get('/api/getAllReservation')
                .then((response) => {
                    if (response.data.status == 200) {
                        return response.data.data
                    } else {
                        return null
                    }
                })
                .catch(function (error) {
                    console.log(error)
                    return null
                })
        }

        function initReservationArr() {
            getAllReservation().then(reservations => {
                const promises = reservations.map(reservation => {
                    const userPromise = getUserData(reservation.user_id)
                    const devicePromise = getDeviceData(reservation.device_id)

                    return Promise.all([userPromise, devicePromise]).then(([userInfo, deviceInfo]) => ({
                        ...reservation,
                        userInfo: userInfo,
                        deviceInfo: deviceInfo
                    }))
                })

                Promise.all(promises).then(allReservationArr => {
                    state.reservationsArr = allReservationArr
                    state.filteredReservations = allReservationArr
                })
            })
        }
        initReservationArr()

        function toReservationDetail(reservation) {
            sessionStorage.setItem('currentReservation', JSON.stringify(reservation))
            router.push('/reservationDetail')
        }

        function exportToExcel() {
            // 添加确认框
            ElMessageBox.confirm(
                '确定要导出当前筛选的预约数据吗？',
                '导出确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    // 用户点击确定后执行导出
                    try {
                        // 准备要导出的数据
                        const exportData = state.filteredReservations.map(reservation => ({
                            '预约ID': reservation.reservation_id,
                            '设备名称': reservation.deviceInfo?.device_name || '',
                            '设备型号': reservation.deviceInfo?.model || '',
                            '使用地点': reservation.deviceInfo?.location || '',
                            '预约人': reservation.userInfo?.username || '',
                            '预约提交时间': formatDateTime(reservation.created_at),
                            '预约开始时间': reservation.start_time ? reservation.start_time.split('T')[0] : '',
                            '预约结束时间': reservation.end_time ? reservation.end_time.split('T')[0] : '',
                            '设备归还时间': reservation.return_at ? formatDateTime(reservation.return_at) : '未归还'
                        }))

                        if (exportData.length === 0) {
                            ElMessage.warning('没有数据可导出')
                            return
                        }

                        // 创建工作簿和工作表
                        const wb = XLSX.utils.book_new()
                        const ws = XLSX.utils.json_to_sheet(exportData)

                        // 设置列宽（可选）
                        const colWidths = [
                            { wch: 10 }, // 预约ID
                            { wch: 20 }, // 设备名称
                            { wch: 15 }, // 设备型号
                            { wch: 30 }, // 使用地点
                            { wch: 15 }, // 预约人
                            { wch: 30 }, // 预约提交时间
                            { wch: 15 }, // 预约开始时间
                            { wch: 15 }, // 预约结束时间
                            { wch: 30 }, // 设备归还时间
                        ]
                        ws['!cols'] = colWidths

                        // 将工作表添加到工作簿
                        XLSX.utils.book_append_sheet(wb, ws, '预约列表')

                        // 生成文件名（包含当前日期）
                        const date = new Date()
                        const fileName = `预约列表_${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}.xlsx`

                        // 导出文件
                        XLSX.writeFile(wb, fileName)

                        ElMessage.success(`成功导出 ${exportData.length} 条记录`)
                    } catch (error) {
                        console.error('导出失败:', error)
                        ElMessage.error('导出失败')
                    }
                })
                .catch(() => {
                    // 用户点击取消，什么都不做
                    ElMessage.info('已取消导出')
                })
        }

        function formatDateTime(dateTimeStr) {
            if (!dateTimeStr) return ''

            // 创建 Date 对象（会自动处理时区）
            const date = new Date(dateTimeStr)

            // 获取本地时间各部分
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            const hours = String(date.getHours()).padStart(2, '0')
            const minutes = String(date.getMinutes()).padStart(2, '0')
            const seconds = String(date.getSeconds()).padStart(2, '0')

            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }

        return {
            ...toRefs(state),
            toReservationDetail,
            handleSearch,
            reset,
            exportToExcel,
            formatDateTime,
        }
    },
}
</script>

<style scoped>
.device-management {
    max-width: 2000px;
    margin: 0 auto;
    padding: 20px 24px;
    box-sizing: border-box;
    font-family: system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
    height: 100%;
    min-height: 828px;
    width: 100%;
    min-width: 1300px;
    position: relative;
}

.fixed-header {
    position: fixed;
    top: 0;
    left: 250px;
    transform: none;
    width: calc(100% - 250px);
    max-width: calc(2000px - 250px);
    padding: 20px 24px 0 24px;
    background-color: #ffffff;
    z-index: 100;
    box-sizing: border-box;
}

/* 滚动内容区域 */
.scroll-content {
    margin-top: 180px;
    /* 根据头部高度调整 */
    padding-bottom: 20px;
    min-height: calc(100vh - 220px);
    display: flex;
    flex-direction: column;
}

/* 头部容器 */
.header-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;
}

/* 标题 */
.section-title {
    font-size: 1.6rem;
    font-weight: 500;
    color: #2c3e50;
    border-left: 6px solid #409eff;
    padding-left: 16px;
    line-height: 1.3;
    margin: 0;
}

.search-container {
    margin-bottom: 24px;
    width: 800px;
    /* 加宽以适应更多元素 */
}

.search-box {
    display: flex;
    gap: 10px;
    align-items: center;
}

.search-input {
    flex: 2;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    box-sizing: border-box;
}

.date-input {
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    box-sizing: border-box;
    width: 140px;
}

.date-separator {
    color: #7f8c8d;
    font-size: 14px;
}

.status-select {
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    background-color: white;
    cursor: pointer;
    width: 120px;
}

.status-select:hover {
    border-color: #c0c4cc;
}

/* 设备网格 - 固定布局，不伸缩 */
.device-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    /* 将原来的 20px 改为 12px */
    margin-bottom: 12px;
    /* 将原来的 20px 改为 12px */
}

/* 卡片样式 - 自动高度 */
.device-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 16px;
    box-sizing: border-box;
    min-height: 140px;
    /* 改为 min-height */
    height: auto;
    /* 高度自适应 */
    margin-bottom: 0;
}

.device-card:hover {
    border-color: #c0c4cc;
}


/* 右侧信息区域 */
.device-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.info-item {
    font-size: 14px;
    color: #3c4a5a;
    line-height: 1.6;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 6px;
}

.info-item:last-child {
    margin-bottom: 0;
}

.info-item .label {
    color: #7f8c8d;
    margin-right: 6px;
}

/* 无结果提示 - 固定高度，和卡片一致 */
.no-result {
    text-align: center;
    padding: 40px;
    color: #909399;
    font-size: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    height: 140px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    margin: 0;
    order: -1;
    /* 确保显示在最前面 */
}

/* 导出按钮样式 */
.export-button {
    padding: 8px 20px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    margin-left: auto;
    /* 靠右对齐 */
}

.export-button:hover {
    background-color: #85ce61;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.export-button:active {
    transform: translateY(0);
}
</style>