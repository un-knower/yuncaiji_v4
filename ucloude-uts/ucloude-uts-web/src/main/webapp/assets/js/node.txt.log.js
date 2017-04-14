/**
 * 节点文本日志
 */
require(["jquery", "ufa.core", "ufa.pager","ufa.dropdownlist", "ufa.dialog","messages/ufa.messages.zh-CN",
		"cultures/ufa.culture.zh-CN"], function($, ufa) {
	var dataSource = new ufa.data.DataSource({
		transport : {
			read : {
				url : 'api/node/node-txt-log-get',
				data : {
					identity : identity
				},
				type : "get",
				dataType : "json"
			},
		},
		schema : {
			"data" : "data",
			"total" : "total",
			"errors" : "errors"
		},
		requestEnd : function(e) {
			if(!e.response.errors){
				$("#divTxtLog").html(e.response.data[0].replace(/\r\n/g, "<br/>"));
			}else{
				ufa.alert('获取日志错误',e.response.errors);
			}
		},
		pageSize : 2000,
		serverPaging : true
	});

	dataSource.read();
	var pager = $("#divPager").ufaPager({
		dataSource : dataSource,
		refresh: true, pageSizes: true,
        pageSizes: [2000, 5000, 10000],
        buttonCount: 5,
        messages: {
            display: "显示{0}-{1}条",
            empty: "没有数据",
            page: "页",			                   
            itemsPerPage: "条/页",
            first: "第一页",
            previous: "前一页",
            next: "下一页",
            last: "最后一页",
            refresh: "刷新"
        }	
	}).data("ufaPager");
});