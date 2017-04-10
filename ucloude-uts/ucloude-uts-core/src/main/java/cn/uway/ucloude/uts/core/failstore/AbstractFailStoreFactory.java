package cn.uway.ucloude.uts.core.failstore;

import java.io.File;
import java.io.IOException;

import cn.uway.ucloude.filehandling.FileUtils;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.UtsConfiguration;

public abstract class AbstractFailStoreFactory implements FailStoreFactory {

	@Override
	public FailStore getFailStore(UtsConfiguration config, String storePath) {
		// TODO Auto-generated method stub
		if (StringUtil.isEmpty(storePath)) {
            throw new IllegalStateException("storePath should not be empty");
        }
        File dbPath = new File(storePath.concat(getName()).concat("/").concat(config.getIdentity()));
        try {
            FileUtils.createDirIfNotExist(dbPath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return newInstance(dbPath, true);
        
        
	}
	
	protected abstract String getName();

	protected abstract FailStore newInstance(File dbPath, boolean needLock);

}
