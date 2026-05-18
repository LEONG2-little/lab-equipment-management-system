<template>
    <div class="device-management">
        <div class="header-container">
            <div class="section-title">设备详情</div>
        </div>

        <div class="detail-container">
            <div class="detail-item">
                <span class="detail-label">设备ID：</span>
                <div>{{ showDeviceDetail.device_id }}</div>
            </div>
            <div class="detail-item">
                <span class="detail-label">设备名称：</span>
                <input type="text" v-model="showDeviceDetail.device_name" :readonly="!isChange" class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">设备分类：</span>
                <input type="text" v-model="showDeviceDetail.category" :readonly="!isChange" class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">设备型号：</span>
                <input type="text" v-model="showDeviceDetail.model" :readonly="!isChange" class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">购买时间：</span>
                <span class="purchase-date">{{ showDeviceDetail.purchase_date ?
                    showDeviceDetail.purchase_date.split('T')[0] : '' }}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">购买价格：</span>
                <span class="purchase-date">{{ showDeviceDetail.price }}</span>
            </div>
            <div class="detail-item">
                <span class="detail-label">使用地点：</span>
                <select v-model="selectedLaboratoryId" class="detail-input" :disabled="!isChange">
                    <option value="">请选择使用地点</option>
                    <option v-for="lab in laboratoryArr" :key="lab.laboratory_id" :value="lab.laboratory_id">
                        {{ lab.laboratory_name }}
                    </option>
                </select>
            </div>
            <div class="detail-item">
                <span class="detail-label">规格参数：</span>
                <input type="text" v-model="showDeviceDetail.spec" :readonly="!isChange" class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">设备描述：</span>
                <input type="text" v-model="showDeviceDetail.description" :readonly="!isChange" class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">设备状态：</span>
                <select v-model="status" class="detail-input" :disabled="!isChange">
                    <option value="空闲" v-if="deviceDetail.status == '空闲'">空闲</option>
                    <option value="闲置">闲置</option>
                    <option value="课题组专用">课题组专用</option>
                    <option value="可能发生故障" v-if="deviceDetail.status == '可能发生故障'">可能发生故障</option>
                    <option value="故障">故障</option>
                    <option value="报废">报废</option>
                </select>
            </div>

            <div v-if="status == '故障' && isChange && deviceDetail.status != '故障'" class="fault-reason-container">
                <div class="fault-reason-label">故障原因：</div>
                <textarea v-model="faultReason" placeholder="请输入故障原因" class="fault-reason-input" rows="4"></textarea>
            </div>

            <div class="detail-item">
                <span class="detail-label">权限等级：</span>
                <input type="text" v-model="showDeviceDetail.required_permission" :readonly="!isChange"
                    class="detail-input">
            </div>
            <div class="detail-item">
                <span class="detail-label">负责人ID：</span>
                <input type="text" v-model="showDeviceDetail.manager_id" :readonly="!isChange" class="detail-input">
            </div>

            <div class="detail-item" v-if="status == '报废审核'">
                <div class="detail-item" v-if="status == '报废审核'">
                    <span class="detail-label">报废原因：</span>
                    <span class="detail-value">{{ scrapArr.reason }}</span>
                </div>
            </div>

            <div class="button-container" v-if="status == '报废审核'">
                <button @click="confirmScrap" class="action-button confirmScrap-button">审核通过，确认报废</button>
                <button @click="openRejectDialog" class="action-button confirmScrap-button2">审核不通过</button>
            </div>

            <div class="button-container" v-if="status != '报废审核'">
                <button @click="changeDevice" class="action-button"
                    :class="isChange ? 'cancel-button' : 'modify-button'">{{ buttonText }}</button>
                <button v-if="isChange" @click="confirmChanges" class="action-button confirm-button">确认修改</button>
            </div>
        </div>
    </div>

    <!-- 审核不通过弹窗 -->
    <div v-if="showRejectDialog" class="dialog-overlay" @click.self="closeRejectDialog">
        <div class="dialog-container">
            <div class="dialog-header">
                <h3>审核不通过</h3>
                <button class="close-btn" @click="closeRejectDialog">×</button>
            </div>
            <div class="dialog-body">
                <div class="reason-input">
                    <label for="rejectReason">不通过原因：</label>
                    <textarea id="rejectReason" v-model="rejectReason" placeholder="请输入不通过原因" rows="8"></textarea>
                </div>
            </div>
            <div class="dialog-footer">
                <button class="cancel-btn" @click="closeRejectDialog">取消</button>
                <button class="confirm-btn" @click="notPassed">确认</button>
            </div>
        </div>
    </div>

    <Footer></Footer>
