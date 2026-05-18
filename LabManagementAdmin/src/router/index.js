import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DeviceManagementView from '@/views/DeviceManagementView.vue'
import AppointmentManagementView from '@/views/AppointmentManagementView.vue'
import UserManagementView from '@/views/UserManagementView.vue'
import AnnouncementManagementView from '@/views/AnnouncementManagementView.vue'
import DataAnalysisView from '@/views/DataAnalysisView.vue'
import ChangStatusView from '@/views/ChangStatusView.vue'
import DeviceEntryView from '@/views/DeviceEntryView.vue'
import DeviceDetailView from '@/views/DeviceDetailView.vue'
import ReservationDetailView from '@/views/ReservationDetailView.vue'
import UserDetailView from '@/views/UserDetailView.vue'
import PostAnnouncementView from '@/views/PostAnnouncementView.vue'
import AnnouncementDetailView from '@/views/AnnouncementDetailView.vue'
import LaboratoryManagementView from '@/views/LaboratoryManagementView.vue'
import LaboratoryDetailView from '@/views/LaboratoryDetailView.vue'
import LaboratoryEntryView from '@/views/LaboratoryEntryView.vue'
import LaboratoryArrangeView from '@/views/LaboratoryArrangeView.vue'
import LabReservationView from '@/views/LabReservationView.vue'
import DisabledPeriodView from '@/views/DisabledPeriodView.vue'
import LabReservationDetailView from '@/views/LabReservationDetailView.vue'
import ChangeLabStatusView from '@/views/ChangeLabStatusView.vue'
import UserEntryView from '@/views/UserEntryView.vue'
import ChangeUserStatusView from '@/views/ChangeUserStatusView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '/login',
      component: LoginView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/deviceManagement',
      name: 'deviceManagement',
      component: DeviceManagementView,
    },
    {
      path: '/appointmentManagement',
      name: 'appointmentManagement',
      component: AppointmentManagementView,
    },
    {
      path: '/userManagement',
      name: 'userManagement',
      component: UserManagementView,
    },
    {
      path: '/announcementManagement',
      name: 'announcementManagement',
      component: AnnouncementManagementView,
    },
    {
      path: '/dataAnalysis',
      name: 'dataAnalysis',
      component: DataAnalysisView,
    },
    {
      path: '/changStatus',
      name: 'changStatus',
      component: ChangStatusView,
    },
    {
      path: '/deviceEntry',
      name: 'deviceEntry',
      component: DeviceEntryView,
    },
    {
      path: '/deviceDetail',
      name: 'deviceDetail',
      component: DeviceDetailView,
    },
    {
      path: '/reservationDetail',
      name: 'reservationDetail',
      component: ReservationDetailView,
    },
    {
      path: '/userDetail',
      name: 'userDetail',
      component: UserDetailView,
    },
    {
      path: '/postAnnouncement',
      name: 'postAnnouncement',
      component: PostAnnouncementView,
    },
    {
      path: '/announcementDetail',
      name: 'announcementDetail',
      component: AnnouncementDetailView,
    },
    {
      path: '/laboratoryManagement',
      name: 'laboratoryManagement',
      component: LaboratoryManagementView,
    },
    {
      path: '/laboratoryDetail',
      name: 'laboratoryDetail',
      component: LaboratoryDetailView,
    },
    {
      path: '/laboratoryEntry',
      name: 'laboratoryEntry',
      component: LaboratoryEntryView,
    },
    {
      path: '/laboratoryArrange',
      name: 'laboratoryArrange',
      component: LaboratoryArrangeView,
    },
    {
      path: '/labReservation',
      name: 'labReservation',
      component: LabReservationView,
    },
    {
      path: '/disabledPeriod',
      name: 'disabledPeriod',
      component: DisabledPeriodView,
    },
    {
      path: '/labReservationDetail',
      name: 'labReservationDetail',
      component: LabReservationDetailView,
    },
    {
      path: '/changeLabStatus',
      name: 'changeLabStatus',
      component: ChangeLabStatusView,
    },
    {
      path: '/userEntry',
      name: 'userEntry',
      component: UserEntryView,
    },
    {
    path: '/changeUserStatus',
    name: 'changeUserStatus',
    component: ChangeUserStatusView,
},
  ],
})


router.beforeEach((to, from, next) => {
  // 获取用户数据
  const userData = sessionStorage.getItem('UserData')

  // 允许访问的页面列表（不需要登录的页面）
  const publicPages = ['/login', '/']

  // 判断当前路由是否在公开页面列表中
  const isPublicPage = publicPages.includes(to.path)

  // 如果是公开页面，直接放行
  if (isPublicPage) {
    next()
    return
  }

  // 如果不是公开页面且没有登录，跳转到登录页
  if (!userData) {
    next('/login')
    return
  }

  // 已登录，放行
  next()
})

export default router
