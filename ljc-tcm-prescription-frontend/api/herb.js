const request = require('../utils/request.js');

/**
 * 药材库 API
 */
module.exports = {
    /**
     * 获取药材列表
     */
    getHerbs: (keyword = '', page = 1, size = 50) => {
        return request({
            url: '/api/v1/herbs',
            method: 'GET',
            params: { keyword, page, size }
        });
    },

    /**
     * 新增药材
     */
    createHerb: (nameCn) => {
        return request({
            url: '/api/v1/herbs',
            method: 'POST',
            data: { nameCn }
        });
    },

    /**
     * 删除药材
     */
    deleteHerb: (id) => {
        return request({
            url: `/api/v1/herbs/${id}`,
            method: 'DELETE'
        });
    }
};
