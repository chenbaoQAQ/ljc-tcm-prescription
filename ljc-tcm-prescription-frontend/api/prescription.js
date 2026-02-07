const request = require('../utils/request.js');

module.exports = {
    // Get all prescriptions (with optional search)
    getPrescriptions: (keyword = '') => request.get('/prescriptions', { keyword }),

    // Get single prescription detail
    getPrescription: (id) => request.get(`/prescriptions/${id}`),

    // Create prescription
    createPrescription: (data) => request.post('/prescriptions', data),

    // Update prescription
    updatePrescription: (id, data) => request.put(`/prescriptions/${id}`, data),

    // Delete prescription
    deletePrescription: (id) => request.del(`/prescriptions/${id}`),

    // Merge prescriptions
    mergePrescriptions: (ids) => request.post('/prescriptions/merge', { prescriptionIds: ids })
};
