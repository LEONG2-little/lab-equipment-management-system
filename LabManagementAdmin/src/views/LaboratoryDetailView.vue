<template>
    <div class="laboratory-management">
        <div class="header-container">
            <div class="section-title">实验室详情</div>
        </div>

        <div class="detail-container">
            <div class="detail-item">
                <span class="detail-label">实验室ID：</span>
                <div class="detail-content">{{ showLaboratoryDetail.laboratory_id }}</div>
            </div>

            <!-- 图片区域 - 参考公告详情页面 -->
            <div class="detail-item">
                <span class="detail-label">图片：</span>
                <div v-if="!isChange" class="detail-content">
                    <img :src="showLaboratoryDetail.image_url" :alt="showLaboratoryDetail.location || '实验室图片'"
                        class="laboratory-image" v-if="showLaboratoryDetail.image_url">
                    <div v-else class="no-image">暂无图片</div>
                </div>
                <div v-else class="image-upload-wrapper">
                    <div class="image-preview-container">
                        <img :src="previewImageUrl || showLaboratoryDetail.image_url" alt="实验室图片预览"
                            class="laboratory-image preview-image"
                            v-if="previewImageUrl || showLaboratoryDetail.image_url">
                        <div v-else class="no-image">暂无图片</div>
                    </div>
                    <div class="image-upload-controls">
                        <input type="file" ref="imageInput" @change="handleImageUpload" accept="image/*"
                            class="image-input">
                        <button type="button" class="upload-btn" @click="triggerImageUpload">选择图片</button>
                    </div>
                </div>
            </div>

            <div class="detail-item">
                <span class="detail-label">名称：</span>
                <input type="text" v-model="showLaboratoryDetail.laboratory_name" :readonly="!isChange"
                    class="detail-input">
            </div>

            <div class="detail-item">
                <span class="detail-label">地点：</span>
                <input type="text" v-model="showLaboratoryDetail.location" :readonly="!isChange" class="detail-input">
            </div>

            <div class="detail-item">
                <span class="detail-label">管理人ID：</span>
                <input type="text" v-model="showLaboratoryDetail.manager_id" :readonly="!isChange" class="detail-input">
            </div>

            <div class="detail-item">
                <span class="detail-label">描述：</span>
                <input type="text" v-model="showLaboratoryDetail.description" :readonly="!isChange"
                    class="detail-input">
            </div>

            <div class="detail-item">
                <span class="detail-label">状态：</span>
                <select v-model="status" class="detail-input" :disabled="!isChange">
                    <option value="闲置">闲置</option>
                    <option value="空闲" v-if="laboratoryDetail.status == '空闲'">空闲</option>
                    <option value="可能发生故障" v-if="laboratoryDetail.status == '可能发生故障'">可能发生故障</option>
                    <option value="维修">维修</option>
                    <option value="停用">停用</option>
                    <option value="故障">故障</option>
                </select>
            </div>

            <div class="button-container">
                <button @click="changeLaboratory" class="action-button"
                    :class="isChange ? 'cancel-button' : 'modify-button'">{{ buttonText }}</button>
                <button v-if="isChange" @click="confirmChanges" class="action-button confirm-button">确认修改</button>
            </div>
        </div>

        <div class="devices-section">
            <div class="section-subtitle">该实验室中的设备</div>
            <div v-if="devicesArr.length > 0" class="devices-grid">
                <div v-for="device in devicesArr" :key="device.device_id" class="device-card"
                    @click="toDeviceDetail(device.device_id)">
                    <img :src="device.image_url" :alt="device.device_name || '设备图片'" class="device-image">
                    <div class="device-info">
                        <div class="info-item"><span class="label">设备ID：</span>{{ device.device_id }}</div>
                        <div class="info-item"><span class="label">设备名称：</span>{{ device.device_name }}</div>
                        <div class="info-item"><span class="label">设备状态：</span>{{ device.status }}</div>
                    </div>
                </div>
            </div>
            <div v-else class="no-devices">该实验室暂无设备</div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import axios from 'axios';
