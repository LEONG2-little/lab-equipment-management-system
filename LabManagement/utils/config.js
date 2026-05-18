// utils/config.js
const LOCAL_IP = '192.168.1.197'  // ⚠️ 改成你的IP

const getBaseUrl = () => {
  const systemInfo = wx.getSystemInfoSync()
  return systemInfo.platform === 'devtools' 
    ? 'http://localhost:8080'
    : `http://${LOCAL_IP}:8080`
}

module.exports = {
  BASE_URL: getBaseUrl()
}