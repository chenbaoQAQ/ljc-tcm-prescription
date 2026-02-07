const prescriptionApi = require('../../api/prescription.js');
const herbApi = require('../../api/herb.js');

Page({
  data: {
    id: null, // Prescription ID (null for create)
    form: {
      name: '',
      description: ''
    },
    items: [], // { herbId, herbName, dose_g }
    showHerbSelector: false,
    availableHerbs: [],
    allHerbs: [],
    herbKeyword: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadPrescription(options.id);
    }
    this.loadAllHerbs();
  },

  loadPrescription(id) {
    prescriptionApi.getPrescription(id)
      .then(res => {
        const data = res.data;
        this.setData({
          form: {
            name: data.name || '',
            description: data.description || ''
          },
          items: (data.items || []).map(item => ({
            herbId: item.herbId,
            herbName: item.herbNameSnapshot,
            dose_g: item.doseG
          }))
        });
      })
      .catch(err => console.error(err));
  },

  loadAllHerbs() {
    herbApi.getHerbs('')
      .then(res => {
        const herbs = res.data.content || [];
        this.setData({
          allHerbs: herbs,
          availableHerbs: herbs
        });
      });
  },

  onAddHerb() {
    this.setData({
      showHerbSelector: true,
      herbKeyword: '',
      availableHerbs: this.data.allHerbs
    });
  },

  onHerbSearch(e) {
    const keyword = e.detail.value.toLowerCase();
    const filtered = this.data.allHerbs.filter(h =>
      h.name_cn.toLowerCase().includes(keyword)
    );
    this.setData({
      herbKeyword: keyword,
      availableHerbs: filtered
    });
  },

  onSelectHerb(e) {
    const herb = e.currentTarget.dataset.herb;

    // Check duplicate
    const exists = this.data.items.some(item => item.herbId === herb.id);
    if (exists) {
      wx.showToast({ title: 'Herb already added', icon: 'none' });
      return;
    }

    // Add to items
    const newItem = {
      herbId: herb.id,
      herbName: herb.name_cn,
      dose_g: herb.default_dose_g || ''
    };

    this.setData({
      items: [...this.data.items, newItem],
      showHerbSelector: false
    });
  },

  onCancelHerbSelect() {
    this.setData({ showHerbSelector: false });
  },

  onDoseChange(e) {
    const index = e.currentTarget.dataset.index;
    const value = e.detail.value;

    // Only allow digits and decimal point
    const validValue = value.replace(/[^\d.]/g, '');

    this.setData({
      [`items[${index}].dose_g`]: validValue
    });
  },

  onRemoveHerb(e) {
    const index = e.currentTarget.dataset.index;
    const items = this.data.items.filter((_, i) => i !== index);
    this.setData({ items });
  },

  onSave() {
    const { form, items, id } = this.data;

    // Validation
    if (!form.name) {
      wx.showToast({ title: 'Name required', icon: 'none' });
      return;
    }

    if (items.length === 0) {
      wx.showToast({ title: 'At least 1 herb required', icon: 'none' });
      return;
    }

    // Validate doses
    for (let item of items) {
      const dose = parseFloat(item.dose_g);
      if (isNaN(dose) || dose <= 0) {
        wx.showToast({ title: 'All doses must be > 0', icon: 'none' });
        return;
      }
    }

    // Build request payload
    const payload = {
      name: form.name,
      description: form.description,
      items: items.map(item => ({
        herbId: item.herbId,
        doseG: parseFloat(item.dose_g)
      }))
    };

    const promise = id
      ? prescriptionApi.updatePrescription(id, payload)
      : prescriptionApi.createPrescription(payload);

    promise.then(() => {
      wx.showToast({ title: 'Saved', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1000);
    });
  }
});