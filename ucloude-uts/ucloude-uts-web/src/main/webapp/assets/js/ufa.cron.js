/**
 * 
 */
require(["jquery", "ufa.tabstrip", "messages/ufa.messages.zh-CN",
		"cultures/ufa.culture.zh-CN"], function($, ufa) {
	ufa.culture("zh-CN");
	buildView();
	$("#cronTabstrip").ufaTabStrip({});
	$("#cronTabstrip input[type=number]").ufaNumericTextBox({
		change : function() {
			selectCurType(this.element[0]);
		}
	});
	function selectCurType(_this){
		$(_this).closest("li").find("input[type=radio]").click();
	}

	var vals = $("input[name^='v_']");
	var cron = $("#cron");
	vals.change(function() {
		var item = [];
		vals.each(function() {
			item.push(this.value);
		});
		cron.val(item.join(" "));
	});
});

function buildView() {
	// 秒
	for (var i = 0; i < 60; i++) {
		var html = '<span><input id="secondItem_' + i + '" type="checkbox" class="k-checkbox k-dt-checkbox" value="'
				+ i + '"><label class="k-checkbox-label" for="secondItem_' + i + '">' + i + '</label></span>';
		$("#secondList").append(html);
	}
	// 分
	for (var i = 0; i < 60; i++) {
		var html = '<span><input id="minItem_' + i + '" type="checkbox"class="k-checkbox k-dt-checkbox" value="' + i
				+ '"><label class="k-checkbox-label" for="minItem_' + i + '">' + i + '</label></span>';
		$("#minList").append(html);
	}
	// 小时
	for (var i = 0; i < 24; i++) {
		var html = '<span><input id="hourItem_' + i + '" type="checkbox"class="k-checkbox k-dt-checkbox" value="' + i
				+ '"><label class="k-checkbox-label" for="hourItem_' + i + '">' + i + '</label></span>';
		$("#hourList").append(html);
	}
	// 日
	for (var i = 1; i < 32; i++) {
		var html = '<span><input id="dayItem_' + i + '" type="checkbox" class="k-checkbox k-dt-checkbox" value="' + i
				+ '"><label class="k-checkbox-label" for="dayItem_' + i + '">' + i + '</label></span>';
		$("#dayList").append(html);
	}
	// 周
	for (var i = 1; i < 8; i++) {
		var html = '<span><input id="weekItem_' + i + '" type="checkbox" class="k-checkbox k-dt-checkbox" value="' + i
				+ '"><label class="k-checkbox-label" for="weekItem_' + i + '">' + i + '</label></span>';
		$("#weekList").append(html);
	}
	// 月
	for (var i = 1; i < 13; i++) {
		var html = '<span><input id="mouthItem_' + i + '" type="checkbox" class="k-checkbox k-dt-checkbox" value="'
				+ i + '"><label class="k-checkbox-label" for="mouthItem_' + i + '">' + i + '</label></span>';
		$("#mouthList").append(html);
	}
	
	function checkLastRadio(){
		$(this).closest("li").prev().find("input[type=radio]").click();
	}
	$("#secondList input[type=checkbox]").click(checkLastRadio);
	$("#minList input[type=checkbox]").click(checkLastRadio);
	$("#hourList input[type=checkbox]").click(checkLastRadio);
	$("#dayList input[type=checkbox]").click(checkLastRadio);
	$("#weekList input[type=checkbox]").click(checkLastRadio);
	$("#mouthList input[type=checkbox]").click(checkLastRadio);
}

/**
 * 每周期
 */
function everyTime(dom) {
	var item = $("input[name=v_" + dom.name + "]");
	item.val("*");
	item.change();
}

/**
 * 不指定
 */
function unAppoint(dom) {
	var name = dom.name;
	var val = "?";
	if (name == "year")
		val = "";
	var item = $("input[name=v_" + name + "]");
	item.val(val);
	item.change();
}

/**
 * 指定
 */
function appoint(dom) {
	var name = dom.name;
	var itemList = $("#"+name+"List").find("input[type=checkbox]");
	var vals=[];
	itemList.each(function() {
		if (this.checked) {
			vals.push(this.value);
		}
	});
	var val = "?";
	if (vals.length > 0 && vals.length < itemList.length) {
		val = vals.join(",");
	} else if (vals.length == itemList.length) {
		val = "*";
	}
	var item = $("input[name=v_" + name + "]");
	item.val(val);
	item.change();
}

/**
 * 周期
 */
function cycle(dom) {
	var name = dom.name;
	var ns = $(dom).parent().find("input[data-role=numerictextbox]");
	var start = ns.eq(0).val();
	var end = ns.eq(1).val();
	var item = $("input[name=v_" + name + "]");
	item.val(start + "-" + end);
	item.change();
}

/**
 * 从开始
 */
function startOn(dom) {
	var name = dom.name;
	var ns = $(dom).parent().find("input[data-role=numerictextbox]");
	var start = ns.eq(0).val();
	var end = ns.eq(1).val();
	var item = $("input[name=v_" + name + "]");
	item.val(start + "/" + end);
	item.change();
}

function lastDay(dom) {
	var item = $("input[name=v_" + dom.name + "]");
	item.val("L");
	item.change();
}

function weekOfDay(dom) {
	var name = dom.name;
	var ns = $(dom).parent().find("input[data-role=numerictextbox]");
	var start = ns.eq(0).val();
	var end = ns.eq(1).val();
	var item = $("input[name=v_" + name + "]");
	item.val(start + "#" + end);
	item.change();
}

function lastWeek(dom) {
	var item = $("input[name=v_" + dom.name + "]");
	var ns = $(dom).parent().find("input[data-role=numerictextbox]");
	var start = ns.eq(0).val();
	item.val(start + "L");
	item.change();
}

function workDay(dom) {
	var name = dom.name;
	var ns = $(dom).parent().find("input[data-role=numerictextbox]");
	var start = ns.eq(0).val();
	var item = $("input[name=v_" + name + "]");
	item.val(start + "W");
	item.change();
}
