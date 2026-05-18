<template>
    <div class="device-management">
        <div class="header-container">
            <div class="section-title">设备录入</div>
            <div class="header-buttons">
                <input type="file" ref="fileInput" @change="fileUpload" accept=".xlsx,.xls,.csv" style="display: none;">
                <button @click="batchEntry">批量录入</button>
            </div>
        </div>

        <div class="device-entry-container">
            <!-- 循环渲染所有设备卡片 -->
            <div v-for="(device, index) in devices" :key="index" class="device-entry-card">
                <div class="device-card-header">
                    <div class="device-card-title">设备 {{ index + 1 }}</div>
                    <!-- 删除按钮，只有多于1个设备时才显示 -->
                    <button v-if="devices.length > 1" class="remove-button" @click="removeDevice(index)">
                        <span class="remove-icon">x</span>
                    </button>
                </div>

                <div class="device-input-grid">
                    <div class="input-wrapper">
                        <span class="input-label">设备名称 <span class="required">*</span></span>
                        <input type="text" v-model="device.device_name" placeholder="请输入设备名称" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">设备分类 <span class="required">*</span></span>
                        <input type="text" v-model="device.category" placeholder="请输入设备分类" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">设备型号 <span class="required">*</span></span>
                        <input type="text" v-model="device.model" placeholder="请输入设备型号" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">购买时间 <span class="required">*</span></span>
                        <input type="date" v-model="device.purchase_date" placeholder="请输入购买时间" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">购买价格 <span class="required">*</span></span>
                        <input type="text" v-model="device.price" placeholder="请输入购买价格" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">使用地点 <span class="required">*</span></span>
                        <input type="text" v-model="device.laboratory_id" placeholder="请输入使用地点" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">设备图片 <span class="required">*</span></span>
                        <div class="image-upload-wrapper">
                            <input type="text" v-model="device.image_url" placeholder="图片上传后自动填充" class="device-input"
                                readonly>
                            <button type="button" class="upload-btn" @click="triggerFileUpload(index)">选择图片</button>
                        </div>
                        <!-- 图片预览 -->
                        <div v-if="previewUrls && previewUrls[index]" class="image-preview">
                            <img :src="previewUrls[index]" alt="设备图片预览" class="preview-img">
                        </div>
                        <!-- 隐藏的文件上传输入框 -->
                        <input type="file" ref="fileInputs" @change="handleFileUpload($event, index)" accept="image/*"
                            style="display: none;">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">规格参数</span>
                        <input type="text" v-model="device.spec" placeholder="请输入规格参数" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">设备描述</span>
                        <input type="text" v-model="device.description" placeholder="请输入设备描述" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">设备状态</span>
                        <input type="text" v-model="device.status" placeholder="请输入设备状态" class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">权限等级<span class="required">*</span></span>
                        <input type="text" v-model="device.required_permission" placeholder="请输入权限等级"
                            class="device-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">负责人ID</span>
                        <input type="text" v-model="device.manager_id" placeholder="请输入负责人ID" class="device-input">
                    </div>
                </div>
            </div>

            <!-- 添加设备卡片 - 虚线框 -->
            <div class="add-device-card" @click="addNewDevice">
                <span class="plus-icon">+</span>
                <span class="add-text">添加新设备</span>
            </div>

            <!-- 提交按钮 -->
            <div class="submit-container">
                <button class="submit-button" @click="addDevices">
                    提交所有设备 ({{ devices.length }}个)
                </button>
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import axios from 'axios';
import { reactive, toRefs, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as XLSX from 'xlsx';

export default {
    setup() {

        const fileInputs = ref([])
        const router = useRouter()
        const fileInput = ref(null)

        const state = reactive({
            devices: [
                {
                    device_name: '',
                    category: '',
                    model: '',
                    purchase_date: '',
                    price: '',
                    laboratory_id: '',
                    image_url: '',
                    spec: '',
                    description: '',
                    status: '',
                    required_permission: '',
                    manager_id: ''
                }
            ],
            deviceFiles: [], // 存储每个设备对应的文件对象
            previewUrls: []
        })

        function triggerFileUpload(index) {
            if (fileInputs.value[index]) {
                fileInputs.value[index].click()
            }
        }

        function handleFileUpload(event, index) {
            const file = event.target.files[0]
            if (!file) return

            console.log('选择文件:', file.name)

            // 临时保存文件对象，不上传
            if (!state.deviceFiles) {
                state.deviceFiles = []
            }
            state.deviceFiles[index] = file

            // 本地预览（可选）
            const reader = new FileReader()
            reader.onload = (e) => {
                // 只用于预览，不保存到 image_url
                if (!state.previewUrls) {
                    state.previewUrls = []
                }
                state.previewUrls[index] = e.target.result
            }
            reader.readAsDataURL(file)

            ElMessage.success('文件已选择')
            event.target.value = ''
        }

        function batchEntry() {
            fileInput.value.click()
        }

        function fileUpload(event) {
            const file = event.target.files[0]
            if (!file) return

            const reader = new FileReader()
            reader.onload = (e) => {
                try {
                    const data = new Uint8Array(e.target.result)
                    const workbook = XLSX.read(data, { type: 'array' })

                    const firstSheetName = workbook.SheetNames[0]
                    const worksheet = workbook.Sheets[firstSheetName]

                    const jsonData = XLSX.utils.sheet_to_json(worksheet)

                    importDevicesFromExcel(jsonData)
                }
                catch (error) {
                    console.error('解析Excel失败:', error)
                    ElMessage.error('解析Excel文件失败，请检查文件格式')
                }
            }
            reader.readAsArrayBuffer(file)
            event.target.value = ''
        }

        function importDevicesFromExcel(excelData) {
            if (!excelData || excelData.length === 0) {
                ElMessage.warning('Excel文件中没有数据')
                return
            }

            const fileUploadDevices = excelData.map(row => {
                // 处理购买时间 - 放在map循环内部
                let purchaseDate = ''
                const dateValue = row['购买时间']

                if (dateValue) {
                    // 如果是字符串类型
                    if (typeof dateValue === 'string') {
                        // 如果是 "2022-09-10 00:00:00" 格式，只取日期部分
                        if (dateValue.includes(' ')) {
                            purchaseDate = dateValue.split(' ')[0]
                        } else {
                            purchaseDate = dateValue
                        }
                    }
                    // 如果是数字类型（Excel序列号）
                    else if (typeof dateValue === 'number') {
                        // Excel日期序列号转JS日期
                        // Excel的1900日期系统从1900-01-01开始
                        const date = new Date((dateValue - 25569) * 86400 * 1000)
                        // 格式化为 YYYY-MM-DD
                        const year = date.getFullYear()
                        const month = String(date.getMonth() + 1).padStart(2, '0')
                        const day = String(date.getDate()).padStart(2, '0')
                        purchaseDate = `${year}-${month}-${day}`
                    }
                }

                return {
                    device_name: row['设备名称'] || '',
                    category: row['设备分类'] || '',
                    model: row['设备型号'] || '',
                    purchase_date: purchaseDate,  // 使用处理后的日期
                    price: row['购买价格'] || '',
                    laboratory_id: row['使用地点'] || '',
                    image_url: row['设备图片'] || '',
                    spec: row['规格参数'] || '',
                    description: row['设备描述'] || '',
                    status: row['设备状态'] || '',
                    required_permission: row['设备权限'] ? String(row['设备权限']) : '',
                    manager_id: row['负责人ID'] ? String(row['负责人ID']) : ''
                }
            })

            state.devices = [...state.devices, ...fileUploadDevices]
            ElMessage.success(`成功导入 ${fileUploadDevices.length} 个设备`)
        }

        function addNewDevice() {
            state.devices.push({
                device_name: '',
                category: '',
                model: '',
                purchase_date: '',
                price:'',
                laboratory_id: '',
                image_url: '',
                spec: '',
                description: '',
                status: '',
                required_permission: '',
                manager_id: ''
            })
            state.deviceFiles.push(null)
            state.previewUrls.push(null)
        }

        function removeDevice(index) {
            if (state.devices.length > 1) {
                state.devices.splice(index, 1)

                if (state.deviceFiles) {
                    state.deviceFiles.splice(index, 1)
                }
                if (state.previewUrls) {
                    state.previewUrls.splice(index, 1)
                }
            } else {
                ElMessage.warning('至少需要一个设备')
            }
        }

        function addDevices() {
            for (let i = 0; i < state.devices.length; i++) {
                const device = state.devices[i]

                // 检查必填字段
                if (!device.device_name) {
                    ElMessage.error(`设备 ${i + 1}：设备名称不能为空`)
                    return
                }
                if (!device.category) {
                    ElMessage.error(`设备 ${i + 1}：设备分类不能为空`)
                    return
                }
                if (!device.laboratory_id) {
                    ElMessage.error(`设备 ${i + 1}：使用地点不能为空`)
                    return
                }
                if (!device.purchase_date) {
                    ElMessage.error(`设备 ${i + 1}：购买时间不能为空`)
                    return
                }
                if (!device.model) {
                    ElMessage.error(`设备 ${i + 1}：设备型号不能为空`)
                    return
                }
                if (!device.price) {
                    ElMessage.error(`设备 ${i + 1}：购买价格不能为空`)
                    return
                }
            }

            ElMessageBox.confirm(
                `确认要添加${state.devices.length}个新设备吗？`,
                '添加确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(async () => {

                    const uploadPromises = []

                    for (let i = 0; i < state.devices.length; i++) {
                        const file = state.deviceFiles[i]

                        if (file) {
                            // 有图片，先上传
                            const formData = new FormData()
                            formData.append('file', file)

                            const uploadPromise = axios.post('/api/uploadImage', formData, {
                                headers: { 'Content-Type': 'multipart/form-data' }
                            }).then(response => {
                                if (response.data.status === 200) {
                                    //将返回的图片URL赋给对应的设备
                                    state.devices[i].image_url = response.data.data
                                } else {
                                    throw new Error(`设备${i + 1}图片上传失败`)
                                }
                            })

                            uploadPromises.push(uploadPromise)
                        } else {
                            // 没有图片，可以设置默认图片或留空
                            state.devices[i].image_url = ''
                        }
                    }

                    try {
                        await Promise.all(uploadPromises)
                        axios
                            .post("/api/addDevices", state.devices)
                            .then((response) => {
                                if (response.data.status == 200) {
                                    ElMessage.success(`成功添加 ${state.devices.length} 个设备`)
                                    router.push('/deviceManagement')
                                } else {
                                    ElMessage.error(response.data.message)
                                }
                            })
                            .catch(function (error) {
                                console.log(error)
                            })
                    } catch (error) {
                        console.error('图片上传失败:', error)
                        ElMessage.error('图片上传失败，请重试')
                    }
                }).catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            addNewDevice,
            removeDevice,
            addDevices,
            fileInput,
            fileInputs,
            batchEntry,
            fileUpload,
            triggerFileUpload,
            handleFileUpload,
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

/* 设备录入容器 - 添加在原有样式后面 */
.device-entry-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
}

/* 设备卡片样式 - 实线框 */
.device-entry-card {
    border: 2px solid #dcdfe6;
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    box-sizing: border-box;
    background-color: #ffffff;
}

/* 设备卡片头部 - 用于放置标题和删除按钮 */
.device-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;
}

/* 设备卡片标题 - 修改原有样式 */
.device-card-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin: 0;
}

