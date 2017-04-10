package cn.uway.ucloude.uts.biz.logger;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.container.SPI;
@SPI(key = ExtConfigKeys.JOB_LOGGER, dftValue = "db")
public interface JobLoggerFactory {

    JobLogger getJobLogger();

}
