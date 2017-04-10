package cn.uway.ucloude.uts.core.cmd;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.uts.core.UtsConfiguration;

/**
 * 主要用于启动检测, 通过调用该命令检测是否启动成功
 * @author magic.s.g.xie
 */
public class StatusCheckHttpCmd implements HttpCmdProcessor {

    private UtsConfiguration configuration;

    public StatusCheckHttpCmd(UtsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String nodeIdentity() {
        return configuration.getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_STATUS_CHECK;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(true);
        response.setMsg("ok");
        return response;
    }
}
