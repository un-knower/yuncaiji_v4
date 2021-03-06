(function (f, define) {
    define('ufa.filtercell', [
        'ufa.autocomplete',
        'ufa.datepicker',
        'ufa.numerictextbox',
        'ufa.combobox',
        'ufa.dropdownlist'
    ], f);
}(function () {
    var __meta__ = {
        id: 'filtercell',
        name: 'Row filter',
        category: 'framework',
        depends: ['autocomplete'],
        advanced: true
    };
    (function ($, undefined) {
        var ufa = window.ufa, ui = ufa.ui, DataSource = ufa.data.DataSource, Widget = ui.Widget, CHANGE = 'change', BOOL = 'boolean', ENUM = 'enums', STRING = 'string', EQ = 'Is equal to', NEQ = 'Is not equal to', proxy = $.proxy, nonValueOperators = [
                'isnull',
                'isnotnull',
                'isempty',
                'isnotempty'
            ];
        function isNonValueFilter(filter) {
            var operator = typeof filter === 'string' ? filter : filter.operator;
            return $.inArray(operator, nonValueOperators) > -1;
        }
        function findFilterForField(filter, field) {
            var filters = [];
            if ($.isPlainObject(filter)) {
                if (filter.hasOwnProperty('filters')) {
                    filters = filter.filters;
                } else if (filter.field == field) {
                    return filter;
                }
            }
            if ($.isArray(filter)) {
                filters = filter;
            }
            for (var i = 0; i < filters.length; i++) {
                var result = findFilterForField(filters[i], field);
                if (result) {
                    return result;
                }
            }
        }
        function removeFiltersForField(expression, field) {
            if (expression.filters) {
                expression.filters = $.grep(expression.filters, function (filter) {
                    removeFiltersForField(filter, field);
                    if (filter.filters) {
                        return filter.filters.length;
                    } else {
                        return filter.field != field;
                    }
                });
            }
        }
        function removeDuplicates(dataSelector, dataTextField) {
            var getter = ufa.getter(dataTextField, true);
            return function (e) {
                var items = dataSelector(e), result = [], index = 0, seen = {};
                while (index < items.length) {
                    var item = items[index++], text = getter(item);
                    if (!seen.hasOwnProperty(text)) {
                        result.push(item);
                        seen[text] = true;
                    }
                }
                return result;
            };
        }
        var FilterCell = Widget.extend({
            init: function (element, options) {
                element = $(element).addClass('k-filtercell');
                var wrapper = this.wrapper = $('<span/>').appendTo(element);
                var that = this, dataSource, viewModel, passedOptions = options, first, type, operators = that.operators = options.operators || {}, input = that.input = $('<input/>').attr(ufa.attr('bind'), 'value: value').appendTo(wrapper);
                Widget.fn.init.call(that, element[0], options);
                options = that.options;
                dataSource = that.dataSource = options.dataSource;
                that.model = dataSource.reader.model;
                type = options.type = STRING;
                var fields = ufa.getter('reader.model.fields', true)(dataSource) || {};
                var target = fields[options.field];
                if (target && target.type) {
                    type = options.type = target.type;
                }
                if (options.values) {
                    options.type = type = ENUM;
                }
                operators = operators[type] || options.operators[type];
                if (!passedOptions.operator) {
                    for (first in operators) {
                        options.operator = first;
                        break;
                    }
                }
                that._parse = function (value) {
                    return value + '';
                };
                if (that.model && that.model.fields) {
                    var field = that.model.fields[options.field];
                    if (field) {
                        if (field.parse) {
                            that._parse = proxy(field.parse, field);
                        }
                    }
                }
                that.viewModel = viewModel = ufa.observable({
                    operator: options.operator,
                    value: null,
                    operatorVisible: function () {
                        var val = this.get('value');
                        return val !== null && val !== undefined && val != 'undefined' || isNonValueFilter(this.get('operator')) && !that._clearInProgress && !that.manuallyUpdatingVM;
                    }
                });
                viewModel.bind(CHANGE, proxy(that.updateDsFilter, that));
                if (type == STRING) {
                    that.initSuggestDataSource(options);
                }
                if (options.inputWidth !== null) {
                    input.width(options.inputWidth);
                }
                that._setInputType(options, type);
                if (type != BOOL && options.showOperators !== false) {
                    that._createOperatorDropDown(operators);
                } else {
                    wrapper.addClass('k-operator-hidden');
                }
                that._createClearIcon();
                ufa.bind(this.wrapper, viewModel);
                if (type == STRING) {
                    if (!options.template) {
                        that.setAutoCompleteSource();
                    }
                }
                if (type == ENUM) {
                    that.setComboBoxSource(that.options.values);
                }
                that._refreshUI();
                that._refreshHandler = proxy(that._refreshUI, that);
                that.dataSource.bind(CHANGE, that._refreshHandler);
            },
            _setInputType: function (options, type) {
                var that = this, input = that.input;
                if (typeof options.template == 'function') {
                    options.template.call(that.viewModel, {
                        element: that.input,
                        dataSource: that.suggestDataSource
                    });
                    that._angularItems('compile');
                } else if (type == STRING) {
                    input.attr(ufa.attr('role'), 'autocomplete').attr(ufa.attr('text-field'), options.dataTextField || options.field).attr(ufa.attr('filter'), options.suggestionOperator).attr(ufa.attr('delay'), options.delay).attr(ufa.attr('min-length'), options.minLength).attr(ufa.attr('value-primitive'), true);
                } else if (type == 'date') {
                    input.attr(ufa.attr('role'), 'datepicker');
                } else if (type == BOOL) {
                    input.remove();
                    var radioInput = $('<input type=\'radio\'/>');
                    var wrapper = that.wrapper;
                    var inputName = ufa.guid();
                    var labelTrue = $('<label/>').text(options.messages.isTrue).append(radioInput);
                    radioInput.attr(ufa.attr('bind'), 'checked:value').attr('name', inputName).val('true');
                    var labelFalse = labelTrue.clone().text(options.messages.isFalse);
                    radioInput.clone().val('false').appendTo(labelFalse);
                    wrapper.append([
                        labelTrue,
                        labelFalse
                    ]);
                } else if (type == 'number') {
                    input.attr(ufa.attr('role'), 'numerictextbox');
                } else if (type == ENUM) {
                    input.attr(ufa.attr('role'), 'combobox').attr(ufa.attr('text-field'), 'text').attr(ufa.attr('suggest'), true).attr(ufa.attr('filter'), 'contains').attr(ufa.attr('value-field'), 'value').attr(ufa.attr('value-primitive'), true);
                }
            },
            _createOperatorDropDown: function (operators) {
                var items = [];
                for (var prop in operators) {
                    items.push({
                        text: operators[prop],
                        value: prop
                    });
                }
                var dropdown = $('<input class="k-dropdown-operator" ' + ufa.attr('bind') + '="value: operator"/>').appendTo(this.wrapper);
                this.operatorDropDown = dropdown.ufaDropDownList({
                    dataSource: items,
                    dataTextField: 'text',
                    dataValueField: 'value',
                    open: function () {
                        this.popup.element.width(150);
                    },
                    valuePrimitive: true
                }).data('ufaDropDownList');
                this.operatorDropDown.wrapper.find('.k-i-arrow-s').removeClass('k-i-arrow-s').addClass('k-filter');
            },
            initSuggestDataSource: function (options) {
                var suggestDataSource = options.suggestDataSource;
                if (!(suggestDataSource instanceof DataSource)) {
                    if (!options.customDataSource && suggestDataSource) {
                        suggestDataSource.group = undefined;
                    }
                    suggestDataSource = this.suggestDataSource = DataSource.create(suggestDataSource);
                }
                if (!options.customDataSource) {
                    suggestDataSource._pageSize = undefined;
                    suggestDataSource.reader.data = removeDuplicates(suggestDataSource.reader.data, this.options.field);
                }
                this.suggestDataSource = suggestDataSource;
            },
            setAutoCompleteSource: function () {
                var autoComplete = this.input.data('ufaAutoComplete');
                if (autoComplete) {
                    autoComplete.setDataSource(this.suggestDataSource);
                }
            },
            setComboBoxSource: function (values) {
                var dataSource = DataSource.create({ data: values });
                var comboBox = this.input.data('ufaComboBox');
                if (comboBox) {
                    comboBox.setDataSource(dataSource);
                }
            },
            _refreshUI: function () {
                var that = this, filter = findFilterForField(that.dataSource.filter(), this.options.field) || {}, viewModel = that.viewModel;
                that.manuallyUpdatingVM = true;
                filter = $.extend(true, {}, filter);
                if (that.options.type == BOOL) {
                    if (viewModel.value !== filter.value) {
                        that.wrapper.find(':radio').prop('checked', false);
                    }
                }
                if (filter.operator) {
                    viewModel.set('operator', filter.operator);
                }
                viewModel.set('value', filter.value);
                that.manuallyUpdatingVM = false;
            },
            updateDsFilter: function (e) {
                var that = this, model = that.viewModel;
                if (that.manuallyUpdatingVM || e.field == 'operator' && model.value === undefined && !isNonValueFilter(model)) {
                    return;
                }
                var currentFilter = $.extend({}, that.viewModel.toJSON(), { field: that.options.field });
                var expression = {
                    logic: 'and',
                    filters: []
                };
                if (currentFilter.value !== undefined && currentFilter.value !== null || isNonValueFilter(currentFilter) && !this._clearInProgress) {
                    expression.filters.push(currentFilter);
                }
                var mergeResult = that._merge(expression);
                if (mergeResult.filters.length) {
                    that.dataSource.filter(mergeResult);
                } else {
                    that.dataSource.filter({});
                }
            },
            _merge: function (expression) {
                var that = this, logic = expression.logic || 'and', filters = expression.filters, filter, result = that.dataSource.filter() || {
                        filters: [],
                        logic: 'and'
                    }, idx, length;
                removeFiltersForField(result, that.options.field);
                for (idx = 0, length = filters.length; idx < length; idx++) {
                    filter = filters[idx];
                    filter.value = that._parse(filter.value);
                }
                filters = $.grep(filters, function (filter) {
                    return filter.value !== '' && filter.value !== null || isNonValueFilter(filter);
                });
                if (filters.length) {
                    if (result.filters.length) {
                        expression.filters = filters;
                        if (result.logic !== 'and') {
                            result.filters = [{
                                    logic: result.logic,
                                    filters: result.filters
                                }];
                            result.logic = 'and';
                        }
                        if (filters.length > 1) {
                            result.filters.push(expression);
                        } else {
                            result.filters.push(filters[0]);
                        }
                    } else {
                        result.filters = filters;
                        result.logic = logic;
                    }
                }
                return result;
            },
            _createClearIcon: function () {
                var that = this;
                $('<button type=\'button\' class=\'k-button k-button-icon\' title = ' + that.options.messages.clear + '/>').attr(ufa.attr('bind'), 'visible:operatorVisible').html('<span class=\'k-icon k-i-close\'/>').click(proxy(that.clearFilter, that)).appendTo(that.wrapper);
            },
            clearFilter: function () {
                this._clearInProgress = true;
                this.viewModel.set('value', null);
                this._clearInProgress = false;
            },
            _angularItems: function (action) {
                var elements = this.wrapper.closest('th').get();
                var column = this.options.column;
                this.angular(action, function () {
                    return {
                        elements: elements,
                        data: [{ column: column }]
                    };
                });
            },
            destroy: function () {
                var that = this;
                that.filterModel = null;
                that.operatorDropDown = null;
                that._angularItems('cleanup');
                if (that._refreshHandler) {
                    that.dataSource.bind(CHANGE, that._refreshHandler);
                    that._refreshHandler = null;
                }
                ufa.unbind(that.element);
                Widget.fn.destroy.call(that);
                ufa.destroy(that.element);
            },
            events: [CHANGE],
            options: {
                name: 'FilterCell',
                delay: 200,
                minLength: 1,
                inputWidth: null,
                values: undefined,
                customDataSource: false,
                field: '',
                dataTextField: '',
                type: 'string',
                suggestDataSource: null,
                suggestionOperator: 'startswith',
                operator: 'eq',
                showOperators: true,
                template: null,
                messages: {
                    isTrue: 'is true',
                    isFalse: 'is false',
                    filter: 'Filter',
                    clear: 'Clear',
                    operator: 'Operator'
                },
                operators: {
                    string: {
                        eq: EQ,
                        neq: NEQ,
                        startswith: 'Starts with',
                        contains: 'Contains',
                        doesnotcontain: 'Does not contain',
                        endswith: 'Ends with',
                        isnull: 'Is null',
                        isnotnull: 'Is not null',
                        isempty: 'Is empty',
                        isnotempty: 'Is not empty'
                    },
                    number: {
                        eq: EQ,
                        neq: NEQ,
                        gte: 'Is greater than or equal to',
                        gt: 'Is greater than',
                        lte: 'Is less than or equal to',
                        lt: 'Is less than',
                        isnull: 'Is null',
                        isnotnull: 'Is not null'
                    },
                    date: {
                        eq: EQ,
                        neq: NEQ,
                        gte: 'Is after or equal to',
                        gt: 'Is after',
                        lte: 'Is before or equal to',
                        lt: 'Is before',
                        isnull: 'Is null',
                        isnotnull: 'Is not null'
                    },
                    enums: {
                        eq: EQ,
                        neq: NEQ,
                        isnull: 'Is null',
                        isnotnull: 'Is not null'
                    }
                }
            }
        });
        ui.plugin(FilterCell);
    }(window.ufa.jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));