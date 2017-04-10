package cn.uway.ucloude.data.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;

public class WorkOrderDao extends JdbcAbstractAccess {

	public WorkOrderDao(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	public WorkOrder getWorkOrder(long id){
		WorkOrder order = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().columns("ID","WorkOrder_NO","CITY_ID").from().table("mod_workorder").where("ID=?", id).single(new ResultSetHandler<WorkOrder>(){

			@Override
			public WorkOrder handle(ResultSet rs) throws SQLException {
				WorkOrder order1 = null;
				// TODO Auto-generated method stub
				if(rs.next()){
					order1 = new WorkOrder();
					order1.setID(rs.getLong("ID"));
					order1.setOrderNo(rs.getString("WorkOrder_NO"));
					order1.setCityID(rs.getInt("CITY_ID"));
				}
				return order1;
			}
			
		});
		
		return order;
	}
}
