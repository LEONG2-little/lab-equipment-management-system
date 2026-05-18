<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">公告详情</div>
                <button @click="changeAnnouncement" class="edit-btn">{{ buttonText }}</button>
            </div>
        </div>

        <div class="scroll-content">
            <div class="detail-container">

                <!-- 标题区域 -->
                <div class="detail-item">
                    <div class="detail-label">图片：</div>
                    <div v-if="!isChange" class="detail-content">
                        <img :src="showAnnouncementArr.image_url" :alt="showAnnouncementArr.title || '公告图片'"
                            class="detail-image">
                    </div>
                    <div v-else class="image-upload-wrapper">
                        <div class="image-preview-container">
                            <img :src="previewImageUrl || showAnnouncementArr.image_url" alt="公告图片预览"
                                class="detail-image preview-image">
                        </div>
                        <div class="image-upload-controls">
                            <input type="file" ref="imageInput" @change="handleImageUpload" accept="image/*"
                                class="image-input">
                            <button type="button" class="upload-btn" @click="triggerImageUpload">选择图片</button>
                        </div>
                    </div>
                </div>

                <div class="detail-item">
                    <div class="detail-label">标题：</div>
                    <div v-if="!isChange" class="detail-content">{{ showAnnouncementArr.title }}</div>
                    <input v-else type="text" v-model="showAnnouncementArr.title" class="detail-input">
                </div>

                <!-- 发布时间 -->
                <div class="detail-item">
                    <div class="detail-label">发布时间：</div>
                    <div class="detail-content">{{ showAnnouncementArr.publish_time?.split('T')[0] }}</div>
                </div>

                <!-- 内容区域 - 使用 textarea 显示多行文本 -->
                <div class="detail-item content-item">
                    <div class="detail-label">内容：</div>
                    <div v-if="!isChange" class="detail-content content-text">{{ showAnnouncementArr.content }}</div>
                    <textarea v-else v-model="showAnnouncementArr.content" class="detail-textarea" rows="10"></textarea>
                </div>

                <!-- 确认修改按钮 -->
                <div v-if="isChange" class="button-container">
                    <button @click="updateAnnouncement" class="confirm-btn">确认修改</button>
                </div>
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import { reactive, toRefs, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    setup() {

        const route = useRoute()

        const state = reactive({
            announcement_id: '',
            announcementArr: [],
            showAnnouncementArr: [],
            announcementFiles: [],
            previewImageUrl: '',
            isChange: false,
            buttonText: '修改公告',
        })


        onMounted(() => {
            state.announcement_id = route.query.announcement_id
            initAnnouncementArr()
        })


        function initAnnouncementArr() {
            axios
                .post('/api/getAnnouncementDetail', {
                    announcement_id: state.announcement_id
                })
                .then((response) => {
                    if (response.data.status == 200) {
                        state.announcementArr = response.data.data
                        state.showAnnouncementArr = JSON.parse(JSON.stringify(response.data.data))
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }


        function changeAnnouncement() {
            if (state.isChange == false) {
                state.buttonText = '取消'
                state.isChange = true
                // 清空之前可能的上传记录
                state.announcementFiles = []
                state.previewImageUrl = ''
            }
            else {
                state.buttonText = '修改公告'
                state.showAnnouncementArr = JSON.parse(JSON.stringify(state.announcementArr))
                state.isChange = false
                // 取消修改时清空上传记录
                state.announcementFiles = []
                state.previewImageUrl = ''
            }
        }


        const imageInput = ref(null)

        function triggerImageUpload() {
            imageInput.value.click()
        }

        function handleImageUpload(event) {
            const file = event.target.files[0]
            if (!file) return

            // 保存文件对象
            state.announcementFiles[0] = file

            // 生成预览URL
            const reader = new FileReader()
            reader.onload = (e) => {
                state.previewImageUrl = e.target.result
            }
            reader.readAsDataURL(file)

            // 清空input以便重新选择同一文件
            event.target.value = ''
        }


        async function updateAnnouncement() {
            ElMessageBox.confirm(
                '确认要修改该公告吗？',
                '修改确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(async () => {
                    try {
                        // 准备要更新的数据
                        const updateData = {
                            announcement_id: state.announcement_id,
                            title: state.showAnnouncementArr.title,
                            content: state.showAnnouncementArr.content,
                        }

                        // 如果有新图片，先上传图片
                        if (state.announcementFiles[0]) {
                            const formData = new FormData()
                            formData.append('file', state.announcementFiles[0])

                            const uploadResponse = await axios.post('/api/uploadImage', formData, {
                                headers: { 'Content-Type': 'multipart/form-data' }
                            })

                            if (uploadResponse.data.status === 200) {
                                // 上传成功，直接使用返回的文件名（已经是相对路径）
                                updateData.image_url = uploadResponse.data.data
                            } else {
                                throw new Error('图片上传失败')
                            }
                        } else {
                            // 关键修改：没有新图片时，只传文件名（提取路径部分）
                            let imageUrl = state.showAnnouncementArr.image_url
                            if (imageUrl) {
                                // 如果是完整URL或带路径的，提取文件名
                                if (imageUrl.includes('/')) {
                                    const parts = imageUrl.split('/')
                                    imageUrl = parts[parts.length - 1]
                                }
                                updateData.image_url = imageUrl
                            }
                        }

                        // 提交更新
                        const response = await axios.post('/api/updateAnnouncement', updateData)

                        if (response.data.status == 200) {
                            ElMessage.success('修改成功')
                            state.isChange = false
                            state.buttonText = '修改公告'
                            // 清空上传文件
                            state.announcementFiles = []
                            state.previewImageUrl = ''
                            // 重新获取最新数据
                            initAnnouncementArr()
                        }
                    } catch (error) {
                        console.log(error)
                        ElMessage.error('修改失败')
                    }
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }


        return {
            ...toRefs(state),
            updateAnnouncement,
            changeAnnouncement,
            triggerImageUpload,
            handleImageUpload,
            imageInput,
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
    position: relative;
}

/* 固定头部区域 */
.fixed-header {
    position: fixed;
    top: 0;
    left: 250px;
    /* 改为固定像素值，而不是50% */
    transform: none;
    /* 移除 transform */
    width: calc(100% - 250px);
    /* 减去左侧导航栏宽度 */
    max-width: calc(2000px - 250px);
    padding: 20px 24px 0 24px;
    background-color: #ffffff;
    z-index: 100;
    box-sizing: border-box;
}

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

.scroll-content {
    margin-top: 100px;
    padding-bottom: 20px;
    min-height: calc(100vh - 200px);
    display: flex;
    flex-direction: column;
}

/* 编辑按钮 */
.edit-btn {
    padding: 8px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
}

.edit-btn:hover {
    background-color: #66b1ff;
}

/* 详情容器 */
.detail-container {
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    margin-top: 20px;
    max-width: 1200px;
}

/* 详情项 */
/* 修改详情项，添加底部边框 */
.detail-item {
    display: flex;
    margin-bottom: 16px;
    font-size: 15px;
    line-height: 1.6;
    padding-bottom: 16px;
    /* 添加底部内边距 */
    border-bottom: 1px dashed #3d3d3d;
    /* 添加虚线边框 */
}

/* 最后一个详情项去掉边框 */
.detail-item:last-of-type {
    border-bottom: none;
    padding-bottom: 0;
}

/* 内容项特殊处理 */
.content-item {
    align-items: flex-start;
}

/* 详情标签 */
.detail-label {
    width: 80px;
    color: #7f8c8d;
    font-weight: normal;
    flex-shrink: 0;
}

/* 详情内容（非编辑状态） */
.detail-content {
    flex: 1;
    color: #2c3e50;
    word-break: break-word;
    white-space: pre-wrap;
}

/* 内容文本（保持原有格式） */
.content-text {
    white-space: pre-wrap;
    line-height: 1.8;
}

/* 详情输入框（编辑状态） */
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

/* 详情文本域（编辑状态） */
.detail-textarea {
    flex: 1;
    padding: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 15px;
    line-height: 1.8;
    outline: none;
    transition: border-color 0.3s;
    background-color: #ffffff;
    font-family: inherit;
    resize: vertical;
    min-height: 800px;
}

.detail-textarea:focus {
    border-color: #409eff;
}

/* 分割线 */
.detail-divider {
    color: #909399;
    font-size: 14px;
    margin: 16px 0;
    text-align: center;
}

/* 按钮容器 */
.button-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}

/* 确认修改按钮 */
.confirm-btn {
    padding: 8px 24px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
}

.confirm-btn:hover {
    background-color: #85ce61;
}

/* 公告图片样式 */
.detail-container img {
    width: 100%;
    max-width: 500px;
    height: auto;
    max-height: 500px;
    object-fit: contain;
    border-radius: 8px;
    margin-bottom: 24px;
    display: block;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
}

/* 图片上传相关样式 */
.image-upload-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.image-preview-container {
    width: 100%;
    max-width: 500px;
}

.detail-image {
    width: 100%;
    max-width: 500px;
    height: auto;
    max-height: 500px;
    object-fit: contain;
    border-radius: 8px;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
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
</style>