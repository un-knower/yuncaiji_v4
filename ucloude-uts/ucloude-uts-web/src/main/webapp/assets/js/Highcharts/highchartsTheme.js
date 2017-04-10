/**
 * Dark blue theme for Highcharts JS
 * @author Torstein Honsi
 */

Highcharts.theme = {
//    colors: ["#7cb5ec", "#f7a35c", "#90ee7e", "#7798BF", "#aaeeee", "#ff0066", "#eeaaee",
//		"#55BF3B", "#DF5353", "#7798BF", "#aaeeee"],
    colors: ["#6479fc", "#c7b11e", "#7ea700", "#767371", "#ff9500", "#eb0101", "#1bd0dc","#55BF3B", "#DF5353", "#7798BF", "#aaeeee"],
    chart: {
        backgroundColor: "#fff",
        margin: 75,
        style: {
            fontFamily: "roman,Microsoft YaHei"
        }
    },
    title: {
        text:null,
        style: {
            fontSize: '16px',
            fontWeight: 'bold',
            textTransform: 'uppercase'
        }
    },
    tooltip: {
        borderWidth: 0,
        backgroundColor: 'rgba(219,219,216,0.8)',
        shadow: false
    },
    legend: {
        itemStyle: {
            fontWeight: 'bold',
            fontSize: '13px'
        }
    },
    xAxis: {
        gridLineWidth: 0,
        title: {
            text: null
        },
        labels: {
            style: {
                fontSize: '13px',
                color:"#333"
            }
        }
    },
    yAxis: {
        gridLineWidth: 1,
        gridLineColor: "#f1f1f1",
        title: {
            text: null
        },   
        labels: {
            style: {
                fontSize: '12px'
            }
        }
    },
    toolbar: {
        itemStyle: {
            color: 'silver'
        }
    },
    plotOptions: {
        column: {
            allowOverlap:true,
            colorByPoint:true,
            borderRadiusTopLeft: 5,
            borderRadiusTopRight: 5,
            dataLabels: {
                enabled: true
            }
        },
        line: {
            dataLabels: {
                color: '#CCC'
            },
            marker: {
                lineColor: '#333'
            }
        },
        spline: {
            marker: {
                lineColor: '#333'
            }
        },
        scatter: {
            marker: {
                lineColor: '#333'
            }
        },
        candlestick: {
            lineColor: '#404048'
        }
    },
    credits: {
        enabled:false
    },
    labels: {
        style: {
            color: '#999'
        }
    },

    navigation: {
        buttonOptions: {
            symbolStroke: '#DDDDDD',
            hoverSymbolStroke: '#FFFFFF',
            theme: {
                fill: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                    stops: [
						[0.4, '#606060'],
						[0.6, '#333333']
                    ]
                },
                stroke: '#000000'
            }
        }
    },

    // scroll charts
    rangeSelector: {
        buttonTheme: {
            fill: {
                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                stops: [
					[0.4, '#888'],
					[0.6, '#555']
                ]
            },
            stroke: '#000000',
            style: {
                color: '#CCC',
                fontWeight: 'bold'
            },
            states: {
                hover: {
                    fill: {
                        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                        stops: [
							[0.4, '#BBB'],
							[0.6, '#888']
                        ]
                    },
                    stroke: '#000000',
                    style: {
                        color: 'white'
                    }
                },
                select: {
                    fill: {
                        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                        stops: [
							[0.1, '#000'],
							[0.3, '#333']
                        ]
                    },
                    stroke: '#000000',
                    style: {
                        color: 'yellow'
                    }
                }
            }
        },
        inputStyle: {
            backgroundColor: '#333',
            color: 'silver'
        },
        labelStyle: {
            color: 'silver'
        }
    },

    navigator: {
        handles: {
            backgroundColor: '#666',
            borderColor: '#AAA'
        },
        outlineColor: '#CCC',
        maskFill: 'rgba(16, 16, 16, 0.5)',
        series: {
            color: '#7798BF',
            lineColor: '#A6C7ED'
        }
    },

    scrollbar: {
        barBackgroundColor: {
            linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
            stops: [
                [0.4, '#888'],
                [0.6, '#555']
            ]
        },
        barBorderColor: '#CCC',
        buttonArrowColor: '#CCC',
        buttonBackgroundColor: {
            linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
            stops: [
                [0.4, '#888'],
                [0.6, '#555']
            ]
        },
        buttonBorderColor: '#CCC',
        rifleColor: '#FFF',
        trackBackgroundColor: {
            linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
            stops: [
				[0, '#000'],
				[1, '#333']
            ]
        },
        trackBorderColor: '#666'
    },

    // special colors for some of the
    legendBackgroundColor: 'rgba(0, 0, 0, 0.5)',
    background2: 'rgb(35, 35, 70)',
    dataLabelsColor: '#444',
    textColor: '#C0C0C0',
    maskColor: 'rgba(255,255,255,0.3)'
};

// Apply the theme
var highchartsOptions = Highcharts.setOptions(Highcharts.theme);

