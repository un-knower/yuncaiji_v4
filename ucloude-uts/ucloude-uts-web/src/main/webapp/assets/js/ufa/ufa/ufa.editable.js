(function (f, define) {
    define('ufa.editable', [
        'ufa.datepicker',
        'ufa.numerictextbox',
        'ufa.validator',
        'ufa.binder'
    ], f);
}(function () {
    var __meta__ = {
        id: 'editable',
        name: 'Editable',
        category: 'framework',
        depends: [
            'datepicker',
            'numerictextbox',
            'validator',
            'binder'
        ],
        hidden: true
    };
    (function ($, undefined) {
        var ufa = window.ufa, ui = ufa.ui, Widget = ui.Widget, extend = $.extend, oldIE = ufa.support.browser.msie && ufa.support.browser.version < 9, isFunction = ufa.isFunction, isPlainObject = $.isPlainObject, inArray = $.inArray, nameSpecialCharRegExp = /("|\%|'|\[|\]|\$|\.|\,|\:|\;|\+|\*|\&|\!|\#|\(|\)|<|>|\=|\?|\@|\^|\{|\}|\~|\/|\||`)/g, ERRORTEMPLATE = '<div class="k-widget k-tooltip k-tooltip-validation" style="margin:0.5em"><span class="k-icon k-warning"> </span>' + '#=message#<div class="k-callout k-callout-n"></div></div>', CHANGE = 'change';
        var specialRules = [
            'url',
            'email',
            'number',
            'date',
            'boolean'
        ];
        function fieldType(field) {
            field = field != null ? field : '';
            return field.type || $.type(field) || 'string';
        }
        function convertToValueBinding(container) {
            container.find(':input:not(:button, [' + ufa.attr('role') + '=upload], [' + ufa.attr('skip') + '], [type=file]), select').each(function () {
                var bindAttr = ufa.attr('bind'), binding = this.getAttribute(bindAttr) || '', bindingName = this.type === 'checkbox' || this.type === 'radio' ? 'checked:' : 'value:', fieldName = this.name;
                if (binding.indexOf(bindingName) === -1 && fieldName) {
                    binding += (binding.length ? ',' : '') + bindingName + fieldName;
                    $(this).attr(bindAttr, binding);
                }
            });
        }
        function createAttributes(options) {
            var field = (options.model.fields || options.model)[options.field], type = fieldType(field), validation = field ? field.validation : {}, ruleName, DATATYPE = ufa.attr('type'), BINDING = ufa.attr('bind'), rule, attr = { name: options.field };
            for (ruleName in validation) {
                rule = validation[ruleName];
                if (inArray(ruleName, specialRules) >= 0) {
                    attr[DATATYPE] = ruleName;
                } else if (!isFunction(rule)) {
                    attr[ruleName] = isPlainObject(rule) ? rule.value || ruleName : rule;
                }
                attr[ufa.attr(ruleName + '-msg')] = rule.message;
            }
            if (inArray(type, specialRules) >= 0) {
                attr[DATATYPE] = type;
            }
            attr[BINDING] = (type === 'boolean' ? 'checked:' : 'value:') + options.field;
            return attr;
        }
        function convertItems(items) {
            var idx, length, item, value, text, result;
            if (items && items.length) {
                result = [];
                for (idx = 0, length = items.length; idx < length; idx++) {
                    item = items[idx];
                    text = item.text || item.value || item;
                    value = item.value == null ? item.text || item : item.value;
                    result[idx] = {
                        text: text,
                        value: value
                    };
                }
            }
            return result;
        }
        var editors = {
            'number': function (container, options) {
                var attr = createAttributes(options);
                $('<input type="text"/>').attr(attr).appendTo(container).ufaNumericTextBox({ format: options.format });
                $('<span ' + ufa.attr('for') + '="' + options.field + '" class="k-invalid-msg"/>').hide().appendTo(container);
            },
            'date': function (container, options) {
                var attr = createAttributes(options), format = options.format;
                if (format) {
                    format = ufa._extractFormat(format);
                }
                attr[ufa.attr('format')] = format;
                $('<input type="text"/>').attr(attr).appendTo(container).ufaDatePicker({ format: options.format });
                $('<span ' + ufa.attr('for') + '="' + options.field + '" class="k-invalid-msg"/>').hide().appendTo(container);
            },
            'string': function (container, options) {
                var attr = createAttributes(options);
                $('<input type="text" class="k-input k-textbox"/>').attr(attr).appendTo(container);
            },
            'boolean': function (container, options) {
                var attr = createAttributes(options);
                $('<input type="checkbox" />').attr(attr).appendTo(container);
            },
            'values': function (container, options) {
                var attr = createAttributes(options);
                var items = ufa.stringify(convertItems(options.values));
                $('<select ' + ufa.attr('text-field') + '="text"' + ufa.attr('value-field') + '="value"' + ufa.attr('source') + '=\'' + (items ? items.replace(/\'/g, '&apos;') : items) + '\'' + ufa.attr('role') + '="dropdownlist"/>').attr(attr).appendTo(container);
                $('<span ' + ufa.attr('for') + '="' + options.field + '" class="k-invalid-msg"/>').hide().appendTo(container);
            }
        };
        function addValidationRules(modelField, rules) {
            var validation = modelField ? modelField.validation || {} : {}, rule, descriptor;
            for (rule in validation) {
                descriptor = validation[rule];
                if (isPlainObject(descriptor) && descriptor.value) {
                    descriptor = descriptor.value;
                }
                if (isFunction(descriptor)) {
                    rules[rule] = descriptor;
                }
            }
        }
        var Editable = Widget.extend({
            init: function (element, options) {
                var that = this;
                if (options.target) {
                    options.$angular = options.target.options.$angular;
                }
                Widget.fn.init.call(that, element, options);
                that._validateProxy = $.proxy(that._validate, that);
                that.refresh();
            },
            events: [CHANGE],
            options: {
                name: 'Editable',
                editors: editors,
                clearContainer: true,
                errorTemplate: ERRORTEMPLATE
            },
            editor: function (field, modelField) {
                var that = this, editors = that.options.editors, isObject = isPlainObject(field), fieldName = isObject ? field.field : field, model = that.options.model || {}, isValuesEditor = isObject && field.values, type = isValuesEditor ? 'values' : fieldType(modelField), isCustomEditor = isObject && field.editor, editor = isCustomEditor ? field.editor : editors[type], container = that.element.find('[' + ufa.attr('container-for') + '=' + fieldName.replace(nameSpecialCharRegExp, '\\$1') + ']');
                editor = editor ? editor : editors.string;
                if (isCustomEditor && typeof field.editor === 'string') {
                    editor = function (container) {
                        container.append(field.editor);
                    };
                }
                container = container.length ? container : that.element;
                editor(container, extend(true, {}, isObject ? field : { field: fieldName }, { model: model }));
            },
            _validate: function (e) {
                var that = this, input, value = e.value, preventChangeTrigger = that._validationEventInProgress, values = {}, bindAttribute = ufa.attr('bind'), fieldName = e.field.replace(nameSpecialCharRegExp, '\\$1'), bindingRegex = new RegExp('(value|checked)\\s*:\\s*' + fieldName + '\\s*(,|$)');
                values[e.field] = e.value;
                input = $(':input[' + bindAttribute + '*="' + fieldName + '"]', that.element).filter('[' + ufa.attr('validate') + '!=\'false\']').filter(function () {
                    return bindingRegex.test($(this).attr(bindAttribute));
                });
                if (input.length > 1) {
                    input = input.filter(function () {
                        var element = $(this);
                        return !element.is(':radio') || element.val() == value;
                    });
                }
                try {
                    that._validationEventInProgress = true;
                    if (!that.validatable.validateInput(input) || !preventChangeTrigger && that.trigger(CHANGE, { values: values })) {
                        e.preventDefault();
                    }
                } finally {
                    that._validationEventInProgress = false;
                }
            },
            end: function () {
                return this.validatable.validate();
            },
            destroy: function () {
                var that = this;
                that.angular('cleanup', function () {
                    return { elements: that.element };
                });
                Widget.fn.destroy.call(that);
                that.options.model.unbind('set', that._validateProxy);
                ufa.unbind(that.element);
                if (that.validatable) {
                    that.validatable.destroy();
                }
                ufa.destroy(that.element);
                that.element.removeData('ufaValidator');
                if (that.element.is('[' + ufa.attr('role') + '=editable]')) {
                    that.element.removeAttr(ufa.attr('role'));
                }
            },
            refresh: function () {
                var that = this, idx, length, fields = that.options.fields || [], container = that.options.clearContainer ? that.element.empty() : that.element, model = that.options.model || {}, rules = {}, field, isObject, fieldName, modelField, modelFields;
                if (!$.isArray(fields)) {
                    fields = [fields];
                }
                for (idx = 0, length = fields.length; idx < length; idx++) {
                    field = fields[idx];
                    isObject = isPlainObject(field);
                    fieldName = isObject ? field.field : field;
                    modelField = (model.fields || model)[fieldName];
                    addValidationRules(modelField, rules);
                    that.editor(field, modelField);
                }
                if (that.options.target) {
                    that.angular('compile', function () {
                        return {
                            elements: container,
                            data: container.map(function () {
                                return { dataItem: model };
                            })
                        };
                    });
                }
                if (!length) {
                    modelFields = model.fields || model;
                    for (fieldName in modelFields) {
                        addValidationRules(modelFields[fieldName], rules);
                    }
                }
                convertToValueBinding(container);
                if (that.validatable) {
                    that.validatable.destroy();
                }
                ufa.bind(container, that.options.model);
                that.options.model.unbind('set', that._validateProxy);
                that.options.model.bind('set', that._validateProxy);
                that.validatable = new ufa.ui.Validator(container, {
                    validateOnBlur: false,
                    errorTemplate: that.options.errorTemplate || undefined,
                    rules: rules
                });
                var focusable = container.find(':ufaFocusable').eq(0).focus();
                if (oldIE) {
                    focusable.focus();
                }
            }
        });
        ui.plugin(Editable);
    }(window.ufa.jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));