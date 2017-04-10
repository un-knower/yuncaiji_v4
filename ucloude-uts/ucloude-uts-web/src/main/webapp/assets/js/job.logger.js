/**
 * 
 */
require(["jquery","common","ufa.datetimepicker", "ufa.dropdownlist", "ufa.grid",
		"messages/ufa.messages.zh-CN", "cultures/ufa.culture.zh-CN"], function(
		$, ufa) {
	ufa.culture("zh-CN");
	var taskId = ufaUtil.getQueryString("taskId");
	var taskTrackerGroup = ufaUtil.getQueryString("taskTrackerNodeGroup");
	if (taskId) {
		$("#txtTaskID").val(taskId);
	}
	if (taskTrackerGroup) {
		$("#ddlNodeGroup").val(taskTrackerGroup);
	}
	var currentDt = ufa.date.addDays(new Date(), -1);
	console.log(currentDt)
	$("#startTime").ufaDateTimePicker({
		value : currentDt,
		format : "{0:yyyy-MM-dd HH:mm:ss}"
	});
	$("#endTime").ufaDateTimePicker({
		value : new Date(),
		format : "{0:yyyy-MM-dd HH:mm:ss}"
	});
	$("#ddlNodeGroup").ufaDropDownList({
		dataTextField : "name",
		dataValueField : "name",
		dataSource : {
			transport : {
				read : {
					url : 'api/node/node-group-all',
					type : "POST",
					dataType : "json"
				}
			},
			requestEnd : function(e) {
				// console.log(e);
			}
		}
	});
	$("#gdJobLogger").loading();
	$("#gdJobLogger").ufaGrid({
		columns : [{
			field : "taskId",
			width : 100,
			title : "任务ID"
		}, {
			field : "jobType",
			width : 100,
			title : "任务类型",
			values : [{
				text : "实时任务",
				value : "0"
			}, {
				text : "定时任务",
				value : "1"
			}, {
				text : "周期性任务",
				value : "2"
			}, {
				text : "重复性任务",
				value : "3"
			}]
		}, {
			field : "logTime",
			width : 150,
			title : "日志记录时间",
			format : "{0:yyyy-MM-dd HH:mm:ss}",
		}, {
			field : "gmtCreated",
			width : 150,
			title : "日志创建时间",
			format : "{0:yyyy-MM-dd HH:mm:ss}",
		}, {
			field : "taskTrackerNodeGroup",
			width : 100,
			title : "执行节点组"
		}, {
			field : "taskTrackerIdentity",
			width : 100,
			title : "执行节点标示"
		}, {
			field : "submitNodeGroup",
			width : 100,
			title : "提交节点组"
		}, {
			field : "logType",
			width : 100,
			title : "日志类型",
			values : [{
				text : "接受任务",
				value : "RECEIVE"
			}, {
				text : "发送任务",
				value : "SENT"
			}, {
				text : "完成任务",
				value : "FINISHED"
			}, {
				text : "重新发送任务结果",
				value : "RESEND"
			}, {
				text : "修复死任务",
				value : "FIXED_DEAD"
			}, {
				text : "业务日志",
				value : "BIZ"
			}, {
				text : "更新",
				value : "UPDATE"
			}, {
				text : "删除",
				value : "DEL"
			}, {
				text : "暂停",
				value : "SUSPEND"
			}, {
				text : "恢复",
				value : "RESUME"
			}]
		}, {
			field : "success",
			width : 100,
			title : "执行结果",
			values : [{
				text : "成功",
				value : true
			}, {
				text : "失败",
				value : false
			}]
		}, {
			field : "level",
			width : 100,
			title : "日志级别",
			values : [{
				text : "调试",
				value : "DEBUG"
			}, {
				text : "信息",
				value : "INFO"
			}, {
				text : "警告",
				value : "WARN"
			}, {
				text : "错误",
				value : "ERROR"
			}]
		}, {
			field : "depPreCycle",
			width : 100,
			title : "依赖上一周期",
			values : [{
				text : "是",
				value : true
			}, {
				text : "否",
				value : false
			}]
		}, {
			field : "priority",
			width : 100,
			title : "优先级"
		}, {
			field : "retryTimes",
			width : 100,
			title : "重试次数"
		}, {

			field : "cronExpression",
			width : 100,
			title : "Cron表达式"
		}, {
			field : "needFeedback",
			width : 100,
			title : "反馈客户端",
			values : [{
				text : "需要",
				value : true
			}, {
				text : "不需要",
				value : false
			}]
		}, {
			field : "extParams",
			width : 400,
			title : "用户参数",
			template: '<a style="color:grey;cursor:pointer" onclick="windowShow(this)">#= extParams?JSON.stringify(extParams):"" # </a>'
			//template : "#= extParams?JSON.stringify(extParams):'' #"
		}, {
			field : "msg",
			width : 100,
			title : "内容",
			template: '<a style="color:grey;cursor:pointer" onclick="windowShow(this)">#=msg# </a>'
		}],
		groupable : false,
		sortable : true,
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
					url : 'api/job-logger/job-logger-get',
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
			},
			requestEnd : function(e) {
				if(e.response.data.length == 0){
					$("#gdJobLogger").complete().info('查询没有数据');;
				}else{
					$("#gdJobLogger").complete().noInfo();
				}
			}
		}
	});

	$("#btnSearch").bind("click", function() {
		$("#gdJobLogger").loading();
		$("#gdJobLogger").data().ufaGrid.dataSource.read();
	});

	$('.header_tab li a').removeClass("tab_active").eq(2).addClass('tab_active').html('任务日志');

	function getparameters() {
		var params = {};
		var startTime = $("#startTime").data("ufaDateTimePicker").value();
		var endTime = $("#endTime").data("ufaDateTimePicker").value();
		params["startLogTime"] = startTime.getTime();
		params["endLogTime"] = endTime.getTime();
		var taskTrackerNodeGroup = $("#ddlNodeGroup").data("ufaDropDownList")
				.value();
		var taskID = $("#txtTaskID").val();
		if (taskTrackerNodeGroup != null && taskTrackerNodeGroup.length > 0)
			params["taskTrackerNodeGroup"] = taskTrackerNodeGroup;
		if (taskID != null && taskID.length > 0)
			params["taskId"] = taskID;
		return params;
	}
	//表格高度自适应
	function gridHeight() {
	    var H = $(window).height() - 310 + "px";
	    $('.k-grid-content').css('height', H)

	}
    $(window).resize(function () {
        gridHeight()

    })
    

});
//弹窗生成
function windowShow(_this) {
	var text = $(_this).html()
	$(".details").html("")
    $("#winWrap").ufaWindow({
        title: "详情",
        width: 600,
        height: 300,
        modal: true,
    }).data("ufaWindow").close();
	
	if(text.trim()!="null"){
		$(".details").html(text);
		$("#winWrap").data('ufaWindow').center().open();
	} 
    
    
}   