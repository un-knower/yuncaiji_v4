/**
 * 
 */

function initechart(title,datalegents,data){
	var options={};
	options.title=title;
	options.grid={
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    };
	options.toolbox={
	        feature: {
	            saveAsImage: {}
	        }
	    };
	for(var i = 0; i <  datalegents.length; i ++){
   		var chatConfig = datalegents[i];
   		seriesMap[chartConfig['field']] = {
   			 name:chartConfig["title"],
   			 type:'line',
   			 stack: '总量',
   			 data:[]
   		};
   		
   		
   }
  options.xAxis = {
    		type: 'time',
    		data:[]
    	};
  var rows = json.rows;
  for (var i = 0; i < rows.length; i++) {
      var row = rows[i];
    	options.xAxis.data.push(row['timestamp']);
    	for (var seriesKey in seriesMap) {
          var value = row[seriesKey];
          seriesMap[seriesKey]['data'].push(value);
      }
  }
  
  options.series = [];
  for(var seriesKey in seriesMap) {
  		options.series.put(seriesMap[seriesKey]);
  }
  var myChart = echarts.init(document.getElementById('taskTotalChart'));
  myChart.setOption(options);
  
  
}
