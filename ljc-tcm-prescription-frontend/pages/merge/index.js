const api = require('../../api/prescription.js');

Page({
  data: {
    prescriptions: [],
    keyword: '',
    selectedCount: 0
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
          selected: false
        }));
        this.setData({ prescriptions });
      })
      .catch(err => console.error(err));
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
    this.loadPrescriptions();
  },

  onToggleSelect(e) {
    const id = e.currentTarget.dataset.id;
    const prescriptions = this.data.prescriptions.map(item => {
      if (item.id === id) {
        return { ...item, selected: !item.selected };
      }
      return item;
    });

    const selectedCount = prescriptions.filter(p => p.selected).length;
    this.setData({ prescriptions, selectedCount });
  },

  onMerge() {
    const selectedIds = this.data.prescriptions
      .filter(p => p.selected)
      .map(p => p.id);

    if (selectedIds.length === 0) {
      wx.showToast({ title: 'Please select at least 1 prescription', icon: 'none' });
      return;
    }

    // Navigate to result page with IDs
    wx.navigateTo({
      url: `/pages/merge/result?ids=${selectedIds.join(',')}`
    });
  }
});