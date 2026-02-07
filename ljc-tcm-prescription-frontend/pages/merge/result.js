const api = require('../../api/prescription.js');

Page({
  data: {
    mergedHerbs: []
  },

  onLoad(options) {
    if (options.ids) {
      const ids = options.ids.split(',').map(id => parseInt(id));
      this.mergePrescriptions(ids);
    }
  },

  mergePrescriptions(ids) {
    wx.showLoading({ title: 'Merging...' });

    api.mergePrescriptions(ids)
      .then(res => {
        wx.hideLoading();
        // Backend returns { data: { items: [ { name, doseG, sources } ] } }
        const items = res.data.items || [];
        const mergedHerbs = items.map(item => ({
          herbName: item.name,
          totalDoseG: item.doseG,
          sources: item.sources || []
        }));
        this.setData({ mergedHerbs });
      })
      .catch(err => {
        wx.hideLoading();
        console.error(err);
        wx.showToast({ title: 'Merge failed', icon: 'none' });
      });
  },

  onCopy() {
    const text = this.generateCopyText();
    wx.setClipboardData({
      data: text,
      success: () => {
        wx.showToast({ title: 'Copied to clipboard', icon: 'success' });
      }
    });
  },

  generateCopyText() {
    const lines = this.data.mergedHerbs.map(herb => {
      return `${herb.herbName} ${herb.totalDoseG}g`;
    });
    return lines.join('\n');
  },

  onBack() {
    wx.navigateBack();
  }
});