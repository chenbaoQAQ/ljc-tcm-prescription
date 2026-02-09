// pages/herb/list.js
const herbApi = require('../../api/herb.js');

Page({
  data: {
    newHerbName: '',
    herbs: []
  },

  onShow() {
    this.loadHerbs();
  },

  /**
   * 加载药材列表
   */
  loadHerbs() {
    herbApi.getHerbs('', 1, 200)
      .then(res => {
        this.setData({
          herbs: res.content || []
        });
      })
      .catch(err => {
        console.error('加载药材失败', err);
      });
  },

  /**
   * 新药材名称输入
   */
  onNewHerbInput(e) {
    this.setData({
      newHerbName: e.detail.value
    });
  },

  /**
   * 添加药材
   */
  onAddHerb() {
    const { newHerbName } = this.data;

    if (!newHerbName || !newHerbName.trim()) {
      wx.showToast({
        title: '请输入药材名称',
        icon: 'none'
      });
      return;
    }

    herbApi.createHerb(newHerbName.trim())
      .then(() => {
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        });
        this.setData({ newHerbName: '' });
        this.loadHerbs();
      })
      .catch(err => {
        console.error('添加失败', err);
      });
  },

  /**
   * 删除药材
   */
  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除药材"${name}"吗？`,
      success: (res) => {
        if (res.confirm) {
          herbApi.deleteHerb(id)
            .then(() => {
              wx.showToast({
                title: '已删除',
                icon: 'success'
              });
              this.loadHerbs();
            })
            .catch(err => {
              console.error('删除失败', err);
            });
        }
      }
    });
  }
});