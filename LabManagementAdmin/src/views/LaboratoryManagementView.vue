<template>
    <!-- 简约实验室管理面板：一行四卡，左图右文字 -->
    <div class="laboratory-management">
        <!-- 固定头部区域 -->
        <div class="fixed-header">
            <div class="header-container">
                <div class="section-title">实验室管理</div>

                <div class="button-container">
                    <button @click="batchOperation" class="batch-button">{{ operation }}</button>
                    <button v-show="isOperation" class="status-button" @click="toChangStatus">修改状态</button>
                    <button class="batch-input-button" @click="toLaboratoryArrange">教学安排</button>
                    <button class="batch-input-button" @click="toLaboratoryEntry">实验室录入</button>
                </div>
            </div>

            <div class="search-container">
                <div class="search-box">
                    <input type="text" v-model="searchKeyword" @input="handleSearch" placeholder="搜索名称或地点或ID"
                        class="search-input">
                    <select v-model="searchStatus" @change="handleSearch" class="status-select">
                        <option value="">全部</option>
                        <option value="空闲">空闲</option>
                        <option value="闲置">闲置</option>
                        <option value="维修">维修</option>
                        <option value="停用">停用</option>
                        <option value="故障">故障</option>
                        <option value="可能发生故障">可能发生故障</option>
                    </select>
                    <button @click="reset">重置</button>
                </div>
            </div>
        </div>

        <!-- 滚动内容区域 -->
        <div class="scroll-content">
            <div class="laboratory-grid">
                <div v-for="laboratory in filteredLaboratory" :key="laboratory.laboratory_id" class="laboratory-card"
                    :class="{
                        'operation-mode': isOperation,
                        'selected': isOperation && selectedLabs.includes(laboratory.laboratory_id)
                    }"
                    @click="handleCardClick(laboratory.laboratory_id)">

                    <div class="checkbox-wrapper" v-show="isOperation">
                        <input type="checkbox" :value="laboratory.laboratory_id" v-model="selectedLabs"
                            class="laboratory-checkbox" @click.stop>
                    </div>

                    <img :src="laboratory.image_url" :alt="laboratory.laboratory_name || '实验室图片'" class="laboratory-image">
                    <div class="laboratory-info">
                        <div class="info-item"><span class="label">实验室ID:</span> {{ laboratory.laboratory_id }}</div>
                        <div class="info-item"><span class="label">名称:</span> {{ laboratory.laboratory_name }}</div>
                        <div class="info-item"><span class="label">地点:</span> {{ laboratory.location }}</div>
                        <div class="info-item"><span class="label">状态:</span> {{ laboratory.status }}</div>
                    </div>
                </div>
            </div>

            <div v-if="filteredLaboratory.length === 0 && searchKeyword" class="no-result">
                没有找到匹配的实验室
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
    components: {
        Footer
    },
    setup() {
        const router = useRouter()
        const state = reactive({
            LaboratoryArr: [],
            filteredLaboratory: [],
            searchKeyword: '',
            searchStatus: '',
            operation: "批量操作",
            isOperation: false,
            selectedLabs: []
        })

        function batchOperation() {
            if (!state.isOperation) {
                state.operation = "取消"
                state.isOperation = true
                state.selectedLabs = []
            } else {
                state.operation = "批量操作"
                state.isOperation = false
                state.selectedLabs = []
            }
        }

        function initLaboratoryArr() {
            axios
                .get("/api/getAllLaboratory")
                .then((response) => {
                    if (response.data.status == 200) {
                        console.log(response.data.data)
                        state.LaboratoryArr = response.data.data
                        state.filteredLaboratory = response.data.data
                    } else {
                        ElMessage.error("获取实验室信息失败")
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        }
        initLaboratoryArr()

        function handleSearch() {
            const keyword = state.searchKeyword.trim().toLowerCase()
            const status = state.searchStatus

            if (!keyword && !status) {
                state.filteredLaboratory = state.LaboratoryArr
                return
            }
            state.filteredLaboratory = state.LaboratoryArr.filter(laboratory => {
                let keywordMatch = true
                if (keyword) {
                    const labName = laboratory.laboratory_name?.toLowerCase() || ''
                    const location = laboratory.location?.toLowerCase() || ''
                    const laboratoryId = String(laboratory.laboratory_id).toLowerCase()
                    keywordMatch = labName.includes(keyword) || location.includes(keyword) || laboratoryId.includes(keyword)
                }

                let statusMatch = true
                if (status) {
                    statusMatch = laboratory.status === status
                }

                return keywordMatch && statusMatch
            })
        }

        function reset() {
            state.filteredLaboratory = state.LaboratoryArr
            state.searchKeyword = ''
            state.searchStatus = ''
        }

        function toChangStatus() {
            if (state.selectedLabs.length === 0) {
                ElMessage.error("请选择实验室")
                return
            }

            const selectedLabsInfo = state.LaboratoryArr.filter(lab => state.selectedLabs.includes(lab.laboratory_id))

            const firstStatus = selectedLabsInfo[0]?.status
            const allSameStatus = selectedLabsInfo.every(lab => lab.status === firstStatus)

            if (!allSameStatus) {
                ElMessage.error("选中的实验室状态不一致，不能进行批量修改状态操作")
                return
            }

            const labIds = state.selectedLabs.join(',')
            const labNames = selectedLabsInfo.map(lab => lab.laboratory_name).join(',')

            router.push({
                path: '/changeLabStatus',
                query: {
                    labIds: labIds,
                    labNames: labNames,
                    currentStatus: firstStatus
                }
            })
        }

        function toLaboratoryArrange() {
            router.push('/laboratoryArrange')
        }

        function toLaboratoryEntry() {
            router.push('/laboratoryEntry')
        }

        function toLaboratoryDetail(laboratoryId) {
            router.push({
                path: '/laboratoryDetail',
                query: {
                    laboratory_id: laboratoryId
                }
            })
        }

        function handleCardClick(laboratoryId) {
            if (state.isOperation) {
                // 批量操作模式：切换选中状态
                const index = state.selectedLabs.indexOf(laboratoryId)
                if (index === -1) {
                    state.selectedLabs.push(laboratoryId)
                } else {
                    state.selectedLabs.splice(index, 1)
                }
            } else {
                // 非批量操作模式：跳转到详情
                toLaboratoryDetail(laboratoryId)
            }
        }

        return {
            ...toRefs(state),
            batchOperation,
            toChangStatus,
            handleSearch,
            toLaboratoryEntry,
            reset,
            toLaboratoryDetail,
            toLaboratoryArrange,
            handleCardClick,
        }
    },
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
    position: relative;
}

/* 固定头部区域 */
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
    margin-top: 160px;
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
}

/* 批量录入按钮 / 教学安排按钮 */
.batch-input-button {
    padding: 8px 20px;
    background-color: #e6a23c;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}

/* 搜索容器 */
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

/* 状态选择器 */
.status-select {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 14px;
    outline: none;
    background-color: white;
    cursor: pointer;
}

.status-select:hover {
    border-color: #c0c4cc;
}

.laboratory-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
}

/* 卡片样式 */
.laboratory-card {
    position: relative;
    display: flex;
    align-items: flex-start;
    background-color: #ffffff;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    padding: 16px;
    box-sizing: border-box;
    height: 140px;
    cursor: pointer;
}

/* 操作模式下的卡片效果 */
.laboratory-card.operation-mode {
    background-color: #fafafa;
}

/* 选中状态下的卡片效果 */
.laboratory-card.selected {
    border-color: #409eff;
}

/* 勾选框包装器 */
.checkbox-wrapper {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 10;
    background-color: white;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 勾选框样式 */
.laboratory-checkbox {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: #409eff;
}

.laboratory-card:hover {
    border-color: #c0c4cc;
}

/* 左侧图片区域 */
.laboratory-image {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
    background-color: #f5f7fa;
    border: 1px solid #ebeef5;
    margin-right: 16px;
    flex-shrink: 0;
}

/* 右侧信息区域 */
.laboratory-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.info-item {
    font-size: 14px;
    color: #3c4a5a;
    line-height: 1.6;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 6px;
}

.info-item:last-child {
    margin-bottom: 0;
}

.info-item .label {
    color: #7f8c8d;
    margin-right: 6px;
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
</style>