package cn.uway.ucloude.query;

/**
 * 查询请求
 * @author uway
 *
 */
public class QueryRequest {

    private String field;

    private String direction = "ASC";
    
    private Integer pageSize = 10;
    
    private Integer page = 1;
    
    /**
     * 是否排序，默认排序
     */
    private boolean paging = true;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

	public boolean isPaging() {
		return paging;
	}

	public void setPaging(boolean paging) {
		this.paging = paging;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

}
