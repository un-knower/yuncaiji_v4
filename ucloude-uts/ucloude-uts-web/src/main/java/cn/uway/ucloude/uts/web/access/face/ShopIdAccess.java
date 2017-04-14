package cn.uway.ucloude.uts.web.access.face;

import java.util.List;

import cn.uway.ucloude.uts.web.access.domain.ShopItem;

/**
 * ShopId访问接口
 * @author Uway-M3
 */
public interface ShopIdAccess {
	
	/**
	 * 获取ShopId可选列表
	 * @return 可选列表
	 */
	List<ShopItem> getShopIdList();
}
