package cn.uway.ucloude.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分页信息
 * 
 * @author uway
 *
 * @param <T>
 */
public class Pagination<T> {
	private long total;

	private List<T> data;
	
	private Map<String, Object> aggregates;
	
	public Map<String, Object> getAggregates() {
        return aggregates;
    }

    public void setAggregates(Map<String, Object> aggregates) {
        this.aggregates = aggregates;
    }

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

}
