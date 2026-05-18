<template>

    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">公告管理</div>

                <div class="button-container">
                    <button class="batch-input-button" @click="toPostAnnouncement">发布公告</button>
                </div>
            </div>

            <div class="search-container">
                <div class="search-box">
                    <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索公告标题"
                        class="search-input">
                </div>
            </div>
        </div>

        <div class="scroll-content">
            <div class="announcement-list">
                <div v-for="announcement in filterAnnouncementArr" :key="announcement.announcement_id"
                    class="announcement-card" @click="toAnnouncementDetail(announcement.announcement_id)">

                    <img :src="announcement.image_url" :alt="announcement.title || '公告图片'" class="announcement-image">
                    <div class="announcement-content">
                        <div class="announcement-header">
                            <div class="announcement-title">{{ announcement.title }}</div>
                            <div class="announcement-time">{{ announcement.publish_time.split('T')[0] }}</div>
                        </div>
                        <div class="announcement-text" v-html="formatContent(announcement.content)"></div>
                        <div class="announcement-footer">
                            <button @click.stop="toggleTop(announcement)" class="top-btn">
                                {{ announcement.is_top ? '取消置顶' : '置顶' }}
                            </button>
                            <button @click.stop="deleteAnnouncement(announcement.announcement_id)"
                                class="delete-btn">删除该公告</button>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="filterAnnouncementArr.length === 0 && searchKeyword" class="no-result">
                没有找到对应的公告
            </div>
        </div>

    </div>
    <Footer></Footer>
</template>

<script>
import { reactive, toRefs } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios, { Axios } from 'axios';
import { useRouter, useRoute } from 'vue-router';


export default {
    setup() {
        const router = useRouter()

        const state = reactive({
            announcementArr: [],
            filterAnnouncementArr: [],
            searchKeyword: ''
        })

        function initAnnouncementArr() {
            axios
                .get('/api/getAllAnnouncement')
                .then((response) => {
                    if (response.data.status == 200) {
                        state.announcementArr = response.data.data
                        state.filterAnnouncementArr = response.data.data
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initAnnouncementArr()


        function formatContent(content) {
            if (!content) return ''
            // 将换行符替换为 <br> 标签
            return content.replace(/\n/g, '<br>')
        }


        function handleSearch() {
            const keyword = state.searchKeyword.trim().toLowerCase()

            if (!state.searchKeyword) {
                state.filterAnnouncementArr = state.announcementArr
                return
            }

            state.filterAnnouncementArr = state.announcementArr.filter(announcement => {
                const title = announcement.title?.toLowerCase() || ''

                return title.includes(keyword)
            })
        }

        function deleteAnnouncement(announcement_id) {
            ElMessageBox.confirm(
                '确认要删除该公告吗？',
                '删除确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    axios
                        .post('/api/deleteAnnouncement', {
                            announcement_id: announcement_id
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success('删除成功')
                                initAnnouncementArr()
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                        })
                }).catch(() => {
                    ElMessage.info('操作取消')
                })
        }


        function toPostAnnouncement() {
            router.push('/postAnnouncement')
        }


        function toAnnouncementDetail(announcement_id) {
            router.push({
                path: '/announcementDetail',
                query: {
                    announcement_id: announcement_id
                }
            })
        }

        function toggleTop(announcement) {
            const isTop = !announcement.is_top;
            const actionText = isTop ? '置顶' : '取消置顶';

            ElMessageBox.confirm(
                `确认要${actionText}该公告吗？`,
                `${actionText}确认`,
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    axios
                        .post('/api/setAnnouncementTop', {
                            announcement_id: announcement.announcement_id,
                            is_top: isTop
                        })
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success(response.data.message || `${actionText}成功`);
                                initAnnouncementArr();
                            } else {
                                ElMessage.error(response.data.message || `${actionText}失败`);
                            }
                        })
                        .catch(function (error) {
                            console.log(error);
                            ElMessage.error(`${actionText}失败`);
                        });
                })
                .catch(() => {
                    ElMessage.info('操作取消');
                });
        }


        return {
            ...toRefs(state),
            formatContent,
            deleteAnnouncement,
            toPostAnnouncement,
            handleSearch,
            toAnnouncementDetail,
            toggleTop,
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

.button-container {
    display: flex;
    gap: 10px;
    align-items: center;
}

.batch-input-button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

.search-container {
    margin-bottom: 24px;
    width: 500px;
}

.search-box {
    display: flex;
    gap: 10px;
    align-items: center;
}

.search-input {
    flex: 2;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    box-sizing: border-box;
}

.scroll-content {
    margin-top: 160px;
    padding-bottom: 20px;
    min-height: calc(100vh - 200px);
    display: flex;
    flex-direction: column;
}

.announcement-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
}

.announcement-card {
    display: flex;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;
    gap: 24px;
}

.announcement-card:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.announcement-image {
    width: 220px;
    height: 157px;
    object-fit: cover;
    border-radius: 8px;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
    flex-shrink: 0;
}

.announcement-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.announcement-title {
    font-size: 20px;
    font-weight: 500;
    color: #2c3e50;
    margin-bottom: 0;
}

.announcement-time {
    font-size: 14px;
    color: #909399;
    margin-bottom: 0;
    white-space: nowrap;
    margin-left: 20px;
}

.announcement-text {
    font-size: 15px;
    color: #3c4a5a;
    line-height: 1.6;
    /* 首行缩进2个字符 */
    text-indent: 2em;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
}

.delete-btn {
    padding: 6px 16px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
}

.delete-btn:hover {
    background-color: #f78989;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

.delete-btn:active {
    transform: translateY(0);
}

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
    order: -1;
}

.top-tag {
    display: inline-block;
    padding: 2px 8px;
    margin-right: 8px;
    background-color: #e6a23c;
    color: white;
    font-size: 12px;
    border-radius: 4px;
    vertical-align: middle;
}

.top-btn {
    padding: 6px 16px;
    margin-right: 10px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
}

.top-btn:hover {
    background-color: #ebb563;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.announcement-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
    gap: 10px;
}
</style>