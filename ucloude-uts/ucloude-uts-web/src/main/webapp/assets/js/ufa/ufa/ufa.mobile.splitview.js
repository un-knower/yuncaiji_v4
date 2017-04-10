(function (f, define) {
    define('ufa.mobile.splitview', ['ufa.mobile.pane'], f);
}(function () {
    var __meta__ = {
        id: 'mobile.splitview',
        name: 'SplitView',
        category: 'mobile',
        description: 'The mobile SplitView is a tablet-specific view that consists of two or more mobile Pane widgets.',
        depends: ['mobile.pane']
    };
    (function ($, undefined) {
        var ufa = window.ufa, ui = ufa.mobile.ui, Widget = ui.Widget, EXPANED_PANE_SHIM = '<div class=\'km-expanded-pane-shim\' />', View = ui.View;
        var SplitView = View.extend({
            init: function (element, options) {
                var that = this, pane, modalViews;
                Widget.fn.init.call(that, element, options);
                element = that.element;
                $.extend(that, options);
                that._id();
                if (!that.options.$angular) {
                    that._layout();
                    that._overlay();
                } else {
                    that._overlay();
                }
                that._style();
                modalViews = element.children(that._locate('modalview'));
                if (!that.options.$angular) {
                    ufa.mobile.init(modalViews);
                } else {
                    modalViews.each(function (idx, element) {
                        ufa.compileMobileDirective($(element), options.$angular[0]);
                    });
                }
                that.panes = [];
                that._paramsHistory = [];
                if (!that.options.$angular) {
                    that.content.children(ufa.roleSelector('pane')).each(function () {
                        pane = ufa.initWidget(this, {}, ui.roles);
                        that.panes.push(pane);
                    });
                } else {
                    that.element.children(ufa.directiveSelector('pane')).each(function () {
                        pane = ufa.compileMobileDirective($(this), options.$angular[0]);
                        that.panes.push(pane);
                    });
                    that.element.children(ufa.directiveSelector('header footer')).each(function () {
                        ufa.compileMobileDirective($(this), options.$angular[0]);
                    });
                }
                that.expandedPaneShim = $(EXPANED_PANE_SHIM).appendTo(that.element);
                that._shimUserEvents = new ufa.UserEvents(that.expandedPaneShim, {
                    fastTap: true,
                    tap: function () {
                        that.collapsePanes();
                    }
                });
            },
            _locate: function (selectors) {
                return this.options.$angular ? ufa.directiveSelector(selectors) : ufa.roleSelector(selectors);
            },
            options: {
                name: 'SplitView',
                style: 'horizontal'
            },
            expandPanes: function () {
                this.element.addClass('km-expanded-splitview');
            },
            collapsePanes: function () {
                this.element.removeClass('km-expanded-splitview');
            },
            _layout: function () {
                var that = this, element = that.element;
                that.transition = ufa.attrValue(element, 'transition');
                ufa.mobile.ui.View.prototype._layout.call(this);
                ufa.mobile.init(this.header.add(this.footer));
                that.element.addClass('km-splitview');
                that.content.addClass('km-split-content');
            },
            _style: function () {
                var style = this.options.style, element = this.element, styles;
                if (style) {
                    styles = style.split(' ');
                    $.each(styles, function () {
                        element.addClass('km-split-' + this);
                    });
                }
            },
            showStart: function () {
                var that = this;
                that.element.css('display', '');
                if (!that.inited) {
                    that.inited = true;
                    $.each(that.panes, function () {
                        if (this.options.initial) {
                            this.navigateToInitial();
                        } else {
                            this.navigate('');
                        }
                    });
                    that.trigger('init', { view: that });
                } else {
                    this._invokeNgController();
                }
                that.trigger('show', { view: that });
            }
        });
        ui.plugin(SplitView);
    }(window.ufa.jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));