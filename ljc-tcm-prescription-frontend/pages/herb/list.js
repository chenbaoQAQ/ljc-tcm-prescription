const api = require('../../api/herb.js');

Page({
  data: {
    herbs: [],
    keyword: '',
    showEditor: false,
    isEdit: false,
    editForm: {
      id: null,
      name_cn: '',
      unit: 'g',
      default_dose_g: '',
      status: 1
    }
  },

  onShow() {
    this.loadHerbs();
  },

  loadHerbs() {
    api.getHerbs(this.data.keyword)
      .then(res => {
        this.setData({ herbs: res.data.content || [] });
      })
      .catch(err => console.error(err));
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadHerbs();
  },

  onAdd() {
    this.setData({
      showEditor: true,
      isEdit: false,
      editForm: {
        id: null,
        name_cn: '',
        unit: 'g',
        default_dose_g: '', // Ensure string or number handled correctly
        status: 1
      }
    });
  },

  onEdit(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showEditor: true,
      isEdit: true,
      editForm: {
        id: item.id,
        name_cn: item.name_cn,
        unit: item.unit,
        default_dose_g: item.default_dose_g,
        status: item.status
      }
    });
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this herb?',
      success: (res) => {
        if (res.confirm) {
          api.deleteHerb(id).then(() => {
            wx.showToast({ title: 'Deleted', icon: 'success' });
            this.loadHerbs();
          });
        }
      }
    });
  },

  onCancelEdit() {
    this.setData({ showEditor: false });
  },

  onStatusChange(e) {
    this.setData({
      'editForm.status': e.detail.value ? 1 : 0
    });
  },

  onSave() {
    const form = this.data.editForm;

    // Validation
    if (!form.name_cn) {
      wx.showToast({ title: 'Name required', icon: 'none' });
      return;
    }

    const promise = this.data.isEdit
      ? api.updateHerb(form.id, form)
      : api.createHerb(form);

    promise.then(() => {
      wx.showToast({ title: 'Saved', icon: 'success' });
      this.setData({ showEditor: false });
      this.loadHerbs();
    });
  }
});