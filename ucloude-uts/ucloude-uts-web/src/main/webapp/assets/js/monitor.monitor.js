/**
 * 监控
 */
// 获取并显示任务数
function getJobCount() {
	$.ajax({
		type : "post",
		url : "/ucloude-uts-web/api/monitor/monitor-data-get",
		data : {
			nodeType : $('#nodeType').val(),
			nodeGroup : $('#nodeGroup').val(),
			identity : $("#node").val(),
			startTime : $('#dtBegintime').data("ufaDateTimePicker").value().getTime(),
			endTime : $('#dtEndtime').data("ufaDateTimePicker").value().getTime(),
		},
		dataType : "json",
		success : function(obj) {
			if (obj && obj.length > 0) {
				var nodeType = obj[0].nodeType;
				if (nodeType == "JOB_CLIENT") {
					showJobClientJobCount(obj);
				} else if (nodeType == "JOB_TRACKER") {
					showJobTrackerJobCount(obj);
				} else if (nodeType == "TASK_TRACKER") {
					showTaskTrackerJobCount(obj);
				}
			} else {
				// show No Data
				$('#task').empty().info('查询没有数据');
			}
		}
	});
}

// JobClient任务数
function showJobClientJobCount(obj) {
	var chartSeries = [{
		name : '成功数',
		data : []
	}, {
		name : '失败数',
		data : []
	}, {
		name : '存储错误数',
		data : []
	}, {
		name : '提交错误数',
		data : []
	}, {
		name : '处理反馈数',
		data : []
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		chartSeries[0].data.push(obj[item].submitSuccessNum);
		chartSeries[1].data.push(obj[item].submitFailedNum);
		chartSeries[2].data.push(obj[item].failStoreNum);
		chartSeries[3].data.push(obj[item].submitFailStoreNum);
		chartSeries[4].data.push(obj[item].handleFeedbackNum);
	}
	charts({
		id : 'task',
		unit : ['个数'],
		series : chartSeries,
		categories : chartCategories
	});
}

// JobTracker任务数
function showJobTrackerJobCount(obj) {
	var chartSeries = [{
		name : '接收数',
		data : []
	}, {
		name : '分发数',
		data : []
	}, {
		name : '成功数',
		data : []
	}, {
		name : '失败数',
		data : []
	}, {
		name : '延迟数',
		data : []
	}, {
		name : '异常数',
		data : []
	}, {
		name : '修复数',
		data : []
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		chartSeries[0].data.push(obj[item].receiveJobNum);
		chartSeries[1].data.push(obj[item].pushJobNum);
		chartSeries[2].data.push(obj[item].exeSuccessNum);
		chartSeries[3].data.push(obj[item].exeFailedNum);
		chartSeries[4].data.push(obj[item].exeLaterNum);
		chartSeries[5].data.push(obj[item].exeExceptionNum);
		chartSeries[6].data.push(obj[item].fixExecutingJobNum);
	}
	charts({
		id : 'task',
		unit : ['个数'],
		series : chartSeries,
		categories : chartCategories
	});
}

// TaskTracker任务数
function showTaskTrackerJobCount(obj) {
	var chartSeries = [{
		name : '成功数',
		data : []
	}, {
		name : '失败数',
		data : []
	}, {
		name : '延迟数',
		data : []
	}, {
		name : '异常数',
		data : []
	}, {
		name : '总运行时间',
		data : [],
		yAxis : 1
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		chartSeries[0].data.push(obj[item].exeSuccessNum);
		chartSeries[1].data.push(obj[item].exeFailedNum);
		chartSeries[2].data.push(obj[item].exeLaterNum);
		chartSeries[3].data.push(obj[item].exeExceptionNum);
		chartSeries[4].data.push(obj[item].totalRunningTime);
	}
	charts({
		id : 'task',
		unit : ['个数', '秒'],
		series : chartSeries,
		categories : chartCategories
	});
}

// 获取JVM数据 jvmType：数据类型 0：GC 1:内存 3:线程
function getJVMData(jvmType) {
	$.ajax({
		type : "post",
		url : "/ucloude-uts-web/api/monitor/jvm-monitor-data-get",
		data : {
			jvmType : jvmType,
			nodeType : $('#nodeType').val(),
			nodeGroup : $('#nodeGroup').val(),
			identity : $("#node").val(),
			startTime : $('#dtBegintime').data("ufaDateTimePicker").value()
					.getTime(),
			endTime : $('#dtEndtime').data("ufaDateTimePicker").value()
					.getTime(),
		},
		dataType : "json",
		success : function(obj) {
			if (obj && obj.length > 0) {
				var nodeType = obj[0].nodeType;
				if (jvmType == 0) {
					showGCData(obj);
				} else if (jvmType == 1) {
					showMemoryData(obj);
				} else if (jvmType == 3) {
					showThreadData(obj);
				}
			} else {
				// show No Data
				$('#CPU').empty().info('查询没有数据');
				$('#Heap').empty().info('查询没有数据');
				$('#NonHeap').empty().info('查询没有数据');
				$('#PermGen').empty().info('查询没有数据');
				$('#OldGen').empty().info('查询没有数据');
				$('#EdenSpace').empty().info('查询没有数据');
				$('#Survivor').empty().info('查询没有数据');
				$('#GCCount').empty().info('查询没有数据');
				$('#GCTime').empty().info('查询没有数据');
				$('#TheadCount').empty().info('查询没有数据');
			}
		}
	});
}

// JVM GC指标
function showGCData(obj) {
	var GCCountSeries = [{
		name : 'fullGCCollectionCount',
		data : []
	}, {
		name : 'spanFullGCCollectionCount',
		data : []
	}, {
		name : 'spanYoungGCCollectionCount',
		data : []
	}, {
		name : 'youngGCCollectionCount',
		data : []
	}];
	var GCTimeSeries = [{
		name : 'fullGCCollectionTime',
		data : []
	}, {
		name : 'spanFullGCCollectionTime',
		data : []
	}, {
		name : 'spanYoungGCCollectionTime',
		data : []
	}, {
		name : 'youngGCCollectionTime',
		data : []
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		GCCountSeries[0].data.push(obj[item].fullGCCollectionCount);
		GCCountSeries[1].data.push(obj[item].spanFullGCCollectionCount);
		GCCountSeries[2].data.push(obj[item].spanYoungGCCollectionCount);
		GCCountSeries[3].data.push(obj[item].youngGCCollectionCount);
		GCTimeSeries[0].data.push(obj[item].fullGCCollectionTime);
		GCTimeSeries[1].data.push(obj[item].spanFullGCCollectionTime);
		GCTimeSeries[2].data.push(obj[item].spanYoungGCCollectionTime);
		GCTimeSeries[3].data.push(obj[item].youngGCCollectionTime);
	}
	charts({
		id : 'GCCount',
		unit : ['个数'],
		series : GCCountSeries,
		categories : chartCategories
	});
	charts({
		id : 'GCTime',
		unit : ['次数'],
		series : GCTimeSeries,
		categories : chartCategories
	});
}

// JVM 内存指标
function showMemoryData(obj) {
	var heapSeries = [{
		name : 'heapMemoryCommitted',
		data : []
	}, {
		name : 'heapMemoryInit',
		data : []
	}, {
		name : 'heapMemoryMax',
		data : []
	}, {
		name : 'heapMemoryUsed',
		data : []
	}];
	var nonHeapSeries = [{
		name : 'nonHeapMemoryCommitted',
		data : []
	}, {
		name : 'nonHeapMemoryInit',
		data : []
	}, {
		name : 'nonHeapMemoryMax',
		data : []
	}, {
		name : 'nonHeapMemoryUsed',
		data : []
	}];
	var permGenSeries = [{
		name : 'permGenCommitted',
		data : []
	}, {
		name : 'permGenInit',
		data : []
	}, {
		name : 'permGenMax',
		data : []
	}, {
		name : 'permGenUsed',
		data : []
	}];
	var oldGenSeries = [{
		name : 'oldGenCommitted',
		data : []
	}, {
		name : 'oldGenInit',
		data : []
	}, {
		name : 'oldGenMax',
		data : []
	}, {
		name : 'oldGenUsed',
		data : []
	}];
	var edenSpaceSeries = [{
		name : 'edenSpaceCommitted',
		data : []
	}, {
		name : 'edenSpaceInit',
		data : []
	}, {
		name : 'edenSpaceMax',
		data : []
	}, {
		name : 'edenSpaceUsed',
		data : []
	}];
	var survivorSeries = [{
		name : 'survivorCommitted',
		data : []
	}, {
		name : 'survivorInit',
		data : []
	}, {
		name : 'survivorMax',
		data : []
	}, {
		name : 'survivorUsed',
		data : []
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		heapSeries[0].data.push(obj[item].heapMemoryCommitted);
		heapSeries[1].data.push(obj[item].heapMemoryInit);
		heapSeries[2].data.push(obj[item].heapMemoryMax);
		heapSeries[3].data.push(obj[item].heapMemoryUsed);
		nonHeapSeries[0].data.push(obj[item].nonHeapMemoryCommitted);
		nonHeapSeries[1].data.push(obj[item].nonHeapMemoryInit);
		nonHeapSeries[2].data.push(obj[item].nonHeapMemoryMax);
		nonHeapSeries[3].data.push(obj[item].nonHeapMemoryUsed);
		permGenSeries[0].data.push(obj[item].permGenCommitted);
		permGenSeries[1].data.push(obj[item].permGenInit);
		permGenSeries[2].data.push(obj[item].permGenMax);
		permGenSeries[3].data.push(obj[item].permGenUsed);
		oldGenSeries[0].data.push(obj[item].oldGenCommitted);
		oldGenSeries[1].data.push(obj[item].oldGenInit);
		oldGenSeries[2].data.push(obj[item].oldGenMax);
		oldGenSeries[3].data.push(obj[item].oldGenUsed);
		edenSpaceSeries[0].data.push(obj[item].edenSpaceCommitted);
		edenSpaceSeries[1].data.push(obj[item].edenSpaceInit);
		edenSpaceSeries[2].data.push(obj[item].edenSpaceMax);
		edenSpaceSeries[3].data.push(obj[item].edenSpaceUsed);
		survivorSeries[0].data.push(obj[item].survivorCommitted);
		survivorSeries[1].data.push(obj[item].survivorInit);
		survivorSeries[2].data.push(obj[item].survivorMax);
		survivorSeries[3].data.push(obj[item].survivorUsed);
	}
	charts({
		id : 'Heap',
		unit : ['MB'],
		series : heapSeries,
		categories : chartCategories
	});
	charts({
		id : 'NonHeap',
		unit : ['MB'],
		series : nonHeapSeries,
		categories : chartCategories
	});
	charts({
		id : 'PermGen',
		unit : ['MB'],
		series : permGenSeries,
		categories : chartCategories
	});
	charts({
		id : 'OldGen',
		unit : ['MB'],
		series : oldGenSeries,
		categories : chartCategories
	});
	charts({
		id : 'EdenSpace',
		unit : ['MB'],
		series : edenSpaceSeries,
		categories : chartCategories
	});
	charts({
		id : 'Survivor',
		unit : ['MB'],
		series : survivorSeries,
		categories : chartCategories
	});
}

// JVM 线程指标
function showThreadData(obj) {
	var cpuSeries = [{
		name : 'CPU利用率',
		data : []
	}];
	var threadCountSeries = [{
		name : 'daemonThreadCount',
		data : []
	}, {
		name : 'deadLockedThreadCount',
		data : []
	}, {
		name : 'totalStartedThreadCount',
		data : []
	}, {
		name : 'threadCount',
		data : []
	}];
	var chartCategories = [];
	for ( var item in obj) {
		chartCategories.push(new Date(obj[item].timestamp));
		cpuSeries[0].data.push(obj[item].processCpuTimeRate);
		threadCountSeries[0].data.push(obj[item].daemonThreadCount);
		threadCountSeries[1].data.push(obj[item].deadLockedThreadCount);
		threadCountSeries[2].data.push(obj[item].totalStartedThreadCount);
		threadCountSeries[3].data.push(obj[item].threadCount);
	}
	// cpu
	charts({
		id : 'CPU',
		unit : ['%'],
		series : cpuSeries,
		categories : chartCategories
	});
	// 线程数
	charts({
		id : 'TheadCount',
		unit : ['个数'],
		series : threadCountSeries,
		categories : chartCategories
	});
}


require(["jquery","common","ufa.dropdownlist", "ufa.datetimepicker",
	"ufa.mobile.switch","ufa.dialog", "messages/ufa.messages.zh-CN",
		"cultures/ufa.culture.zh-CN"], function($, ufa) {
	ufa.culture("zh-CN");
	$('.chart').info();
	$('#nodeType').ufaDropDownList({
		dataValueField : "value",
		dataTextField : "text",
		dataSource : {
			data : [{
				value : "0",
				text : "作业调度"
			}, {
				value : "1",
				text : "作业执行"
			}, {
				value : "2",
				text : "作业提交"
			}]
		},
		change : function() {
			$('#nodeGroup').data("ufaDropDownList").dataSource.read();
		}
	});
	$('#nodeGroup').ufaDropDownList({
		optionLabel : "不限",
		dataTextField : "name",
		dataValueField : "name",
		dataSource : {
			transport : {
				read : {
					url : '/ucloude-uts-web/api/node/node-group-all',
					type : "POST",
					dataType : "json",
					data : function() {
						return {
							nodeType : $("#nodeType").val()
						};
					}
				}
			},
			requestEnd : function(e) {

			}
		}
	});
	$('#dtBegintime').ufaDateTimePicker({
		value : new Date(new Date().getTime() - 7 * 24 * 3600 * 1000)
	});
	$('#dtEndtime').ufaDateTimePicker({
		value : new Date()
	});
	function onOK(){
		$('#node').focus().select();
	}
	$("#btnSearch").bind("click", function() {
		if($('#node').val()== ""){
			ufa.alert('系统提示','请输入节点标识、必填');
			return;
		}
		$('.chart').empty().loading();
		getJobCount();
		getJVMData(0);
		getJVMData(1);
		getJVMData(3);
	});
	//返回TOP
	$('#returnTop').on('click',function(){
		var speed = 200;
		$('#wrapperScroll').animate({ scrollTop: 0 }, speed);
	})
});

function charts(options) {
	var defaults = {
		id : 'task',
		unit : ['个数'],
		colors : ["#6479fc", "#c7b11e", "#7ea700", "#767371", "#ff9500",
				"#eb0101", "#1bd0dc", "#55BF3B", "#DF5353", "#7798BF",
				"#aaeeee"],
		series : [{
			name : '示例',
			data : []
		}],
		categories : []
	};
	var opts = $.extend(defaults, options);
	var dataTime = formatTimeFn(opts.categories);	
	var chartOption = {
		colors : opts.colors,
		title : {
			text : null
		},
		chart : {},
		xAxis : {
			type : 'datetime',
			lineColor : '#c2cdd7',
			tickWidth:0,
			labels : {
				style : {
					"color" : "#666",
					"fontSize" : "13px",
					"fontWeight" : "normal",
					"fontFamily" : "Arial,宋体"
				}
			},
			title : {
				text : null
			},
			categories : dataTime				
		},
		yAxis : [{
			gridLineDashStyle : 'dash',
			title : {
				text : opts.unit[0]
			},
			gridLineColor : '#e7eaec',
			labels : {
				style : {
					"color" : "#666",
					"fontSize" : "13px",
					"fontWeight" : "normal",
					"fontFamily" : "Arial,宋体"
				}
			},
		}],
		legend : {
			layout : 'horizontal',
			align : 'center',
			verticalAlign : 'bottom'
		},
		plotOptions : {
			series : {
				marker : {
					lineWidth : 0,
					symbol : "circle"
				}
			}
		},
		series : opts.series
	};
	if (opts.unit.length > 1) {
		chartOption.yAxis.push({
			gridLineDashStyle : 'dash',
			title : {
				text : opts.unit[1]
			},
			gridLineColor : '#e7eaec',
			labels : {
				style : {
					"color" : "#666",
					"fontSize" : "13px",
					"fontWeight" : "normal",
					"fontFamily" : "Arial,宋体"
				}
			},
			opposite : true
		});
	}
	Highcharts.chart(opts.id, chartOption);
};
function formatTimeFn(options){
	var dataTime = [];
	for(var i=0;i<options.length;i++){
		var time = options[i];
		var hour = time.getHours()==1?0+time.getHours():time.getHours();
		var minute = time.getMinutes()==1?0+time.getMinutes():time.getMinutes();
		dataTime.push(hour+":"+minute);
	};
	return dataTime;
}