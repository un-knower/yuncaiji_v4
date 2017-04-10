package cn.uway.ucloude.uts.core.failstore.leveldb;

import java.io.File;

import cn.uway.ucloude.uts.core.failstore.AbstractFailStoreFactory;
import cn.uway.ucloude.uts.core.failstore.FailStore;

public class LeveldbFailStoreFactory extends AbstractFailStoreFactory {

	@Override
	protected String getName() {
		// TODO Auto-generated method stub
		return LeveldbFailStore.name;
	}

	@Override
	protected FailStore newInstance(File dbPath, boolean needLock) {
		// TODO Auto-generated method stub
		return new LeveldbFailStore(dbPath, needLock);
	}


}
