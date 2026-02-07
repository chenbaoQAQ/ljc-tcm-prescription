Page({
  navToHerbList() {
    wx.navigateTo({ url: '/pages/herb/list' })
  },
  navToPrescriptionList() {
    wx.navigateTo({ url: '/pages/prescription/list' })
  },
  navToMerge() {
    wx.navigateTo({ url: '/pages/merge/index' })
  }
})