</template>

<script>
import axios from 'axios';
import { reactive, toRefs, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import Footer from '@/components/Footer.vue';

export default {
    setup() {

        const router = useRouter()
        const route = useRoute()

        const state = reactive({
            device_id: '',
            deviceDetail: {},
            scrapArr: {},
            status: '',
            showDeviceDetail: {},
            buttonText: '修改设备信息',
            isChange: false,
            faultReason: '',
            laboratoryArr: [],
            selectedLaboratoryId: '',
            showRejectDialog: false,
            rejectReason: '',
        })

        onMounted(() => {
            state.device_id = route.query.device_id
            initDeviceDetailArr()
        })

        function initLaboratoryArr() {
            axios
                .post('/api/getAllLaboratory')
                .then((response) => {
                    if (response.data.status == 200) {
                        state.laboratoryArr = response.data.data
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initLaboratoryArr()

        function initDeviceDetailArr() {
            axios
                .post('/api/getDeviceDetail', {
                    device_id: state.device_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.deviceDetail = response.data.data
                        state.showDeviceDetail = JSON.parse(JSON.stringify(response.data.data))
                        state.status = state.showDeviceDetail.status
                        state.selectedLaboratoryId = state.showDeviceDetail.laboratory_id

                        if (state.deviceDetail.status == '报废审核') {
                            axios.post('/api/selectScrap', {
                                device_id: state.device_id
                            })
                                .then((response) => {
                                    if (response.data.status == 200) {
                                        state.scrapArr = response.data.data
                                    }
                                })
                                .catch(function (error) {
                                    console.log(error)
                                })
                        }
                    }
                    else {
                        ElMessage.error(response.data.message)
                    }
                })
        }

        function changeDevice() {
            if (state.isChange == false) {
                state.buttonText = '取消'
                state.isChange = true
            }
            else {
                state.buttonText = '修改设备信息'
                state.showDeviceDetail = JSON.parse(JSON.stringify(state.deviceDetail))
                state.isChange = false
                state.status = state.deviceDetail.status
                state.selectedLaboratoryId = state.deviceDetail.laboratory_id
            }

        }

        function confirmChanges() {

            if (!state.showDeviceDetail.device_name) {
                ElMessage.error('设备名称不能为空')
                return
            }
            if (!state.showDeviceDetail.category) {
                ElMessage.error('设备分类不能为空')
                return
            }
            if (!state.selectedLaboratoryId) {
                ElMessage.error('使用地点不能为空')
                return
            }

            ElMessageBox.confirm(
                '确认要修改这些设备的信息吗？',
                '修改确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {

                    state.showDeviceDetail.status = state.status

                    if (state.showDeviceDetail.status == '故障' && state.deviceDetail.status != '故障' && !state.faultReason) {
                        ElMessage.error('请填写故障信息')
                        return
                    }

                    state.showDeviceDetail.laboratory_id = state.selectedLaboratoryId

                    const requestData = {
                        ...state.showDeviceDetail,
                        faultReason: state.faultReason
                    }

                    axios
                        .post('/api/updateDevice', requestData)
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success('修改成功')
                                router.push('/deviceManagement')
                            }
                            if (response.data.status !== 200) {
                                ElMessage.error('修改失败')
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                    changeDevice()
                })
        }

        function confirmScrap() {
            ElMessageBox.confirm(
                '确认通过该报废申请吗？',
                '报废确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {

                    const approver_id = JSON.parse(sessionStorage.getItem("UserData")).admin_id

                    axios
                        .post('/api/confirmScrap', {
                            device_id: state.device_id,
                            approver_id: approver_id
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success('审核成功')
                                router.push('/deviceManagement')
                            }
                            if (response.data.status !== 200) {
                                ElMessage.error('修改失败')
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                    changeDevice()
                })
        }

        function openRejectDialog() {
            state.rejectReason = ''
            state.showRejectDialog = true
        }

        function closeRejectDialog() {
            state.showRejectDialog = false
            state.rejectReason = ''
        }

        function notPassed() {
            if (!state.rejectReason.trim()) {
                ElMessage.warning('请输入不通过原因')
                return
            }

            axios.post('/api/notPassed', {
                device_id: state.device_id,
                approver_reason: state.rejectReason
            })
                .then((response) => {
                    if (response.data.status == 200) {
                        ElMessage.success('审核成功')
                        router.push('/deviceManagement')
                    }
                })
        }

        return {
            ...toRefs(state),
            changeDevice,
            confirmChanges,
            confirmScrap,
            notPassed,
            openRejectDialog,
            closeRejectDialog,
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

/* 详情容器 */
.detail-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
    width: 100%;
    max-width: 800px;
    margin-top: 20px;
}

/* 详情项 */
.detail-item {
    display: flex;
    align-items: center;
    gap: 16px;
    width: 100%;
}

/* 详情标签 */
.detail-label {
    width: 100px;
    font-size: 15px;
    color: #606266;
    font-weight: 500;
    text-align: right;
}

/* 详情值 - 文本显示 */
.detail-value {
    flex: 1;
    font-size: 15px;
    color: #2c3e50;
    line-height: 38px;
    padding: 0 12px;
}

/* 详情输入框 */
.detail-input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 15px;
    outline: none;
    transition: border-color 0.3s;
    background-color: #ffffff;
}

.detail-input:focus {
    border-color: #409eff;
}

.detail-input[readonly] {
    background-color: #f5f7fa;
    border-color: #e4e7ed;
    color: #909399;
    cursor: not-allowed;
}

/* 购买时间特殊样式 */
.purchase-date {
    flex: 1;
    font-size: 15px;
    color: #2c3e50;
    line-height: 38px;
    padding: 0 12px;
}

/* 按钮容器 */
.button-container {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 30px;
    width: 100%;
    max-width: 800px;
}

/* 操作按钮 */
.action-button {
    padding: 10px 24px;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
}

/* 修改按钮 */
.modify-button {
    background-color: #409eff;
    color: white;
}

.modify-button:hover {
    background-color: #66b1ff;
}

/* 确认修改按钮 */
.confirm-button {
    background-color: #67c23a;
    color: white;
}

.confirm-button:hover {
    background-color: #85ce61;
}

.confirmScrap-button {
    background-color: #409eff;
    color: white;
}

.confirmScrap-button:hover {
    background-color: #66b1ff;
}

.confirmScrap-button2 {
    background-color: #ed5656;
    color: white;
}

.confirmScrap-button2:hover {
    background-color: #f58383;
}

/* 取消按钮（当处于修改状态时） */
.cancel-button {
    background-color: #909399;
    color: white;
}

.cancel-button:hover {
    background-color: #a6a9ad;
}

/* 故障原因容器 */
.fault-reason-container {
    width: 100%;
    max-width: 800px;
    margin-top: 10px;
    padding: 16px;
    background-color: #fef0f0;
    border: 1px solid #fde2e2;
    border-radius: 8px;
    box-sizing: border-box;
}

/* 故障原因标签 */
.fault-reason-label {
    font-size: 15px;
    color: #f56c6c;
    margin-bottom: 10px;
    font-weight: 500;
}

/* 故障原因输入框 */
.fault-reason-input {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #f56c6c;
    border-radius: 6px;
    font-size: 14px;
    line-height: 1.6;
    resize: vertical;
    box-sizing: border-box;
    font-family: inherit;
    background-color: #fff;
    transition: all 0.3s;
}

.fault-reason-input:focus {
    outline: none;
    border-color: #f78989;
    box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1);
}

.fault-reason-input::placeholder {
    color: #b3b8c5;
    font-style: italic;
}

/* 弹窗遮罩层 */
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

/* 弹窗容器 */
.dialog-container {
    background-color: #ffffff;
    border-radius: 12px;
    width: 500px;
    max-width: 90%;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 弹窗头部 */
.dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e4e7ed;
}

.dialog-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    color: #2c3e50;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #909399;
    padding: 0;
    line-height: 1;
}

/* 弹窗主体 */
.dialog-body {
    padding: 20px;
}

.reason-input label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    color: #2c3e50;
    font-weight: 500;
}

.reason-input textarea {
    width: 100%;
    padding: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    resize: vertical;
    box-sizing: border-box;
    font-family: inherit;
}

/* 弹窗底部 */
.dialog-footer {
    padding: 16px 20px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.cancel-btn {
    padding: 8px 20px;
    background-color: #ffffff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

.confirm-btn {
    padding: 8px 20px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}
</style>