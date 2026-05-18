<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">数据统计</div>
            </div>
        </div>

        <div class="scroll-content">
            <!-- 设备/实验室使用概况 -->
            <div class="device-stats">
                <div class="device-stats-item">
                    <div class="label">设备总数</div>
                    <div class="value">{{ devicesCount }}</div>
                </div>
                <div class="device-stats-item">
                    <div class="label">故障设备数量</div>
                    <div class="value">{{ faultDevicesCount }}</div>
                </div>
                <div class="device-stats-item">
                    <div class="label">今日预约数量</div>
                    <div class="value">{{ bookTodayCount }}</div>
                </div>
            </div>

            <div class="rankings-container">
                <!-- 热门设备TOP10 -->
                <div class="rankings-box">
                    <div class="rankings-header">
                        <div class="sub-title">热门设备TOP10</div>
                        <select v-model="searchYearForDevice" @change="yearSearchForDevice" class="year-select">
                            <option value="">今年</option>
                            <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                        </select>
                    </div>
                    <div v-if="deviceHotRankings.length > 0">
                        <div v-for="(rankings, index) in deviceHotRankings.slice(0, 10)" :key="rankings.device_id"
                            class="ranking-item">
                            <span class="ranking-index">{{ index + 1 }}.</span>
                            <span class="ranking-name">{{ rankings.device_name }}</span>
                            <span class="ranking-count">{{ rankings.count }}次</span>
                        </div>
                    </div>
                    <div v-else class="no-data">暂无数据</div>
                </div>

                <!-- 活跃用户TOP10 （合并设备预约和实验室预约） -->
                <div class="rankings-box">
                    <div class="rankings-header">
                        <div class="sub-title">活跃用户TOP10</div>
                        <select v-model="searchYearForUser" @change="yearSearchForUser" class="year-select">
                            <option value="">今年</option>
                            <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                        </select>
                    </div>
                    <div v-if="userHotRankings.length > 0">
                        <div v-for="(rankings, index) in userHotRankings.slice(0, 10)" :key="rankings.user_id"
                            class="ranking-item">
                            <span class="ranking-index">{{ index + 1 }}.</span>
                            <span class="ranking-name">{{ rankings.username }}</span>
                            <span class="ranking-count">{{ rankings.count }}次</span>
                        </div>
                    </div>
                    <div v-else class="no-data">暂无数据</div>
                </div>
            </div>

            <div class="charts-container">
                <!-- 预约身份比例饼状图（合并设备和实验室） -->
                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">预约身份比例</div>
                        <select v-model="searchYearForIdentity" @change="yearSearchForIdentity" class="year-select">
                            <option value="">今年</option>
                            <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                        </select>
                    </div>
                    <div v-if="identityRankings.length > 0" id="identityChart" style="width: 100%; height: 300px;">
                    </div>
                    <div v-else class="no-data-chart">暂无数据</div>
                </div>

                <!-- 预约状态比例饼状图（合并设备和实验室） -->
                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">预约状态比例</div>
                        <select v-model="searchYearForStatus" @change="yearSearchForStatus" class="year-select">
                            <option value="">今年</option>
                            <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                        </select>
                    </div>
                    <div v-if="reservationStatusRankings.length > 0" id="reservationStatusChart"
                        style="width: 100%; height: 300px;"></div>
                    <div v-else class="no-data-chart">暂无数据</div>
                </div>

                <!-- 设备状态比例饼状图 -->
                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">设备状态比例</div>
                    </div>
                    <div v-if="deviceStatusRankings.length > 0" id="deviceStatusChart"
                        style="width: 100%; height: 300px;"></div>
                    <div v-else class="no-data-chart">暂无数据</div>
                </div>

                <!-- 实验室状态比例饼状图 -->
                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">实验室状态比例</div>
                    </div>
                    <div v-if="labStatusRankings.length > 0" id="labStatusChart" style="width: 100%; height: 300px;">
                    </div>
                    <div v-else class="no-data-chart">暂无数据</div>
                </div>

                <!-- 最近七天预约趋势折线图（合并设备和实验室） -->
                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">最近七天预约趋势</div>
                    </div>
                    <div id="dayTrendChart" style="width: 100%; height: 300px;"></div>
                </div>

                <div class="chart-box">
                    <div class="chart-header">
                        <div class="sub-title">年预约趋势</div>
                        <select v-model="searchYear" @change="yearSearch" class="year-select">
                            <option value="">今年</option>
                            <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                        </select>
                    </div>
                    <div id="monthTrendChart" style="width: 100%; height: 300px;"></div>
                </div>
            </div>

        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import axios from 'axios';
