// pages/medical/list.js
const medicalApi = require('../../api/medical.js');

Page({
    data: {
        patientName: '',
        records: [],
        searched: false,
        showDetailModal: false,
        selectedRecord: {}
    },

    onLoad(options) {
        // 从参数获取患者姓名（从开病历页跳转过来时）
        if (options.patientName) {
            this.setData({
                patientName: decodeURIComponent(options.patientName)
            });
            // 自动搜索
            this.loadRecords();
        }
    },

    /**
     * 姓名输入
     */
    onNameInput(e) {
        this.setData({
            patientName: e.detail.value
        });
    },

    /**
     * 搜索按钮
     */
    onSearch() {
        const { patientName } = this.data;

        if (!patientName || !patientName.trim()) {
            wx.showToast({
                title: '请输入患者姓名',
                icon: 'none'
            });
            return;
        }

        this.loadRecords();
    },

    /**
     * 加载病历列表
     */
    loadRecords() {
        const { patientName } = this.data;

        wx.showLoading({ title: '加载中...' });

        medicalApi.getMedicalRecords(patientName.trim(), 1, 50)
            .then(res => {
                wx.hideLoading();
                this.setData({
                    records: res.list || [],
                    searched: true
                });
            })
            .catch(err => {
                wx.hideLoading();
                console.error('加载病历失败', err);
                this.setData({
                    records: [],
                    searched: true
                });
            });
    },

    /**
     * 点击病历项 - 显示详情
     */
    onRecordTap(e) {
        const item = e.currentTarget.dataset.item;
        this.setData({
            selectedRecord: item,
            showDetailModal: true
        });
    },

    /**
     * 复制药材清单
     */
    onCopy() {
        wx.setClipboardData({
            data: this.data.selectedRecord.mergedHerbsText,
            success: () => {
                wx.showToast({
                    title: '已复制',
                    icon: 'success'
                });
            }
        });
    },

    /**
     * 关闭详情弹窗
     */
    onCloseDetail() {
        this.setData({
            showDetailModal: false
        });
    },

    /**
     * 阻止冒泡
     */
    stopPropagation() { }
});
