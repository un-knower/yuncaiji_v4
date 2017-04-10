package cn.uway.ucloude.uts.core.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import cn.uway.ucloude.uts.core.support.ConsistentHashSelector;

public class ConsistentHashLoadBalance extends AbstractLoadBalance {

	@Override
	protected <S> S doSelect(List<S> shards, String seed) {
		// TODO Auto-generated method stub
		if (seed == null || seed.length() == 0) {
			seed = "HASH-".concat(String.valueOf(ThreadLocalRandom.current().nextInt()));
		}
		ConsistentHashSelector<S> selector = new ConsistentHashSelector<S>(shards);
		return selector.selectForKey(seed);
	}

}
