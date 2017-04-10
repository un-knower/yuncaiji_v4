package cn.uway.ucloude.uts.core.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡算法
 * @author uway
 *
 */
public class RandomLoadBalance extends AbstractLoadBalance {

	@Override
	protected <S> S doSelect(List<S> shards, String seed) {
		// TODO Auto-generated method stub
		return shards.get(ThreadLocalRandom.current().nextInt(shards.size()));
	}

}
