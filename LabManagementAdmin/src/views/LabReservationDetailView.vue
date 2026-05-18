<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">实验室预约详情</div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="detail-container">
                <div class="detail-item">
                    <span class="label">预约ID：</span>
                    <span class="value">{{ reservationData.lab_reservation_id }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">实验室名称：</span>
                    <span class="value">{{ reservationData.laboratoryInfo?.laboratory_name || '未知' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">地点：</span>
                    <span class="value">{{ reservationData.laboratoryInfo?.location || '' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">实验室状态：</span>
                    <span class="value">{{ reservationData.laboratoryInfo?.status || '' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">管理人ID：</span>
                    <span class="value">{{ reservationData.manager_id || '' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">预约人：</span>
                    <span class="value">{{ reservationData.userInfo?.username || '教学安排' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">使用用途：</span>
                    <span class="value">{{ reservationData.purpose || '无' }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">预约时间：</span>
                    <span class="value">
                        {{ reservationData.start_time ? formatDateTime(reservationData.start_time) : '' }} 到
                        {{ reservationData.end_time ? formatDateTime(reservationData.end_time) : '' }}
                    </span>
                </div>
                <div class="detail-item">
                    <span class="label">预约状态：</span>
                    <span class="value">{{ reservationData.status }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">完成时间：</span>
                    <span class="value">
                        {{ reservationData.return_at ? formatDateTime(reservationData.return_at) : '未完成' }}
                    </span>
                </div>

                <!-- 取消按钮：仅当状态为“未完成”时显示 -->
                <button v-if="reservationData.status == '未完成'" @click.stop="cancelReservation"
                    class="cancel-reservation-btn">取消预约</button>
            </div>
        </div>

        <!-- 取消理由弹窗 -->
        <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
            <div class="dialog-container">
                <div class="dialog-header">
                    <h3>强制取消预约</h3>
                    <button class="close-btn" @click="closeDialog">×</button>
                </div>
                <div class="dialog-body">
                    <div class="user-info">
                        <p><span class="info-label">实验室：</span>{{ reservationData.laboratoryInfo?.laboratory_name || '未知' }}</p>
                        <p><span class="info-label">用户：</span>{{ reservationData.userInfo?.username || '教学安排' }}</p>
                    </div>
                    <div class="reason-input">
                        <label for="reason">理由：</label>
                        <textarea id="reason" v-model="reason" placeholder="请输入理由" rows="8"></textarea>
                    </div>
                </div>
                <div class="dialog-footer">
                    <button class="cancel-btn" @click="closeDialog">取消</button>
                    <button class="confirm-btn" @click="confirmCancelReservation">确定取消预约</button>
                </div>
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
import { reactive, toRefs } from 'vue';

export default {
    components: {
        Footer
    },
    setup() {
        const router = useRouter()
        // 从 sessionStorage 获取存储的实验室预约数据
        const reservationData = JSON.parse(sessionStorage.getItem('currentLabReservation'))

        const state = reactive({
            showDialog: false,
            reason: '',
        })

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

        function cancelReservation() {
            state.showDialog = true
        }

        function closeDialog() {
            state.showDialog = false
            state.reason = ''
        }

        function confirmCancelReservation() {
            if (!state.reason.trim()) {
                ElMessage.error("请输入取消理由")
                return
            }
            ElMessageBox.confirm(
                `确认要强制取消该实验室预约吗？`,
                '取消预约确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    axios
                        .post('/api/cancelLabReservation', {
                            lab_reservation_id: reservationData.lab_reservation_id,
                            reason: state.reason
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success("取消预约成功")
                                router.push('/labReservation')
                            } else {
                                ElMessage.error(response.data.message || "取消预约失败")
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                            ElMessage.error("取消失败")
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            reservationData,
            formatDateTime,
            cancelReservation,
            closeDialog,
            confirmCancelReservation,
        }
    }
}
</script>

<style scoped>
/* 复用与 ReservationDetailView 完全相同的样式 */
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

.scroll-content {
    margin-top: 100px;
    padding-bottom: 20px;
    min-height: calc(100vh - 120px);
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

.detail-container {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    margin-top: 20px;
    overflow: hidden;
}

.detail-item {
    font-size: 15px;
    line-height: 2;
    border-bottom: 1px dashed #f0f2f5;
    padding: 8px 0;
    display: flex;
}

.detail-item:last-of-type {
    border-bottom: none;
}

.label {
    color: #7f8c8d;
    width: 100px;
    min-width: 100px;
    font-weight: normal;
}

.value {
    color: #2c3e50;
    flex: 1;
}

.cancel-reservation-btn {
    margin-top: 20px;
    padding: 10px 24px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    float: right;
}

.cancel-reservation-btn:hover {
    background-color: #f78989;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

.cancel-reservation-btn:active {
    transform: translateY(0);
}

/* 弹窗样式，与 ReservationDetailView 保持完全一致 */
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

.dialog-container {
    background-color: #ffffff;
    border-radius: 12px;
    width: 500px;
    max-width: 90%;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    animation: slideIn 0.3s ease;
}

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

.close-btn:hover {
    color: #2c3e50;
}

.dialog-body {
    padding: 20px;
}

.user-info {
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 16px;
}

.user-info p {
    margin: 6px 0;
    font-size: 14px;
    color: #2c3e50;
}

.info-label {
    color: #7f8c8d;
    width: 70px;
    display: inline-block;
}

.reason-input {
    margin-top: 16px;
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

.reason-input textarea:focus {
    outline: none;
    border-color: #409eff;
}

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
    color: #2c3e50;
}

.cancel-btn:hover {
    background-color: #f5f7fa;
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

.confirm-btn:hover {
    background-color: #f78989;
}

/* 动画 */
@keyframes slideIn {
    from { transform: translateY(-50px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
}
</style>