// pages/medical/create.js
const prescriptionApi = require('../../api/prescription.js');
const medicalApi = require('../../api/medical.js');
const { getToday } = require('../../utils/date.js');

Page({
    data: {
        patientName: '',
        visitDate: getToday(),
        notes: '', // New Field
        prescriptions: [],
        allPrescriptions: [],
        searchKeyword: '',
        selectedCount: 0,
        showSuccessModal: false,
        savedResult: {}
    },

    onLoad() {
        this.loadPrescriptions();
    },

    /**
     * 加载药方列表
     */
    loadPrescriptions() {
        prescriptionApi.getPrescriptions('', 1, 100)
            .then(res => {
                const prescriptions = (res.content || []).map(item => ({
                    id: item.id,
                    name: item.name,
                    checked: false
                }));
                this.setData({
                    prescriptions,
                    allPrescriptions: prescriptions
                });
            })
            .catch(err => {
                console.error('加载药方失败', err);
            });
    },

    /**
     * 患者姓名输入
     */
    onPatientNameInput(e) {
        this.setData({
            patientName: e.detail.value
        });
    },

    /**
     * 备注输入
     */
    onNotesInput(e) {
        this.setData({
            notes: e.detail.value
        });
    },

    /**
     * 日期选择
     */
    onDateChange(e) {
        this.setData({
            visitDate: e.detail.value
        });
    },

    /**
     * 搜索药方
     */
    onSearchInput(e) {
        const keyword = e.detail.value.toLowerCase();
        this.setData({ searchKeyword: keyword });

        if (!keyword) {
            this.setData({ prescriptions: this.data.allPrescriptions });
        } else {
            const filtered = this.data.allPrescriptions.filter(p =>
                p.name.toLowerCase().includes(keyword)
            );
            this.setData({ prescriptions: filtered });
        }
    },

    /**
     * 药方选择变化
     */
    onPrescriptionChange(e) {
        const selectedIds = e.detail.value.map(id => parseInt(id));
        const prescriptions = this.data.prescriptions.map(item => ({
            ...item,
            checked: selectedIds.includes(item.id)
        }));

        this.setData({
            prescriptions,
            selectedCount: selectedIds.length
        });
    },

    /**
     * 保存病历
     */
    onSave() {
        const { patientName, visitDate, prescriptions, notes } = this.data;

        // 校验姓名
        if (!patientName || !patientName.trim()) {
            wx.showToast({
                title: '请输入患者姓名',
                icon: 'none'
            });
            return;
        }

        // 获取选中的药方ID
        const prescriptionIds = prescriptions
            .filter(p => p.checked)
            .map(p => p.id);

        // 校验至少选择1个药方
        if (prescriptionIds.length === 0) {
            wx.showToast({
                title: '请至少选择1个药方',
                icon: 'none'
            });
            return;
        }

        // 调用后端创建病历
        wx.showLoading({ title: '保存中...' });

        medicalApi.createMedicalRecord({
            patientName: patientName.trim(),
            visitDate: visitDate,
            prescriptionIds: prescriptionIds,
            notes: notes ? notes.trim() : null
        })
            .then(res => {
                wx.hideLoading();

                // 显示成功弹窗
                this.setData({
                    showSuccessModal: true,
                    savedResult: {
                        patientName: patientName.trim(), // Save patientName for modal/history
                        prescriptionNames: res.prescriptionNames,
                        mergedHerbsText: res.mergedHerbsText
                    }
                });

                // 成功提示音
                wx.showToast({
                    title: '保存成功！',
                    icon: 'success',
                    duration: 1500
                });
            })
            .catch(err => {
                wx.hideLoading();
                console.error('保存病历失败', err);
                wx.showToast({
                    title: '保存失败',
                    icon: 'none'
                });
            });
    },

    /**
     * 复制药材清单
     */
    onCopyHerbs() {
        wx.setClipboardData({
            data: this.data.savedResult.mergedHerbsText,
            success: () => {
                wx.showToast({
                    title: '已复制',
                    icon: 'success'
                });
            }
        });
    },

    /**
     * 确定（返回首页）
     */
    onGoHome() {
        // 关闭弹窗并返回首页
        this.setData({
            showSuccessModal: false
        });

        // 返回首页
        wx.reLaunch({
            url: '/pages/home/index'
        });
    },

    /**
     * 查看历史（跳转并搜索）
     */
    onGoToHistory() {
        const patientName = this.data.savedResult.patientName || '';

        this.setData({
            showSuccessModal: false
        });

        // 跳转到列表页并带上参数
        wx.navigateTo({
            url: `/pages/medical/list?patientName=${encodeURIComponent(patientName)}`
        });
    },

    /**
     * 点击遮罩关闭（可选，暂时禁用）
     */
    onMaskTap() {
        // 不允许点击遮罩关闭，必须选择按钮
    },

    /**
     * 阻止冒泡
     */
    stopPropagation() { }
});
