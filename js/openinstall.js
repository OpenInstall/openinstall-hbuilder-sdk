document.addEventListener( "plusready",  function(){
    var _BARCODE = 'openinstall',
		B = window.plus.bridge;
    var openinstall = {
        // 初始化
        init : function (permission) {
            return B.exec(_BARCODE, "init", [permission]);
        },

		//注册拉起回调
		registerWakeUpHandler: function (successCallback, permission) {
		    var success = typeof successCallback !== 'function' ? null : function(args) {
                successCallback(args);
            },
            callbackID = B.callbackId(success, null);
            return B.exec(_BARCODE, "registerWakeUpHandler", [callbackID, permission]);
		},
		// 获取安装来源数据
		getInstall : function (successCallback, timeout) {
			var success = typeof successCallback !== 'function' ? null : function(args) {
				successCallback(args);
			},
			callbackID = B.callbackId(success, null);
			return B.exec(_BARCODE, "getInstall", [callbackID, timeout]);
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