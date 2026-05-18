<template>
    <div class="disabled-period-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">禁用时段管理</div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <!-- 添加按钮 -->
            <div class="add-button-container">
                <button class="add-btn" @click="openAddDialog">+ 添加禁用时段</button>
            </div>

            <!-- 禁用时段列表 -->
            <div class="period-list">
                <div v-for="disabled in disabledPeriodArr" :key="disabled.disable_id" class="period-item">
                    <div class="period-info">
                        <div class="info-row">
                            <span class="info-label">开始时间：</span>
                            <span class="info-value">{{ disabled.start_time.split('T')[0] }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">结束时间：</span>
                            <span class="info-value">{{ disabled.end_time.split('T')[0] }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">停用原因：</span>
                            <span class="info-value">{{ disabled.reason }}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">执行状态：</span>
                            <span class="info-value">{{ disabled.status }}</span>
                        </div>
                    </div>
                    <div class="period-actions">
                        <button v-if="disabled.status == '未执行'" @click="executeOperation(disabled)" class="execute-btn">
                            开始执行
                        </button>
                        <button v-if="disabled.status == '执行中'" @click="executeOperation(disabled)" class="cancel-execute-btn">
                            取消执行
                        </button>
                    </div>
                </div>
            </div>

            <div v-if="disabledPeriodArr.length === 0" class="no-result">
                暂无禁用时段
            </div>
        </div>

        <!-- 添加禁用时段弹窗 -->
        <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
            <div class="dialog-container">
                <div class="dialog-header">
                    <h3>添加禁止预约时间段</h3>
                    <button class="close-btn" @click="closeDialog">×</button>
                </div>
                <div class="dialog-body">
                    <div class="form-row">
                        <div class="form-item">
                            <label class="form-label">开始时间：</label>
                            <input type="date" v-model="start_time" class="form-input">
                        </div>
                        <div class="form-item">
                            <label class="form-label">结束时间：</label>
                            <input type="date" v-model="end_time" class="form-input">
                        </div>
                    </div>
                    <div class="form-item reason-item">
                        <label class="form-label">停用原因：</label>
                        <textarea id="reason" v-model="reason" placeholder="请输入停用原因" rows="4" class="form-textarea"></textarea>
                    </div>
                </div>
                <div class="dialog-footer">
                    <button class="cancel-btn" @click="closeDialog">取消</button>
                    <button class="confirm-btn" @click="confirmAdd">确定添加</button>
                </div>
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import axios from 'axios';
import { reactive, toRefs } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import Footer from '@/components/Footer.vue';

export default {
    components: {
        Footer
    },
    setup() {

        const state = reactive({
            disabledPeriodArr: [],
            showDialog: false,
            start_time: '',
            end_time: '',
            reason: '',
            status: '执行中',
        })

        function initDisabledPeriodArr() {
            axios
                .post('/api/selectAll')
                .then((response) => {
                    if (response.data.status == 200) {
                        state.disabledPeriodArr = response.data.data
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initDisabledPeriodArr()

        function executeOperation(disabled) {
            ElMessageBox.confirm(
                `确认要执行该操作吗？`,
                `操作确认`,
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    if (disabled.status == '未执行') {
                        const status = '执行中'
                        axios
                            .post('/api/executeOperation', {
                                disable_id: disabled.disable_id,
                                status: status
                            })
                            .then((response) => {
                                if (response.data.status == 200) {
                                    ElMessage.success("操作成功")
                                    initDisabledPeriodArr()
                                }
                            })
                            .catch(function (error) {
                                console.log(error)
                            })
                    }

                    if (disabled.status == '执行中') {
                        const status = '未执行'
                        axios
                            .post('/api/executeOperation', {
                                disable_id: disabled.disable_id,
                                status: status
                            })
                            .then((response) => {
                                if (response.data.status == 200) {
                                    ElMessage.success("操作成功")
                                    initDisabledPeriodArr()
                                }
                            })
                            .catch(function (error) {
                                console.log(error)
                            })
                    }
                }).catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        function openAddDialog() {
            state.showDialog = true
        }

        function closeDialog() {
            state.showDialog = false
            state.start_time = ''
            state.end_time = ''
            state.reason = ''
        }

        function confirmAdd() {
            if (!state.start_time || !state.end_time || !state.reason) {
                ElMessage.warning('请填写完整信息')
                return
            }

            if (state.start_time > state.end_time) {
                ElMessage.warning('开始时间不能大于结束时间')
                return
            }

            ElMessageBox.confirm(
                '确认要添加该禁止预约时间段吗？',
                '添加确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    axios
                        .post('/api/addTime', {
                            start_time: state.start_time,
                            end_time: state.end_time,
                            reason: state.reason,
                            status: state.status
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success("添加成功")
                                closeDialog()
                                initDisabledPeriodArr()
                            } else {
                                ElMessage.error(response.data.message || "添加失败")
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                            ElMessage.error("添加失败")
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            executeOperation,
            openAddDialog,
            closeDialog,
            confirmAdd,
        }
    }
}
</script>

<style scoped>
.disabled-period-management {
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
    padding-bottom: 20px;
    min-height: calc(100vh - 140px);
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

/* 添加按钮容器 */
.add-button-container {
    margin-bottom: 24px;
}

/* 添加按钮 */
.add-btn {
    padding: 8px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.add-btn:hover {
    background-color: #66b1ff;
}

/* 禁用时段列表 */
.period-list {
    width: 100%;
    margin-top: 10px;
}

/* 列表项 */
.period-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 12px;
    transition: all 0.2s;
}

.period-item:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 信息区域 */
.period-info {
    flex: 1;
}

.info-row {
    margin-bottom: 8px;
    font-size: 14px;
}

.info-row:last-child {
    margin-bottom: 0;
}

.info-label {
    color: #7f8c8d;
    width: 80px;
    display: inline-block;
}

.info-value {
    color: #2c3e50;
    font-weight: 500;
}

/* 操作按钮区域 */
.period-actions {
    margin-left: 20px;
}

/* 开始执行按钮 */
.execute-btn {
    padding: 6px 16px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.execute-btn:hover {
    background-color: #85ce61;
}

/* 取消执行按钮 */
.cancel-execute-btn {
    padding: 6px 16px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.cancel-execute-btn:hover {
    background-color: #f78989;
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

@keyframes slideIn {
    from {
        transform: translateY(-50px);
        opacity: 0;
    }
    to {
        transform: translateY(0);
        opacity: 1;
    }
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

.form-row {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
}

.form-item {
    flex: 1;
}

.form-label {
    display: block;
    margin-bottom: 8px;
    font-size: 14px;
    color: #2c3e50;
    font-weight: 500;
}

.form-input {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    box-sizing: border-box;
    outline: none;
}

.form-input:focus {
    border-color: #409eff;
}

.reason-item {
    margin-top: 8px;
}

.form-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    resize: vertical;
    box-sizing: border-box;
    font-family: inherit;
    outline: none;
}

.form-textarea:focus {
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
    transition: all 0.2s;
}

.cancel-btn:hover {
    background-color: #f5f7fa;
}

.confirm-btn {
    padding: 8px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.confirm-btn:hover {
    background-color: #66b1ff;
}
</style>