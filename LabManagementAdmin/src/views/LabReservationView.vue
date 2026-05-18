<template>
    <!-- 简约实验室预约管理面板 -->
    <div class="laboratory-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">实验室预约管理</div>

                <button @click="exportToExcel" class="export-button">导出为Excel文件</button>
            </div>

            <div class="search-container">
                <div class="search-box">
                    <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索实验室名称或预约人"
                        class="search-input">
                    <input type="date" v-model="startDate" @change="handleSearch" class="date-input" placeholder="开始日期">
                    <span class="date-separator">至</span>
                    <input type="date" v-model="endDate" @change="handleSearch" class="date-input" placeholder="结束日期">
                    <select v-model="searchStatus" @change="handleSearch" class="status-select">
                        <option value="">全部</option>
                        <option value="未完成">未完成</option>
                        <option value="已取消">已取消</option>
                        <option value="已完成">已完成</option>
                        <option value="处理中">处理中</option>
                    </select>
                    <button @click="reset">重置</button>
                </div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="laboratory-grid">
                <div v-for="reservation in filteredReservations" :key="reservation.lab_reservation_id"
                    class="laboratory-card" @click="toReservationDetail(reservation)">

                    <div class="laboratory-info">
                        <div class="info-item"><span class="label">预约ID:</span> {{ reservation.lab_reservation_id }}
                        </div>
                        <div class="info-item"><span class="label">实验室名称:</span> {{
                            reservation.laboratoryInfo?.laboratory_name || '未知' }}</div>
                        <div class="info-item"><span class="label">地点:</span> {{ reservation.laboratoryInfo?.location ||
                            '' }}</div>
                        <div class="info-item"><span class="label">预约人:</span> {{ reservation.userInfo?.username ||
                            '教学安排'
                        }}</div>
                        <div class="info-item"><span class="label">预约时间:</span> {{
                            reservation.start_time ? formatDateTime(reservation.start_time) : '' }} 到 {{
                                reservation.end_time ? formatDateTime(reservation.end_time) : '' }}</div>
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
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as XLSX from 'xlsx';