import { reactive, toRefs, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts'

export default {
    setup() {
        const router = useRouter()

        const state = reactive({
            devicesCount: '',
            deviceData: [],
            faultDevicesCount: '',
            bookTodayCount: '',
            deviceReservationArr: [],       // 设备预约
            labReservationArr: [],          // 实验室预约
            filteredReservationArr: [],     // 合并后的所有预约（包含设备+实验室）
            labStatusRankings: [],          // 实验室状态统计
            deviceHotRankings: [],
            userHotRankings: [],
            identityRankings: [],
            reservationStatusRankings: [],
            deviceStatusRankings: [],
            dayStats: [],
            monthStats: [],
            searchYear: '',
            searchYearForUser: '',
            searchYearForStatus: '',
            searchYearForIdentity: '',
            searchYearForDevice: '',
            yearOptions: [],
        })

        // 获取设备数据（故障设备数量会在这里更新）
        function getDevicesData() {
            return axios.get('/api/getAllDevices')
                .then((response) => {
                    if (response.data.status == 200) {
                        state.devicesCount = response.data.data.length
                        state.deviceData = response.data.data
                        // 计算故障设备数量
                        state.faultDevicesCount = response.data.data.filter(d => d.status === '故障').length
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }

        // 获取所有实验室数据（用于实验室状态统计）
        function getAllLaboratories() {
            return axios.get('/api/getAllLaboratory')
                .then((response) => {
                    if (response.data.status == 200) {
                        const rankings = {}
                        response.data.data.forEach(lab => {
                            const status = lab.status
                            if (!rankings[status]) {
                                rankings[status] = { status, count: 0 }
                            }
                            rankings[status].count++
                        })
                        state.labStatusRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }

        // 获取设备详情
        function getDeviceData(device_id) {
            return axios.post('/api/getDeviceDetail', { device_id })
                .then((response) => response.data.status == 200 ? response.data.data : null)
                .catch(() => null)
        }

        // 获取实验室详情
        function getLaboratoryData(laboratory_id) {
            return axios.post('/api/getLaboratoryDetail', { laboratory_id })
                .then((response) => response.data.status == 200 ? response.data.data : null)
                .catch(() => null)
        }

        // 获取用户详情
        function getUserData(user_id) {
            return axios.post('/api/getUserDetail', { user_id })
                .then((response) => response.data.status == 200 ? response.data.data[0] : null)
                .catch(() => null)
        }

        // 获取设备预约并附加用户信息和设备信息
        function getAllDeviceReservations() {
            return axios.get('/api/getAllReservation')
                .then((response) => {
                    if (response.data.status == 200) {
                        const reservations = response.data.data
                        const promises = reservations.map(r =>
                            Promise.all([getUserData(r.user_id), getDeviceData(r.device_id)])
                                .then(([userInfo, deviceInfo]) => ({
                                    ...r,
                                    type: 'device',
                                    userInfo,
                                    deviceInfo
                                }))
                        )
                        return Promise.all(promises)
                    }
                    return []
                })
        }

        // 获取实验室预约并附加用户信息和实验室信息
        function getAllLabReservations() {
            return axios.get('/api/getAllLabReservation')
                .then((response) => {
                    if (response.data.status == 200) {
                        const reservations = response.data.data
                        const promises = reservations.map(r =>
                            Promise.all([getUserData(r.user_id), getLaboratoryData(r.laboratory_id)])
                                .then(([userInfo, labInfo]) => ({
                                    ...r,
                                    type: 'lab',
                                    userInfo,
                                    laboratoryInfo: labInfo
                                }))
                        )
                        return Promise.all(promises)
                    }
                    return []
                })
        }

        // 合并两种预约
        function initReservationArr() {
            Promise.all([
                getDevicesData(),
                getAllLaboratories(),
                getAllDeviceReservations(),
                getAllLabReservations()
            ]).then(([devicesResult, labResult, deviceReservations, labReservations]) => {
                // 1. 赋值两类预约到 state
                state.deviceReservationArr = deviceReservations || []
                state.labReservationArr = labReservations || []

                // 2. 合并为所有预约
                state.filteredReservationArr = [
                    ...state.deviceReservationArr,
                    ...state.labReservationArr
                ]

                // 3. 执行后续统计
                initBookTodayCount()
                updateDeviceStats()
                updateUserStats()
                updataIdentityStats()
                updataReservationStatusStats()
                updataDeviceStatusStats()
                updataDayStats()
                updataMonthStats()

                // 4. 延迟绘制图表，确保 DOM 已渲染
                setTimeout(() => {
                    drawCharts()
                }, 200)
            })
        }

        // 今日预约数量（包含设备+实验室）
        function initBookTodayCount() {
            const today = new Date()
            const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
            let count = 0
            state.filteredReservationArr.forEach(r => {
                const dateStr = r.created_at ? r.created_at.split('T')[0] : ''
                if (dateStr === todayStr) count++
            })
            state.bookTodayCount = count
        }

        // 热门设备（仅设备预约）
        function updateDeviceStats(year = null) {
            const targetYear = year || new Date().getFullYear()
            const rankings = {}
            state.deviceReservationArr.forEach(reservation => {
                const reservationDate = new Date(reservation.created_at)
                const reservationYear = reservationDate.getFullYear()
                if (targetYear === reservationYear) {
                    const deviceId = reservation.device_id
                    const deviceName = reservation.deviceInfo?.device_name || '未知设备'
                    if (!rankings[deviceId]) {
                        rankings[deviceId] = { device_id: deviceId, device_name: deviceName, count: 0 }
                    }
                    rankings[deviceId].count++
                }
            })
            state.deviceHotRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
        }

        // 活跃用户（合并设备预约和实验室预约）
        function updateUserStats(year = null) {
            const targetYear = year || new Date().getFullYear()
            const rankings = {}
            state.filteredReservationArr.forEach(reservation => {
                const reservationDate = new Date(reservation.created_at)
                const reservationYear = reservationDate.getFullYear()
                if (targetYear === reservationYear) {
                    const userId = reservation.user_id
                    const userName = reservation.userInfo?.username || '未知用户'
                    if (!rankings[userId]) {
                        rankings[userId] = { user_id: userId, username: userName, count: 0 }
                    }
                    rankings[userId].count++
                }
            })
            state.userHotRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
        }

        // 预约身份比例（合并）
        function updataIdentityStats(year = null) {
            const targetYear = year || new Date().getFullYear()
            const rankings = {}
            state.filteredReservationArr.forEach(reservation => {
                const reservationDate = new Date(reservation.created_at)
                const reservationYear = reservationDate.getFullYear()
                if (targetYear === reservationYear) {
                    const identity = reservation.userInfo?.identity_type || '未知'
                    if (!rankings[identity]) {
                        rankings[identity] = { identity_type: identity, count: 0 }
                    }
                    rankings[identity].count++
                }
            })
            state.identityRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
        }

        // 预约状态比例（合并）
        function updataReservationStatusStats(year = null) {
            const targetYear = year || new Date().getFullYear()
            const rankings = {}
            state.filteredReservationArr.forEach(reservation => {
                const reservationDate = new Date(reservation.created_at)
                const reservationYear = reservationDate.getFullYear()
                if (targetYear === reservationYear) {
                    const status = reservation.status
                    if (!rankings[status]) {
                        rankings[status] = { status, count: 0 }
                    }
                    rankings[status].count++
                }
            })
            state.reservationStatusRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
        }

        // 设备状态比例
        function updataDeviceStatusStats() {
            const rankings = {}
            state.deviceData.forEach(device => {
                const status = device.status
                if (!rankings[status]) {
                    rankings[status] = { status, count: 0 }
                }
                rankings[status].count++
            })
            state.deviceStatusRankings = Object.values(rankings).sort((a, b) => b.count - a.count)
        }

        // 最近七天趋势（合并）
        function updataDayStats() {
            const today = new Date()
            const last7Days = []
            for (let i = 6; i >= 0; i--) {
                const date = new Date(today)
                date.setDate(today.getDate() - i)
                const year = date.getFullYear()
                const month = String(date.getMonth() + 1).padStart(2, '0')
                const day = String(date.getDate()).padStart(2, '0')
                last7Days.push({ date: `${year}-${month}-${day}`, count: 0 })
            }

            state.filteredReservationArr.forEach(reservation => {
                const reservationDate = reservation.created_at ? reservation.created_at.split('T')[0] : ''
                const dayStat = last7Days.find(d => d.date === reservationDate)
                if (dayStat) dayStat.count++
            })
            state.dayStats = last7Days
        }

        // 月度趋势（合并）
        function updataMonthStats(year = null) {
            const targetYear = year || new Date().getFullYear()
            const months = Array.from({ length: 12 }, (_, i) => ({ month: i + 1, count: 0 }))
            state.filteredReservationArr.forEach(reservation => {
                const reservationDate = new Date(reservation.created_at)
                const reservationYear = reservationDate.getFullYear()
                if (reservationYear === targetYear) {
                    const month = reservationDate.getMonth() + 1
                    const m = months.find(m => m.month === month)
                    if (m) m.count++
                }
            })
            state.monthStats = months
        }

        // 年份选项
        function initYearOptions() {
            const currentYear = new Date().getFullYear()
            const years = []
            for (let year = 2020; year < currentYear; year++) {
                years.push(year)
            }
            state.yearOptions = years
        }
        initYearOptions()

        // 图表绘制
        function drawCharts() {
            // 设备状态饼图
            const deviceStatusChart = echarts.init(document.getElementById('deviceStatusChart'))
            deviceStatusChart.setOption({
                tooltip: { trigger: 'item' },
                legend: { orient: 'vertical', left: 'left' },
                series: [{
                    name: '设备状态',
                    type: 'pie',
                    radius: '50%',
                    data: state.deviceStatusRankings.map(item => ({
                        name: `${item.status}(${item.count})`,
                        value: item.count
                    }))
                }]
            })

            // 实验室状态饼图
            const labStatusChart = echarts.init(document.getElementById('labStatusChart'))
            labStatusChart.setOption({
                tooltip: { trigger: 'item' },
                legend: { orient: 'vertical', left: 'left' },
                series: [{
                    name: '实验室状态',
                    type: 'pie',
                    radius: '50%',
                    data: state.labStatusRankings.map(item => ({
                        name: `${item.status}(${item.count})`,
                        value: item.count
                    }))
                }]
            })

            // 七天趋势
            const dayTrendChart = echarts.init(document.getElementById('dayTrendChart'))
            dayTrendChart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                xAxis: { type: 'category', data: state.dayStats.map(item => item.date) },
                yAxis: { type: 'value', name: '预约数量', min: 0, max: function (value) { return Math.ceil(Math.max(...state.dayStats.map(i => i.count), 1) * 1.2) }, interval: 1 },
                series: [{
                    name: '预约数量',
                    type: 'line',
                    data: state.dayStats.map(item => item.count),
                    smooth: true,
                    lineStyle: { color: '#409eff' },
                    areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.5)' }, { offset: 1, color: 'rgba(64,158,255,0.1)' }]) }
                }]
            })

            // 身份比例
            updataIdentityChart()
            // 预约状态比例
            updataReservationStatusChart()
            // 月度趋势
            updateMonthChart()
        }

        function updataIdentityChart() {
            const identityChart = echarts.init(document.getElementById('identityChart'))
            identityChart.setOption({
                tooltip: { trigger: 'item' },
                legend: { orient: 'vertical', left: 'left' },
                series: [{
                    name: '预约身份',
                    type: 'pie',
                    radius: '50%',
                    data: state.identityRankings.map(item => ({
                        name: `${item.identity_type}(${item.count})`,
                        value: item.count
                    }))
                }]
            })
        }

        function updataReservationStatusChart() {
            const reservationStatusChart = echarts.init(document.getElementById('reservationStatusChart'))
            reservationStatusChart.setOption({
                tooltip: { trigger: 'item' },
                legend: { orient: 'vertical', left: 'left' },
                series: [{
                    name: '预约状态',
                    type: 'pie',
                    radius: '50%',
                    data: state.reservationStatusRankings.map(item => ({
                        name: `${item.status}(${item.count})`,
                        value: item.count
                    }))
                }]
            })
        }

        function updateMonthChart() {
            const monthTrendChart = echarts.init(document.getElementById('monthTrendChart'))
            monthTrendChart.setOption({
                tooltip: { trigger: 'axis' },
                grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                xAxis: { type: 'category', data: state.monthStats.map(item => `${item.month}月`) },
                yAxis: { type: 'value', name: '预约数量', min: 0, max: function (value) { return Math.ceil(Math.max(...state.monthStats.map(i => i.count), 1) * 1.2) }, interval: 1 },
                series: [{
                    name: '预约数量',
                    type: 'line',
                    data: state.monthStats.map(item => item.count),
                    smooth: true,
                    lineStyle: { color: '#409eff' },
                    areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.5)' }, { offset: 1, color: 'rgba(64,158,255,0.1)' }]) }
                }]
            })
        }

        // 年份切换处理
        function yearSearch() {
            if (state.searchYear) {
                updataMonthStats(parseInt(state.searchYear))
            } else {
                updataMonthStats()
            }
            setTimeout(() => updateMonthChart(), 200)
        }
        function yearSearchForDevice() {
            if (state.searchYearForDevice) {
                updateDeviceStats(parseInt(state.searchYearForDevice))
            } else {
                updateDeviceStats()
            }
        }
        function yearSearchForUser() {
            if (state.searchYearForUser) {
                updateUserStats(parseInt(state.searchYearForUser))
            } else {
                updateUserStats()
            }
        }
        function yearSearchForIdentity() {
            if (state.searchYearForIdentity) {
                updataIdentityStats(parseInt(state.searchYearForIdentity))
            } else {
                updataIdentityStats()
            }
            setTimeout(() => updataIdentityChart(), 200)
        }
        function yearSearchForStatus() {
            if (state.searchYearForStatus) {
                updataReservationStatusStats(parseInt(state.searchYearForStatus))
            } else {
                updataReservationStatusStats()
            }
            setTimeout(() => updataReservationStatusChart(), 200)
        }

        // 初始化
        onMounted(() => {
            initReservationArr()
        })

        return {
            ...toRefs(state),
            yearSearch,
            yearSearchForDevice,
            yearSearchForUser,
            yearSearchForIdentity,
            yearSearchForStatus,
        }
    }
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

/* 固定头部区域 */
.fixed-header {
    position: fixed;
    top: 0;
    left: 250px;
    /* 改为固定像素值，而不是50% */
    transform: none;
    /* 移除 transform */
    width: calc(100% - 250px);
    /* 减去左侧导航栏宽度 */
    max-width: calc(2000px - 250px);
    padding: 20px 24px 0 24px;
    background-color: #ffffff;
    z-index: 100;
    box-sizing: border-box;
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

/* 滚动内容区域 */
.scroll-content {
    margin-top: 100px;
    padding-bottom: 20px;
    min-height: calc(100vh - 180px);
    display: flex;
    flex-direction: column;
}

/* 设备使用概况卡片 */
.device-stats {
    display: flex;
    gap: 20px;
    margin: 20px 0;
    padding: 20px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
}

.device-stats-item {
    flex: 1;
    text-align: center;
    padding: 10px;
}

.device-stats-item .label {
    font-size: 14px;
    color: #7f8c8d;
    margin-bottom: 8px;
}

.device-stats-item .value {
    font-size: 24px;
    font-weight: 500;
    color: #2c3e50;
}

/* 排行榜容器 */
.rankings-container {
    display: flex;
    gap: 30px;
    margin: 20px 0;
}

.rankings-box {
    flex: 1;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 20px;
}

/* 排行榜头部容器 */
.rankings-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #409eff;
}

.rankings-box .sub-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin: 0;
    padding: 0;
    border-bottom: none;
}

.rankings-box .ranking-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px dashed #f0f2f5;
}

.rankings-box .ranking-item:last-child {
    border-bottom: none;
}

.rankings-box .ranking-index {
    width: 30px;
    font-weight: 500;
    color: #909399;
}

.rankings-box .ranking-name {
    flex: 1;
    color: #2c3e50;
}

.rankings-box .ranking-count {
    color: #409eff;
    font-weight: 500;
}

/* 在 style 末尾添加饼状图容器样式 */
.charts-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin: 20px 0;
}

.chart-box {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 20px;
}

/* 图表头部容器，用于放置标题和年份选择器 */
.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #409eff;
}

/* 年份选择器样式 */
.year-select {
    padding: 6px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    background-color: white;
    cursor: pointer;
    width: 100px;
}

.year-select:hover {
    border-color: #c0c4cc;
}

/* 移除原来 .chart-box .sub-title 的 border-bottom，因为现在在 .chart-header 上 */
.chart-box .sub-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin: 0;
    /* 移除原来的 margin-bottom */
    padding: 0;
    /* 移除原来的 padding-bottom */
    border-bottom: none;
    /* 移除原来的 border-bottom */
}

/* 在 style 末尾添加 */
.no-data {
    text-align: center;
    padding: 40px 0;
    color: #909399;
    font-size: 14px;
    background-color: #f5f7fa;
    border-radius: 8px;
    margin-top: 10px;
}

.no-data-chart {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 300px;
    color: #909399;
    font-size: 14px;
    background-color: #f5f7fa;
    border-radius: 8px;
}
</style>