<template>
    <div class="laboratory-management">
        <div class="header-container">
            <div class="section-title">实验室录入</div>
            <div class="header-buttons">
                <input type="file" ref="fileInput" @change="fileUpload" accept=".xlsx,.xls,.csv" style="display: none;">
                <button @click="batchEntry">批量录入</button>
            </div>
        </div>

        <div class="laboratory-entry-container">
            <!-- 循环渲染所有实验室卡片 -->
            <div v-for="(laboratory, index) in laboratories" :key="index" class="laboratory-entry-card">
                <div class="laboratory-card-header">
                    <div class="laboratory-card-title">实验室 {{ index + 1 }}</div>
                    <!-- 删除按钮，只有多于1个实验室时才显示 -->
                    <button v-if="laboratories.length > 1" class="remove-button" @click="removeLaboratory(index)">
                        <span class="remove-icon">x</span>
                    </button>
                </div>

                <div class="laboratory-input-grid">
                    <div class="input-wrapper">
                        <span class="input-label">实验室名称 <span class="required">*</span></span>
                        <input type="text" v-model="laboratory.laboratory_name" placeholder="请输入实验室名称"
                            class="laboratory-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">地点 <span class="required">*</span></span>
                        <input type="text" v-model="laboratory.location" placeholder="请输入地点" class="laboratory-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">实验室图片</span>
                        <div class="image-upload-wrapper">
                            <input type="text" v-model="laboratory.image_url" placeholder="图片上传后自动填充"
                                class="laboratory-input" readonly>
                            <button type="button" class="upload-btn" @click="triggerFileUpload(index)">选择图片</button>
                        </div>
                        <!-- 图片预览 -->
                        <div v-if="previewUrls && previewUrls[index]" class="image-preview">
                            <img :src="previewUrls[index]" alt="实验室图片预览" class="preview-img">
                        </div>
                        <!-- 隐藏的文件上传输入框 -->
                        <input type="file" ref="fileInputs" @change="handleFileUpload($event, index)" accept="image/*"
                            style="display: none;">
                    </div>

                    <div class="input-wrapper">
                        <span class="input-label">描述</span>
                        <input type="text" v-model="laboratory.description" placeholder="请输入描述"
                            class="laboratory-input">
                    </div>

                    <div class="input-wrapper">
                        <span class="input-label">管理员ID</span>
                        <input type="text" v-model="laboratory.manager_id" placeholder="请输入管理员ID"
                            class="laboratory-input">
                    </div>

                    <div class="input-wrapper">
                        <span class="input-label">实验室状态</span>
                        <select v-model="laboratory.status" class="laboratory-input">
                            <option value="">请选择状态</option>
                            <option value="闲置">闲置</option>
                            <option value="维修">维修</option>
                            <option value="停用">停用</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- 添加实验室卡片 - 虚线框 -->
            <div class="add-laboratory-card" @click="addNewLaboratory">
                <span class="plus-icon">+</span>
                <span class="add-text">添加新实验室</span>
            </div>

            <!-- 提交按钮 -->
            <div class="submit-container">
                <button class="submit-button" @click="addLaboratories">
                    提交所有实验室 ({{ laboratories.length }}个)
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
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as XLSX from 'xlsx';

