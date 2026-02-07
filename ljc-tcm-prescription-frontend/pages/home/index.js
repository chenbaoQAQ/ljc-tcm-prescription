// pages/home/index.js
Page({
  goToCreateMedical() {
    wx.navigateTo({
      url: '/pages/medical/create'
    });
  },

  goToMedicalHistory() {
    wx.navigateTo({
      url: '/pages/medical/list'
    });
  },

  goToPrescriptionList() {
    wx.navigateTo({
      url: '/pages/prescription/list'
    });
  },

  goToHerbList() {
    wx.navigateTo({
      url: '/pages/herb/list'
    });
  }
});