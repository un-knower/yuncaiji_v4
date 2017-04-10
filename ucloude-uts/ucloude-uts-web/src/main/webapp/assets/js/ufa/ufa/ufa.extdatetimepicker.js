(function (f, define) {
    define('ufa.extdatetimepicker', ['ufa.core','ufa.datetimepicker', 'ufa.datepicker','ufa.dropdownlist'], f);
}(function () {
	 var __meta__ = {
        id: 'extdatetimepicker',
        name: 'extdatetimepicker utils',
        category: 'framework',
        advanced: true,
        description: 'netree utilities used across components',
        depends: ['core','datetimepicker', 'datepicker','dropdownlist']
    };
    (function ($, undefined) {
    	var ufa = window.ufa;
	    ui = ufa.ui,
	    Widget = ui.Widget,
	    CHANGE = "change",
	    support = ufa.support,  
	    NS = ".ufaExtDateTimePicker",
	    proxy = $.proxy,
	    browser = support.browser,
        isIE8 = browser.msie && browser.version < 9,
        getCulture = ufa.getCulture,
        DATE = Date,
        adjustDST = ufa.date.adjustDST,
        LEFT = "left",
        keys = ufa.keys,
        MS_PER_MINUTE = 60000,
        MS_PER_HOUR = 3600000,
        MS_PER_DAY = 86400000,
         DISABLED = "k-state-disabled",
                PREVARROW = "_prevArrow",
        NEXTARROW = "_nextArrow",
        ns = ".ufaDuplexCalendar",
                ARIA_DISABLED = "aria-disabled",
        ARIA_SELECTED = "aria-selected",
        CLICK = "click" + ns,
         KEYDOWN_NS = "keydown" + ns,
          BLUR = "blur" + ns,
         MOUSEENTER = support.touch ? "touchstart" : "mouseenter",
        MOUSEENTER_WITH_NS = support.touch ? "touchstart" + ns : "mouseenter" + ns,
        MOUSELEAVE = support.touch ? "touchend" + ns + " touchmove" + ns : "mouseleave" + ns,
        ID = "id",
         FOCUSED = "k-state-focused",
        parse = ufa.parseDate,
              HOVER = "k-state-hover",
                SLIDE = "slideIn",
                CHANGE = "change",
                        template = ufa.template,
        NAVIGATE = "navigate",
        VALUE = "value",
        OTHERMONTH = "k-other-month",
        OTHERMONTHCLASS = ' class="' + OTHERMONTH + '"',
        cellTemplate = template('<td#=data.cssClass# role="gridcell" style="padding:3px 0"><span><input type="checkbox" class="k-checkbox k-dt-checkbox" id="#=data.id#" name="\\#" data-#=data.ns#value="#=data.dateString#" style="width:auto;opacity:10" /><label class="k-checkbox-label" for="#=data.id#">#=data.value#</label></td>', { useWithBlock: false }),
        emptyCellTemplate = template('<td role="gridcell">&nbsp;</td>', { useWithBlock: false }),
                     FOCUS = "focus",
                FOCUS_WITH_NS = FOCUS + ns,
                 proxy = $.proxy,
        extend = $.extend,
        views = {
            view1: 0,
            view2: 1,
            view3: 2,
            view4: 3
        },
        datetimepopuTemplate = '<div id="timepanel#= uid #" class="k-ext-timepanel" style="width: 300px;"><div id="k-ext-timeway#= uid #" class="k-ext-timeway">' +
		'# if(multi == 1 && (discrete == 1 || cycle == 1 ) ) {#' +
	'<span class="k-ext-continue">#= title.multitext #</span>' +
	'#} if(discrete ==1) {#' +
	'<span class ="k-ext-discrete">#=title.discretetext #</span>' +
	'#} if(cycle == 1) {#' +
	'<span class ="k-ext-cycle">#= title.cycletext #</span>' +
	'#}#' +
    	'# if(multi == 1) {#' +
	'</div><div id="multitimebox#= uid #" class="k-ext-timebox" style="display:none" />' +
    '#} if(discrete ==1) {#' +
    '<div id="discretetimebox#= uid #" class="k-ext-timebox" style="display:none" />' +
    '#} if(cycle == 1) {#' +
    '<div id="cycletimebox#= uid #" class="k-ext-timebox" style="display:none" />' +
    '#}#' +
	'<div class="k-ext-timeBtn"><button  class="k-button k-primary k-ext-confirm">#= message.confirm #</button><button class="k-button k-primary k-ext-cancel">#= message.cancel #</button></div></div><div>';
	 function beforeDay(timeType) {
        var enddayTime = new Date();
        var startdayTime = ufa.date.addDays(enddayTime, -1);
        //默认获取当前天
        if (timeType == 3) {
            startdayTime = ufa.date.addDays(enddayTime, -7);
        }
        else if (timeType == 4) {
            startdayTime = startdayTime.setMonth(enddayTime - 1);
        }


        startdayTime = new Date(startdayTime.getFullYear(), startdayTime.getMonth(), startdayTime.getDate());
        enddayTime = new Date(enddayTime.getFullYear(), enddayTime.getMonth(), enddayTime.getDate());
        return { StartTime: startdayTime, EndTime: enddayTime };
    }

    function BeforeHour(hour) {
        var time = new Date();
        //5个小时前的时间秒
        var fivehourbefore = time.getTime() - 1000 * 60 * 60 * hour;
        var timefive = new Date();
        //获取默认结束时间（5个小时前）
        timefive.setTime(fivehourbefore);
        var behYear = timefive.getFullYear();
        var behMonth = timefive.getMonth();
        var behDay = timefive.getDate();
        var behhour = timefive.getHours();
        if (behMonth < 10) {
            behMonth = "0" + behMonth;
        }

        if (behDay < 10) {
            behDay = "0" + behDay;
        }
        if (behhour < 10) {
            behhour = "0" + behhour;
        }
        //endhourTime = behYear + "-" + behMonth + "-" + behDay + " " + behhour + ":00";
        return new Date(behYear, behMonth, behDay, behDay);
    }
    /*
     * 开始结束时间，带小时
     */
    var MultiDateTimePicker = Widget.extend({
        _uid: null,
        _startTime: null,
        _endTime: null,
        _v: null,
        init: function (element, options) {
            var that = this;
            Widget.fn.init.call(that, element, options);
            that._uid = new Date().getTime();
            var dayTimePicker = ufa.format("<input id='startTime{0}'/>&nbsp;到&nbsp;", that._uid);
            dayTimePicker = dayTimePicker + ufa.format("<input id='endTime{0}'/>&nbsp;&nbsp;", that._uid);
            $(element).append(dayTimePicker);
            //var beforeValue = BeforeHour(6);
            //var endValue = BeforeHour(5);
            that._startTime = $(ufa.format("#startTime{0}", that._uid)).ufaDateTimePicker({
                value: ufa.date.addDays(new Date(), -7),
                interval: 60,
                format: that.options.format,
                culture: "zh-CN",
            }).data("ufaDateTimePicker");
           
            that._endTime = $(ufa.format("#endTime{0}", that._uid)).ufaDateTimePicker({
                value: new Date(),
                interval: 60,
                format: that.options.format,
                culture: "zh-CN"
            }).data("ufaDateTimePicker");
            that._startTime.bind("change", function () {
                var start = ufa.parseDate(ufa.toString(that._startTime.value(), "yyyy-MM-dd HH")+ ":00");
                var end = ufa.parseDate(ufa.toString(that._endTime.value(), "yyyy-MM-dd HH")+ ":00");
                if (start.getTime() > end.getTime()) {
                    that._startTime.value(new Date());
                    uwayalert("开始时间不能大于结束时间", 1);
                }
            });
            that._endTime.bind("change", function () {
                var end= ufa.parseDate(ufa.toString(this.value(), "yyyy-MM-dd HH")+ ":00");
                var start = ufa.parseDate(ufa.toString(that._startTime.value(), "yyyy-MM-dd HH") + ":00");
                if (start.getTime() > end.getTime()) {
                    that._endTime.value(new Date());
                    uwayalert("开始时间不能大于结束时间", 1);
                }
            });
            if (options.hasOwnProperty("value") && options.value !== undefined) {
                that.value(options.value);
            }
        },
        destroy: function () {
            var that = this,
                element = that.element;
            that._startTime.destroy();
            that._endTime.destroy();
            $(element).empty();
        },
        value: function (v) {
            var that = this,
                field = that.options.field;
            if (v !== undefined) {
                if (v.length > 0) {
                    that._startTime.value(v[0]);
                }
                if (v.length > 1) {
                    that._endTime.value(v[1]);
                }
            }
            else {
                var starthourTime = that._startTime.value();
                var endhourTime = that._endTime.value();
                var text = ufa.toString(starthourTime, that.options.format, that.options.culture) + "~" + ufa.toString(endhourTime, that.options.format, that.options.culture);
                endhourTime = new Date(endhourTime.getTime() + 60 * 60000);
                that._v = {
                    Text: text,
                    filter: {
                        filters: [{
                            field: field,
                            operator: "gte",
                            value: new Date(starthourTime.getFullYear(), starthourTime.getMonth(), starthourTime.getDate(), starthourTime.getHours())
                        }, {
                            field: field,
                            operator: "lt",
                            value: new Date(endhourTime.getFullYear(), endhourTime.getMonth(), endhourTime.getDate(), endhourTime.getHours())
                        }
                        ]
                    }
                };

                return that._v;
            }
        },
        options: {
            format: "yyyy-MM-dd HH:mm",
            field: "start_time",
            culture: "zh-CN",
            //url:"/api/DateTime/GetDiscreteMonth",
            name: "MultiDateTimePicker"
        }
    });
    ui.plugin(MultiDateTimePicker);
    /*
     * 开始结束时间，不带小时
     */
    var MultiDatePicker = Widget.extend({
        _uid: null,
        _startTime: null,
        _endTime: null,  
        _input: null,
        _v: null,
        init: function (element, options) {
            var that = this;
            Widget.fn.init.call(that, element, options);
            that._uid = new Date().getTime();
            var dayPicker = ufa.format("<div id='dayPicker{0}' >", that._uid);
            dayPicker = dayPicker + ufa.format("从<input id='startTime{0}'/>&nbsp;到&nbsp;", that._uid);
            dayPicker = dayPicker + ufa.format("<input id='endTime{0}'/>&nbsp;&nbsp;", that._uid);
            dayPicker = dayPicker + "</div>"
            $(element).append(dayPicker);
            //var beforeday = beforeDay(2);
           
            that._startTime = $(ufa.format("#startTime{0}", that._uid)).ufaDatePicker({
                value: ufa.date.addDays(new Date(), -7),
                format: that.options.format,
                culture: "zh-CN",
                start: that.options.start,
                depth: that.options.depth
            }).data("ufaDatePicker");

            //开始时间按参数规定格式显示
            that._endTime = $(ufa.format("#endTime{0}", that._uid)).ufaDatePicker({
                value: new Date(),
                format: that.options.format,
                culture: "zh-CN",
                start: that.options.start,
                depth: that.options.depth
            }).data("ufaDatePicker");
            that._startTime.bind("change", function () {
                var start = ufa.parseDate(ufa.toString(that._startTime.value(), "yyyy-MM-dd"));
                var end = ufa.parseDate(ufa.toString(that._endTime.value(), "yyyy-MM-dd"));
                if (start.getTime() > end.getTime()) {
                    that._startTime.value(new Date());
                    uwayalert("开始时间不能大于结束时间", 1);
                }
            });
            that._endTime.bind("change", function () {
                var end = ufa.parseDate(ufa.toString(this.value(), "yyyy-MM-dd"));
                var start = ufa.parseDate(ufa.toString(that._endTime.value(), "yyyy-MM-dd") );
                if (start.getTime() > end.getTime()) {
                    that._endTime.value(new Date());
                    uwayalert("开始时间不能大于结束时间", 1);
                }
            });
            if (options.hasOwnProperty("value") && options.value != undefined) {
                that.value(options.value);
            }
        },
        destroy: function () {
            var that = this,
                element = that.element;
            that._startTime.destroy();
            that._endTime.destroy();

        },
        value: function (v) {
            var that = this,
                timetype = that.options.timetype,
                timeway = that._timeway,
                field = that.options.field;

            if (v != undefined) {
                //format:yyyy-MM-dd
                if (v.length > 0) {
                    that._startTime.value(v[0]);
                }
                if (v.length > 1) {
                    that._endTime.value(v[1]);
                }
            }
            else {
                var endTime = ufa.parseDate(ufa.toString(that._endTime.value(), "yyyy-MM-dd"));
                var startTime = ufa.parseDate(ufa.toString(that._startTime.value(), "yyyy-MM-dd"));
                var text = ufa.toString(startTime, that.options.format, that.options.culture) + "~" + ufa.toString(endTime, that.options.format, that.options.culture);
                if (timetype == 2) //天
                    endTime = ufa.date.addDays(endTime, 1);
                if (timetype == 3) //周
                    endTime = ufa.date.addDays(endTime, 7);
                if (timetype == 4) //月
                {
                    var startMon = startTime.getMonth();
                    var startYear = startTime.getFullYear();
                    var month = endTime.getMonth();
                    var year = endTime.getFullYear();
                    month = month + 1;
                    if (month > 11) {
                        year = year + 1;
                        month = 0
                    }
                    endTime = new Date(year, month, 1);
                    startTime = new Date(startYear, startMon, 1);
                }

                if (timetype == 5) //年
                {
                    var year = endTime.getFullYear() + 1;
                    endTime = new Date(year, 0, 1);
                }


                that._v = {
                    Text: text,
                    filter: {
                        filters: [{
                            field: field,
                            operator: "gte",
                            value: startTime
                        }, {
                            field: field,
                            operator: "lt",
                            value: endTime
                        }
                        ]
                    }
                };

                return that._v;
            }
        },
        options: {
            format: "yyyy-MM-dd",
            field: "start_time",
            culture: "zh-CN",
            start: "month", //"year" year-month-firstday,"month" year-month-day, "week" year-week 
            depth: "month",  //"year"  year-month-firstday,"month" year-month-day, "week", year-month-firstweekday
            name: "MultiDatePicker"
        }
    });
    ui.plugin(MultiDatePicker);
	     function mousetoggle(e) {
        $(this).toggleClass(HOVER, MOUSEENTER.indexOf(e.type) > -1 || e.type == FOCUS);
    }

    function setDate(date, value, multiplier) {
        value = value instanceof DATE ? value.getFullYear() : date.getFullYear() + multiplier * value;
        date.setFullYear(value);
    }

    function getMap() {
        var map_ = new Object();
        map_.put = function (key, value) {
            map_[key + '_'] = value;
        };
        map_.clear = function () {
            map_ = new Object();
        };
        map_.get = function (key) {
            return map_[key + '_'];
        };
        map_.remove = function (key) {
            delete map_[key + '_'];
        };
        map_.keyset = function () {
            var ret = "";
            for (var p in map_) {
                if (typeof p == 'string' && p.substring(p.length - 1) == "_") {
                    ret += ",";
                    ret += p.substring(0, p.length - 1);
                }
            }
            if (ret == "") {
                return ret.split(",");
            } else {
                return ret.substring(1).split(",");
            }
        };
        return map_;
    }

    /*
       * 多选框
       */
    var DuplexCalendar = Widget.extend({
        _uid: null,
        _datePicker: null,
        _map: getMap(),
        //_key:null,
        init: function (element, options) {
            var that = this,
                value,
                id;
            ufa.ui.Widget.fn.init.call(that, element, options);
            that._uid = new Date().getTime();
            element = that.wrapper = that.element;
            options = that.options,
            timetype = options.timetype,
            timeway = options.timeway;
            if (timeway == 2 || (timeway == 3 && (timetype == 1 || timetype == 2))) {
                options.url = window.unescape(options.url);
                that._header();
                that._templates();
                id = element
                            .addClass("k-widget k-ext-calendar").attr(ID);
                if (value != undefined && value.length > 0) {
                    var parseValue = [];
                    for (var i = 0, n = value.length; i < n; i++) {
                        parseValue.push(parse(options.value[i], options.format, options.culture))
                    }
                    value = parseValue;
                }




                that._addClassProxy = function () {
                    that._active = true;
                    //that._cell.addClass(FOCUSED);
                };

                that._removeClassProxy = function () {
                    that._active = false;
                    //that._cell.removeClass(FOCUSED);
                };
                that._index = views[ufa.format("view{0}", options.timetype)];
                that._current = new DATE(+restrictValue(value, options.min, options.max));

                that.navigate();
            }

        },

        _header: function () {
            var that = this,
                element = that.element,
                options = that.options,
                timeway = that.options.timeway,
                timetype = that.options.timetype,
                links;
            var start = "month";
            var depth = "month";
            var format = "yyyy-MM-dd"
            var temptype = 2;
            if (timetype == 2)//天
            {
                start = "year";
                depth = "year";
                format = "yyyy-MM"
                temptype = 4;
            }
            else if (timetype == 3 || timetype == 4)//周,月
            {
                start = "decade";
                depth = "decade";
                format = "yyyy";
                temptype = 5
            }
            if (!element.find(".k-header")[0]) {
                if (timeway == 2) //离散
                {
                    element.html('<div class="k-header">' +
                                 '<a href="#" role="button" class="k-link k-nav-prev"><span class="k-icon k-i-arrow-w"></span></a>' +
                                 ufa.format('<input id="datepicker{0}" />', that._uid) +
                                 '<a href="#" role="button" class="k-link k-nav-next"><span class="k-icon k-i-arrow-e"></span></a>' +
                                 '<a href="#" role="button" class="k-link"><span>清除</span></a>' +
                                 '</div>');

                    that._datePicker = $(ufa.format("#datepicker{0}", that._uid)).ufaDatePicker({
                        depth: depth,
                        start: start,
                        format: format,
                        culture:"zh-CN"
                    }).data("ufaDatePicker");
                    that._datePicker.value(new Date());
                    that._datePicker.bind("open", function () {
                        var result = that._view.getResult(2);
                        if (result.length > 0) {
                            if ($.inArray(ufa.toString(this._old, format), that._map.keyset()) > -1) {
                                that._map.remove(ufa.toString(this._old, format));
                            }
                            that._map.put(ufa.toString(this._old, format), result);
                        }
                    });
                    that._datePicker.bind("change", function () {
                        var value = this.value();
                        that.navigate(value);
                    });
                    links = element.find(".k-link")
                              .on(MOUSEENTER_WITH_NS + " " + MOUSELEAVE + " " + FOCUS_WITH_NS + " " + BLUR, mousetoggle)
                              .click(false);
                    that[PREVARROW] = links.eq(0).on(CLICK, function () {
                        that._active = that.options.focusOnNav !== false;



                        that.navigateToPast();
                    });
                    that[NEXTARROW] = links.eq(1).on(CLICK, function () {
                        that._active = that.options.focusOnNav !== false;
                        that.navigateToFuture();
                    });
                    that[NEXTARROW] = links.eq(2).on(CLICK, function () {
                        that._map.clear();
                        var value = that._datePicker.value();
                        that.navigate(value);
                    });

                }
                else if (timeway == 3 && (timetype == 1 || timetype == 2)) //周期
                {
                    element.html(ufa.format('<div class="k-header">' +
                                    ufa.format(' <div id="datePicker{0}" />  ', that._uid) +
                                 '</div>'));
                    that._datePicker = $(ufa.format("#datePicker{0}", that._uid)).ufaMultiDatePicker({
                        depth: depth,
                        start: start,
                        format: format,
                        timetype: temptype
                    }).data("ufaMultiDatePicker");
                    that._datePicker.value([new Date(), new Date()]);
                }

            }
        },
        datePicker: function () {
            return _datePicker;
        },
        navigate: function (value, view, value2) {
            view = isNaN(view) ? views[view] : view;
            var that = this,
                   options = that.options,
                   culture = options.culture,
                   timeway = options.timeway,
                   timetype = options.timetype,
                   min = options.min,
                   max = options.max,
                   title = that._title,
                   from = that._table,
                   old = that._oldTable,
                   selectedValue = that._value,
                   currentValue = that._current,
                   content = that.month.content,
                   empty = that.month.empty,
                   vertical = view !== undefined && view !== that._index,
                   to, currentView, compare,
                   html = '<table id="{0}" tabindex="0" role="grid" class="k-content k-meta-view" cellspacing="0"><tbody><tr role="row">',
                   disabled;


            if (!value) {
                value = ufa.date.addDays(currentValue, -1);
            }
            if (timeway == 3) {
                if (!value2)
                    value2 = currentValue;
            }
            future = value && +value > +currentValue;
            if (view === undefined) {
                view = that._index;
            } else {
                that._index = view;
            }
            that._view = currentView = duplexcalender.views[view];

            if (from && old && old.data("animating")) {
                old.ufaStop(true, true);
                from.ufaStop(true, true);
            }

            that._oldTable = from;
            if (!from || that._changeView) {
                that._datePicker.value(value);
                that._table = to = $(currentView.content(extend({
                    min: min,
                    max: max,
                    value: value,
                    value1: value2,
                    url: options.url,
                    dates: options.dates,
                    format: options.format,
                    timeway: timeway,
                    timetype: timetype,
                    culture: culture,
                }, that[currentView.name])));
                

                makeUnselectable(to);
                if (timeway == 2) {
                    var format = "yyyy-MM-dd";
                    if (timetype == 2)
                        format = "yyyy-MM";
                    else
                        format = "yyyy";
                    var temp10 = ufa.toString(value, format);
                    var discreateDate = that._map.get(temp10);
                    //that._view.setResult(timeway, discreateDate);
                    if (discreateDate !== undefined && discreateDate.length > 0)
                    {
                        setTableCheckBox(to, discreateDate);
                    }
                    
                }
                that._animate({
                    from: from,
                    to: to,
                    vertical: vertical,
                    future: future
                });
                

                //that._focus(value);
                that.trigger(NAVIGATE);
            }
            //that._class(FOCUSED, currentView.toDateString(value));
            if (!from && that._cell) {
                that._cell.removeClass(FOCUSED);
            }
            that._changeView = true;
        },
        _class: function (className, value) {
            var that = this,
                id = that._cellID,
                cell = that._cell;

            if (cell) {
                cell.removeAttr(ARIA_SELECTED)
                    .removeAttr("aria-label")
                    .removeAttr(ID);
            }

            cell = that._table
                       .find("td:not(." + OTHERMONTH + ")")
                       .removeClass(className)
                       .filter(function () {
                           return $(this.firstChild).attr(ufa.attr(VALUE)) === value;
                       })
                       .attr(ARIA_SELECTED, true);

            if (className === FOCUSED && !that._active && that.options.focusOnNav !== false) {
                className = "";
            }

            cell.addClass(className);

            if (cell[0]) {
                that._cell = cell;
            }

            if (id) {
                cell.attr(ID, id);
                that._table.removeAttr("aria-activedescendant").attr("aria-activedescendant", id);
            }
        },

        _animate: function (options) {
            var that = this,
                from = options.from,
                to = options.to,
                active = that._active;

            if (!from) {
                to.insertAfter(that.element[0].firstChild);
                that._bindTable(to);
            } else if (from.parent().data("animating")) {
                from.off(ns);
                from.parent().ufaStop(true, true).remove();
                from.remove();

                to.insertAfter(that.element[0].firstChild);
                that._focusView(active);
            } else if (!from.is(":visible") || that.options.animation === false) {
                to.insertAfter(from);
                from.off(ns).remove();

                that._focusView(active);
            } else {
                that[options.vertical ? "_vertical" : "_horizontal"](from, to, options.future);
            }
        },
        _templates: function () {
            var that = this,
                options = that.options,
                footer = options.footer,
                month = options.month,
                content = month.content,
                empty = month.empty;

            that.month = {
                content: template('<td#=data.cssClass# role="gridcell" style="padding:3px 0"><span><input type="checkbox" class="k-checkbox k-dt-checkbox" id="#=data.id#" name="\\#" data-#=data.ns#value="#=data.dateString#" /><label class="k-checkbox-label" for="#=data.id#">#=data.value#</label></span></td>', { useWithBlock: false }),
                empty: template('<td role="gridcell">' + (empty || "&nbsp;") + "</td>", { useWithBlock: !!empty })
            };
        },

        value: function (value) {
            var that = this,
                view = that._view,
                options = that.options,
                timetype = options.timetype,
                field = options.field,
                timeway = options.timeway;

            if (value === undefined) {
                //return that._value;
                var aww = new Array(),
                ary = new Array();
                if (timeway == 2) {
                    var format = "yyyy-MM-dd";
                    if (timetype == 2)
                        format = "yyyy-MM";
                    else if (timetype == 3 || timetype ==4)
                        format = "yyyy";
                    if ($.inArray(ufa.toString(that._datePicker.value(), format), that._map.keyset()) > -1) {
                        that._map.remove(ufa.toString(that._datePicker.value(), format));
                    }
                    that._map.put(ufa.toString(that._datePicker.value(), format), view.getResult(2));
                    var mapsort = that._map.keyset().sort();
                    if (timetype == 1) //小时
                    {
                        for (i = 0; i < mapsort.length; i++) {
                            var valuesort = that._map.get(mapsort[i]);
                            if (valuesort != null && valuesort != "") {
                                for (j = 0; j < valuesort.length; j++) {
                                    aww.push(ufa.parseDate(mapsort[i] + " " + valuesort[j] + ":00"));
                                    ary.push(mapsort[i] + " " + valuesort[j] + "时")
                                }
                            }
                        }

                    }
                    else if (timetype == 2)//天
                    {
                        for (i = 0; i < mapsort.length; i++) {
                            var valuesort = that._map.get(mapsort[i]);
                            if (valuesort != null && valuesort != "") {
                                for (j = 0; j < valuesort.length; j++) {
                                    aww.push(ufa.parseDate(mapsort[i] + "-" + valuesort[j]));
                                    ary.push(mapsort[i] + "-" + valuesort[j]);
                                }
                            }
                        }
                    }
                    else if (timetype == 3) //周
                    {
                        for (i = 0; i < mapsort.length; i++) {
                            var valuesort = that._map.get(mapsort[i]);
                            var firstDay = ufa.date.getFirstWeekBeginDay(mapsort[i]);
                            if (valuesort != null && valuesort != "") {
                                for (j = 0; j < valuesort.length; j++) {
                                    var temp = ufa.date.addDays(firstDay, valuesort[j] * 7);
                                    aww.push(temp);
                                    ary.push(ufa.format("{0}-{1}周", mapsort[i], valuesort[j]));
                                }
                            }
                        }
                    }
                    else if (timetype == 4) //月
                    {
                        for (i = 0; i < mapsort.length; i++) {
                            var valuesort = that._map.get(mapsort[i]);
                            if (valuesort != null && valuesort != "") {
                                for (j = 0; j < valuesort.length; j++) {
                                    var tempMonth = (parseInt(valuesort[j]) + 1);
                                    aww.push(ufa.parseDate(mapsort[i] + "-" + tempMonth + "-1"));
                                    ary.push(mapsort[i] + "-" + tempMonth + "-1");
                                }
                            }
                        }
                    }

                }
                else if (timeway == 3) { //循环
                    var result1 = view.getResult(3),
                        tempresult = result1.dates,
                            first,
                            end,
                            dtval = that._datePicker.value();

                    if (dtval != null) {
                        if (dtval.filter != null) {
                            if (dtval.filter.filters != null) {
                                first = dtval.filter.filters[0].value;
                                end = dtval.filter.filters[1].value;
                                var temp;
                                if (timetype == 1) { //小时
                                    for (var i = 0, n = Math.ceil((end - first) / (24 * 60 * 60 * 1000)) ; i <= n; i++) {
                                        temp = ufa.date.addDays(first, i);
                                        for (var j = 0, cols = result1.length; j < cols; j++) {
                                            var result = new DATE(temp.getFullYear(), temp.getMonth(), temp.getDate(), result1[j], 0, 0);
                                            aww.push(result);
                                            ary.push(ufa.toString(result, 'yyyy-MM-dd HH:mm:ss'));
                                        }
                                    }
                                }
                                else if (timetype == 2) {
                                    var yeardis = end.getFullYear() - first.getFullYear();
                                    length = end.getMonth() - first.getMonth(),
          							days = result1.days;
                                    var temp, month, year;
                                    if (yeardis > 0) {
                                        length = (yeardis - 1) * 12 + (end.getMonth() + 1) + (11 - first.getMonth());
                                    }
                                    year = first.getFullYear();
                                    month = first.getMonth();

                                    for (var i = 0, n = length; i < n; i++) {
                                        month = month + i;
                                        if (month == 12) {
                                            year = year + 1;
                                            month = 0;
                                        }
                                        for (var j = 0, cols = tempresult.length; j < cols; j++) {
                                            var isleapYear = ((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0);
                                            if ((isleapYear == true && month == 2 && tempresult[i] > 29)
                                                || (isleapYear = false && month == 2 && tempresult[i] > 28)) {
                                                continue;
                                            }
                                            var result = new DATE(year, month, tempresult[j]);
                                            if (days.length > 0)
                                            {
                                                if ($.inArray(result.getDay() + "", days) > -1) {
                                                    aww.push(result);
                                                    ary.push(ufa.toString(result, 'yyyy-MM-dd'));
                                                }
                                            }
                                            else
                                            {
                                                aww.push(result);
                                                ary.push(ufa.toString(result, 'yyyy-MM-dd'));
                                            }
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                that._value = {
                    Text: ary.join(','),
                    filter: {
                        filters: [{
                            field: field,
                            operator: "in",
                            value: aww
                        }]
                    }
                };
                return that._value;
            }
            else {
                var start = value.starttime,
                    end,
                    checkeds;
                if (value.hasOwnProperty("endtime"))
                    end = value.endtime;
                if (value.hasOwnProperty("checkeds"))
                    checkeds = value.checkeds;
            }
        },
        options: {
            name: "DuplexCalendar",
            url: "",
            culture: "zh-CN",
            footer: "",
            format: "",
            month: {},
            field: "start_time",
            min: new DATE(1900, 0, 1),
            max: new DATE(2099, 11, 31),
            animation: {
                horizontal: {
                    effects: SLIDE,
                    reverse: true,
                    duration: 500,
                    divisor: 2
                },
                vertical: {
                    effects: "zoomIn",
                    duration: 400
                }
            },
            timetype: 1,
            timeway: 1
        },
        events: [
                CHANGE,
                NAVIGATE
        ],
        destroy: function () {
            var that = this,
                today = that._today,
                timeway = that.options.timetype;
            that.element.off(ns);
            //that._title.off(ns);
            if (timeway == 2) {
                that[PREVARROW].off(ns);
                that[NEXTARROW].off(ns);

            }

            ufa.destroy(that._table);

            if (today) {
                ufa.destroy(today.off(ns));
            }

            Widget.fn.destroy.call(that);
        },
        current: function () {
            return this._current;
        },

        view: function () {
            return this._view;
        },
        focus: function (table) {
            table = table || this._table;
            this._bindTable(table);
            table.focus();
        },
        _horizontal: function (from, to, future) {
            var that = this,
                active = that._active,
                horizontal = that.options.animation.horizontal,
                effects = horizontal.effects,
                viewWidth = from.outerWidth();

            if (effects && effects.indexOf(SLIDE) != -1) {
                from.add(to).css({ width: viewWidth });

                from.wrap("<div/>");

                that._focusView(active, from);

                from.parent()
                    .css({
                        position: "relative",
                        width: viewWidth * 2,
                        "float": LEFT,
                        "margin-left": future ? 0 : -viewWidth
                    });

                to[future ? "insertAfter" : "insertBefore"](from);

                extend(horizontal, {
                    effects: SLIDE + ":" + (future ? "right" : LEFT),
                    complete: function () {
                        from.off(ns).remove();
                        that._oldTable = null;

                        to.unwrap();

                        that._focusView(active);

                    }
                });

                from.parent().ufaStop(true, true).ufaAnimate(horizontal);
            }
        },

        _vertical: function (from, to) {
            var that = this,
                vertical = that.options.animation.vertical,
                effects = vertical.effects,
                active = that._active, //active state before from's blur
                cell, position;

            if (effects && effects.indexOf("zoom") != -1) {
                to.css({
                    position: "absolute",
                    top: from.prev().outerHeight(),
                    left: 0
                }).insertBefore(from);

                if (transitionOrigin) {
                    cell = that._cellByDate(that._view.toDateString(that._current));
                    position = cell.position();
                    position = (position.left + parseInt(cell.width() / 2, 10)) + "px" + " " + (position.top + parseInt(cell.height() / 2, 10) + "px");
                    to.css(transitionOrigin, position);
                }

                from.ufaStop(true, true).ufaAnimate({
                    effects: "fadeOut",
                    duration: 600,
                    complete: function () {
                        from.off(ns).remove();
                        that._oldTable = null;

                        to.css({
                            position: "static",
                            top: 0,
                            left: 0
                        });

                        that._focusView(active);
                    }
                });

                to.ufaStop(true, true).ufaAnimate(vertical);
            }
        },
        min: function (value) {
            return this._option(MIN, value);
        },

        _blur: function () {
            var that = this,
                value = that.element.val();

            that.close();
            if (value !== that._oldText) {
                that._change(value);
            }

            that._inputWrapper.removeClass(FOCUSED);
        },
        _focus: function (value) {
            var that = this,
                view = that._view;

            if (view.compare(value, that._current) !== 0) {
                that.navigate(value);
            } else {
                that._current = value;
                //that._class(FOCUSED, view.toDateString(value));
            }
        },

        _focusView: function (active, table) {
            if (active) {
                this.focus(table);
            }
        },
        max: function (value) {
            return this._option("max", value);
        },

        navigateToPast: function () {

            this._navigate(PREVARROW, -1);
        },

        navigateToFuture: function () {

            this._navigate(NEXTARROW, 1);
        },
        _navigate: function (arrow, modifier) {
            var that = this,
          	timetype = that.options.timetype,
          	timeway = that.options.timeway,
                index = that._index + 1,
                currentValue = new DATE(+that._current);
            //that._map.push(that._datePicker.value(),that._view.getResult());
            arrow = that[arrow];
            var temp = that._datePicker.value();
            if (!arrow.hasClass(DISABLED)) {
                if (timetype == 1) {
                    currentValue = ufa.date.addDays(temp, modifier)
                }
                else if (timetype == 2) {
                    currentValue = new DATE(temp.getFullYear(),
					temp.getMonth() + (modifier), temp.getDate());
                }
                else if (timetype == 4 || timetype == 3) {
                    currentValue = new DATE(temp.getFullYear() + (modifier),
						temp.getMonth(), temp.getDate());
                }
            }
            var format = "yyyy-MM-dd";
            if (timetype == 2)
                format = "yyyy-MM";
            else if (timetype == 3)
                format = "yyyy";
            var timeHours = that._view.getResult(timeway);
            if (timeHours.length > 0)
            {
                if ($.inArray(ufa.toString(this._old, format), that._map.keyset()) > -1) {
                    that._map.remove(ufa.toString(this._old, format));
                }
                that._map.put(ufa.toString(temp, format), timeHours);
            }
            
            that.navigate(currentValue);
        },
        navigateUp: function () {
            var that = this,
                index = that._index;

            if (that._title.hasClass(DISABLED)) {
                return;
            }

            that.navigate(that._current, ++index);
        },
        _bindTable: function (table) {
            table
                .on(FOCUS_WITH_NS, this._addClassProxy)
                .on(BLUR, this._removeClassProxy);
        },

        navigateDown: function (value) {
            var that = this,
            index = that._index,
            depth = that.options.depth;

            if (!value) {
                return;
            }

            if (index === views[depth]) {
                if (+that._value != +value) {
                    that.value(value);
                    that.trigger(CHANGE);
                }
                return;
            }

            that.navigate(value, --index);
        },
    });
    ui.plugin(DuplexCalendar);
    
    var duplexcalender = {
        setTime: function (date, time) {
            var tzOffsetBefore = date.getTimezoneOffset(),
            resultDATE = new DATE(date.getTime() + time),
            tzOffsetDiff = resultDATE.getTimezoneOffset() - tzOffsetBefore;

            date.setTime(resultDATE.getTime() + tzOffsetDiff * MS_PER_MINUTE);
        },
        WeeksView: function (options) {
            var that = this,
	    		timetype = options.timeype,
	    		secondhtml = ufa.format('<span style="line-height:24px;color:#187ce9;">星期选择</span><table id="weekview{0}" tabindex="1" role="grid" class="k-content k-meta-view" cellspacing="0"><tbody><tr role="row">', options.timeway),
	    		content = template('<td#=data.cssClass# role="gridcell" style="padding:3px 0"><span><input type="checkbox" class="k-checkbox k-dt-checkbox" id="#=data.id#" name="\\#" data-#=data.ns#value="#=data.dateString#" /><label class="k-checkbox-label" for="#=data.id#">#=data.value#</label></span></td>', { useWithBlock: false }),
	    		empty = template('<td role="gridcell">' + (empty || "&nbsp;") + "</td>", { useWithBlock: !!empty }),
	    		culture = options.culture,
	    		cellsPerRow = 7,

	    		namesAbbr = getCalendarInfo(culture).days.namesAbbr;
            for (idx = 0; idx < 2; idx++) {
                if (idx > 0 && idx % cellsPerRow === 0) {
                    secondhtml += '</tr><tr role="row">';
                }
                for (var col = 0; col < 5; col++) {
                    if (idx * 5 + col < cellsPerRow) {
                        secondhtml += content({
                            dateString: idx * 5 + col,
                            value: namesAbbr[idx * 5 + col],
                            cssClass: "",
                            id: ufa.format("wcheckbox{0}", idx * 5 + col),
                            ns: ufa.ns,

                        });
                    }
                    else {
                        secondhtml += empty();
                    }

                }
            }

            return secondhtml + "</tr></tbody></table>";
        },
        views: [{
            name: "view1", //离散小时
            content: function (options) {
                //var currentDate = ufa.toString(value[0], "yyyy-MM-dd", options.culture);
                var that = this,
                    date = options.value,
                    timeway = options.timeway,
                culture = options.culture,
                calendarformate = getCalendarInfo(options.culture);
                //date.getHours()
                var html = ufa.format('<table id="view1{0}" tabindex="0" role="grid" class="k-ext-content k-meta-view" cellspacing="0"><tbody><tr role="row">', timeway);
                var min = new DATE(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0),
                     max = new DATE(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59, 999);
                var result = view({
                    cells: 24,
                    perRow: 6,
                    html: html += '</tr></thead><tbody><tr role="row">',
                    start: min,
                    min: min,
                    max: max,
                    content: options.content,
                    empty: options.empty,
                    setter: that.setDate,
                    build: function (date, idx) {
                        return {
                            ns: ufa.ns,
                            value: ufa.toString(date, "HH") + ":00",
                            dateString: ufa.toString(date, "HH") ,
                            id: ufa.format("checkbox{0}{1}", timeway,ufa.toString(date, "HH")),
                            cssClass: ""
                        };
                    }
                });
                return result;
            },
            compare: function (date1, date2) {
                var result,
                month1 = date1.getMonth(),
                year1 = date1.getFullYear(),
                month2 = date2.getMonth(),
                year2 = date2.getFullYear(),
				day1 = date1.getDate(),
				day2 = date2.getDate(),
				hour = date1.get
                if (year1 > year2) {
                    result = 1;
                } else if (year1 < year2) {
                    result = -1;
                } else {
                    result = month1 == month2 ? 0 : month1 > month2 ? 1 : -1;
                }

                return result;
            },
            setDate: function (date, value) {
                var hours = date.getHours();
                if (value instanceof DATE) {
                    date.setFullYear(value.getFullYear(), value.getMonth(), value.getDate());
                } else {
                    var dt = date.getTime() + value * MS_PER_HOUR;
                    date.setTime(dt);
                }
                adjustDST(date, hours);
            },
            getResult: function (timeway) {
                return getCheckBox(ufa.format("{0}{1}", this.name, timeway));
            },
            setResult: function (timeway, results) {
                setCheckBox(ufa.format("{0}{1}", this.name, timeway), results)
            }
        },
            {
                name: "view2", //离散天,
                content: function (options) {
                    var date = options.value,
                        days = ufa.date.lastDayOfMonth(date).getDate(),
                        that = this,
                        toDateString = that.toDateString,
                        firstMonth = date.getMonth(),
                        timeway = options.timeway,
                        html = ufa.format('<table id="view2{0}" tabindex="0" role="grid" class="k-ext-content k-meta-view" cellspacing="0"><thead><tr role="row">', timeway),
                        culture = options.culture;
                    var min = new DATE(date.getFullYear(), date.getMonth(), 1);
                    var max = new DATE(date.getFullYear(), date.getMonth(), days, 23, 59, 59, 999);
                    getCalendarInfo(culture).days.namesAbbr;
                    that.timeway = options.timeway;
                    days == 30;
                    if ($.inArray((firstMonth + 1), [1, 3, 5, 7, 8, 10, 12]) > -1)
                        days = 31;
                    if (that.timeway == 3) {
                        var secondDate = options.value1,
                            maxLastDay = ufa.date.lastDayOfMonth(secondDate).getDate(),
                            secondMonth = secondDate.getMonth(),
                            days = days > maxLastDay ? days : maxLastDay,
                            min = new DATE(secondDate.getFullYear(), secondDate.getMonth(), 1),
                            max = new DATE(secondDate.getFullYear(), secondDate.getMonth(), maxLastDay);
                        if (firstMonth == secondMonth && secondMonth == 2) {
                            days = 28;
                        }
                        else if (firstMonth == secondMonth && $.inArray((firstMonth + 1), [1, 3, 5, 7, 8, 10, 12]) < 0) {
                            days = 30;
                        }
                    }

                    var result = view({
                        cells: days,
                        perRow: 6,
                        html: html += '</tr></thead><tbody><tr role="row">',
                        start: min,
                        min: min,
                        max: max,
                        content: options.content,
                        empty: options.empty,
                        setter: that.setDate,
                        build: function (date) {
                            var dt = date;
                            return {
                                date: date,
                                ns: ufa.ns,
                                value: ufa.toString(date, "dd"),
                                dateString: ufa.toString(date, "dd"),
                                id: ufa.format("checkbox{0}{1}", timeway, ufa.toString(date, "dd")),
                                cssClass: ""
                            };
                        }
                    });

                    //if (timeway == 3) {
                    //    result += duplexcalender.WeeksView(options);
                    //    //bindCheckboxEvent(date, ufa.format("view2{0}", timeway), ufa.format("weekview{0}", timeway))
                    //}
                    return result;
                },
                first: function (date) {
                    return duplexcalender.firstDayOfMonth(date);
                },
                last: function (date) {
                    var last = new DATE(date.getFullYear(), date.getMonth() + 1, 0),
                        first = duplexcalender.firstDayOfMonth(date),
                        timeOffset = Math.abs(last.getTimezoneOffset() - first.getTimezoneOffset());

                    if (timeOffset) {
                        last.setHours(first.getHours() + (timeOffset / 60));
                    }

                    return last;
                },
                compare: function (date1, date2) {
                    return compare(date1, date2);
                },
                setDate: function (date, value) {
                    var month,
                        hours = date.getHours();

                    if (value instanceof DATE) {
                        month = value.getMonth();

                        date.setFullYear(value.getFullYear(), month, date.getDate());

                        if (month !== date.getMonth()) {
                            date.setDate(0);
                        }
                    } else {
                        var dt = ufa.date.addDays(date, value);
                        date.setDate(dt.getDate());
                    }

                    adjustDST(date, hours);
                },
                toDateString: function (date) {
                    return date.getFullYear() + "/" + date.getMonth() + "/" + date.getDate();
                },
                getResult: function (timeway) {
                    var result1 = getCheckBox(ufa.format("{0}{1}", this.name, timeway));
                    if (timeway == 3) {
                        var result2 = getCheckBox(ufa.format("weekview{0}", timeway));
                        return {
                            dates: result1,
                            days: result2
                        };
                    }
                    else {
                        return result1;
                    }


                },
                setResult: function (timeway, results) {
                    setCheckBox(ufa.format("{0}{1}", this.name, timeway), results)
                    if (timeway == 3) {
                        setCheckBox(ufa.format("weekview{0}", that.timeway), results);
                    }
                }

            },
            {
                name: "view3", //离散周

                content: function (options) {
                    var date = options.value,
                        year = date.getFullYear(),
                        firstDay = ufa.date.getFirstWeekBeginDay(year),
                        that = this,
                        timeway = options.timeway,
                        setter = that.setDate,
                        toDateString = that.toDateString,
                        s1 = Math.ceil((ufa.date.addDays(new DATE(year + 1, 0, 1), -1) - firstDay) / (24 * 60 * 60 * 1000)),
                        wNo = Math.ceil(s1 / 7) + 1,
                        timeway = options.timeway,
                        html = ufa.format('<table id="view3{0}" tabindex="0" role="grid" class="k-ext-content k-meta-view" cellspacing="0"><tbody><tr role="row">', timeway),
                        min = ufa.date.addDays(firstDay, -1),
                        max = ufa.date.addDays(new DATE(year + 1, 0, 1), -1);

                    return view({
                        cells: wNo,
                        perRow: 8,
                        html: html += '</tr></thead><tbody><tr role="row">',
                        start: firstDay,
                        min: min,
                        max: max,
                        content: options.content,
                        empty: options.empty,
                        setter: setter,
                        build: function (date, idx) {
                            //var start = ufa.date.getFirstWeekBeginDay(date.getFullYear()),
                            //    date = ufa.date.addDays(start,idx*7);
                            return {
                                value: (idx + 1) + "周",
                                ns: ufa.ns,
                                dateString: (idx + 1),
                                id: ufa.format("checkbox{0}{1}", timeway, idx),
                                cssClass: ""
                            };
                        }
                    });
                },
                compare: function (date1, date2) {
                    return compare(date1, date2, 10);
                },
                setDate: function (date, value) {
                    var hours = date.getHours();
                    //var date = calendar.getFirstWeekBeginDay(date.getFullYear());
                    if (value instanceof DATE) {
                        // date = ufa.date.addDays(date,week);
                        var month = value.getMonth();
                        //date = new DATE(value.getFullYear(),value.getMonth(),value.getDate());
                        date.setFullYear(value.getFullYear(), month, value.getDate());

                    }
                    else {
                        var date1 = ufa.date.addDays(date, value * 7)
                        date.setFullYear(date1.getFullYear(), date1.getMonth(), date1.getDate());
                    }
                    adjustDST(date, hours);
                },
                toDateString: function (date) {
                    return date.getFullYear() + "/0/1";
                },
                getResult: function (timeway) {
                    return getCheckBox(ufa.format("{0}{1}", this.name, timeway));
                },
                setResult: function (timeway, results) {
                    setCheckBox(ufa.format("{0}{1}", this.name, timeway), results)
                }
            },
            {
                name: "view4", //离散月
                content: function (options) {
                    var that = this,
                        date = options.value,
                        year = date.getFullYear(),
                        start = new DATE(year, 0, 1),
                        min = new DATE(year, 0, 1),
                        max = new DATE(year + 1, 0, 1),
                        toDateString = that.toDateString,
                        timeway = options.timeway,
                        html = ufa.format('<table id="view4{0}" tabindex="0" role="grid" class="k-ext-content k-meta-view" cellspacing="0"><tbody><tr role="row">', timeway);
                    var namesAbbr = getCalendarInfo(options.culture).months.namesAbbr;

                    return view({
                        start: start,
                        min: min,
                        max: max,
                        cells: 12,
                        perRow: 6,
                        html: html += '</tr></thead><tbody><tr role="row">',
                        setter: this.setDate,
                        build: function (date, idx) {
                            return {
                                ns: ufa.ns,
                                value: namesAbbr[date.getMonth()],
                                dateString: idx,
                                id: ufa.format("checkbox{0}{1}", timeway, idx),
                                cssClass: ""
                            };
                        }
                    });
                },
                setDate: function (date, value) {
                    var month,
                       hours = date.getHours();

                    if (value instanceof DATE) {
                        month = value.getMonth();

                        date.setFullYear(value.getFullYear(), month, date.getDate());

                        if (month !== date.getMonth()) {
                            date.setDate(0);
                        }
                    } else {
                        month = date.getMonth() + value;

                        date.setMonth(month);

                        if (month > 11) {
                            month -= 12;
                        }

                        if (month > 0 && date.getMonth() != month) {
                            date.setDate(0);
                        }
                    }

                    adjustDST(date, hours);
                },
                compare: function (date1, date2) {
                    var result,
                    month1 = date1.getMonth(),
                    year1 = date1.getFullYear(),
                    month2 = date2.getMonth(),
                    year2 = date2.getFullYear();

                    if (year1 > year2) {
                        result = 1;
                    } else if (year1 < year2) {
                        result = -1;
                    } else {
                        result = month1 == month2 ? 0 : month1 > month2 ? 1 : -1;
                    }

                    return result;
                },
                toDateString: function (date) {
                    var year = date.getFullYear();
                    return (year - year % 10) + "/0/1";
                },
                getResult: function (timeway) {
                    return getCheckBox(ufa.format("{0}{1}", this.name, timeway));
                },
                setResult: function (timeway, results) {
                    setCheckBox(ufa.format("{0}{1}", this.name, timeway), results)
                }
            }]
    };
    function getCheckBox(elementName) {
        var checkboxs = $(ufa.format("#{0} input:checkbox", elementName)),
    		result = [];
        checkboxs.each(function () {
            if ($(this).is(':checked')) {
                result.push($(this).attr("data-value"));
            }
        });
        return result;
    }
    function setCheckBox(elementName, results) {
        var checkboxs = $(ufa.format("#{0} input:checkbox", elementName));
        checkboxs.each(function () {
            var value = $(this).attr("data-value");
            if ($.inArray(value, results)) {
                $(this).prop("checked", true);
            }
        });
    }
    function setTableCheckBox(element, results)
    {
        var checkboxs = element.find("input:checkbox");
        checkboxs.each(function () {
            var value = $(this).attr("data-value");
            if ($.inArray(value, results) > -1) {
                $(this).prop("checked", true);
            }
        });
    }

    function getToday() {
        var today = new DATE();
        return new DATE(today.getFullYear(), today.getMonth(), today.getDate());
    }
    function restrictValue(value, min, max) {
        var today = getToday();

        if (value) {
            today = new DATE(+value);
        }

        if (min > today) {
            today = new DATE(+min);
        } else if (max < today) {
            today = new DATE(+max);
        }
        return today;
    }

    function isInRange(date, min, max) {
        return +date >= +min && +date <= +max;
    }

    function shiftArray(array, idx) {
        return array.slice(idx).concat(array.slice(0, idx));
    }
    function view(options) {
        var idx = 0,
            data,
            build = options.build,
            length = options.cells || 12,
            timetype = options.timetype,
            start = options.start,
            min = options.min,
            max = options.max,
            setter = options.setter,
            cellsPerRow = options.perRow || 4,
            content = options.content || cellTemplate,
            empty = options.empty || emptyCellTemplate,
            html = !options.html ? '<table tabindex="0" role="grid" class="k-content k-meta-view" cellspacing="0"><tbody><tr role="row">' : options.html;

        var rows = length;

        for (; idx < length; idx++) {
            if (idx > 0 && idx % cellsPerRow === 0) {
                html += '</tr><tr role="row">';
            }

            data = build(start, idx);

            html += isInRange(start, min, max) ? content(data) : empty(data);

            setter(start, 1);
        }

        return html + "</tr></tbody></table>";

    }

    function compare(date1, date2, modifier) {
        var year1 = date1.getFullYear(),
            start = date2.getFullYear(),
            end = start,
            result = 0;

        if (modifier) {
            start = start - start % modifier;
            end = start - start % modifier + modifier - 1;
        }

        if (year1 > end) {
            result = 1;
        } else if (year1 < start) {
            result = -1;
        }

        return result;
    }

    function getCalendarInfo(culture) {
        return getCulture(culture).calendars.standard;
    }
    function makeUnselectable(element) {
        if (isIE8) {
            element.find("*").attr("unselectable", "on");
        }
    }
    	var ExtDateTimePicker = Widget.extend({
        _timePicker: null,
        _v: null,
        _timeway: null,
        init: function (element, options) {
            /*
             * options:包含下面的东东
             * 
             */
            var that = this,
                link,
                confirm,
                cancel,
                text;
            Widget.fn.init.call(that, element, options);
            that.options.uid = new Date().getTime();
            options = that.options;
            var template = ufa.template(datetimepopuTemplate);
            var result = template(that.options);

            $(element).append(ufa.format("<input id='extDropDown{0}' class='k-ext-dropdown'/>", that.options.uid));
            $(document.body).append(result);
            that._dropdown = $(ufa.format("#extDropDown{0}", that.options.uid)).ufaDropDownList({
                dataSource: [{ text: "", value: "" }],
                dataTextField: "text",
                dataValueField: "value",
                open: function (e) {
                    //to prevent the dropdown from opening or closing.
                    e.preventDefault();
                    // If the grid is not visible, then make it visible.
                    if (!$(ufa.format('#timepanel{0}', that.options.uid)).hasClass("k-custom-visible")) {
                        // Position the grid so that it is below the dropdown.
                        //$(ufa.format('#timepanel{0}', that.options.uid)).css({
                        //    "top": $dropdownRootElem.position().top + $dropdownRootElem.height(),
                        //    "left": $dropdownRootElem.position().left
                        //});


                        var boxH = $(ufa.format('#timepanel{0}', that.options.uid)).height();
                        var docH = document.documentElement.clientHeight;
                        var top = $dropdownRootElem.offset().top + $dropdownRootElem.height();
                        var left = $dropdownRootElem.offset().left;
                        if (docH - top < boxH) {
                            top = docH - boxH;
                        }
                        $(ufa.format('#timepanel{0}', that.options.uid)).css({
                            "top": top,
                            "left": left,
                            'background-color': '#fff'
                        });

                        // Display the grid.
                        $(ufa.format('#timepanel{0}', that.options.uid)).slideToggle('fast', function () {
                            that._dropdown.close();
                            $(ufa.format("#extDropDown{0}_listbox", that._uid)).parent().parent().hide();
                            $(ufa.format('#timepanel{0}', that.options.uid)).addClass("k-custom-visible");
                        });
                        //that._init();
                    }
                }
            }).data("ufaDropDownList");
            if (options.dropDownWidth) {
                that._dropdown._focused.width(options.dropDownWidth);
            }

            var $dropdownRootElem = $(that._dropdown.element).closest("span.k-dropdown");
            $(ufa.format('#timepanel{0}', that.options.uid)).hide().css({
                "border": "1px solid grey",
                "position": "absolute"
            });
            confirm = $(ufa.format('#timepanel{0}', that.options.uid)).find(".k-ext-confirm");
            that.confirm = confirm.on("click" + NS, proxy(function (e) {
                var value = that._timePicker.value();
                $dropdownRootElem.find("span.k-input").text(value.Text);
                $dropdownRootElem.find("span.k-input").attr("title", value.Text);
                that._v = value;
                $(ufa.format('#timepanel{0}', that.options.uid)).slideToggle('fast', function (e) {
                    $(ufa.format('#timepanel{0}', that.options.uid)).removeClass("k-custom-visible");
                });

                that.trigger("change", e);
            }, that));
            cancel = $(ufa.format('#timepanel{0}', that.options.uid)).find(".k-ext-cancel");
            that.cancel = cancel.on("click" + NS, proxy(function (e) {
                $(ufa.format('#timepanel{0}', that.options.uid)).slideToggle('fast', function () {
                    $(ufa.format('#timepanel{0}', that.options.uid)).removeClass("k-custom-visible");
                });
                that.trigger("change", e);
            }, that));

            if (that.options.multi == 1) {
                timecontinue = $(ufa.format('#timepanel{0}', that.options.uid)).find(".k-ext-continue");
                that.timecontinue = timecontinue.on("click" + NS, proxy(that._continue, that));
            }
            if (that.options.discrete == 1) {
                timecycle = $(ufa.format('#timepanel{0}', that.options.uid)).find(".k-ext-cycle");
                that.timecycle = timecycle.on("click" + NS, proxy(that._cycle, that));
            }
            if (that.options.cycle == 1) {
                timediscrete = $(ufa.format('#timepanel{0}', that.options.uid)).find(".k-ext-discrete");
                that.timediscrete = timediscrete.on("click" + NS, proxy(that._discrete, that));
            }
            that._init();
            $(document).click(function (e) {
                // Ignore clicks on the grid.
                if ($(e.target).closest(ufa.format('#timepanel{0}', that.options.uid)).length == 0 &&
                    (!$(e.target).hasClass("k-ext-discrete")) &&  /* discrete */
                   (!$(e.target).hasClass("k-ext-continue")) && /* continue */
                  (!$(e.target).hasClass("k-ext-cycle")) && /* cycle */
                 (!$(e.target).hasClass("k-i-calendar")) && /*日期*/
                (!$(e.target).hasClass("k-dt-checkbox")) &&
                (!$(e.target).hasClass("k-ext-confirm")) &&
                (!$(e.target).hasClass("k-state-selected")) &&
                (!$(e.target).hasClass("k-link"))) {
                    // If visible, then close the grid.
                    if ($(ufa.format('#timepanel{0}', that.options.uid)).hasClass("k-custom-visible")) {
                        $(ufa.format('#timepanel{0}', that.options.uid)).slideToggle('fast', function () {
                            $(ufa.format('#timepanel{0}', that.options.uid)).removeClass("k-custom-visible");
                        });
                    }
                }
            });

        },
        _initContinue: function () {
            var that = this,
                options = that.options,
                uid = options.uid,
               field = options.field,
               timetype = options.timetype;
            $(ufa.format("#multitimebox{0}", uid)).empty();
            
            if (timetype == 1) {
                 $(ufa.format("#multitimebox{0}", uid)).ufaMultiDateTimePicker({
                    field: field,
                    value: options.value,
                    format: "yyyy-MM-dd HH",
                    timetype: timetype
                }).data("ufaMultiDateTimePicker")
            }
            else {
                var start = "month",
                dept = "month",
                format = "yyyy-MM-dd";
                if (timetype == 3) {
                    start = "week";
                    dept = "week";
                    format = "yyyy-WW";
                }
                else if (timetype == 4) {
                    start = "year";
                    dept = "year";
                    format = "yyyy-MM";
                }

                $(ufa.format("#multitimebox{0}", uid)).ufaMultiDatePicker({
                    field:field,
                    value: options.value,
                    start: start,
                    depth: dept,
                    format: format,
                    timetype: timetype
                }).data("ufaMultiDatePicker");
            }
        },
        _initCycle: function () {
            var that = this,
               options = that.options,
               uid = options.uid,
               field = options.field,
               timetype = options.timetype;
            $(ufa.format("#cycletimebox{0}", options.uid)).empty();
            $(ufa.format("#cycletimebox{0}", uid)).ufaDuplexCalendar({
                field: field,
                timeway: 3,
                timetype: timetype,
            }).data("ufaDuplexCalendar");
          
        },
        _initDiscrete: function () {
            var that = this,
               options = that.options,
                uid = options.uid,
               field = options.field,
               timetype = options.timetype;
            that._timePicker = null;
            $(ufa.format("#discretetimebox{0}", options.uid)).empty();
            $(ufa.format("#discretetimebox{0}", uid)).ufaDuplexCalendar({
                field: field,
                timeway: 2,
                timetype: timetype
            }).data("ufaDuplexCalendar");

        },   
        _continue: function (e) {
            var that = this,
                options = that.options,
                uid = options.uid,
                timetype = that.options.timetype,
                value = that.options.value;
            $(ufa.format("#k-ext-timeway{0}", uid)).find("span").each(function () {
                $(this).removeClass("k-ext-timeway-active")
            });
            $(ufa.format("#k-ext-timeway{0}", uid)).find(".k-ext-continue").addClass("k-ext-timeway-active");
            if (options.multi == 1)
                $(ufa.format("#multitimebox{0}", uid)).css("display", "");
            if (options.discrete == 1)
                $(ufa.format("#discretetimebox{0}", uid)).css("display", "none");
            if (options.cycle == 1)
                $(ufa.format("#cycletimebox{0}", uid)).css("display", "none");
            if (timetype == 1) 
                that._timePicker = $(ufa.format("#multitimebox{0}", uid)).data("ufaMultiDateTimePicker");
            else
                that._timePicker = $(ufa.format("#multitimebox{0}", uid)).data("ufaMultiDatePicker");
            if (options.value != null && options.value !== undefined)
                that._timePicker.value(options.value);
        },
        _cycle: function (e) {
            var that = this,
                options = that.options,
                uid = options.uid,
                value = options.value;
            $(ufa.format("#k-ext-timeway{0}", uid)).find("span").each(function () {
                $(this).removeClass("k-ext-timeway-active")
            });
            $(ufa.format("#k-ext-timeway{0}", uid)).find(".k-ext-cycle").addClass("k-ext-timeway-active");
            if (options.multi == 1)
                $(ufa.format("#multitimebox{0}", uid)).css("display", "none");
            if (options.discrete == 1)
                $(ufa.format("#discretetimebox{0}", uid)).css("display", "none");
            if (options.cycle == 1)
                $(ufa.format("#cycletimebox{0}", uid)).css("display", "");
            that._timePicker = $(ufa.format("#cycletimebox{0}", uid)).data("ufaDuplexCalendar");
            if (options.value != null && options.value !== undefined)
                that._timePicker.value(options.value);
        },
        _discrete: function (e) {
            var that = this,
                options = that.options,
                uid = options.uid,
                value = options.value;;
            $(ufa.format("#k-ext-timeway{0}", uid)).find("span").each(function () {
                $(this).removeClass("k-ext-timeway-active")
            });
            $(ufa.format("#k-ext-timeway{0}", uid)).find(".k-ext-discrete").addClass("k-ext-timeway-active");
            if (options.multi == 1)
                $(ufa.format("#multitimebox{0}", uid)).css("display", "none");
            if (options.discrete == 1)
                $(ufa.format("#discretetimebox{0}", uid)).css("display", "");
            if (options.cycle == 1)
                $(ufa.format("#cycletimebox{0}",uid)).css("display", "none");
            that._timePicker = $(ufa.format("#discretetimebox{0}", uid)).data("ufaDuplexCalendar");
            if (options.value != null && options.value !== undefined)
                that._timePicker.value(options.value);
        },
        reinit: function (timetype) {
            var that = this;
            that.options = $.extend(true, that.options, { timetype: timetype });
            that._reinit();
        },
        TimePicker: function () {
            return _timepicker;
        },
        _init: function (e) {
            var that = this,
    			options = that.options
            cascade = options.cascadeFrom,
            casCadeType = options.casCadeType;
            //$(ufa.format("#timepanel{0}", options.uid)).show();
            if (cascade) {
                var parentElement = $("#" + cascade);
                var parent = parentElement.data("ufa" + casCadeType);
                parent.first("cascade", function (e) {
                    var $dropdownRootElem = $(that._dropdown.element).closest("span.k-dropdown");
                    var _innerTimeType = parent.value();
                    if (_innerTimeType != null && _innerTimeType != undefined && _innerTimeType.length > 0 && that.options.timetype != _innerTimeType)
                    {
                        that._v = null;
                        $dropdownRootElem.find("span.k-input").text("");
                        $dropdownRootElem.find("span.k-input").attr("title", "");
                        that.reinit(_innerTimeType);
                    }
                });
            }
            that._reinit();
        },
        _reinit: function () {
            var that = this,
    			options = that.options,
                element = that.element,
                timetype = that.options.timetype,
                uid = options.uid;
            if (options.multi == 1) {
                that._initContinue();
            }
            if (options.discrete == 1) {
                that._initDiscrete();
            }
            if (options.cycle == 1 && (options.timetype == 1 || options.timetype == 2)) {
                $(element).find(".k-ext-cycle").show();
                that._initCycle();
            }
            else
            {
                $(element).find(".k-ext-cycle").hide();
            }
            if (options.multi == 1 && (options.discrete == 1 || options.cycle == 1))
                $(ufa.format("#k-ext-timeway{0}", uid)).find("span").first().click();
            else
            {
                if(timetype == 1)
                    that._timePicker = $(ufa.format("#multitimebox{0}", uid)).data("ufaMultiDateTimePicker")
                else
                    that._timePicker = $(ufa.format("#multitimebox{0}", uid)).data("ufaMultiDatePicker")
                $(ufa.format("#multitimebox{0}", uid)).css("display", "");
            }
                
        },
        text: function (text) {
            var that = this;
            var $dropdownRootElem = $(that._dropdown.element).closest("span.k-dropdown");
            $dropdownRootElem.find("span.k-input").text(text);
            $dropdownRootElem.find("span.k-input").attr("title", text);
        },
        value: function (v) {
            var that = this;
            if (that._timePicker) {
                if (v != undefined) {
                    that._timePicker.value(v);
                    that._v = that._timePicker.value();
                    that.text(that._timePicker.value().Text);
                }
                else {
                    return that._v;
                }
            } else {
                return undefined;
            }
        },
        options: {
            uid: null,
            name: "ExtDateTimePicker",
            message: {
                confirm: "确定",
                cancel: "取消",

            },
            title: {
                cycletext: "周期",
                discretetext: "离散",
                multitext: "连续"
            },
            casCadeType: "DropDownList",
            cycle: 1, //1：显示周期，0：不显示
            discrete: 1, //1：显示离散，0：不显示
            multi: 1,//1：显示连续，0：不显示
            field: "start_time",
            cascadeFrom: "",
            cascadeFromField: "",
            timetype: 1
        }
    });

    ufa.ui.plugin(ExtDateTimePicker);
    }(window.ufa.jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));