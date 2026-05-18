<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">发布公告</div>
            </div>
        </div>

        <div class="scroll-content">
            <div class="detail-container">
                <!-- 标题区域 -->
                <!-- 标题区域 -->
                <div class="detail-item">
                    <div class="detail-label">图片：</div>
                    <div class="image-upload-wrapper">
                        <div class="image-preview-container">
                            <img v-if="previewImageUrl" :src="previewImageUrl" alt="公告图片预览"
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
                    <input type="text" v-model="announcementArr.title" class="detail-input">
                </div>

                <!-- 内容区域 - 使用 textarea 显示多行文本 -->
                <div class="detail-item content-item">
                    <div class="detail-label">内容：</div>
                    <textarea v-model="announcementArr.content" class="detail-textarea" rows="10"></textarea>
                </div>

                <!-- 确认修改按钮 -->
                <div class="button-container">
                    <button @click="postAnnouncement" class="confirm-btn">确认发布</button>
                </div>
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import { reactive, toRefs, ref } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
    setup() {

        const router = useRouter()
        const imageInput = ref(null)

        const state = reactive({
            announcementArr: {
                image_url: '',
                title: '',
                content: '',
            },
            previewImageUrl: '',
            ImageFiles: [],

        })


        function triggerImageUpload() {
            imageInput.value.click()
        }


        function handleImageUpload(event) {
            const file = event.target.files[0]
            if (!file) return

            state.ImageFiles[0] = file

            const reader = new FileReader()
            reader.onload = (e) => {
                state.previewImageUrl = e.target.result
            }
            reader.readAsDataURL(file)

            event.target.value = ''
        }


        function postAnnouncement() {
            ElMessageBox.confirm(
                '确认要发布该公告吗？',
                '发布确认',
                {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                }
            )
                .then(async () => {
                    try {
                        if (!state.previewImageUrl) {
                            ElMessage.info('请添加公告图片')
                            return
                        }
                        if (!state.announcementArr.title) {
                            ElMessage.info('请输入标题')
                            return
                        }
                        if (!state.announcementArr.content) {
                            ElMessage.info('请输入内容')
                            return
                        }

                        const formData = new FormData()
                        formData.append('file', state.ImageFiles[0])

                        const uploadResponse = await axios.post('/api/uploadImage', formData, {
                            headers: { 'Content-Type': 'multipart/form-data' }
                        })

                        if (uploadResponse.data.status == 200) {
                            state.announcementArr.image_url = uploadResponse.data.data
                        } else {
                            throw new Error('图片上传失败')
                        }

                        const response = await axios.post('/api/postAnnouncement',state.announcementArr)
                        if(response.data.status==200){
                            ElMessage.success('发布公告成功')
                            router.push('/announcementManagement')
                        }
                        else{
                            ElMessage.error('/发布公告失败')
                        }
                    }
                    catch (error) {
                        console.log(error)
                    }

                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            postAnnouncement,
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

/* 详情标签 */
.detail-label {
    width: 80px;
    color: #7f8c8d;
    font-weight: normal;
    flex-shrink: 0;
}

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

/* 内容项特殊处理 */
.content-item {
    align-items: flex-start;
}

/* 按钮容器 */
.button-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}

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
</style>