/* 输入网格布局 - 每行三个输入框 */
.device-input-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
}

/* 输入框容器 */
.input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

/* 输入框标签 */
.input-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}

/* 输入框样式 */
.device-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.3s;
    outline: none;
    box-sizing: border-box;
}

.device-input:focus {
    border-color: #409eff;
}

/* 添加设备卡片 - 虚线框 */
.add-device-card {
    border: 2px dashed #dcdfe6;
    border-radius: 12px;
    padding: 40px;
    width: 100%;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    background-color: #fafafa;
}

.add-device-card:hover {
    border-color: #409eff;
    background-color: #f0f7ff;
}

/* 加号样式 */
.plus-icon {
    font-size: 48px;
    color: #909399;
    line-height: 1;
    transition: color 0.3s;
}

.add-device-card:hover .plus-icon {
    color: #409eff;
}

/* 添加按钮文字（可选） */
.add-text {
    font-size: 16px;
    color: #909399;
    margin-left: 10px;
}

/* 提交按钮容器 */
.submit-container {
    margin-top: 30px;
    display: flex;
    justify-content: flex-end;
}

/* 提交按钮 */
.submit-button {
    padding: 12px 40px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.submit-button:hover {
    background-color: #66b1ff;
}

/* 删除按钮 */
.remove-button {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 1px solid #dcdfe6;
    background-color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
}

.remove-button:hover {
    border-color: #f56c6c;
    background-color: #fef0f0;
}

/* 删除图标 */
.remove-icon {
    font-size: 20px;
    color: #909399;
    line-height: 1;
}

.remove-button:hover .remove-icon {
    color: #f56c6c;
}

/* 必填字段标记 */
.required {
    color: #f56c6c;
    margin-left: 4px;
}

/* 批量录入按钮 */
.header-container button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
    white-space: nowrap;
}

.header-container button:hover {
    background-color: #ebb563;
}

/* 头部按钮容器 */
.header-buttons {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* 批量录入按钮 - 修改原有选择器 */
.header-buttons button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
    white-space: nowrap;
}

.header-buttons button:hover {
    background-color: #ebb563;
}

.image-upload-wrapper {
    display: flex;
    gap: 10px;
    align-items: center;
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
    min-width: 80px;
}

.upload-btn:hover {
    background-color: #66b1ff;
}

.image-preview {
    margin-top: 10px;
    width: 100px;
    height: 100px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    overflow: hidden;
}

.preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
</style>