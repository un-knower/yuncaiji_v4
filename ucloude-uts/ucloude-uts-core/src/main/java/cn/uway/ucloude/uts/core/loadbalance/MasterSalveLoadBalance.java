package cn.uway.ucloude.uts.core.loadbalance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.uway.ucloude.uts.core.cluster.Node;

/**
 * 主从模式: 譬如将JobClient和TaskTracker设置为这种负载均衡模式,
 * 那么总会去连接最老的一台JobTracker,从而达到主从模式的效果
 * @author uway
 *
 */
public class MasterSalveLoadBalance extends AbstractLoadBalance {

	@Override
	protected <S> S doSelect(List<S> shards, String seed) {
		// TODO Auto-generated method stub
		if (shards.get(0) instanceof Node) {
			Collections.sort(shards, new Comparator<S>() {
				@Override
				public int compare(S left, S right) {
					return ((Node) left).getCreateTime().compareTo(((Node) right).getCreateTime());
				}
			});
		}

		return shards.get(0);
	}

}
