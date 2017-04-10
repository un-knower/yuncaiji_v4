(function(f,define){
	define('common', ['ufa.core'], f);
}(function () {
var __meta__ = {
        id: 'common',
        name: 'ComboBox',
        category: 'web',
        description: 'The ComboBox widget allows the selection from pre-defined values or entering a new value.',
        depends: ['list'],
        features: [
            {
                id: 'mobile-scroller',
                name: 'Mobile scroller',
                description: 'Support for kinetic scrolling in mobile device',
                depends: ['mobile.scroller']
            },
            {
                id: 'virtualization',
                name: 'VirtualList',
                description: 'Support for virtualization',
                depends: ['virtuallist']
            }
        ]
    };
    (function ($, undefined) {
    	$.fn.extend({
    		//加载动画
            loading: function () {
                if(this.length > 1){
                	$.each(this,function(index,item){
            			var w = $(item).innerWidth();
                        var h = $(item).innerHeight();
                        if (item == document.body) {
                            w = $(window).width();
                            h = $(window).height();
                        } else if ($(item).css('position') !== 'absolute'){
                        	$(item).css('position', 'relative');
                        };
                        var loading = $('<div class="loading" style="width:' + w + 'px;height:' + h + 'px"><p class="loading-text">请稍等...</p></div>');
                        $(item).append(loading);
                	})
                }else{
                	var w = this.innerWidth();
                    var h = this.innerHeight();
                    if (this[0] == document.body) {
                        w = $(window).width();
                        h = $(window).height();
                    } else if (this.css('position') !== 'absolute'){
                    	this.css('position', 'relative');
                    };
                    var loading = $('<div class="loading" style="width:' + w + 'px;height:' + h + 'px"><p class="loading-text">请稍等...</p></div>');
                    this.append(loading);
                }
                return $(this);
            },
            complete: function () {
                this.children('.loading').fadeTo('normal', 0, function () {
                    var navigatorName = "Microsoft Internet Explorer";
                    if (navigator.appName == navigatorName) {
                        $('.loading').removeNode(true);
                    } else {
                        $('.loading').remove();
                    }
                });
                return $(this);
            },
            //提示信息
            info: function (text) {
            	var textInfo = "请选择查询条件";
            	if(text){
            		textInfo = text;
            	}
                if(this.length > 1){
                	$.each(this,function(index,item){
                		var w = $(item).innerWidth();
                        var h = $(item).innerHeight();
                        if (item == document.body) {
                            w = $(window).width();
                            h = $(window).height();
                        } else if ($(item).css('position') !== 'absolute'){
                        	$(item).css('position', 'relative');
                        };
                        var loading = $('<div class="z-info" style="width:' + w + 'px;height:' + h + 'px"><p class="z-info-text">'+textInfo+'</p></div>');
                        $(item).append(loading);
                	})
                }else{
                	if(this.find(".z-info").length !== 0){
                		return;
                	}
                	var w = this.innerWidth();
                    var h = this.innerHeight();
                    if (this[0] == document.body) {
                        w = $(window).width();
                        h = $(window).height();
                    } else if (this.css('position') !== 'absolute'){
                    	this.css('position', 'relative');
                    };
                    var loading = $('<div class="z-info" style="width:' + w + 'px;height:' + h + 'px"><p class="z-info-text">'+textInfo+'</p></div>');
                    this.append(loading);
                }          
            },
            //删除提示信息
            noInfo: function () {
                this.children('.z-info').fadeTo('normal', 0, function () {
                    var navigatorName = "Microsoft Internet Explorer";
                    if (navigator.appName == navigatorName) {
                        $('.z-info').removeNode(true);
                    } else {
                        $('.z-info').remove();
                    }
                });
                return $(this);
            },
    	})
    }(jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));