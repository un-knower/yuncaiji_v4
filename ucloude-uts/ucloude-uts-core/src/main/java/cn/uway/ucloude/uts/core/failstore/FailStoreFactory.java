package cn.uway.ucloude.uts.core.failstore;

import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.uts.core.UtsConfiguration;

@SPI(key = "job.fail.store", dftValue = "leveldb")
public interface FailStoreFactory {
	 FailStore getFailStore(UtsConfiguration config, String storePath);
}
