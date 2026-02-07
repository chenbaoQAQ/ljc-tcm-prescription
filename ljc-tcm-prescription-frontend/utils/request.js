const baseUrl = 'http://localhost:8080/api/v1';

const request = (url, method, data) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}${url}`,
      method: method,
      data: data,
      header: {
        'content-type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const { code, message, data } = res.data;
          // Assuming backend returns { code: 0, message: "...", data: ... } for success
          // If the backend strictly follows the description "code != 0弹toast", we handle it here.
          // Note: Some backends might just return data directly or have different structure.
          // The user said: "code != 0 弹 toast 显示 message"
          if (typeof code !== 'undefined' && code !== 0) {
            wx.showToast({
              title: message || 'Error',
              icon: 'none'
            });
            reject(res.data);
          } else {
            resolve(res.data);
          }
        } else {
          wx.showToast({
            title: `HTTP Error: ${res.statusCode}`,
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        wx.showToast({
          title: 'Network Error',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

const get = (url, data) => request(url, 'GET', data);
const post = (url, data) => request(url, 'POST', data);
const put = (url, data) => request(url, 'PUT', data);
const del = (url, data) => request(url, 'DELETE', data);

module.exports = {
  get,
  post,
  put,
  del
};
