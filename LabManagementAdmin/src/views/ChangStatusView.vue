<template>
    <div class="device-management">
        <div class="header-container">
            <div class="section-title">设备管理</div>
        </div>

        <!-- 状态修改卡片 -->
        <div class="status-card">
            <div class="info-row">
                <span class="info-label">设备ID：</span>
                <span class="info-value">{{ deviceIds }}</span>
            </div>
            <div class="info-row">
                <span class="info-label">设备名称：</span>
                <span class="info-value">{{ deviceNames }}</span>
            </div>
            <div class="info-row">
                <span class="info-label">当前状态：</span>
                <span class="info-value status-tag">{{ currentStatus }}</span>
            </div>
        </div>

        <!-- 状态选择区域 -->
        <div class="status-select-container">
            <div class="select-wrapper">
                <select v-model="selectStatus" class="status-select">
                    <option value="" disabled selected>选择要修改的状态</option>
                    <option value="闲置">闲置</option>
                    <option value="故障">故障</option>
                    <option value="报废">报废</option>
                </select>
            </div>
        </div>

        <div v-if="selectStatus == '故障'" class="fault-reason-container">
            <div class="fault-reason-label">故障原因：</div>
            <textarea v-model="faultReason" placeholder="请输入故障原因" class="fault-reason-input" rows="4"></textarea>
        </div>

        <!-- 确认按钮 -->
        <div class="button-wrapper">
            <button @click="changeStatus" class="confirm-button">确认修改</button>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { reactive, onMounted, toRefs } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    setup() {

        const router = useRouter()
        const route = useRoute()
        const state = reactive({
            deviceIds: '',
            deviceNames: '',
            currentStatus: '',
            selectStatus: '',
            faultReason: '',
        })

        onMounted(() => {
            state.deviceIds = route.query.deviceIds
            state.deviceNames = route.query.deviceNames
            state.currentStatus = route.query.currentStatus
        })

        function changeStatus() {
            if (!state.selectStatus) {
                ElMessage.error("请选择要修改的状态")
                return
            }

            if (state.selectStatus === state.currentStatus) {
                ElMessage.error("你要修改的状态和当前状态一样")
                return
            }

            if (state.selectStatus === '故障' && !state.faultReason.trim()) {
                ElMessage.error("请输入故障原因")
                return
            }

            ElMessageBox.confirm(
                '确认要修改这些设备的状态吗？',
                '修改确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {

                    const deviceIdArray = state.deviceIds.split(',').map(id => parseInt(id.trim()))

                    // 准备要发送的数据
                    const requestData = {
                        deviceIds: deviceIdArray,
                        status: state.selectStatus,
                        faultReason: state.selectStatus === '故障' ? state.faultReason : '',
                    }

                    axios
                        .post('/api/changeDeviceStatus', requestData)
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success("修改成功")
                                router.push('/deviceManagement')
                            }
                            else {
                                ElMessage.error(response.data.message)
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            changeStatus,
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

.status-card {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
    width: 600px;
}

.info-row {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px dashed #f0f2f5;
}

.info-row:last-child {
    border-bottom: none;
}

.info-label {
    width: 100px;
    color: #7f8c8d;
    font-size: 15px;
}

.info-value {
    flex: 1;
    color: #2c3e50;
    font-size: 15px;
}

.status-tag {
    display: inline-block;
    padding: 4px 12px;
    background-color: #ecf5ff;
    color: #409eff;
    border-radius: 4px;
    font-size: 14px;
}

/* 状态选择区域 */
.status-select-container {
    margin-bottom: 24px;
    width: 600px;
}

.select-wrapper {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 4px;
}

.status-select {
    width: 100%;
    padding: 12px 16px;
    border: none;
    outline: none;
    font-size: 15px;
    color: #2c3e50;
    background-color: transparent;
    cursor: pointer;
}

.status-select option {
    padding: 8px;
    font-size: 14px;
}

/* 按钮容器 */
.button-wrapper {
    width: 600px;
    text-align: center;
}

.confirm-button {
    padding: 12px 40px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
}

.confirm-button:hover {
    background-color: #66b1ff;
}

.confirm-button:active {
    background-color: #3a8ee6;
}

/* 故障原因容器 */
.fault-reason-container {
    width: 600px;
    margin-bottom: 24px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 20px;
    box-sizing: border-box;
}

.fault-reason-label {
    font-size: 15px;
    color: #2c3e50;
    margin-bottom: 12px;
    font-weight: 500;
}

.fault-reason-input {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.6;
    resize: vertical;
    box-sizing: border-box;
    font-family: inherit;
    background-color: #f9f9f9;
}

.fault-reason-input:focus {
    outline: none;
    border-color: #409eff;
    background-color: #ffffff;
}

.fault-reason-input::placeholder {
    color: #b3b8c5;
}
</style>