<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">预约详情</div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="detail-container">
                <!-- 详情内容保持不变 -->
                <div class="detail-item">
                    <span class="label">预约ID：</span>
                    <span class="value">{{ reservationData.reservation_id }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">借用设备：</span>
                    <span class="value">{{ reservationData.deviceInfo.device_name }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">设备分类：</span>
                    <span class="value">{{ reservationData.deviceInfo.category }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">设备型号：</span>
                    <span class="value">{{ reservationData.deviceInfo.model }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">购买时间：</span>
                    <span class="value">{{ reservationData.deviceInfo.purchase_date.split('T')[0] }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">使用地点：</span>
                    <span class="value">{{ reservationData.deviceInfo.location }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">规格参数：</span>
                    <span class="value">{{ reservationData.deviceInfo.spec }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">描述：</span>
                    <span class="value">{{ reservationData.deviceInfo.description }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">预约人：</span>
                    <span class="value">{{ reservationData.userInfo.username }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">使用用途：</span>
                    <span class="value">{{ reservationData.purpose }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">预约时间：</span>
                    <span class="value">{{ reservationData.start_time.split('T')[0] }} 到 {{
                        reservationData.end_time.split('T')[0] }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">预约状态：</span>
                    <span class="value">{{ reservationData.status }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">完成时间：</span>
                    <span class="value">{{ reservationData.return_at ? reservationData.return_at.split('T')[0] : '未完成'
                        }}</span>
                </div>

                <button v-if="reservationData.status == '未完成'" @click.stop="cancelReservation"
                    class="cancel-reservation-btn">取消预约</button>
            </div>
        </div>

        <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
            <div class="dialog-container">
                <div class="dialog-header">
                    <h3>强制取消预约</h3>
                    <button class="close-btn" @click="closeDialog">×</button>
                </div>
                <div class="dialog-body">
                    <div class="user-info">
                        <p><span class="info-label">设备：</span>{{ reservationData.deviceInfo.device_name }}</p>
                        <p><span class="info-label">用户：</span>{{ reservationData.userInfo.username }}</p>
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
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';
import { reactive, toRefs } from 'vue';

export default {
    setup() {

        const router = useRouter()
        const route = useRoute()
        const reservationData = JSON.parse(sessionStorage.getItem('currentReservation'))

        const state = reactive({
            showDialog: false,
            reason: '',
        })

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


        function cancelReservation() {
            state.showDialog = true
        }

        function closeDialog() {
            state.showDialog = false
        }

        function confirmCancelReservation() {
            if (!state.reason) {
                ElMessage.error("请输入取消理由")
                return

            } ElMessageBox.confirm(
                `确认要强制取消该预约吗？`,
                '取消预约确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {

                    axios
                        .post('/api/cancelReservation', {
                            reservation_id: reservationData.reservation_id,
                            reason: state.reason
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success("取消该预约成功")
                                router.push('/appointmentManagement')
                            }
                            else {
                                ElMessage.success("取消该预约失败")
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
            reservationData,
            formatDateTime,
            cancelReservation,
            closeDialog,
            confirmCancelReservation,
        }
    },
    components: {
        Footer
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
    margin-top: 100px;
    /* 根据头部高度调整 */
    padding-bottom: 20px;
    min-height: calc(100vh - 120px);
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

/* 新增的详情容器样式 */
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

.detail-item:last-child {
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


/* 使用记录区域样式 */
.usage-section {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    margin-top: 20px;
}

.usage-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #409eff;
}

.usage-item {
    background-color: #f8fafc;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
}

.usage-item:last-child {
    margin-bottom: 0;
}

.usage-header {
    font-size: 16px;
    font-weight: 500;
    color: #409eff;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #dcdfe6;
}

.usage-detail {
    font-size: 14px;
    line-height: 1.8;
    color: #3c4a5a;
    display: flex;
    margin-bottom: 4px;
}

.usage-detail:last-child {
    margin-bottom: 0;
}

.usage-label {
    color: #7f8c8d;
    width: 80px;
    min-width: 80px;
}

.no-usage {
    text-align: center;
    padding: 40px 0;
    color: #909399;
    font-size: 14px;
    background-color: #f5f7fa;
    border-radius: 8px;
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
    animation: slideIn 0.3s ease;
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

.close-btn:hover {
    color: #2c3e50;
}

/* 弹窗主体 */
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
    color: #2c3e50;
}

.cancel-btn:hover {
    background-color: #f5f7fa;
}

.confirm-btn {
    padding: 8px 20px;
    background-color: #09ec99;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

.confirm-btn:hover {
    background-color: #09ec99;
}

/* 在 style 末尾添加取消预约按钮样式 */
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
</style>