<template>
    <div class="login-container">
        <div class="login-box">
            <div class="login-header">
                <div class="section-title">实验室设备管理系统</div>
            </div>
            
            <div class="login-form">
                <div class="input-wrapper">
                    <span class="input-label">账号</span>
                    <input type="text" placeholder="请输入账号" v-model="user.account" class="login-input">
                </div>
                
                <div class="input-wrapper">
                    <span class="input-label">密码</span>
                    <input type="password" placeholder="请输入密码" v-model="user.password" class="login-input">
                </div>
                
                <button @click="login" class="login-btn">登录</button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import { useRouter,useRoute } from 'vue-router';
import { reactive,toRefs } from 'vue';
import { ElMessage } from 'element-plus';

export default{
    setup(){
        const router = useRouter()
        const state = reactive({
            user:{
                account:"",
                password:""
            }
        })

        async function login(){
            if(state.user.account == "" || state.user.password==""){
                ElMessage.warning("请输入账号密码")
                return
            }

            try{
                const response = await axios.post("/api/adminLogin",state.user)
                let result = response.data

                if (result.status != 200) {
                    ElMessage.error(result.message)
                    return
                }
                if (result.status == 200) {
                    sessionStorage.setItem("UserData", JSON.stringify(result.data))
                    ElMessage.success('登录成功');
                    setTimeout(() => {
                        router.push('/deviceManagement')
                    }, 1000);
                }
            }
            catch(error){
                console.log(error)
            }
        }

        return{
            ...toRefs(state),
            login,
        }
    }
}
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    width: 100vw;  /* 改为100vw，确保占满整个视口宽度 */
    position: fixed;  /* 固定定位，确保覆盖整个屏幕 */
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

.login-box {
    width: 100%;
    max-width: 450px;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 40px;
    box-sizing: border-box;
}

.login-header {
    margin-bottom: 32px;
}

.section-title {
    font-size: 1.6rem;
    font-weight: 500;
    color: #2c3e50;
    border-left: 6px solid #409eff;
    padding-left: 16px;
    line-height: 1.3;
    margin: 0;
    text-align: left;
}

.login-form {
    display: flex;
    flex-direction: column;
    gap: 24px;
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

.login-input {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 15px;
    outline: none;
    transition: border-color 0.3s;
    box-sizing: border-box;
}

.login-input:focus {
    border-color: #409eff;
}

.login-input::placeholder {
    color: #b3b8c5;
    font-size: 14px;
}

.login-btn {
    padding: 14px 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    margin-top: 8px;
}

.login-btn:hover {
    background-color: #66b1ff;
}

.login-btn:active {
    transform: translateY(0);
}
</style>