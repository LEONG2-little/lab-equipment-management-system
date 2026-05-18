<template>
    <!-- footer -->
    <footer>
        <ul>
            <router-link to="/deviceManagement" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isDeviceActive }">
                    <i class="fa fa-home"></i>
                    <p>设备管理</p>
                </li>
            </router-link>

            <router-link to="/appointmentManagement" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isAppointmentActive }">
                    <i class="fa fa-compass"></i>
                    <p>设备预约</p>
                </li>
            </router-link>

            <router-link to="/laboratoryManagement" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isLaboratoryManagement }">
                    <i class="fa fa-user"></i>
                    <p>实验室管理</p>
                </li>
            </router-link>

            <router-link to="/labReservation" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isLabReservation }">
                    <i class="fa fa-user"></i>
                    <p>实验室预约</p>
                </li>
            </router-link>

            <router-link to="/userManagement" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isUserActive }">
                    <i class="fa fa-user"></i>
                    <p>用户管理</p>
                </li>
            </router-link>

            <router-link to="/disabledPeriod" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isDisabledPeriod }">
                    <i class="fa fa-user"></i>
                    <p>禁用时段</p>
                </li>
            </router-link>

            <router-link to="/announcementManagement" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isAnnouncementActive }">
                    <i class="fa fa-user"></i>
                    <p>公告管理</p>
                </li>
            </router-link>

            <router-link to="/dataAnalysis" custom v-slot="{ navigate }">
                <li @click="navigate" :class="{ active: isDataActive }">
                    <i class="fa fa-user"></i>
                    <p>数据统计</p>
                </li>
            </router-link>

            <button @click="logOut">退出登录</button>
        </ul>
    </footer>
</template>

<script>
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
    setup() {
        const router = useRouter()
        const route = useRoute()

        // 当前路径
        const currentPath = computed(() => route.path)

        // 定义每个菜单对应的所有路径
        const devicePaths = ['/deviceManagement', '/deviceDetail', '/deviceEntry', '/changStatus']
        const laboratoryPaths = ['/laboratoryManagement', '/laboratoryDetail', '/changeLabStatus']
        const labReservationPaths = ['/labReservation', '/labReservationDetail']
        const appointmentPaths = ['/appointmentManagement', '/reservationDetail']
        const userPaths = ['/userManagement', '/userDetail', '/userEntry']
        const announcementPaths = ['/announcementManagement', '/postAnnouncement', '/announcementDetail']
        const dataPaths = ['/dataAnalysis']
        const disabledPeriodPaths = ['/disabledPeriod']

        // 判断当前路径是否在对应菜单的路径数组中
        const isDeviceActive = computed(() => devicePaths.includes(currentPath.value))
        const isLaboratoryManagement = computed(() => laboratoryPaths.includes(currentPath.value))
        const isLabReservation = computed(() => labReservationPaths.includes(currentPath.value))
        const isAppointmentActive = computed(() => appointmentPaths.includes(currentPath.value))
        const isUserActive = computed(() => userPaths.includes(currentPath.value))
        const isDisabledPeriod = computed(() => disabledPeriodPaths.includes(currentPath.value))
        const isAnnouncementActive = computed(() => announcementPaths.includes(currentPath.value))
        const isDataActive = computed(() => dataPaths.includes(currentPath.value))

        function toDeviceManagement() {
            router.push('/deviceManagement')
        }

        function toLaboratoryManagement() {
            router.push('/laboratoryManagement')
        }

        function toLabReservation() {
            router.push('/labReservation')
        }

        function toAppointmentManagement() {
            router.push('/appointmentManagement')
        }

        function toUserManagement() {
            router.push('/userManagement')
        }

        function toDisabledPeriod() {
            router.push('/disabledPeriod')
        }

        function toAnnouncementManagement() {
            router.push('/announcementManagement')
        }

        function toDataAnalysis() {
            router.push('/dataAnalysis')
        }

        function logOut() {
            ElMessageBox.confirm(
                '确定要退出当前账号吗？',
                '退出账号确认',
                {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                }
            )
                .then(() => {
                    sessionStorage.removeItem('UserData')
                    ElMessage.success('退出账号成功')
                    router.replace('/login')
                })
                .catch(() => {
                    ElMessage.info('操作取消')
                })
        }

        return {
            toDeviceManagement,
            toLaboratoryManagement,
            toAppointmentManagement,
            toUserManagement,
            toAnnouncementManagement,
            toDataAnalysis,
            toLabReservation,
            toDisabledPeriod,
            isDeviceActive,
            isLaboratoryManagement,
            isAppointmentActive,
            isUserActive,
            isAnnouncementActive,
            isDataActive,
            isLabReservation,
            isDisabledPeriod,
            logOut,
        }
    }
}
</script>

<style scoped>
/*********************** footer ***********************/
footer {
    width: 12vw;
    /* 改为宽度固定，不再是100% */
    height: 100vh;
    /* 改为全屏高度 */
    box-sizing: border-box;
    border-right: solid 1px #E9E9E9;
    /* 将border-top改为border-right */
    background-color: #FFF;

    position: fixed;
    left: 0;
    top: 0;
    /* 从顶部开始，不再是底部 */
    bottom: 0;
}

footer ul {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    /* 改为垂直排列 */
    align-items: center;
    justify-content: flex-start;
    /* 改为从顶部开始排列 */
    padding-top: 20px;
    /* 添加一些顶部内边距 */
    margin: 0;
    list-style: none;
}

footer ul li {
    width: 100%;
    padding: 20px 0;
    /* 添加上下内边距 */
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    font-size: 1.5vw;
    color: #999;

    user-select: none;
    cursor: pointer;

    transition: background-color 0.3s;
    /* 添加悬停效果 */
}

footer ul li:hover {
    background-color: #f5f5f5;
    /* 悬停时的背景色 */
}

footer ul li .fa {
    font-size: 5vw;
    margin-bottom: 5px;
    /* 图标和文字之间的间距 */
}

/* 修改激活状态的样式为浅蓝色 */
footer ul li.router-link-active,
footer ul li.active {
    color: #409eff;
    /* 改为浅蓝色 */
    background-color: #ecf5ff;
    /* 浅蓝色背景 */
    border-left: 3px solid #409eff;
    /* 左侧蓝色边框，增加视觉效果 */
}

/* 退出登录按钮 */
footer ul button {
    margin-top: auto;
    /* 自动上边距，推到最底部 */
    margin-bottom: 30px;
    /* 底部留一些空间 */
    padding: 12px 24px;
    background-color: #f56c6c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
    width: 80%;
    /* 设置宽度，不占满 */
}

footer ul button:hover {
    background-color: #f78989;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

footer ul button:active {
    transform: translateY(0);
}
</style>