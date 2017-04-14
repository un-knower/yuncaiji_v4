/**
 * 运行中的任务
 */
require(
		["jquery","common","ufa.dropdownlist", "ufa.grid", "ufa.mobile.switch","ufa.dialog",
				"messages/ufa.messages.zh-CN", "cultures/ufa.culture.zh-CN"],
		function($, ufa) {
			ufa.culture("zh-CN");
			ufa.ui.Confirm.prototype.options.messages =
				$.extend(true, ufa.ui.Confirm.prototype.options.messages,{
				  "okText": "确定",
				  "cancel": "取消",
				  "title":"警告"
			});
			ufa.ui.Alert.prototype.options.messages =
				$.extend(true, ufa.ui.Alert.prototype.options.messages,{
				  "okText": "确定"
			});
			$("#ddlClientNodeGroups").ufaDropDownList({
				dataValueField : "name",
				dataTextField : "name",
				optionLabel : "全部",
				dataSource : jobClients
			});
			$("#swtichFeedBackClient").ufaMobileSwitch({
				onLabel : "是",
				offLabel : "否",
			});
			$("#ddlTaskTrackeNodeGroup").ufaDropDownList({
				dataValueField : "name",
				dataTextField : "name",
				optionLabel : "全部",
				dataSource : taskTracks
			});
			$("#ddlTaskType").ufaDropDownList({
				dataValueField : "value",
				dataTextField : "text",
				optionLabel:"全部",
				dataSource : {
					data : jobTypes
				}
			});
			$("#gdJobs")
					.ufaGrid(
							{
								dataSource : {
									transport : {
										read : {
											url : 'api/job-queue/executing-job-get',
											data : function() {
												var params = {
													taskId : $("#txtTaskID")
															.val(),
													needFeedback : $(
															"#swtichFeedBackClient")
															.data(
																	"ufaMobileSwitch")
															.value(),
													submitNodeGroup : $(
															"#ddlClientNodeGroups")
															.data(
																	"ufaDropDownList")
															.value(),
													taskTrackerNodeGroup : $(
															"#ddlTaskTrackeNodeGroup")
															.data(
																	"ufaDropDownList")
															.value()
												};
												var jobType = $("#ddlTaskType").data().ufaDropDownList.value();
												if (jobType||jobType=="0") {
													params.jobType = jobType
												}
												return params;
											},
											type : "POST",
											dataType : "json"
										},
									},
									pageSize : 30,
									serverPaging : true,
									serverFiltering : false,
									serverSorting : false,
									schema : {
										"data" : "data",
										"total" : "total",
										"errors" : "errors",
										"model" : {
											fields : {
											// triggerTime : {
											// type : "date"
											// },
											}
										}
									}
								},
								columns : [{
									field : "realTaskId",
									width : 100,
									title : "任务ID"
								}, {
									field : "submitNodeGroup",
									width : 100,
									title : "提交节点组"
								}, {
									field : "jobType",
									width : 100,
									title : "类型",
									values:enumEnJobTypes
								}, {
									field : "taskTrackerNodeGroup",
									width : 100,
									title : "执行节点组"
								}, {
									field : "triggerTime",
									width : 150,
									format : "{0:yyyy-MM-dd HH:mm:ss}",
									title : "执行时间"
								}, {
									field : "cronExpression",
									width : 100,
									title : "Cron表达式"
								}, {
									field : "repeatInterval",
									width : 100,
									title : "重复时间间隔"
								}, {
									field : "repeatCount",
									width : 100,
									title : "重复次数"
								}, {
									field : "priority",
									width : 100,
									title : "优先级"
								}, {
									field : "retryTimes",
									width : 100,
									title : "重试次数"
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
									field : "relyOnPrevCycle",
									width : 100,
									title : "依赖上一周期",
									values : [{
										text : "是",
										value : true
									}, {
										text : "否",
										value : false
									}]
//								}, {
//									field : "extParams",
//									width : 100,
//									title : "用户参数"
								}, {
									field : "gmtCreated",
									width : 150,
									format : "{0:yyyy-MM-dd HH:mm:ss}",
									title : "创建时间"
								}, {
									field : "gmtModified",
									width : 150,
									format : "{0:yyyy-MM-dd HH:mm:ss}",
									title : "修改时间"
								}, {
									title : "操作",
									width : 100,
									command:[{
										text:"日志",
										click:function(e){
											var tr = $(e.target).closest("tr"); 
								            var data = this.dataItem(tr);
											var url = "job-logger.htm?taskId=" + data.taskId + "&taskTrackerNodeGroup=" + data.taskTrackerNodeGroup;
											window.location.href=url;
										}
									},{
										text:"终止",
										click:function(e){
											var tr = $(e.target).closest("tr"); 
								            var data = this.dataItem(tr);
								            ufa.confirm({title:"系统提示",content:"是否终止当前任务？"}).then(function () {
												$.ajax({
													type : "get",
													url : "api/job-queue/executing-job-terminate",
												    data : data.jobId,
													dataType : "json",
													success : function(obj) {
														if (obj.success) {
															$("#gdJobs").data().ufaGrid.dataSource.read();
														} else {
															ufa.alert('系统提示',obj.msg);
														}
													},
													error:function(){
														ufa.alert('系统提示',"执行异常，请重试。");
													}
												});
								            });
										}
									}]
								}],
								dataBinding: onDataBinding,
								groupable : false,
								sortable : true,
								resizable : true,
								pageable : {
						            refresh: true, pageSizes: true, 
						            pageSizes: [10, 20, 30],
						            buttonCount: 5,
					                messages: {
					                    display: "显示{0}-{1}条，共{2}条",
					                    empty: "没有数据",
					                    page: "页",			                   
					                    itemsPerPage: "条/页",
					                    first: "第一页",
					                    previous: "前一页",
					                    next: "下一页",
					                    last: "最后一页",
					                    refresh: "刷新"
					                }
								},
							});
			
			$("#btnSearch").bind("click", function(e) {
				$("#gdJobs").data().ufaGrid.dataSource.read();
			});
			$("#btnReset").bind("click", function(e) {
				$("#txtTaskID").val('');
				$("#swtichFeedBackClient").data("ufaMobileSwitch").value('');
				$("#ddlClientNodeGroups").data("ufaDropDownList").value('');
				$("#ddlTaskTrackeNodeGroup").data("ufaDropDownList").value('');
				$("#ddlTaskType").data().ufaDropDownList.value('');
			});
			function onDataBinding(arg) {
            	gridHeight();
            	if(this.dataSource._data && this.dataSource._data.length>0){
            		$("#gdJobs").complete().noInfo();
            	}else{
            		$("#gdJobs").complete().info('查询没有数据');
            	}
            }
			//表格高度自适应
			function gridHeight() {
			    var H = $(window).height() - 260 + "px";
			    $('.k-grid-content').css('height', H)

			}
		    $(window).resize(function () {
		        gridHeight()

		    })
		});