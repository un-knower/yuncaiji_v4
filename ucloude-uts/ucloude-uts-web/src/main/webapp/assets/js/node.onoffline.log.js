/**
 * 几点上下线日志查询
 */

require(["jquery","common","ufa.dropdownlist", "ufa.datetimepicker", "ufa.grid",
		"ufa.mobile.switch", "messages/ufa.messages.zh-CN",
		"cultures/ufa.culture.zh-CN"], function($, ufa) {
	ufa.culture("zh-CN");
	Date.prototype.Format = function (fmt) { //author: meizz 
	    var o = {
	        "M+": this.getMonth() + 1, //月份 
	        "d+": this.getDate(), //日 
	        "h+": this.getHours(), //小时 
	        "m+": this.getMinutes(), //分 
	        "s+": this.getSeconds(), //秒 
	        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
	        "S": this.getMilliseconds() //毫秒 
	    };
	    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	    for (var k in o)
	    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	    return fmt;
	}
	switchEv = function(s){
		var v = s=="ONLINE"?"上线":"离线";
		return v;
	};
	var eventTypes = [{
			text : "上线",
			value : "ONLINE"
		}, {
			text : "离线",
			value : "OFFLINE"
		}];
	$("#ddlEvent").ufaDropDownList({
		dataValueField : "value",
		dataTextField : "text",
		optionLabel : "全部",
		dataSource : eventTypes
	});
	$("#dtStartTime").ufaDateTimePicker({
		value : ufa.date.addDays(new Date(), -1),
		format : "{0:yyyy-MM-dd HH:mm:ss}"
	});
	$("#dtEndTime").ufaDateTimePicker({
		value : new Date(),
		format : "{0:yyyy-MM-dd HH:mm:ss}"
	});
	function getparameters(){
		var params =  {
			startLogTime:$("#dtStartTime").data("ufaDateTimePicker").value(),
			endLogTime:$("#dtEndTime").data("ufaDateTimePicker").value()
		};
		if($("#txtNodeGroup").val()){
			params.group = $("#txtNodeGroup").val();
		}
		if($("#txtNode").val()){
			params.identity=$("#txtNode").val();
		}
		if($("#ddlEvent").val()){
			params.event=$("#ddlEvent").val();
		}
		return params;
	}
	$("#gdNodeLogger").loading();
	$("#gdNodeLogger").ufaGrid({
		columns : [{
			field : "logTime",
			width : 100,
			attributes:{title:"#:new Date(logTime).Format('yyyy-MM-dd hh:mm:ss')#"},
			title : "记录时间",
			format : "{0:yyyy-MM-dd HH:mm:ss}",
		}, {
			field : "event",
			width : 100,
			attributes:{title:"#:switchEv(event)#"},
			title : "事件",
			values: eventTypes
		}, {
			field : "clusterName",
			width : 100,
			attributes:{title:"#:clusterName#"},
			title : "集群名称"
		}, {
			field : "nodeType",
			width : 100,
			attributes:{title:"#:nodeType#"},
			title : "节点类型"
		}, {
			field : "group",
			width : 100,
			attributes:{title:"#:group#"},
			title : "节点组名"
		}, {
			field : "identity",
			width : 100,
			attributes:{title:"#:identity#"},
			title : "节点标识"
		}, {
			field : "createTime",
			width : 100,
			attributes:{title:"#:new Date(createTime).Format('yyyy-MM-dd hh:mm:ss')#"},
			title : "节点创建时间",
			format : "{0:yyyy-MM-dd HH:mm:ss}",
		}, {
			field : "ip",
			width : 100,
			attributes:{title:"#:ip#"},
			title : "机器"
		}, {
			field : "httpCmdPort",
			width : 100,
			attributes:{title:"#:httpCmdPort#"},
			title : "CMD端口"
		}, {
			field : "threads",
			width : 100,
			attributes:{title:"#:threads#"},
			title : "工作线程数"
		}],
		dataBinding: onDataBinding,
		groupable : false,
		sortable : false,
		resizable : true,
		pageable : {
			refresh : true,
			pageSizes : true,
			pageSizes : [10, 20, 30],
			buttonCount : 5,
			messages : {
				display : "显示{0}-{1}条，共{2}条",
				empty : "没有数据",
				page : "页",
				itemsPerPage : "条/页",
				first : "第一页",
				previous : "前一页",
				next : "下一页",
				last : "最后一页",
				refresh : "刷新"
			}
		},
		dataSource : {
			transport : {
				read : {
					url : 'api/node/node-onoffline-log-get',
					data : function() {
						return getparameters();
					},
					type : "POST",
					dataType : "json",
				}
			},
			pageSize : 30,
			serverPaging : true,
			serverFiltering : false,
			serverSorting : false,
			schema : {
				"data" : "data",
				"total" : "total",
				"errors" : "errors",
			}
		}
	});

	$("#btnSearch").bind("click", function() {
		$("#gdNodeLogger").loading();
		$("#gdNodeLogger").data().ufaGrid.dataSource.read();
	});
	
	$('.header_tab a').each(function(){
		$('.header_tab a').removeClass('tab_active').eq(2).addClass("tab_active").html("节点上下线日志")
	});
	function onDataBinding(arg) {
    	gridHeight();
    	if(this.dataSource._data && this.dataSource._data.length>0){
    		$("#gdNodeLogger").complete().noInfo();
    	}else{
    		$("#gdNodeLogger").complete().info('查询没有数据');
    	}
    }
	//表格高度自适应
	function gridHeight() {
	    var H = $(window).height() - 316 + "px";
	    $('.k-grid-content').css('height', H)
	}
    $(window).resize(function () {
        gridHeight()
    })
});