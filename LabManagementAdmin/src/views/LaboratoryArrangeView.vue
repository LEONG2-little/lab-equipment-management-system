<template>
    <div class="laboratory-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">教学安排</div>
            </div>

            <!-- 实验室选择放在头部下方 -->
            <div class="laboratory-select-container">
                <select v-model="selectedLaboratoryId" class="laboratory-select">
                    <option value="">请选择实验室</option>
                    <option v-for="lab in laboratoryArr" :key="lab.laboratory_id" :value="lab.laboratory_id">
                        {{ lab.laboratory_name }}（{{ lab.location }}）
                    </option>
                </select>
            </div>

            <div>
                <span>使用人ID：</span>
                <input type="text" v-model="user_id">
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <!-- 多个时间段选择 -->
            <div v-for="(timeSlot, index) in timeSlotSelections" :key="index" class="time-slot-item">
                <div class="time-slot-header">
                    <span>时间段 {{ index + 1 }}</span>
                    <button v-if="timeSlotSelections.length > 1" @click="removeTimeSlot(index)"
                        class="remove-btn">删除</button>
                </div>

                <div class="time-slot-content">
                    <div class="form-item">
                        <span class="form-label">选择日期：</span>
                        <input type="date" v-model="timeSlot.selectDate" class="form-input date-input">
                    </div>
                    <div class="form-item">
                        <span class="form-label">选择时间：</span>
                        <select v-model="timeSlot.selectedTimeSlot" @change="handleTimeSlotChange(index)"
                            class="form-input time-select">
                            <option value="">请安排使用时间</option>
                            <option v-for="(slot, slotIndex) in timeSlots" :key="slotIndex" :value="slotIndex">
                                {{ slot.label }}
                            </option>
                        </select>
                    </div>
                    <div class="time-display">
                        <div class="time-item">
                            <span class="time-label">开始时间：</span>
                            <span class="time-value">{{ timeSlot.start_time || '未选择' }}</span>
                        </div>
                        <div class="time-item">
                            <span class="time-label">结束时间：</span>
                            <span class="time-value">{{ timeSlot.end_time || '未选择' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 添加时间段按钮 -->
            <div class="button-container">
                <button @click="addTimeSlot" class="add-btn">
                    <span>+ 添加时间段</span>
                </button>
                <button @click="submitAllArrange" class="submit-btn">
                    提交安排
                </button>
            </div>
        </div>

        <Footer></Footer>
    </div>
</template>

<script>
import { reactive, toRefs, watch } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import Footer from '@/components/Footer.vue';

export default {
    components: {
        Footer
    },
    setup() {

        const router = useRouter()

        const state = reactive({
            laboratoryArr: [],
            user_id: '',
            laboratoryArrangeArr: [],
            selectedLaboratoryId: '',
            // 改为数组存储多个时间段选择
            timeSlotSelections: [
                {
                    selectDate: '',
                    selectedTimeSlot: '',
                    start_time: '',
                    end_time: ''
                }
            ],
            // 预定义的时间段
            timeSlots: [
                { label: '08:30-09:15', start: '08:30', end: '09:15' },
                { label: '09:25-10:10', start: '09:25', end: '10:10' },
                { label: '10:30-11:15', start: '10:30', end: '11:15' },
                { label: '11:25-12:10', start: '11:25', end: '12:10' },
                { label: '13:05-13:50', start: '13:05', end: '13:50' },
                { label: '14:00-14:45', start: '14:00', end: '14:45' },
                { label: '14:55-15:40', start: '14:55', end: '15:40' },
                { label: '15:50-16:35', start: '15:50', end: '16:35' },
                { label: '16:45-17:30', start: '16:45', end: '17:30' },
                { label: '18:30-19:15', start: '18:30', end: '19:15' },
                { label: '19:25-20:10', start: '19:25', end: '20:10' },
                { label: '20:20-21:05', start: '20:20', end: '21:05' }
            ]
        })

        function initLaboratoryArr() {
            axios
                .get("/api/getAllLaboratory")
                .then((response) => {
                    if (response.data.status == 200) {
                        console.log(response.data.data)
                        state.laboratoryArr = response.data.data
                    } else {
                        ElMessage.error("获取实验室信息失败")
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initLaboratoryArr()

        // 添加新的时间段
        function addTimeSlot() {
            state.timeSlotSelections.push({
                selectDate: '',
                selectedTimeSlot: '',
                start_time: '',
                end_time: ''
            })
        }

        // 删除时间段
        function removeTimeSlot(index) {
            state.timeSlotSelections.splice(index, 1)
        }

        // 处理时间段选择变化
        function handleTimeSlotChange(index) {
            const timeSlot = state.timeSlotSelections[index]
            if (timeSlot.selectedTimeSlot !== '') {
                const slot = state.timeSlots[timeSlot.selectedTimeSlot]
                // 如果有选择日期，将日期和时间组合
                if (timeSlot.selectDate) {
                    timeSlot.start_time = timeSlot.selectDate + ' ' + slot.start + ':00'
                    timeSlot.end_time = timeSlot.selectDate + ' ' + slot.end + ':00'
                } else {
                    // 如果没有选择日期，只显示时间
                    timeSlot.start_time = slot.start
                    timeSlot.end_time = slot.end
                    ElMessage.warning(`时间段 ${index + 1}：请先选择日期`)
                }
            } else {
                timeSlot.start_time = ''
                timeSlot.end_time = ''
            }
        }

        // 为每个时间段单独监听日期变化
        watch(() => state.timeSlotSelections, (newSelections) => {
            newSelections.forEach((timeSlot, index) => {
                if (timeSlot.selectDate && timeSlot.selectedTimeSlot !== '') {
                    const slot = state.timeSlots[timeSlot.selectedTimeSlot]
                    timeSlot.start_time = timeSlot.selectDate + ' ' + slot.start + ':00'
                    timeSlot.end_time = timeSlot.selectDate + ' ' + slot.end + ':00'
                }
            })
        }, { deep: true })

        // 提交所有时间段
        function submitAllArrange() {
            if (!state.selectedLaboratoryId) {
                ElMessage.error('请选择实验室')
                return
            }

            if (!state.user_id) {
                ElMessage.error('请输入使用人ID')
                return
            }

            // 验证每个时间段
            for (let i = 0; i < state.timeSlotSelections.length; i++) {
                const timeSlot = state.timeSlotSelections[i]
                if (!timeSlot.selectDate) {
                    ElMessage.error(`时间段 ${i + 1}：请选择日期`)
                    return
                }
                if (timeSlot.selectedTimeSlot === '') {
                    ElMessage.error(`时间段 ${i + 1}：请选择使用时间`)
                    return
                }
            }

            // 构建提交数据
            const arrangeData = {
                laboratory_id: state.selectedLaboratoryId,
                user_id: state.user_id,
                time_slots: state.timeSlotSelections.map(slot => ({
                    start_time: slot.start_time,
                    end_time: slot.end_time
                }))
            }

            console.log('提交数据：', arrangeData)

            // 发送到后端
            axios.post('/api/addLaboratoryArrange', arrangeData)
                .then(response => {
                    if (response.data.status == 200) {
                        ElMessage.success('安排成功')
                        router.push('/laboratoryManagement')
                    }
                    else {
                        ElMessage({
                            message: response.data.message.replace(/\n/g, '<br>'),
                            type: 'info',
                            dangerouslyUseHTMLString: true,
                            duration: 5000, // 延长显示时间，方便阅读
                            showClose: true  // 显示关闭按钮
                        })
                    }
                })
                .catch(error => {
                    console.log(error)
                    ElMessage.error('安排失败')
                })
        }

        return {
            ...toRefs(state),
            handleTimeSlotChange,
            addTimeSlot,
            removeTimeSlot,
            submitAllArrange
        }
    }
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

/* 固定头部区域 - 与预约详情一致 */
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

/* 头部容器 */
.header-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

/* 标题 - 与预约详情一致 */
.section-title {
    font-size: 1.6rem;
    font-weight: 500;
    color: #2c3e50;
    border-left: 6px solid #409eff;
    padding-left: 16px;
    line-height: 1.3;
    margin: 0;
}

/* 实验室选择容器 */
.laboratory-select-container {
    margin-bottom: 20px;
}

/* 实验室选择框 */
.laboratory-select {
    width: 300px;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    background-color: white;
    cursor: pointer;
}

.laboratory-select:focus {
    border-color: #409eff;
}

/* 滚动内容区域 - 与预约详情一致 */
.scroll-content {
    margin-top: 200px;
    /* 根据头部高度调整 */
    padding-bottom: 20px;
    min-height: calc(100vh - 160px);
}

/* 时间段卡片 - 与使用记录卡片风格一致 */
.time-slot-item {
    background-color: #f8fafc;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
}

.time-slot-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
    color: #409eff;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #dcdfe6;
}

.time-slot-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

/* 表单项 */
.form-item {
    display: flex;
    align-items: center;
    gap: 12px;
}

.form-label {
    width: 80px;
    font-size: 14px;
    color: #7f8c8d;
}

.form-input {
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.3s;
    background-color: #ffffff;
}

.form-input:focus {
    border-color: #409eff;
}

.date-input {
    width: 200px;
}

.time-select {
    width: 200px;
}

/* 时间显示区域 */
.time-display {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    padding: 12px;
    margin-top: 8px;
}

.time-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 4px;
}

.time-item:last-child {
    margin-bottom: 0;
}

.time-label {
    width: 80px;
    font-size: 14px;
    color: #7f8c8d;
}

.time-value {
    font-size: 14px;
    color: #2c3e50;
    font-weight: 500;
}

/* 按钮容器 */
.button-container {
    display: flex;
    gap: 12px;
    margin-top: 20px;
}

/* 添加按钮 - 与确认按钮风格一致 */
.add-btn {
    padding: 10px 24px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
}

.add-btn:hover {
    background-color: #85ce61;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.add-btn:active {
    transform: translateY(0);
}

/* 提交按钮 - 与修改按钮风格一致 */
.submit-btn {
    padding: 10px 24px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
}

.submit-btn:hover {
    background-color: #66b1ff;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.submit-btn:active {
    transform: translateY(0);
}

/* 删除按钮 - 与取消预约按钮风格一致 */
.remove-btn {
    padding: 4px 12px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.3s;
}

.remove-btn:hover {
    background-color: #f78989;
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
}

.remove-btn:active {
    transform: translateY(0);
}

/* 使用人ID容器 */
.fixed-header > div:last-of-type {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 12px;
}

/* 使用人ID标签 */
.fixed-header > div:last-of-type span {
    width: 80px;
    font-size: 14px;
    color: #7f8c8d;
    white-space: nowrap;
}

/* 使用人ID输入框 */
.fixed-header > div:last-of-type input {
    width: 200px;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    transition: all 0.3s;
    background-color: #ffffff;
}

/* 输入框聚焦效果 */
.fixed-header > div:last-of-type input:focus {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 输入框悬停效果 */
.fixed-header > div:last-of-type input:hover {
    border-color: #c0c4cc;
}
</style>