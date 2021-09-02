document.addEventListener( "plusready",  function(){
    var _BARCODE = 'openinstall',
		B = window.plus.bridge;
    var openinstall = {
       /*
       初始化前配置
       options {
           adEnabled: true, //SDK 需要获取广告追踪相关参数
           macDisabled: true, //SDK 不需要获取 mac地址
           imeiDisabled: true, //SDK 不需要获取 imei
           gaid: "通过 google api 获取到的 advertisingId", //SDK 使用传入的gaid，不再获取gaid
           oaid: "通过移动安全联盟获取到的 oaid", //SDK 使用传入的oaid，不再获取oaid
       }
       */
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