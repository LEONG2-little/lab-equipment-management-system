<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">用户详细信息</div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="detail-container">
                <div class="detail-item">
                    <span class="detail-label">ID：</span>
                    <span class="detail-value">{{ userData.user_id }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">姓名：</span>
                    <span class="detail-value">{{ userData.username }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">身份：</span>
                    <span class="detail-value">{{ userData.identity_type }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">电话号码：</span>
                    <span class="detail-value">{{ userData.phone }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">邮箱：</span>
                    <span class="detail-value">{{ userData.email }}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">状态：</span>
                    <span class="detail-value status-tag">{{ userData.status }}</span>
                </div>
            </div>

            <div class="reservation-count">
                预约总次数: {{ labReservationData.length + deviceReservationData.length}}
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import { useRouter, useRoute } from 'vue-router';
import { reactive, toRefs } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
    setup() {

        const route = useRoute()
        const userData = JSON.parse(sessionStorage.getItem('currentUser'))

        const state = reactive({
            reservationData: [],
            deviceReservationData: [],
            labReservationData: [],
        })

        function initDeviceReservationData() {
            axios
                .post('/api/getReservationByUserId', {
                    user_id: userData.user_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.deviceReservationData = response.data.data

                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initDeviceReservationData()

        function initLabReservationData() {
            axios
                .post('/api/getLabReservationByUserId', {
                    user_id: userData.user_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.labReservationData = response.data.data

                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initLabReservationData()

        return {
            ...toRefs(state),
            userData,
        }
    }
}
</script>

<style scoped>
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

/* 在 style 末尾添加 */

/* 固定头部区域 */
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
    padding-bottom: 20px;
    min-height: calc(100vh - 140px);
}

/* 修改原有的 .device-management */
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

/* 详情容器 */
.detail-container {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    margin-top: 20px;
    max-width: 800px;
}

/* 详情项 */
.detail-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px dashed #f0f2f5;
    font-size: 15px;
}

.detail-item:last-child {
    border-bottom: none;
}

/* 详情标签 */
.detail-label {
    width: 100px;
    color: #7f8c8d;
    font-weight: normal;
}

/* 详情值 */
.detail-value {
    flex: 1;
    color: #2c3e50;
}

/* 状态标签 */
.status-tag {
    display: inline-block;
    padding: 4px 12px;
    background-color: #ecf5ff;
    color: #409eff;
    border-radius: 4px;
    font-size: 14px;
    width: auto;
}

.reservation-count {
    margin-top: 20px;
    padding: 16px 24px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    max-width: 800px;
    font-size: 16px;
    color: #2c3e50;
}
</style>