export default {
    components: {
        Footer
    },
    setup() {

        const fileInputs = ref([])
        const router = useRouter()
        const fileInput = ref(null)

        const state = reactive({
            laboratories: [
                {
                    laboratory_name: '',
                    location: '',
                    image_url: '',
                    description: '',
                    manager_id: '',
                    status: ''
                }
            ],
            laboratoryFiles: [], // 存储每个实验室对应的文件对象
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
            if (!state.laboratoryFiles) {
                state.laboratoryFiles = []
            }
            state.laboratoryFiles[index] = file

            // 本地预览
            const reader = new FileReader()
            reader.onload = (e) => {
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

                    importLaboratoriesFromExcel(jsonData)
                }
                catch (error) {
                    console.error('解析Excel失败:', error)
                    ElMessage.error('解析Excel文件失败，请检查文件格式')
                }
            }
            reader.readAsArrayBuffer(file)
            event.target.value = ''
        }

        function importLaboratoriesFromExcel(excelData) {
            if (!excelData || excelData.length === 0) {
                ElMessage.warning('Excel文件中没有数据')
                return
            }

            const fileUploadLaboratories = excelData.map(row => {
                return {
                    laboratory_name: row['实验室名称'] || '',
                    location: row['地点'] || '',
                    image_url: row['图片'] || '',
                    description: row['描述'] || '',
                    status: row['状态'] || ''
                }
            })

            state.laboratories = [...state.laboratories, ...fileUploadLaboratories]
            ElMessage.success(`成功导入 ${fileUploadLaboratories.length} 个实验室`)
        }

        function addNewLaboratory() {
            state.laboratories.push({
                laboratory_name: '',
                location: '',
                image_url: '',
                description: '',
                manager_id: '',
                status: ''
            })
            state.laboratoryFiles.push(null)
            state.previewUrls.push(null)
        }

        function removeLaboratory(index) {
            if (state.laboratories.length > 1) {
                state.laboratories.splice(index, 1)

                if (state.laboratoryFiles) {
                    state.laboratoryFiles.splice(index, 1)
                }
                if (state.previewUrls) {
                    state.previewUrls.splice(index, 1)
                }
            } else {
                ElMessage.warning('至少需要一个实验室')
            }
        }

        function addLaboratories() {
            for (let i = 0; i < state.laboratories.length; i++) {
                const laboratory = state.laboratories[i]

                if (!laboratory.location) {
                    ElMessage.error(`实验室 ${i + 1}：地点不能为空`)
                    return
                }
            }

            ElMessageBox.confirm(
                `确认要添加${state.laboratories.length}个新实验室吗？`,
                '添加确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(async () => {

                    const uploadPromises = []

                    for (let i = 0; i < state.laboratories.length; i++) {
                        const file = state.laboratoryFiles[i]

                        if (file) {
                            // 有图片，先上传
                            const formData = new FormData()
                            formData.append('file', file)

                            const uploadPromise = axios.post('/api/uploadImage', formData, {
                                headers: { 'Content-Type': 'multipart/form-data' }
                            }).then(response => {
                                if (response.data.status === 200) {
                                    // 将返回的图片URL赋给对应的实验室
                                    state.laboratories[i].image_url = response.data.data
                                } else {
                                    throw new Error(`实验室${i + 1}图片上传失败`)
                                }
                            })

                            uploadPromises.push(uploadPromise)
                        } else {
                            // 没有图片，可以设置默认图片或留空
                            state.laboratories[i].image_url = ''
                        }
                    }

                    try {
                        await Promise.all(uploadPromises)
                        axios
                            .post("/api/addLaboratories", state.laboratories)
                            .then((response) => {
                                if (response.data.status == 200) {
                                    ElMessage.success(`成功添加 ${state.laboratories.length} 个实验室`)
                                    router.push('/laboratoryManagement')
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
            addNewLaboratory,
            removeLaboratory,
            addLaboratories,
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

/* 实验室录入容器 */
.laboratory-entry-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
}

/* 实验室卡片样式 - 实线框 */
.laboratory-entry-card {
    border: 2px solid #dcdfe6;
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    box-sizing: border-box;
    background-color: #ffffff;
}

/* 实验室卡片头部 - 用于放置标题和删除按钮 */
.laboratory-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;
}

/* 实验室卡片标题 */
.laboratory-card-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin: 0;
}

/* 输入网格布局 - 每行三个输入框 */
.laboratory-input-grid {
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
.laboratory-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.3s;
    outline: none;
    box-sizing: border-box;
}

.laboratory-input:focus {
    border-color: #409eff;
}

/* 添加实验室卡片 - 虚线框 */
.add-laboratory-card {
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

.add-laboratory-card:hover {
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

.add-laboratory-card:hover .plus-icon {
    color: #409eff;
}

/* 添加按钮文字 */
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

/* 头部按钮容器 */
.header-buttons {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* 批量录入按钮 */
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