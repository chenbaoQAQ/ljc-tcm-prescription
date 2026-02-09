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

                let list = res.content || res.list || [];

                // 计算序号逻辑：按患者分组倒序计数
                // 1. 统计每个患者的总次数
                const patientCounts = {};
                list.forEach(item => {
                    const name = item.patientName;
                    patientCounts[name] = (patientCounts[name] || 0) + 1;
                });

                // 2. 分配序号（倒序）
                const patientIndices = {}; // 用于记录当前遍历到的索引
                list = list.map(item => {
                    const name = item.patientName;
                    const total = patientCounts[name];
                    const currentIndex = patientIndices[name] || 0;

                    const visitNumber = total - currentIndex;
                    patientIndices[name] = currentIndex + 1;

                    return {
                        ...item,
                        visitNumber
                    };
                });

                this.setData({
                    records: list,
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
     * 删除病历
     */
    onDeleteRecord(e) {
        const item = e.currentTarget.dataset.item;

        wx.showModal({
            title: '确认删除',
            content: `确定要删除患者"${item.patientName}"的这条记录吗？`,
            success: (res) => {
                if (res.confirm) {
                    this.doDelete(item.id);
                }
            }
        });
    },

    /**
     * 执行删除
     */
    doDelete(id) {
        wx.showLoading({ title: '删除中...' });

        medicalApi.deleteMedicalRecord(id)
            .then(() => {
                wx.hideLoading();
                wx.showToast({
                    title: '已删除',
                    icon: 'success'
                });
                // 重新加载列表
                this.loadRecords();
            })
            .catch(err => {
                wx.hideLoading();
                console.error('删除失败', err);
                wx.showToast({
                    title: '删除失败',
                    icon: 'none'
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
