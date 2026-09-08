import request from '@/utils/request'

/**
 * 获取仪表板统计数据
 * 功能描述：获取用户、社区帖子、管理员和站点访问统计
 * 入参：无
 * 返回参数：{
 *   totalUsers: number,
 *   todayNewUsers: number,
 *   totalAdmins: number,
 *   totalPosts: number,
 *   todayNewPosts: number,
 *   totalVisits: number,
 *   todayVisits: number,
 *   last7DaysVisits: array
 * }
 * url地址：/dashboard/statistics
 * 请求方式：GET
 */
export function getDashboardStatistics(config = {}) {
  return request.get('/dashboard/statistics', null, {
    enableCache: false,
    ...config
  })
}
