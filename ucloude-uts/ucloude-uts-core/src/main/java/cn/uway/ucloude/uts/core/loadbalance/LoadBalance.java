package cn.uway.ucloude.uts.core.loadbalance;

import java.util.List;
import cn.uway.ucloude.uts.core.ExtConfigKeys;

import cn.uway.ucloude.container.SPI;

@SPI(key = ExtConfigKeys.LOADBALANCE, dftValue = "random")
public interface LoadBalance {

    public <S> S select(List<S> shards, String seed);

}
