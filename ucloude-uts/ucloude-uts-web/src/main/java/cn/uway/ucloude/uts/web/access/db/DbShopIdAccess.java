package cn.uway.ucloude.uts.web.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.monitor.access.domain.JobTrackerMDataPo;
import cn.uway.ucloude.uts.web.access.domain.ShopItem;
import cn.uway.ucloude.uts.web.access.face.ShopIdAccess;

public class DbShopIdAccess extends JdbcAbstractAccess implements ShopIdAccess {

	public DbShopIdAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<ShopItem> getShopIdList() {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from().
		table("CFG_DICTITEM").where("DICT_TYPE=?","UTS_SHOP_ID").orderBy("DICT_NAME", OrderByType.ASC).list(new ResultSetHandler<List<ShopItem>>() {
			@Override
			public List<ShopItem> handle(ResultSet rs) throws SQLException {
				List<ShopItem> list = new ArrayList<ShopItem>();
				while (rs.next()) {
					ShopItem item = new ShopItem();
					item.setId(rs.getString("DICT_CODE"));
					list.add(item);
				}
				return list;
			}
		});
	}

}
