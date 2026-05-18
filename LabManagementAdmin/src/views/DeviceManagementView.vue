<template>
    <!-- 简约设备管理面板：一行四卡，左图右文字 -->
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">设备管理</div>

                <div class="button-container">
                    <button @click="batchOperation" class="batch-button">{{ operation }}</button>
                    <button v-show="isOperation" class="status-button" @click="toChangStatus">修改状态</button>
                    <button class="batch-input-button" @click="toDeviceEntry">设备录入</button>
                </div>
            </div>

            <div class="search-container">
                <div class="search-box">
                    <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索设备名称或ID"
                        class="search-input">
                    <select v-model="searchStatus" @change="handleSearch" class="status-select">
                        <option value="">全部</option>
                        <option value="可能发生故障">可能发生故障</option>
                        <option value="空闲">空闲</option>
                        <option value="闲置">闲置</option>
                        <option value="课题组">课题组</option>
                        <option value="故障">故障</option>
                        <option value="报废审核">报废审核</option>
                    </select>
                    <button @click="reset">重置</button>
                </div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="device-grid">
                <div v-for="device in filteredDevices" :key="device.device_id" class="device-card" :class="{
                    'operation-mode': isOperation,
                    'selected': isOperation && selectedDevices.includes(device.device_id)
                }" @click="handleCardClick(device.device_id)">

                    <div class="checkbox-wrapper" v-show="isOperation">
                        <input type="checkbox" :value="device.device_id" v-model="selectedDevices"
                            class="device-checkbox" @click.stop>
                    </div>

                    <img :src="device.image_url" :alt="device.device_name || '设备图片'" class="device-image">
                    <div class="device-info">
                        <div class="info-item"><span class="label">设备ID:</span> {{ device.device_id }}</div>
                        <div class="info-item"><span class="label">设备名称:</span> {{ device.device_name }}</div>
                        <div class="info-item"><span class="label">设备地点:</span> {{ device.location }}</div>
                        <div class="info-item"><span class="label">设备状态:</span> {{ device.status }}</div>
                    </div>
                </div>
            </div>

            <div v-if="filteredDevices.length === 0 && searchKeyword" class="no-result">
                没有找到匹配的设备
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
import { ElMessage } from 'element-plus';

export default {
    setup() {
        const router = useRouter()
        const state = reactive({
            DevicesArr: [],
            filteredDevices: [],
            searchKeyword: '',
            searchStatus: '',
            operation: "批量操作",
            isOperation: false,
            selectedDevices: []
        })

        function batchOperation() {
            if (!state.isOperation) {
                state.operation = "取消"
                state.isOperation = true
                state.selectedDevices = []

            } else {
                state.operation = "批量操作"
                state.isOperation = false
                state.selectedDevices = []

            }

        }

        function initDevicesArr() {
            axios
                .get("/api/getAllDevices")
                .then((response) => {
                    if (response.data.status == 200) {
                        console.log(response.data.data)
                        state.DevicesArr = response.data.data
                        state.filteredDevices = response.data.data
                    } else {
                        ElMessage.error("获取设备信息失败")
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initDevicesArr()

        function handleSearch() {
            const keyword = state.searchKeyword.trim().toLowerCase()
            const status = state.searchStatus

            if (!keyword && !status) {
                state.filteredDevices = state.DevicesArr
                return
            }
            state.filteredDevices = state.DevicesArr.filter(device => {
                let keywordMatch = true
                if (keyword) {
                    const deviceName = device.device_name?.toLowerCase() || ''
                    const deviceId = String(device.device_id).toLowerCase()
                    keywordMatch = deviceName.includes(keyword) || deviceId.includes(keyword)
                }

                // 状态匹配
                let statusMatch = true
                if (status) {
                    statusMatch = device.status === status
                }

                return keywordMatch && statusMatch
            })
        }

        function reset() {
            state.filteredDevices = state.DevicesArr
            state.searchKeyword = ''
            state.searchStatus = ''
        }

        function toChangStatus() {
            if (state.selectedDevices.length === 0) {
                ElMessage.error("请选择设备")
                return
            }

            const selectedDevicesInfo = state.DevicesArr.filter(device => state.selectedDevices.includes(device.device_id))

            const firstStatus = selectedDevicesInfo[0]?.status
            const allSameStatus = selectedDevicesInfo.every(device => device.status === firstStatus)

            if (!allSameStatus) {
                ElMessage.error("选中的设备状态不一致，不能进行批量修改状态操作")
                return
            }

            // 方法1：只传递 device_id（用逗号拼接）
            const deviceIds = state.selectedDevices.join(',')

            // 方法2：同时传递 device_id 和 device_name（拼接成字符串）
            const deviceNames = selectedDevicesInfo.map(device => device.device_name).join(',')

            router.push({
                path: '/changStatus',
                query: {
                    deviceIds: deviceIds,
                    deviceNames: deviceNames,
                    currentStatus: firstStatus
                }
            })
        }

        function toDeviceEntry() {
            router.push('/deviceEntry')
        }

        function handleCardClick(deviceId) {
            if (state.isOperation) {
                // 批量操作模式：切换选中状态
                const index = state.selectedDevices.indexOf(deviceId)
                if (index === -1) {
                    state.selectedDevices.push(deviceId)
                } else {
                    state.selectedDevices.splice(index, 1)
                }
            } else {
                // 非批量操作模式：跳转到详情
                toDeviceDetail(deviceId)
            }
        }

        function toDeviceDetail(device_id) {
            router.push({
                path: '/deviceDetail',
                query: {
                    device_id: device_id
                }
            })
        }

        return {
            ...toRefs(state),
            batchOperation,
            toChangStatus,
            handleSearch,
            toDeviceEntry,
            handleCardClick,
            reset,
            toDeviceDetail,
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

/* 滚动内容区域 */
.scroll-content {
    margin-top: 160px;
    padding-bottom: 20px;
    min-height: calc(100vh - 180px);
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

/* 按钮容器 */
.button-container {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* 批量操作按钮 */
.batch-button {
    padding: 8px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

/* 修改状态按钮 */
.status-button {
    padding: 8px 20px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

/* 批量录入按钮 */
.batch-input-button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

/* 搜索容器 */
.search-container {
    margin-bottom: 24px;
    width: 500px;
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

/* 状态选择器 */
.status-select {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    background-color: white;
    cursor: pointer;
}

.status-select:hover {
    border-color: #c0c4cc;
}

.device-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
}

/* 卡片样式 */
.device-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 16px;
    box-sizing: border-box;
    height: 140px;
}

/* 勾选框包装器 */
.checkbox-wrapper {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 10;
    background-color: white;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 勾选框样式 */
.device-checkbox {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: #409eff;
}

/* 操作模式下的卡片效果 */
.device-card.operation-mode {
    background-color: #fafafa;
}

/* 选中状态下的卡片效果 */
.device-card.operation-mode:has(.device-checkbox:checked) {
    border-color: #409eff;
}

.device-card:hover {
    border-color: #c0c4cc;
}

/* 左侧图片区域 */
.device-image {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
    margin-right: 16px;
    flex-shrink: 0;
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

/* 无结果提示 */
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
    /* 移除所有 margin */
    order: -1;
    /* 确保显示在最前面 */
}
</style>