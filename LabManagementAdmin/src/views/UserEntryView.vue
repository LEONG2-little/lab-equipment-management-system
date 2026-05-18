<template>
    <div class="user-management">
        <div class="header-container">
            <div class="section-title">用户录入</div>
            <div class="header-buttons">
                <input type="file" ref="fileInput" @change="fileUpload" accept=".xlsx,.xls,.csv" style="display: none;">
                <button @click="batchEntry">批量录入</button>
            </div>
        </div>

        <div class="user-entry-container">
            <!-- 循环渲染所有用户卡片 -->
            <div v-for="(user, index) in users" :key="index" class="user-entry-card">
                <div class="user-card-header">
                    <div class="user-card-title">用户 {{ index + 1 }}</div>
                    <button v-if="users.length > 1" class="remove-button" @click="removeUser(index)">
                        <span class="remove-icon">x</span>
                    </button>
                </div>

                <div class="user-input-grid">
                    <div class="input-wrapper">
                        <span class="input-label">账号（学号/教工号）<span class="required">*</span></span>
                        <input type="number" v-model="user.account" placeholder="请输入账号" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">姓名 <span class="required">*</span></span>
                        <input type="text" v-model="user.username" placeholder="请输入姓名" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">密码 <span class="required">*</span></span>
                        <input type="password" v-model="user.password" placeholder="请输入密码" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">身份 <span class="required">*</span></span>
                        <select v-model="user.identity_type" class="user-input">
                            <option value="">请选择身份</option>
                            <option value="学生">学生</option>
                            <option value="教师">教师</option>
                        </select>
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">电话号码</span>
                        <input type="text" v-model="user.phone" placeholder="请输入电话号码" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">邮箱</span>
                        <input type="email" v-model="user.email" placeholder="请输入邮箱" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">权限等级 <span class="required">*</span></span>
                        <input type="number" v-model="user.permission_id" placeholder="请输入权限等级（默认1）" class="user-input">
                    </div>
                    <div class="input-wrapper">
                        <span class="input-label">状态</span>
                        <select v-model="user.status" class="user-input">
                            <option value="正常">正常</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- 添加用户卡片 -->
            <div class="add-user-card" @click="addNewUser">
                <span class="plus-icon">+</span>
                <span class="add-text">添加新用户</span>
            </div>

            <!-- 提交按钮 -->
            <div class="submit-container">
                <button class="submit-button" @click="addUsers">
                    提交所有用户 ({{ users.length }}个)
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
        const router = useRouter()
        const fileInput = ref(null)

        const state = reactive({
            users: [
                {
                    account: null,
                    username: '',
                    password: '',
                    identity_type: '',
                    phone: '',
                    email: '',
                    permission_id: '',
                    status: ''
                }
            ]
        })

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
                    importUsersFromExcel(jsonData)
                } catch (error) {
                    console.error('解析Excel失败:', error)
                    ElMessage.error('解析Excel文件失败，请检查文件格式')
                }
            }
            reader.readAsArrayBuffer(file)
            event.target.value = ''
        }

        function importUsersFromExcel(excelData) {
            if (!excelData || excelData.length === 0) {
                ElMessage.warning('Excel文件中没有数据')
                return
            }

            const fileUploadUsers = excelData.map(row => ({
                account: row['账号'] || row['account'] || '',
                username: row['姓名'] || row['username'] || '',
                password: String(row['密码'] || row['password'] || '123456'),
                identity_type: row['身份'] || row['identity_type'] || '学生',
                phone: row['电话号码'] || row['phone'] || '',
                email: row['邮箱'] || row['email'] || '',
                permission_id: parseInt(row['权限等级'] || row['permission_id'] || 1),
                status: row['状态'] || row['status'] || '正常'
            }))

            state.users = [...state.users, ...fileUploadUsers]
            ElMessage.success(`成功导入 ${fileUploadUsers.length} 个用户`)
        }

        function addNewUser() {
            state.users.push({
                account: null,
                username: '',
                password: '',
                identity_type: '学生',
                phone: '',
                email: '',
                permission_id: 1,
                status: '正常'
            })
        }

        function removeUser(index) {
            if (state.users.length > 1) {
                state.users.splice(index, 1)
            } else {
                ElMessage.warning('至少需要一个用户')
            }
        }

        function addUsers() {
            // 验证必填字段
            for (let i = 0; i < state.users.length; i++) {
                const user = state.users[i]
                if (!user.account) {
                    ElMessage.error(`用户 ${i + 1}：账号不能为空`)
                    return
                }
                if (!user.username) {
                    ElMessage.error(`用户 ${i + 1}：姓名不能为空`)
                    return
                }
                if (!user.password || user.password.length < 6) {
                    ElMessage.error(`用户 ${i + 1}：密码至少6位`)
                    return
                }
                if (!user.identity_type) {
                    ElMessage.error(`用户 ${i + 1}：请选择身份`)
                    return
                }
                if (!user.permission_id) {
                    ElMessage.error(`用户 ${i + 1}：请输入权限等级`)
                    return
                }
            }

            ElMessageBox.confirm(
                `确认要添加${state.users.length}个新用户吗？`,
                '添加确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            )
                .then(() => {
                    // 确保账号是数字类型
                    const submitUsers = state.users.map(user => ({
                        ...user,
                        account: parseInt(user.account)
                    }))

                    axios
                        .post("/api/addUsers", submitUsers)
                        .then((response) => {
                            if (response.data.status == 200) {
                                ElMessage.success(`成功添加 ${state.users.length} 个用户`)
                                router.push('/userManagement')
                            } else if (response.data.status == 201) {
                                ElMessage.warning(response.data.message)
                                router.push('/userManagement')
                            } else {
                                ElMessage.error(response.data.message)
                            }
                        })
                        .catch(function (error) {
                            console.log(error)
                            ElMessage.error('添加失败')
                        })
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            ...toRefs(state),
            fileInput,
            batchEntry,
            fileUpload,
            addNewUser,
            removeUser,
            addUsers,
        }
    }
}
</script>

<style scoped>
.user-management {
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

.user-entry-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
}

.user-entry-card {
    border: 2px solid #dcdfe6;
    border-radius: 12px;
    padding: 24px;
    width: 100%;
    box-sizing: border-box;
    background-color: #ffffff;
}

.user-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;
}

.user-card-title {
    font-size: 1.2rem;
    font-weight: 500;
    color: #2c3e50;
    margin: 0;
}

.user-input-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
}

.input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.input-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}

.user-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.3s;
    outline: none;
    box-sizing: border-box;
}

.user-input:focus {
    border-color: #409eff;
}

.add-user-card {
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

.add-user-card:hover {
    border-color: #409eff;
    background-color: #f0f7ff;
}

.plus-icon {
    font-size: 48px;
    color: #909399;
    line-height: 1;
    transition: color 0.3s;
}

.add-user-card:hover .plus-icon {
    color: #409eff;
}

.add-text {
    font-size: 16px;
    color: #909399;
    margin-left: 10px;
}

.submit-container {
    margin-top: 30px;
    display: flex;
    justify-content: flex-end;
}

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

.remove-icon {
    font-size: 20px;
    color: #909399;
    line-height: 1;
}

.remove-button:hover .remove-icon {
    color: #f56c6c;
}

.required {
    color: #f56c6c;
    margin-left: 4px;
}

.header-buttons {
    display: flex;
    gap: 10px;
    align-items: center;
}

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
</style>