//圆角
(function (H) {
    H.wrap(H.seriesTypes.column.prototype, 'translate', function (proceed) {
        var options = this.options,
            rTopLeft = options.borderRadiusTopLeft || 0,
            rTopRight = options.borderRadiusTopRight || 0,
            rBottomRight = options.borderRadiusBottomRight || 0,
            rBottomLeft = options.borderRadiusBottomLeft || 0;

        proceed.call(this);

        if (rTopLeft || rTopRight || rBottomRight || rBottomLeft) {
            H.each(this.points, function (point) {
                var shapeArgs = point.shapeArgs,
                    w = shapeArgs.width,
                    h = shapeArgs.height,
                    x = shapeArgs.x,
                    y = shapeArgs.y;

                // Preserve the box for data labels
                point.dlBox = point.shapeArgs;

                point.shapeType = 'path';
                point.shapeArgs = {
                    d: [
                        'M', x + rTopLeft, y,
                        // top side
                        'L', x + w - rTopRight, y,
                        // top right corner
                        'C', x + w - rTopRight / 2, y, x + w, y + rTopRight / 2, x + w, y + rTopRight,
                        // right side
                        'L', x + w, y + h - rBottomRight,
                        // bottom right corner
                        'C', x + w, y + h - rBottomRight / 2, x + w - rBottomRight / 2, y + h, x + w - rBottomRight, y + h,
                        // bottom side
                        'L', x + rBottomLeft, y + h,
                        // bottom left corner
                        'C', x + rBottomLeft / 2, y + h, x, y + h - rBottomLeft / 2, x, y + h - rBottomLeft,
                        // left side
                        'L', x, y + rTopLeft,
                        // top left corner
                        'C', x, y + rTopLeft / 2, x + rTopLeft / 2, y, x + rTopLeft, y,
                        'Z'
                    ]
                };

            });
        }
    });
}(Highcharts));
//全局色调
Highcharts.getOptions().colors = ["#fe0000", "#ff6600", "#fe9900", "#ffcc00", "#ffff01", "#ade601", "#01cc01", "#009cff"];
//单色线性渐变
linearGradient = Highcharts.map(Highcharts.getOptions().colors, function (color) {
    return {
        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
        stops: [
            [0, "#ff3d31"],
            [1, Highcharts.Color("#ff9551").get('rgb')]
        ]
    };
});
//多色线性渐变
linearGradients = Highcharts.map(Highcharts.getOptions().colors, function (color) {
    return {
        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
        stops: [
            [0, Highcharts.Color(color).brighten(.2).get('rgb')],
            [1, color]
        ]
    };
});
//单色径向渐变
radialGradient = Highcharts.map(Highcharts.getOptions().colors, function (color) {
    return {
        radialGradient: { cx: 0.5, cy: 0.3, r: 0.7 },
        stops: [
            [0, "#1bc0ff"],
            [1, Highcharts.Color("#31a8ff").get('rgb')]
        ]
    };
});
//多色径向渐变
radialGradients = Highcharts.map(Highcharts.getOptions().colors, function (color) {
    return {
        radialGradient: { cx: 0.5, cy: 0.3, r: 0.7 },
        stops: [
            [0, color],
            [1, Highcharts.Color(color).brighten(-0.1).get('rgb')]
        ]
    };
});
//单色透明度降底
singleColors = (function () {
    var colors = [],
        base = "#ff9500",
        i;
    for (i = 10; i > 0; i--) {
        // Start out with a darkened base color (negative brighten), and end
        // up with a much brighter color
        var opacity = i / 10;
        colors.push(Highcharts.Color(base).setOpacity(opacity).get());
    }
    return colors;
}());
//过渡色
var transitionColors = (function (minColor, maxColor, count) {
    function _getRGB(color) {
        var obj = new Object();
        obj.r = parseInt(color.substr(1, 2), 16);
        obj.g = parseInt(color.substr(3, 2), 16);
        obj.b = parseInt(color.substr(5, 2), 16);
        return obj;
    }
    //ufa.Color.fromBytes(colorList.SectionList[i].ColorR, colorList.SectionList[i].ColorG, colorList.SectionList[i].ColorB).toCss()
    var result = [minColor, maxColor];
    if (count > 2) {
        var maxRGB = _getRGB(maxColor);
        var minRGB = _getRGB(minColor);
        var rInterval = (maxRGB.r - minRGB.r) / (count - 1);
        var gInterval = (maxRGB.g - minRGB.g) / (count - 1);
        var bInterval = (maxRGB.b - minRGB.b) / (count - 1);
        for (var i = 1; i < count - 1; i++) {
            if (i < 6) {
                var tempRGB = {};
                tempRGB.r = maxRGB.r - i * rInterval;
                tempRGB.g = maxRGB.g - i * gInterval;
                tempRGB.b = maxRGB.b - i * bInterval;
                //result.splice(1, 0, ufa.Color.fromBytes(tempRGB.r, tempRGB.g, tempRGB.b).toCss());
            }
        }
        for (var j = 0; j < 14; j++) {
            result.splice(result.length - 1, 0, maxColor);
        }
    }
    return result;

}("#ff9500", "#fff000", 6));