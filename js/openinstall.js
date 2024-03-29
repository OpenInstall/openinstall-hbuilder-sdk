document.addEventListener( "plusready",  function(){
    var _BARCODE = 'openinstall',
		B = window.plus.bridge;
    var openinstall = {

        configAndroid : function (options) {
            return B.exec(_BARCODE, "config", [options]);
        },

        // 旧版本接口，后续移除
        config : function (adEnabled, oaid, gaid) {
            var options = {};
            options.adEnabled = adEnabled;
            options.oaid = oaid;
            options.gaid = gaid;
            return B.exec(_BARCODE, "config", [options]);
        },

        // (仅支持Android)
        serialEnabled : function(enabled){
            return B.exec(_BARCODE, "serialEnabled", [enabled]);
        },

        // (仅支持Android)
        clipBoardEnabled : function(enabled){
            return B.exec(_BARCODE, "clipBoardEnabled", [enabled]);
        },

        // 初始化
        init : function () {
            return B.exec(_BARCODE, "init", []);
        },

        //注册拉起回调
        registerWakeUpHandler: function (successCallback) {
            var success = typeof successCallback !== 'function' ? null : function(args) {
                successCallback(args);
            },
            callbackID = B.callbackId(success, null);
            return B.exec(_BARCODE, "registerWakeUpHandler", [callbackID]);
        },
        // 获取安装来源数据
        getInstall : function (successCallback, timeout) {
            var success = typeof successCallback !== 'function' ? null : function(args) {
                successCallback(args);
            },
            callbackID = B.callbackId(success, null);
            return B.exec(_BARCODE, "getInstall", [callbackID, timeout]);
        },
        // 获取安装来源数据 (仅支持Android)
        getInstallCanRetry : function (successCallback, timeout) {
            var success = typeof successCallback !== 'function' ? null : function(args) {
                successCallback(args);
            },
            callbackID = B.callbackId(success, null);
            return B.exec(_BARCODE, "getInstallCanRetry", [callbackID, timeout]);
        },
        // 注册上报
        reportRegister : function () {
            return B.exec(_BARCODE, "reportRegister", []);
        },
        // 上报渠道效果
        reportEffectPoint : function (pointId, pointValue) {
            return B.exec(_BARCODE, "reportEffectPoint", [pointId, pointValue]);
        }
    };
    window.plus.openinstall = openinstall;
}, true );