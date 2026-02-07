const API_BASE = '/api/v1';

// Unified Request Handler
async function request(endpoint, options = {}) {
    // Default headers
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    // Build URL
    let url = `${API_BASE}${endpoint}`;
    if (options.params) {
        const query = new URLSearchParams(options.params).toString();
        url += `?${query}`;
    }

    // Fetch
    try {
        const res = await fetch(url, {
            method: options.method || 'GET',
            headers,
            body: options.body ? JSON.stringify(options.body) : undefined
        });

        const data = await res.json();

        // Standard Response Handling: { code, message, data }
        if (data.code === 0) {
            return data.data;
        } else {
            showToast(data.message || '操作失败', 'error');
            throw new Error(data.message);
        }
    } catch (err) {
        console.error('Request Error:', err);
        showToast(err.message || '网络错误', 'error');
        throw err;
    }
}

// Toast Notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.style.display = 'block';
    toast.style.background = type === 'error' ? 'rgba(255, 77, 79, 0.9)' : 'rgba(0, 0, 0, 0.8)';
    toast.textContent = message;

    document.body.appendChild(toast);

    // Fade in
    setTimeout(() => {
        toast.style.opacity = 1;
    }, 10);

    // Remove
    setTimeout(() => {
        toast.style.opacity = 0;
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

// Modal Helper
function showModal(title, contentHtml, onConfirm, confirmText = '确定') {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.style.display = 'flex'; // Show immediately

    overlay.innerHTML = `
        <div class="modal">
            <h3 class="modal-title">${title}</h3>
            <div class="modal-body">${contentHtml}</div>
            <div class="modal-footer">
                <button class="btn btn-sm" style="flex:1; background:#f5f5f5;" onclick="this.closest('.modal-overlay').remove()">取消</button>
                <button class="btn btn-sm btn-primary" style="flex:1" id="modal-confirm">${confirmText}</button>
            </div>
        </div>
    `;

    document.body.appendChild(overlay);

    overlay.querySelector('#modal-confirm').onclick = () => {
        if (onConfirm) onConfirm();
        overlay.remove();
    };
}

// Parse Query Params
function getQueryParam(key) {
    const params = new URLSearchParams(window.location.search);
    return params.get(key);
}

// Date Formatter
function formatDate(dateStr) {
    if (!dateStr) return '';
    return dateStr.split('T')[0];
}

// API Methods
const api = {
    // Herbs
    getHerbs: (params) => request('/herbs', { params }),
    createHerb: (nameCn) => request('/herbs', { method: 'POST', body: { nameCn } }),
    deleteHerb: (id) => request(`/herbs/${id}`, { method: 'DELETE' }),

    // Prescriptions
    getPrescriptions: (params) => request('/prescriptions', { params }),
    getPrescription: (id) => request(`/prescriptions/${id}`),
    createPrescription: (data) => request('/prescriptions', { method: 'POST', body: data }),
    updatePrescription: (id, data) => request(`/prescriptions/${id}`, { method: 'PUT', body: data }),
    deletePrescription: (id) => request(`/prescriptions/${id}`, { method: 'DELETE' }),

    // Medical Records
    createMedicalRecord: (data) => request('/medical-records', { method: 'POST', body: data }),
    getMedicalRecords: (params) => request('/medical-records', { params }),
    getMedicalRecord: (id) => request(`/medical-records/${id}`),
    deleteMedicalRecord: (id) => request(`/medical-records/${id}`, { method: 'DELETE' })
};
