/**
 * 周期队列
 */
require([ "jquery","common","ufa.dropdownlist", "ufa.grid", "ufa.mobile.switch","ufa.dialog",
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
				optionLabel:"全部",
				dataSource : jobClients
			});
			$("#swtichFeedBackClient").ufaMobileSwitch({
				onLabel : "是",
				offLabel : "否",
			});
			$("#ddlTaskTrackeNodeGroup").ufaDropDownList({
				dataValueField : "name",
				dataTextField : "name",
				optionLabel:"全部",
				dataSource : taskTracks
			});
			$("#gdJobs").loading();
			$("#gdJobs").ufaGrid(
					{
						dataSource : {
							transport : {
								read : {
									url : 'api/job-queue/cron-job-get',
									data : function() {
										var params = {
											taskId : $("#txtTaskID").val(),
											needFeedback : $(
													"#swtichFeedBackClient")
													.data("ufaMobileSwitch")
													.value(),
											submitNodeGroup : $(
													"#ddlClientNodeGroups")
													.data("ufaDropDownList")
													.value(),
											taskTrackerNodeGroup : $(
													"#ddlTaskTrackeNodeGroup")
													.data("ufaDropDownList")
													.value(),
										};
										return params;
									},
									type : "POST",
									dataType : "json"
								},
							},
							pageSize : 30,
							serverPaging : true,
							serverFiltering : true,
							serverSorting : true,
							schema : {
								"data" : "data",
								"total" : "total",
								"errors" : "errors",
								"model" : {
									fields : {
									 testTime : {
									 type : "date"
									 },
									}
								},
							},
							requestEnd : function(e) {
								if(e.response.data.length == 0){
									$("#gdJobs").complete().info('查询没有数据');
								}else{
									$("#gdJobs").complete().noInfo();
								};								
							}
						},
						columns : [
						{
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
								values: enumEnJobTypes,
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
							field : "maxRetryTimes",
							width : 100,
							title : "最大重试次数"
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
						}, {
							field : "extParams",
							width : 100,
							title : "用户参数"
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
							width : 300,
							command:[{
								text:"日志", //"job-logger.htm?taskId=" + row['taskId'] + "&taskTrackerNodeGroup=" + row['taskTrackerNodeGroup'];
								click:function(e){
									var tr = $(e.target).closest("tr"); 
						            var data = this.dataItem(tr);
									var url = "job-logger.htm?taskId=" + data.taskId + "&taskTrackerNodeGroup=" + data.taskTrackerNodeGroup;
									window.location.href=url;
									
								}
							},{
								text:"编辑", 
								click:function(e){
									var tr = $(e.target).closest("tr"); 
						            var data = this.dataItem(tr);
						            if($("#editWindow").length>0){
						            	$("#editWindow").data().ufaWindow.destroy();
						            	$("#editWindow").remove();
						            }
						            $(document.body).append($("<div id='editWindow'></div"));
						            $("#editWindow").ufaWindow({
				                        width:600,
				                        height:500,
				                        modal: true,
				                        title: "任务编辑",
				                        visible: false,
				                        content: "job-add.htm?jobqueue=cron&jobId="+data.jobId
				                    }).data().ufaWindow.center().open();
								}
							},{
								text:"暂停",
								click:function(e){
									var tr = $(e.target).closest("tr"); 
									var that = this;
									ufa.confirm({title:"系统提示",content:"是否要暂停当前任务？"}).then(function () {
										//点击OK
							            var data = that.dataItem(tr);
										$.ajax({
											type : "post",
											url : "api/job-queue/cron-job-suspend",
											data : {
												jobId:data.jobId,
												realTaskId:data.realTaskId,
												taskTrackerNodeGroup:data.taskTrackerNodeGroup
											},
											dataType : "json",
											success : function(obj) {
												if (obj.success) {
													$("#gdJobs").data().ufaGrid.dataSource.read();
												} else {
													ufa.alert('系统提示',obj.msg);
												}
											},
											error:function(){
												ufa.alert('执行异常，请重试。',obj.msg);
											}
										});
						            });
								}
							},{
								text:"删除",
								click:function(e){
									var tr = $(e.target).closest("tr"); 
						            var data = this.dataItem(tr);
						            ufa.confirm({title:"系统提示",content:"是否删除当前任务？"}).then(function () {
										$.ajax({
											type : "get",
											url : "api/job-queue/cron-job-delete",
										    data : {
												jobId:data.jobId,
												realTaskId:data.realTaskId,
												taskTrackerNodeGroup:data.taskTrackerNodeGroup
											},
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
						} ],
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
				$("#gdJobs").loading();
				$("#gdJobs").data().ufaGrid.dataSource.read();
			});
			$("#btnReset").bind("click", function(e) {
				 $("#txtTaskID").val('');
				 $("#swtichFeedBackClient").data("ufaMobileSwitch").value('');
				 $("#ddlClientNodeGroups").data("ufaDropDownList").value('');
				 $("#ddlTaskTrackeNodeGroup").data("ufaDropDownList").value('');
			});
		});