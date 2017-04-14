var wnd, KYE_LABEL_MAP = {
	HostName : "主机名",
	LocalIp : "主机IP",
	PID : " 进程PID",
	StartTime : "启动时间",
	InputArguments : "启动参数",
	Arch : "硬件平台",
	AvailableProcessors : "可用CPU个数",
	OSName : "操作系统",
	OSVersion : "操作系统版本",
	FileEncode : "文件编码",
	JVM : "JVM名称",
	JavaVersion : "JavaVersion",
	JavaSpecificationVersion : "JavaSpecVersion",
	JavaHome : "JavaHome",
	JavaLibraryPath : "JavaLibraryPath",
	LoadedClassCount : "当前装载的类总数",
	TotalLoadedClassCount : "总共装载过的类总数",
	UnloadedClassCount : "卸载的类总数",
	TotalCompilationTime : "总共编译时间"
};

require(["jquery", "ufa.datetimepicker", "ufa.grid", "ufa.window",
				"ufa.dialog", "messages/ufa.messages.zh-CN",
				"cultures/ufa.culture.zh-CN"],
		function($, ufa) {

			ufa.culture("zh-CN");
			wnd = $("#jvmInfos").ufaWindow({
				title : "JVM详细信息",
				modal : true,
				visible : false,
				resizable : false,
				width : 940,
				height : 500
			}).data("ufaWindow");
			// 节点类型
			$("#ddlNodeTypes").ufaDropDownList({
				dataValueField : "NodeType",
				dataTextField : "NodeTypeText",
				dataSource : {
					data : [{
						NodeType : "",
						NodeTypeText : "所有"
					}, {
						NodeType : "JOB_CLIENT",
						NodeTypeText : "JOB_CLIENT"
					}, {
						NodeType : "TASK_TRACKER",
						NodeTypeText : "TASK_TRACKER"
					}, {
						NodeType : "MONITOR",
						NodeTypeText : "MONITOR"
					}, {
						NodeType : "JOB_TRACKER",
						NodeTypeText : "JOB_TRACKER"
					}]
				}
			});
			// 状态
			$("#state").ufaDropDownList({
				dataValueField : "NodeType",
				dataTextField : "NodeTypeText",
				dataSource : {
					data : [{
						NodeType : "",
						NodeTypeText : "所有"
					}, {
						NodeType : "JOB_CLIENT",
						NodeTypeText : "JOB_CLIENT"
					}, {
						NodeType : "TASK_TRACKER",
						NodeTypeText : "TASK_TRACKER"
					}, {
						NodeType : "MONITOR",
						NodeTypeText : "MONITOR"
					}, {
						NodeType : "JOB_TRACKER",
						NodeTypeText : "JOB_TRACKER"
					}]
				}
			});
			// 创建时间
			$("#strat_time").ufaDateTimePicker({
				culture : "zh-CN",
				start : "month",
				depth : "month",
				format : "yyyy-MM-dd HH",
				interval : 60,
				value : new Date()
			}).data("ufaDateTimePicker");
			$("#send_time").ufaDateTimePicker({
				culture : "zh-CN",
				start : "month",
				depth : "month",
				format : "yyyy-MM-dd HH",
				interval : 60,
				value : new Date()
			}).data("ufaDateTimePicker");

			function getparameters() {
				var params = {};
				params["identity"] = $("#txtidentity").val();
				params["ip"] = $("#txtIP").val();
				params["Node_Group"] = $("#txtNodeGroup").val();
				params["Node_Type"] = $("#ddlNodeTypes")
						.data("ufaDropDownList").value();
				return JSON.stringify(params);
			}

			$("#gdNodes")
					.ufaGrid(
							{
								columns : [
										{
											field : "identity",
											width : 100,
											title : "节点标识"
										},
										{
											field : "clusterName",
											width : 100,
											title : "集群名称"
										},
										{
											field : "nodeType",
											width : 100,
											title : "节点类型",
										},
										{
											field : "group",
											width : 100,
											title : "节点组名"
										},
										{
											field : "createTime",
											width : 100,
											title : "节点创建时间",
											format : "{0:yyyy-MM-dd HH:mm:ss}"
										},
										{
											field : "ip",
											width : 100,
											title : "机器"
										},
										{
											field : "threads",
											width : 100,
											title : "工作线程数"
										},
										{
											field : "available",
											width : 100,
											title : "状态",
											values : [{
												text : "在线",
												value : true
											}, {
												text : "离线",
												value : false
											}]
										},
										{
											field : "level",
											width : 100,
											title : "监听端口"
										},
										{
											width : 160,
											title : "操作",
											template : '<a class="k-button k-button-icontext k-grid-JVM信息" onclick="viewJVMInfo(this)" href="javascript:"><span></span>JVM信息</a><a class="k-button k-button-icontext k-grid-节点日志" target="_black" href="node-txt-log.htm?identity=#:identity#"><span></span>节点日志</a>'
										}],
								sortable : true,
								filterable : true,
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

							});

			function getDataSource() {
				return new ufa.data.DataSource({
					transport : {
						read : {
							url : 'api/node/node-list-get',
							data : function() {
								return getparameters();
							},
							type : "POST",
							dataType : "json",
							contentType : "application/json"
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
					},
					requestEnd : function(e) {
						console.log(e);
						var data = e.response;
						console.log(data);
					}
				});
			}

			function initDataSource(dataSource) {
				$("#gdNodes").data("ufaGrid").setDataSource(dataSource);
			}

			initDataSource(getDataSource());

			$("#btnSearch").on("click", function() {
				initDataSource(getDataSource());
			});

		});

function viewJVMInfo(_this) {
	var dataItem = $("#gdNodes").data("ufaGrid").dataItem(
			$(_this).closest("tr"));
	var identity = dataItem.identity;
	$.ajax({
		url : 'api/jvm/node-jvm-info-get.do',
		type : 'GET',
		dataType : 'JSON',
		data : {
			identity : identity
		},
		success : function(json) {
			if (json && json['success'] && json['results'] == 1) {
				var data = JSON.parse(json.rows[0]);
				$.each(data, function(key, value) {
					if (key == 'StartTime') {
						value = ufa.toString(value, "yyyy-mm-dd HH:mm:ss");
					}
				});
				showJVMInfo(identity, data);
			} else {
				ufa.alert('系统提示', "获取失败:" + json['msg']);
			}
		}
	});
}

function showJVMInfo(identity, data) {
	console.log(data);
	var jvmDetailsTemplate = ufa.template($("#template").html());
	wnd.content(jvmDetailsTemplate({
		identity : identity,
		data : data,
		KYE_LABEL_MAP : KYE_LABEL_MAP
	}));
	wnd.center().open();
	$("#cloud-unit tbody tr:odd").addClass("even");
}