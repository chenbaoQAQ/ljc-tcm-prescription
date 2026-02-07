const request = require('../utils/request.js');

/**
 * 病历 API
 */
module.exports = {
    /**
     * 创建病历
     * data: { patientName, visitDate, prescriptionIds: [1,3] }
     * 返回: { prescriptionNames, mergedHerbsText, mergedHerbs: [...] }
     */
    createMedicalRecord: (data) => {
        return request({
            url: '/api/v1/medical-records',
            method: 'POST',
            data
        });
    },

    /**
     * 按姓名查询病历列表
     * 返回: { list: [...], total, page, size }
     */
    getMedicalRecords: (patientName, page = 1, size = 50) => {
        return request({
            url: '/api/v1/medical-records',
            method: 'GET',
            params: { patientName, page, size }
        });
    },

    /**
     * 获取病历详情
     */
    getMedicalRecord: (id) => {
        return request({
            url: `/api/v1/medical-records/${id}`,
            method: 'GET'
        });
    },

    /**
     * 删除病历
     */
    deleteMedicalRecord: (id) => {
        return request({
            url: `/api/v1/medical-records/${id}`,
            method: 'DELETE'
        });
    }
};
