// pages/prescription/edit.js
const prescriptionApi = require('../../api/prescription.js');
const herbApi = require('../../api/herb.js');

Page({
  data: {
    id: null, // null表示新建，有值表示编辑
    name: '',
    herbs: [], // 药材库（用于picker选择）
    items: [], // 药材明细行 [{ herbId, herbName, herbIndex, doseG }]
  },

  onLoad(options) {
    // 加载药材库
    this.loadHerbs();

    // 如果有id，加载药方详情
    if (options.id) {
      this.setData({ id: options.id });
      this.loadPrescription(options.id);
    }
  },

  /**
   * 加载药材库
   */
  loadHerbs() {
    herbApi.getHerbs('', 1, 200)
      .then(res => {
        this.setData({
          herbs: res.content || []
        });
      })
      .catch(err => {
        console.error('加载药材库失败', err);
        wx.showToast({
          title: '请先添加药材',
          icon: 'none'
        });
      });
  },

  /**
   * 加载药方详情（编辑模式）
   */
  loadPrescription(id) {
    prescriptionApi.getPrescription(id)
      .then(res => {
        const { herbs } = this.data;

        // 回填药方名称
        this.setData({ name: res.name || '' });

        // 回填药材明细
        const items = (res.items || []).map(item => {
          // 找到药材在herbs数组中的索引
          const herbIndex = herbs.findIndex(h => h.id === item.herbId);
          const herbName = item.herbNameSnapshot || '';

          return {
            herbId: item.herbId,
            herbName,
            herbIndex,
            doseG: item.doseG || ''
          };
        });

        this.setData({ items });
      })
      .catch(err => {
        console.error('加载药方失败', err);
      });
  },

  /**
   * 药方名称输入
   */
  onNameInput(e) {
    this.setData({
      name: e.detail.value
    });
  },

  /**
   * 添加药材行
   */
  onAddItem() {
    const { items } = this.data;
    items.push({
      herbId: null,
      herbName: '',
      herbIndex: -1,
      doseG: ''
    });
    this.setData({ items });
  },

  /**
   * 选择药材
   */
  onHerbChange(e) {
    const idx = e.currentTarget.dataset.idx;
    const herbIndex = parseInt(e.detail.value);
    const herb = this.data.herbs[herbIndex];

    this.setData({
      [`items[${idx}].herbId`]: herb.id,
      [`items[${idx}].herbName`]: herb.nameCn,
      [`items[${idx}].herbIndex`]: herbIndex
    });
  },

  /**
   * 输入克重
   */
  onDoseInput(e) {
    const idx = e.currentTarget.dataset.idx;
    let value = e.detail.value;

    // 只允许数字和小数点
    value = value.replace(/[^\d.]/g, '');

    this.setData({
      [`items[${idx}].doseG`]: value
    });
  },

  /**
   * 删除药材行
   */
  onDeleteItem(e) {
    const idx = e.currentTarget.dataset.idx;
    const { items } = this.data;
    items.splice(idx, 1);
    this.setData({ items });
  },

  /**
   * 保存药方
   */
  onSave() {
    const { id, name, items } = this.data;

    // 校验：药方名非空
    if (!name || !name.trim()) {
      wx.showToast({
        title: '请输入药方名称',
        icon: 'none'
      });
      return;
    }

    // 校验：至少1行
    if (items.length === 0) {
      wx.showToast({
        title: '请至少添加1味药材',
        icon: 'none'
      });
      return;
    }

    // 校验：每行herbId存在且doseG正数
    for (let i = 0; i < items.length; i++) {
      const item = items[i];

      if (!item.herbId) {
        wx.showToast({
          title: `第${i + 1}行请选择药材`,
          icon: 'none'
        });
        return;
      }

      const dose = parseFloat(item.doseG);
      if (!item.doseG || isNaN(dose) || dose <= 0) {
        wx.showToast({
          title: `第${i + 1}行克重必须>0`,
          icon: 'none'
        });
        return;
      }
    }

    // 构建请求数据
    const data = {
      name: name.trim(),
      items: items.map(item => ({
        herbId: item.herbId,
        doseG: parseFloat(item.doseG) // 转换为数字
      }))
    };

    wx.showLoading({ title: '保存中...' });

    const apiCall = id
      ? prescriptionApi.updatePrescription(id, data)
      : prescriptionApi.createPrescription(data);

    apiCall
      .then(() => {
        wx.hideLoading();
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch(err => {
        wx.hideLoading();
        console.error('保存失败', err);
      });
  }
});