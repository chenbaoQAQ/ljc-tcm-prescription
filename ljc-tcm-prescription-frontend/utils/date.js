/**
 * 日期工具函数
 */

/**
 * 获取今天日期 YYYY-MM-DD 格式
 */
function getToday() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * 格式化日期显示（可选）
 */
function formatDate(dateStr) {
    if (!dateStr) return '';
    return dateStr; // 已经是 YYYY-MM-DD 格式
}

module.exports = {
    getToday,
    formatDate
};
