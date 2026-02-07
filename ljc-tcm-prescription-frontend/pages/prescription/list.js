const api = require('../../api/prescription.js');

Page({
  data: {
    prescriptions: [],
    keyword: ''
  },

  onShow() {
    this.loadPrescriptions();
  },

  loadPrescriptions() {
    api.getPrescriptions(this.data.keyword)
      .then(res => {
        const prescriptions = (res.data.content || []).map(item => ({
          ...item,
          herbCount: item.itemCount || 0,
          updated_at: this.formatDate(item.updatedAt)
        }));
        this.setData({ prescriptions });
      })
      .catch(err => console.error(err));
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadPrescriptions();
  },

  onAdd() {
    wx.navigateTo({ url: '/pages/prescription/edit' });
  },

  onEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/prescription/edit?id=${id}` });
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this prescription?',
      success: (res) => {
        if (res.confirm) {
          api.deletePrescription(id).then(() => {
            wx.showToast({ title: 'Deleted', icon: 'success' });
            this.loadPrescriptions();
          });
        }
      }
    });
  }
});