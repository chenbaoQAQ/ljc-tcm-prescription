// pages/prescription/list.js
const prescriptionApi = require('../../api/prescription.js');

Page({
  data: {
    prescriptions: []
  },

  onShow() {
    this.loadPrescriptions();
  },

  /**
   * 加载药方列表
   */
  loadPrescriptions() {
    prescriptionApi.getPrescriptions('', 1, 100)
      .then(res => {
        this.setData({
          prescriptions: res.content || []
        });
      })
      .catch(err => {
        console.error('加载药方失败', err);
      });
  },

  /**
   * 新建药方
   */
  onAdd() {
    wx.navigateTo({
      url: '/pages/prescription/edit'
    });
  },

  /**
   * 编辑药方
   */
  onEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/prescription/edit?id=${id}`
    });
  },

  /**
   * 删除药方
   */
  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除药方"${name}"吗？`,
      success: (res) => {
        if (res.confirm) {
          prescriptionApi.deletePrescription(id)
            .then(() => {
              wx.showToast({
                title: '已删除',
                icon: 'success'
              });
              this.loadPrescriptions();
            })
            .catch(err => {
              console.error('删除失败', err);
            });
        }
      }
    });
  }
});