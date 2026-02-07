const request = require('../utils/request.js');

/**
 * 药方库 API
 */
module.exports = {
    /**
     * 获取药方列表
     */
    getPrescriptions: (keyword = '', page = 1, size = 50) => {
        return request({
            url: '/api/v1/prescriptions',
            method: 'GET',
            params: { keyword, page, size }
        });
    },

    /**
     * 获取药方详情
     */
    getPrescription: (id) => {
        return request({
            url: `/api/v1/prescriptions/${id}`,
            method: 'GET'
        });
    },

    /**
     * 新建药方
     * data: { name, items: [{ herbId, doseG }] }
     */
    createPrescription: (data) => {
        return request({
            url: '/api/v1/prescriptions',
            method: 'POST',
            data
        });
    },

    /**
     * 更新药方
     */
    updatePrescription: (id, data) => {
        return request({
            url: `/api/v1/prescriptions/${id}`,
            method: 'PUT',
            data
        });
    },

    /**
     * 删除药方
     */
    deletePrescription: (id) => {
        return request({
            url: `/api/v1/prescriptions/${id}`,
            method: 'DELETE'
        });
    }
};
