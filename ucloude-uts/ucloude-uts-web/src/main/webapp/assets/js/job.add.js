/**
 * 添加任务
 */
require(["jquery", "common", "ufa.dropdownlist", "ufa.mobile.switch",
		"ufa.window", "ufa.numerictextbox", "ufa.datetimepicker", "ufa.dialog",
		"messages/ufa.messages.zh-CN", "cultures/ufa.culture.zh-CN"], function(
		$, ufa) {
	ufa.culture("zh-CN");
	console.log("job.add");
	ufa.ui.Confirm.prototype.options.messages = $.extend(true,
			ufa.ui.Confirm.prototype.options.messages, {
				"okText" : "确定",
				"cancel" : "取消",
				"title" : "警告"
			});
	ufa.ui.Alert.prototype.options.messages = $.extend(true,
			ufa.ui.Alert.prototype.options.messages, {
				"okText" : "确定"
			});
	$("#jobType").ufaDropDownList({
		dataValueField : "value",
		dataTextField : "name",
		dataSource : {
			data : jobTypes
		},
		change : function(e) {
			jobTypeChange();
		}
	});
	$("#shopId").ufaDropDownList({
		dataValueField : "id",
		dataTextField : "id",
		optionLabel : "请选择",
		dataSource : {
			transport : {
				read : {
					url : 'api/job-queue/shop-id-get',
					type : "get",
					dataType : "json"
				}
			},
			schema : {
				"data" : "rows"
			}
		}
	});
	$("#taskTrackerNodeGroup").ufaDropDownList({
		dataValueField : "name",
		dataTextField : "name",
		optionLabel : "请选择",
		change : function() {
			if ($("#taskTrackerNodeGroup").val() != "") {
				$(".registerHint").hide();
			}
		},
		dataSource : {
			data : taskTrackerNodeGroups
		}
	});
	$("#submitNodeGroup").ufaDropDownList({
		dataValueField : "name",
		dataTextField : "name",
		optionLabel : "请选择",
		dataSource : {
			data : jobClientNodeGroups
		}
	});
	$("#maxRetryTimes").ufaNumericTextBox({
		format : "n0"
	});
	$("#priority").ufaNumericTextBox({
		format : "n0"
	});
	$("#repeatInterval").ufaNumericTextBox();
	$("#repeatCount").ufaNumericTextBox();
	$("#triggerTime").ufaDateTimePicker();
	$("#needFeedback").ufaMobileSwitch({
		onLabel : "需要",
		offLabel : "不需要"
	});

	$("#relyOnPrevCycle").ufaMobileSwitch({
		onLabel : "是",
		offLabel : "否"
	});

	$("#cronWindow").ufaWindow({
		title : "周期表达式编辑工具",
		width : 800,
		height : 560,
		footer : {
			submit : true,
			cancel : true
		},
		confirm : function() {
			$("#cronExpression").val($("#cron").val());
			this.close();
		}
	});
	getJobInfo(function(jobInfo) {
		$("#resetBtn").bind("click", function() {
			reset(jobInfo);
		});
		reset(jobInfo);
		$("#addBtn").bind("click", saveJob);
		$("#cronGeneratorBtn").bind("click", function() {
			$("#cronWindow").data("ufaWindow").center().open();
		});
	});

	function getJobInfo(callBack) {
		var jobqueue = $("#jobqueue").val();
		var jobId = $("#jobId").val();
		if (jobqueue && jobId) {
			var url;
			if (jobqueue == 'cron') {
				url = "api/job-queue/cron-job-getById";
			} else if (jobqueue == 'repeat') {
				url = "api/job-queue/repeat-job-getById";
			} else if (jobqueue == 'suspend') {
				url = "api/job-queue/suspend-job-getById";
			} else if (jobqueue == 'executing') {
				url = "api/job-queue/executing-job-getById";
			} else if (jobqueue == 'executable') {
				url = "api/job-queue/executable-job-getById";
			} else {
				ufa.alert('系统提示', '无效的任务类型');
				return;
			}
			$.ajax({
				type : "get",
				url : url,
				data : {
					jobId : jobId
				},
				dataType : "json",
				success : function(obj) {
					if (obj.success) {
						callBack(obj.rows[0]);
					} else {
						ufa.alert('系统提示', obj.msg);
					}
				},
				error : function() {
					ufa.alert('系统提示', "获取任务内容异常。");
				}
			});
		} else {
			callBack();
		}
	}
});

