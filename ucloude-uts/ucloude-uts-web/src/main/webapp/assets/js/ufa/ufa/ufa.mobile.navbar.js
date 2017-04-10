(function (f, define) {
    define('ufa.mobile.navbar', ['ufa.core'], f);
}(function () {
    var __meta__ = {
        id: 'mobile.navbar',
        name: 'NavBar',
        category: 'mobile',
        description: 'The ufa mobile NavBar widget is used inside a mobile View or Layout Header element to display an application navigation bar.',
        depends: ['core']
    };
    (function ($, undefined) {
        var ufa = window.ufa, mobile = ufa.mobile, ui = mobile.ui, Widget = ui.Widget;
        function createContainer(align, element) {
            var items = element.find('[' + ufa.attr('align') + '=' + align + ']');
            if (items[0]) {
                return $('<div class="km-' + align + 'item" />').append(items).prependTo(element);
            }
        }
        function toggleTitle(centerElement) {
            var siblings = centerElement.siblings(), noTitle = !!centerElement.children('ul')[0], showTitle = !!siblings[0] && $.trim(centerElement.text()) === '', android = !!(ufa.mobile.application && ufa.mobile.application.element.is('.km-android'));
            centerElement.prevAll().toggleClass('km-absolute', noTitle);
            centerElement.toggleClass('km-show-title', showTitle);
            centerElement.toggleClass('km-fill-title', showTitle && !$.trim(centerElement.html()));
            centerElement.toggleClass('km-no-title', noTitle);
            centerElement.toggleClass('km-hide-title', android && !siblings.children().is(':visible'));
        }
        var NavBar = Widget.extend({
            init: function (element, options) {
                var that = this;
                Widget.fn.init.call(that, element, options);
                element = that.element;
                that.container().bind('show', $.proxy(this, 'refresh'));
                element.addClass('km-navbar').wrapInner($('<div class="km-view-title km-show-title" />'));
                that.leftElement = createContainer('left', element);
                that.rightElement = createContainer('right', element);
                that.centerElement = element.find('.km-view-title');
            },
            options: { name: 'NavBar' },
            title: function (value) {
                this.element.find(ufa.roleSelector('view-title')).text(value);
                toggleTitle(this.centerElement);
            },
            refresh: function (e) {
                var view = e.view;
                this.title(view.options.title);
            },
            destroy: function () {
                Widget.fn.destroy.call(this);
                ufa.destroy(this.element);
            }
        });
        ui.plugin(NavBar);
    }(window.ufa.jQuery));
    return window.ufa;
}, typeof define == 'function' && define.amd ? define : function (a1, a2, a3) {
    (a3 || a2)();
}));