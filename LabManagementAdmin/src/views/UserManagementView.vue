<template>
    <div class="device-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">用户管理</div>
                <div class="button-container">
                    <button @click="batchOperation" class="batch-button">{{ operation }}</button>
                    <button v-show="isOperation" class="status-button" @click="toChangeStatus">修改状态</button>
                    <button @click="toUserEntry" class="batch-input-button">用户录入</button>
                </div>
            </div>

            <div class="search-container">
                <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索用户ID或名称"
                    class="search-input">
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <!-- 用户列表容器 -->
            <div class="user-list">
                <div v-for="user in filteredUsers" :key="user.user_id" class="user-item" :class="{
                    'operation-mode': isOperation,
                    'selected': isOperation && selectedUsers.includes(user.user_id)
                }" @click="handleCardClick(user)">
                    <span class="user-field">ID: {{ user.account }}</span>
                    <span class="user-field">姓名: {{ user.username }}</span>
                    <span class="user-field">身份: {{ user.identity_type }}</span>
                    <span class="user-field">电话号码: {{ user.phone }}</span>
                    <span class="user-field">邮箱: {{ user.email }}</span>
                    <span class="user-field">状态: {{ user.status }}</span>
                    <!-- 勾选框移到尾部 -->
                    <div class="checkbox-wrapper" v-show="isOperation">
                        <input type="checkbox" :value="user.user_id" v-model="selectedUsers" class="user-checkbox"
                            @click.stop>
                    </div>
                </div>
            </div>

            <div v-if="filteredUsers.length === 0 && searchKeyword" class="no-result">
                没有找到匹配的用户
            </div>
        </div>
    </div>
    <Footer></Footer>
</template>

<script>
import Footer from '@/components/Footer.vue';
import axios from 'axios';
import { reactive, toRefs } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default {
    setup() {
        const router = useRouter()
        const state = reactive({
            usersArr: [],
            filteredUsers: [],
            searchKeyword: '',
            currentUser: null,
            status: '',
            operation: '批量操作',
            isOperation: false,
            selectedUsers: []
        })

        function batchOperation() {
            if (!state.isOperation) {
                state.operation = '取消'
                state.isOperation = true
                state.selectedUsers = []
            } else {
                state.operation = '批量操作'
                state.isOperation = false
                state.selectedUsers = []
            }
        }

        function handleSearch() {
            const keyword = state.searchKeyword.trim().toLowerCase()

            if (!keyword) {
                state.filteredUsers = state.usersArr
                return
            }

            state.filteredUsers = state.usersArr.filter(user => {
                const userAccount = String(user.account).toLowerCase()
                const userName = user.username?.toLowerCase() || ''

                return userAccount.includes(keyword) || userName.includes(keyword)
            })
        }

        function initUsersArr() {
            axios
                .get('/api/getAllUser')
                .then((response) => {
                    if (response.data.status == 200) {
                        state.usersArr = response.data.data
                        state.filteredUsers = response.data.data
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initUsersArr()

        function handleCardClick(user) {
            if (state.isOperation) {
                // 批量操作模式：切换选中状态
                const index = state.selectedUsers.indexOf(user.user_id)
                if (index === -1) {
                    state.selectedUsers.push(user.user_id)
                } else {
                    state.selectedUsers.splice(index, 1)
                }
            } else {
                // 非批量操作模式：跳转到详情
                toUserDetail(user)
            }
        }

        function toUserDetail(user) {
            sessionStorage.setItem('currentUser', JSON.stringify(user))
            router.push('/userDetail')
        }

        function toUserEntry() {
            router.push('/userEntry')
        }

        function toChangeStatus() {
            if (state.selectedUsers.length === 0) {
                ElMessage.error('请选择用户')
                return
            }

            // 获取选中的用户信息
            const selectedUsersInfo = state.usersArr.filter(user => state.selectedUsers.includes(user.user_id))

            // 检查状态是否一致
            const firstStatus = selectedUsersInfo[0]?.status
            const allSameStatus = selectedUsersInfo.every(user => user.status === firstStatus)

            if (!allSameStatus) {
                ElMessage.error('选中的用户状态不一致，不能进行批量修改')
                return
            }

            // 拼接ID和名称
            const userIds = state.selectedUsers.join(',')
            const userAccounts = selectedUsersInfo.map(user => user.account).join(',')
            const userNames = selectedUsersInfo.map(user => user.username).join(',')

            router.push({
                path: '/changeUserStatus',
                query: {
                    userAccounts: userAccounts,
                    userNames: userNames,
                    currentStatus: firstStatus
                }
            })
        }

        return {
            ...toRefs(state),
            handleSearch,
            toUserDetail,
            toUserEntry,
            batchOperation,
            handleCardClick,
            toChangeStatus,
        }
    },
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
    margin-top: 140px;
    padding-bottom: 20px;
    min-height: calc(100vh - 180px);
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

/* 按钮容器 */
.button-container {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* 批量操作按钮 */
.batch-button {
    padding: 8px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.batch-button:hover {
    background-color: #66b1ff;
}

/* 修改状态按钮 */
.status-button {
    padding: 8px 20px;
    background-color: #67c23a;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.status-button:hover {
    background-color: #85ce61;
}

/* 用户录入按钮 */
.batch-input-button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.batch-input-button:hover {
    background-color: #ebb563;
}

/* 搜索容器 */
.search-container {
    margin-bottom: 24px;
    width: 400px;
}

/* 搜索输入框 */
.search-input {
    width: 100%;
    padding: 10px 16px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    box-sizing: border-box;
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
    order: -1;
}

.user-list {
    width: 100%;
    margin-top: 10px;
}

.user-item {
    position: relative;
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.user-item:hover {
    border-color: #c0c4cc;
}

/* 操作模式下的行样式 */
.user-item.operation-mode {
    background-color: #fafafa;
}

/* 选中状态下的行样式 */
.user-item.selected {
    border-color: #409eff;
    background-color: #ecf5ff;
}

/* 勾选框包装器 - 放在尾部 */
.checkbox-wrapper {
    margin-left: auto;
    /* 推到最右边 */
    flex-shrink: 0;
    /* 不被压缩 */
    display: flex;
    align-items: center;
    padding-left: 16px;
}

/* 勾选框样式 */
.user-checkbox {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: #409eff;
}

.user-field {
    font-size: 14px;
    color: #2c3e50;
    margin-right: 30px;
    white-space: nowrap;
}

.user-field:last-of-type {
    margin-right: 0;
}

.user-field:last-child {
    margin-right: 0;
}

/* 弹窗相关样式保持不变 */
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

.dialog-container {
    background-color: #ffffff;
    border-radius: 12px;
    width: 500px;
    max-width: 90%;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    animation: slideIn 0.3s ease;
}

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
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

.confirm-btn:hover {
    background-color: #f78989;
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
</style>