function jobTypeChange() {
	$("#ulJobInfo li[group]").hide();
	$("#liCron").hide();// 周期
	$("#liRepeatInterval").hide();// 间隔
	$("#liTrigger").hide();// 定时
	$("#liRepeatCount").hide();// 重复
	$("#liRely").hide();// 依赖周期
	var jobType = $("#jobType").val();
	if (jobType == 1) {
		$("#liTrigger").show();// 定时
	} else if (jobType == 2) {
		$("#liCron").show();// 周期
		$("#liRely").show();
	} else if (jobType == 3) {
		$("#liTrigger").show();
		$("#liRepeatInterval").show();
		$("#liRepeatCount").show();
		$("#liRely").show();
	}
}

// 参数校验
function checkParam(validate) {
	var jobInfo = {
		jobId : $("#jobId").val(),
		realTaskId : $("#realTaskId").val(),
		taskId : $("#taskId").val(),
		jobType : $("#jobType").val(),
		cronExpression : $("#cronExpression").val(),
		repeatInterval : $("#repeatInterval").val(),
		repeatCount : $("#repeatCount").val(),
		relyOnPrevCycle : $("#relyOnPrevCycle").data("ufaMobileSwitch").value(),
		needFeedback : $("#needFeedback").data("ufaMobileSwitch").value(),
		priority : $("#priority").val(),
		maxRetryTimes : $("#maxRetryTimes").val(),
		submitNodeGroup : $("#submitNodeGroup").val(),
		taskTrackerNodeGroup : $("#taskTrackerNodeGroup").val(),
		extParams : {}
	};

	if (!jobInfo.taskId || jobInfo.taskId.length > 32) {
		$('.idHint').show().html('任务ID不允许为空，长度不超过32');
		validate.check = false;
	} else {
		$('.idHint').hide();
	}
	if (jobInfo.taskTrackerNodeGroup == "") {
		$(".registerHint").show().html("请选择执行节点");
		validate.check = false;
	} else {
	}
	var triggerTime = $("#triggerTime").data("ufaDateTimePicker").value();
	if (jobInfo.jobType == 1) {// 定时任务
		if (triggerTime == "" || triggerTime <= new Date()) {
			$(".triggerHint").show().html("必须大于当前时间")
			validate.check.check = false;
		} else {
			jobInfo.triggerTime = triggerTime;
			$(".triggerHint").hide()
		}
	} else if (jobInfo.jobType == 2) {// 周期任务
		if (!jobInfo.cronExpression) {
			$('.cronHint').show().html("请选择周期")
			validate.check = false;
		} else {
			$('.cronHint').hide()
		}
	} else if (jobInfo.jobType == 3) {// 重复任务
		if (triggerTime == "" || triggerTime <= new Date()) {
			$(".triggerHint").show().html("触发时间不允许为空,且必须大于当前时间")
			validate.check = false;
		} else {
			jobInfo.triggerTime = triggerTime;
			$(".triggerHint").hide()
		}

	}
	var json = $("#extParams").val();
	if (json) {
		try {
			jobInfo.extParams = $.parseJSON(json);
		} catch (e) {
			// alert("参数必须为Json格式。");
			$(".extHint").show().html("参数必须为Json格式")
		}
		for ( var item in jobInfo.extParams) {
			var type = typeof jobInfo.extParams[item];
			if (type != "string" && type != "number" && type != "boolean") {
				// alert("参数只支持key:value 结构。");
				$(".extHint").show().html("参数只支持key:value 结构")
				validate.check = false;
			}
		}
	}
	if ($("#shopId").val()) {
		$(".shopIdHint").hide();
		jobInfo.extParams.shopId = $("#shopId").val();
	}else{
		$(".shopIdHint").show().text("业务类型不允许为空！");
		validate.check = false;
	}
	return jobInfo;
}