import { reactive, toRefs, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import Footer from '@/components/Footer.vue';

export default {
    components: {
        Footer
    },
    setup() {

        const router = useRouter()
        const route = useRoute()

        const state = reactive({
            laboratory_id: '',
            laboratoryDetail: {},
            status: '',
            showLaboratoryDetail: {},
            buttonText: '修改实验室信息',
            isChange: false,
            laboratoryFiles: [], // 存储上传的图片文件
            previewImageUrl: '', // 图片预览URL
            devicesArr: [],
        })

        const imageInput = ref(null)

        onMounted(() => {
            state.laboratory_id = route.query.laboratory_id
            initLaboratoryDetailArr()
        })

        function initLaboratoryDetailArr() {
            axios
                .post('/api/getLaboratoryDetail', {
                    laboratory_id: state.laboratory_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.laboratoryDetail = response.data.data
                        state.showLaboratoryDetail = JSON.parse(JSON.stringify(response.data.data))

                        state.status = state.showLaboratoryDetail.status
                        initDevicesArr()
                    }
                    else {
                        ElMessage.error(response.data.message)
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }

        function initDevicesArr() {
            axios
                .post('/api/getDeviceByLab', {
                    laboratory_id: state.laboratory_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.devicesArr = response.data.data
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }

        function changeLaboratory() {
            if (state.isChange == false) {
                state.buttonText = '取消'
                state.isChange = true
                // 清空之前可能的上传记录
                state.laboratoryFiles = []
                state.previewImageUrl = ''
            }
            else {
                state.buttonText = '修改实验室信息'
                state.showLaboratoryDetail = JSON.parse(JSON.stringify(state.laboratoryDetail))
                state.isChange = false
                state.status = state.laboratoryDetail.status
                // 取消修改时清空上传记录
                state.laboratoryFiles = []
                state.previewImageUrl = ''
            }
        }

        // 图片上传相关方法
        function triggerImageUpload() {
            imageInput.value.click()
        }

        function handleImageUpload(event) {
            const file = event.target.files[0]
            if (!file) return

            // 保存文件对象
            state.laboratoryFiles[0] = file

            // 生成预览URL
            const reader = new FileReader()
            reader.onload = (e) => {
                state.previewImageUrl = e.target.result
            }
            reader.readAsDataURL(file)

            // 清空input以便重新选择同一文件
            event.target.value = ''
        }

        async function confirmChanges() {

            if (!state.showLaboratoryDetail.location) {
                ElMessage.error('地点不能为空')
                return
            }

            ElMessageBox.confirm(
                '确认要修改这个实验室的信息吗？',
                '修改确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(async () => {

                    state.showLaboratoryDetail.status = state.status

                    try {
                        // 准备要更新的数据
                        const updateData = {
                            laboratory_id: state.laboratory_id,
                            laboratory_name: state.showLaboratoryDetail.laboratory_name,
                            location: state.showLaboratoryDetail.location,
                            description: state.showLaboratoryDetail.description,
                            status: state.showLaboratoryDetail.status,
                        }

                        // 如果有新图片，先上传图片
                        if (state.laboratoryFiles[0]) {
                            const formData = new FormData()
                            formData.append('file', state.laboratoryFiles[0])

                            const uploadResponse = await axios.post('/api/uploadImage', formData, {
                                headers: { 'Content-Type': 'multipart/form-data' }
                            })

                            if (uploadResponse.data.status === 200) {
                                // 将新图片URL添加到更新数据中
                                updateData.image_url = uploadResponse.data.data
                            } else {
                                throw new Error('图片上传失败')
                            }
                        } else {
                            // 如果没有新图片，保留原图片URL
                            let imageUrl = state.showLaboratoryDetail.image_url
                            if (imageUrl) {
                                // 如果是完整URL，提取文件名
                                if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
                                    // 提取最后一个斜杠后面的部分
                                    const parts = imageUrl.split('/')
                                    imageUrl = parts[parts.length - 1]
                                } else if (imageUrl.includes('/')) {
                                    // 如果是 /images/xxx.png 格式，也只取文件名
                                    const parts = imageUrl.split('/')
                                    imageUrl = parts[parts.length - 1]
                                }
                                updateData.image_url = imageUrl
                            }
                        }

                        // 提交更新
                        const response = await axios.post('/api/updateLaboratory', updateData)

                        if (response.data.status == 200) {
                            ElMessage.success('修改成功')
                            router.push('/laboratoryManagement')
                        } else {
                            ElMessage.error('修改失败')
                        }
                    } catch (error) {
                        console.log(error)
                        ElMessage.error('修改失败')
                    }
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                    changeLaboratory()
                })
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
            changeLaboratory,
            confirmChanges,
            triggerImageUpload,
            handleImageUpload,
            imageInput,
            toDeviceDetail,
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
    align-items: flex-start;
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
    flex-shrink: 0;
    line-height: 38px;
}

/* 详情内容（非编辑状态） */
.detail-content {
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

/* 图片样式 */
.laboratory-image {
    max-width: 100%;
    max-height: 200px;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    object-fit: cover;
}

.no-image {
    width: 100%;
    max-width: 200px;
    height: 120px;
    background-color: #f5f7fa;
    border: 1px dashed #dcdfe6;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 14px;
}

/* 图片上传相关样式 - 参考公告详情页面 */
.image-upload-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.image-preview-container {
    width: 100%;
    max-width: 200px;
}

.preview-image {
    border: 2px solid #409eff;
}

.image-upload-controls {
    display: flex;
    gap: 10px;
    align-items: center;
}

.image-input {
    display: none;
}

.upload-btn {
    padding: 8px 16px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    white-space: nowrap;
}

.upload-btn:hover {
    background-color: #66b1ff;
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

/* 取消按钮（当处于修改状态时） */
.cancel-button {
    background-color: #909399;
    color: white;
}

.cancel-button:hover {
    background-color: #a6a9ad;
}

/* 实验室设备区域 */
.devices-section {
    margin-top: 40px;
    width: 100%;
    max-width: 1200px;
}

.section-subtitle {
    font-size: 1.3rem;
    font-weight: 500;
    color: #2c3e50;
    border-left: 4px solid #409eff;
    padding-left: 12px;
    margin-bottom: 20px;
}

/* 设备网格布局 */
.devices-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
}

/* 设备卡片样式 */
.device-card {
    display: flex;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 12px;
    transition: all 0.3s;
}

.device-card:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 设备图片 */
.device-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #ebeef5;
    margin-right: 12px;
}

/* 设备信息区域 */
.device-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.device-info .info-item {
    font-size: 13px;
    color: #3c4a5a;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.device-info .info-item .label {
    color: #7f8c8d;
    margin-right: 6px;
}

/* 暂无设备提示 */
.no-devices {
    padding: 40px;
    text-align: center;
    color: #909399;
    font-size: 14px;
    background-color: #f5f7fa;
    border-radius: 8px;
    border: 1px dashed #dcdfe6;
}
</style>