export default {
    components: {
        Footer
    },
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
                // 关键词匹配（改为匹配 laboratory_name）
                let keywordMatch = true
                if (keyword) {
                    const labName = reservation.laboratoryInfo?.laboratory_name?.toLowerCase() || ''
                    const userName = reservation.userInfo?.username?.toLowerCase() || ''
                    keywordMatch = labName.includes(keyword) || userName.includes(keyword)
                }

                let statusMatch = true
                if (status) {
                    statusMatch = reservation.status === status
                }

                // 日期匹配
                let dateMatch = true
                const reservationStart = reservation.start_time ? reservation.start_time.split('T')[0] : ''
                const reservationEnd = reservation.end_time ? reservation.end_time.split('T')[0] : ''

                if (start && end) {
                    dateMatch = reservationStart >= start && reservationEnd <= end
                } else if (start) {
                    dateMatch = reservationStart >= start
                } else if (end) {
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

        function getLaboratoryData(laboratory_id) {
            return axios.post('/api/getLaboratoryDetail', {
                laboratory_id: laboratory_id
            })
                .then((labResult) => {
                    if (labResult.data.status == 200) {
                        return labResult.data.data
                    } else {
                        return null
                    }
                })
                .catch(function (error) {
                    console.log(error)
                    return null
                })
        }

        function getAllLabReservation() {
            return axios.get('/api/getAllLabReservation')
                .then((response) => {
                    if (response.data.status == 200) {
                        return response.data.data
                    } else {
                        return []
                    }
                })
                .catch(function (error) {
                    console.log(error)
                    return []
                })
        }

        function initReservationArr() {
            getAllLabReservation().then(reservations => {
                if (!reservations || reservations.length === 0) {
                    state.reservationsArr = []
                    state.filteredReservations = []
                    return
                }

                const promises = reservations.map(reservation => {
                    const userPromise = getUserData(reservation.user_id)
                    const labPromise = getLaboratoryData(reservation.laboratory_id)

                    return Promise.all([userPromise, labPromise]).then(([userInfo, labInfo]) => ({
                        ...reservation,
                        userInfo: userInfo,
                        laboratoryInfo: labInfo
                    }))
                })

                Promise.all(promises).then(allReservationArr => {
                    state.reservationsArr = allReservationArr
                    state.filteredReservations = allReservationArr
                })
            })
        }
        initReservationArr()

        // 点击卡片跳转到预约详情页
        function toReservationDetail(reservation) {
            sessionStorage.setItem('currentLabReservation', JSON.stringify(reservation))
            router.push('/labReservationDetail')
        }
        
        function exportToExcel() {
            ElMessageBox.confirm(
                '确定要导出当前筛选的实验室预约数据吗？',
                '导出确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    try {
                        const exportData = state.filteredReservations.map(reservation => ({
                            '预约ID': reservation.lab_reservation_id,
                            '实验室名称': reservation.laboratoryInfo?.laboratory_name || '',
                            '地点': reservation.laboratoryInfo?.location || '',
                            '实验室状态': reservation.laboratoryInfo?.status || '',
                            '预约人': reservation.userInfo?.username || '教学安排',
                            '管理人': reservation.manager_id || '',
                            '预约提交时间': formatDateTime(reservation.created_at),
                            '预约开始时间': reservation.start_time ? reservation.start_time.split('T')[0] : '',
                            '预约结束时间': reservation.end_time ? reservation.end_time.split('T')[0] : '',
                            '预约状态': reservation.status || '',
                            '实际归还时间': reservation.return_at ? formatDateTime(reservation.return_at) : '未归还'
                        }))

                        if (exportData.length === 0) {
                            ElMessage.warning('没有数据可导出')
                            return
                        }

                        const wb = XLSX.utils.book_new()
                        const ws = XLSX.utils.json_to_sheet(exportData)

                        const colWidths = [
                            { wch: 10 }, // 预约ID
                            { wch: 20 }, // 实验室名称
                            { wch: 20 }, // 地点
                            { wch: 10 }, // 实验室状态
                            { wch: 15 }, // 预约人
                            { wch: 10 }, // 管理人
                            { wch: 30 }, // 预约提交时间
                            { wch: 15 }, // 预约开始时间
                            { wch: 15 }, // 预约结束时间
                            { wch: 10 }, // 预约状态
                            { wch: 30 }, // 实际归还时间
                        ]
                        ws['!cols'] = colWidths

                        XLSX.utils.book_append_sheet(wb, ws, '实验室预约列表')

                        const date = new Date()
                        const fileName = `实验室预约列表_${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}.xlsx`

                        XLSX.writeFile(wb, fileName)

                        ElMessage.success(`成功导出 ${exportData.length} 条记录`)
                    } catch (error) {
                        console.error('导出失败:', error)
                        ElMessage.error('导出失败')
                    }
                })
                .catch(() => {
                    ElMessage.info('已取消导出')
                })
        }

        function formatDateTime(dateTimeStr) {
            if (!dateTimeStr) return ''

            const date = new Date(dateTimeStr)

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
            handleSearch,
            reset,
            exportToExcel,
            formatDateTime,
            toReservationDetail,
        }
    },
}
</script>

<style scoped>
.laboratory-management {
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

.scroll-content {
    margin-top: 180px;
    padding-bottom: 20px;
    min-height: calc(100vh - 220px);
    display: flex;
    flex-direction: column;
}

.header-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;
}

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

.laboratory-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 12px;
}

.laboratory-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 16px;
    box-sizing: border-box;
    min-height: 140px;
    height: auto;
    margin-bottom: 0;
    transition: all 0.3s;
    cursor: pointer;
}

.laboratory-card:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.laboratory-info {
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
}

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