// 保存任务
function saveJob() {
	var validate = {
		check : true
	};
	var jobInfo = checkParam(validate);
	if (!validate.check) {
		return false;
	} else {
		// $('form').submit();
		// alert('提交成功')
	}
	$("#page-wrapper").loading();
	// var = checkParam();
	// if (!jobInfo) {
	// return;
	// }
	var url = "api/job-queue/job-add";
	if (jobInfo.jobId) {
		// 编辑保存
		var jobqueue = $("#jobqueue").val();
		if (jobqueue == 'cron') {
			url = "api/job-queue/cron-job-update";
		} else if (jobqueue == 'repeat') {
			url = "api/job-queue/repeat-job-update";
		} else if (jobqueue == 'suspend') {
			url = "api/job-queue/suspend-job-update";
		} else if (jobqueue == 'executing') {
			url = "api/job-queue/executing-job-update";
		} else if (jobqueue == 'executable') {
			url = "api/job-queue/executable-job-update";
		}
	}
	$.ajax({
		type : "post",
		url : url,
		data : jobInfo,
		dataType : "json",
		success : function(obj) {
			if (obj.success) {
				$("#page-wrapper").complete();
				if (jobInfo.jobId) {
					$("#editWindow").data().ufaWindow.close();
					$("#gdJobs").data().ufaGrid.dataSource.read();
				} else {
					ufa.alert('系统提示', "保存成功").then(function() {
						window.location.href = "job-add.htm";
					});
				}
			} else {
				$("#page-wrapper").complete();
				ufa.alert('系统提示', obj.msg);
			}
		},
		error : function() {
			$("#page-wrapper").complete();
			ufa.alert('系统提示', "保存异常，请重试!");
		}
	});
}

// 重置
function reset(jobInfo) {
	if (jobInfo) {
		$("#taskId").attr("readonly", "readonly");
		$("#jobType").data("ufaDropDownList").readonly(true);
		$("#shopId").data("ufaDropDownList").readonly(true);
		$("#realTaskId").val(jobInfo.realTaskId);
		$("#taskId").val(jobInfo.taskId);
		var jobType = jobTypes.filter(function(value) {
			return value.code == jobInfo.jobType;
		})[0];
		$("#jobType").data("ufaDropDownList")
				.value(jobType ? jobType.value : 0);
		jobTypeChange();
		$("#triggerTime").data("ufaDateTimePicker").value(
				new Date(jobInfo.triggerTime));
		$("#cronExpression").val(jobInfo.cronExpression);
		$("#repeatInterval").data("ufaNumericTextBox").value(
				jobInfo.repeatInterval);
		$("#repeatCount").data("ufaNumericTextBox").value(jobInfo.repeatCount);
		$("#relyOnPrevCycle").data("ufaMobileSwitch").value(
				jobInfo.relyOnPrevCycle);
		$("#needFeedback").data("ufaMobileSwitch").value(jobInfo.needFeedback);
		$("#priority").data("ufaNumericTextBox").value(jobInfo.priority);
		$("#maxRetryTimes").data("ufaNumericTextBox").value(
				jobInfo.maxRetryTimes);
		$("#submitNodeGroup").data("ufaDropDownList").value(
				jobInfo.submitNodeGroup);
		$("#taskTrackerNodeGroup").data("ufaDropDownList").value(
				jobInfo.taskTrackerNodeGroup);
		if (jobInfo.extParams) {
			var tempExtParams = $.extend(true,{},jobInfo.extParams);
			delete tempExtParams["shopId"];
			$("#shopId").data("ufaDropDownList").value(jobInfo.extParams.shopId);
			if(!$.isEmptyObject()){
				$("#extParams").val(JSON.stringify(tempExtParams));
			}
		}
	} else {
		// $("#jobId").val('');
		$("#taskId").val('');
		$("#jobType").data("ufaDropDownList").value(0);
		$("#shopId").data("ufaDropDownList").value('');
		jobTypeChange();
		$("#triggerTime").data("ufaDateTimePicker").value(null);
		$("#cronExpression").val('');
		$("#repeatInterval").data("ufaNumericTextBox").value(0);
		$("#repeatCount").data("ufaNumericTextBox").value(0);
		$("#relyOnPrevCycle").data("ufaMobileSwitch").value(false);
		$("#needFeedback").data("ufaMobileSwitch").value(false);
		$("#priority").data("ufaNumericTextBox").value(100);
		$("#maxRetryTimes").data("ufaNumericTextBox").value(0);
		$("#submitNodeGroup").data("ufaDropDownList").value(null);
		$("#taskTrackerNodeGroup").data("ufaDropDownList").value(null);
		$("#extParams").val('');
	}
}
