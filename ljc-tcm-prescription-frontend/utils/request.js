const { BASE_URL } = require('../config/env.js');

/**
 * 统一请求封装
 * 后端返回格式: { code: 0, message: "success", data: {...}, traceId: "..." }
 * code === 0 表示成功，否则失败
 */
const request = ({ url, method = 'GET', data = null, params = null }) => {
  return new Promise((resolve, reject) => {
    // 处理 GET 请求的 query params
    let finalUrl = BASE_URL + url;
    if (params && method === 'GET') {
      const queryString = Object.keys(params)
        .filter(key => params[key] !== null && params[key] !== undefined)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');
      if (queryString) {
        finalUrl += (finalUrl.includes('?') ? '&' : '?') + queryString;
      }
    }

    wx.request({
      url: finalUrl,
      method: method,
      data: method !== 'GET' ? data : undefined,
      header: {
        'content-type': 'application/json'
      },
      timeout: 10000,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const { code, message, data: responseData } = res.data;

          // 只有 code === 0 才认为成功
          if (code === 0) {
            resolve(responseData);
          } else {
            // 失败时 toast 提示
            wx.showToast({
              title: message || '请求失败',
              icon: 'none',
              duration: 2000
            });
            reject({ code, message, data: responseData });
          }
        } else {
          wx.showToast({
            title: `服务器错误 ${res.statusCode}`,
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络错误/后端未启动',
          icon: 'none',
          duration: 2000
        });
        reject(err);
      }
    });
  });
};

module.exports = request;
