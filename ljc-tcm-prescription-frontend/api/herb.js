const request = require('../utils/request.js');

module.exports = {
    // Get all herbs (with optional search)
    getHerbs: (keyword = '') => request.get('/herbs', { keyword }),

    // Create herb
    createHerb: (data) => request.post('/herbs', data),

    // Update herb
    updateHerb: (id, data) => request.put(`/herbs/${id}`, data),

    // Delete herb
    deleteHerb: (id) => request.del(`/herbs/${id}`)
};
