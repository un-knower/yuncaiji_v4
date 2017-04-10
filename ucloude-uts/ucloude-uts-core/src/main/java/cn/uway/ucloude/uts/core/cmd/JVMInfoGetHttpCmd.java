package cn.uway.ucloude.uts.core.cmd;



import java.util.Map;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.jvmmonitor.JVMCollector;

/**
 * 主要用于获取节点的JVM信息
 *
 * @author magic.s.g.xie
 */
public class JVMInfoGetHttpCmd implements HttpCmdProcessor {

    private UtsConfiguration configuraiton;

    public JVMInfoGetHttpCmd(UtsConfiguration configuraiton) {
        this.configuraiton=configuraiton;
    }

    @Override
    public String nodeIdentity() {
        return configuraiton.getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_JVM_INFO_GET;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        Map<String, Object> jvmInfo = JVMCollector.getJVMInfo();

        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(true);
        response.setObj(JsonConvert.serialize(jvmInfo));

        return response;
    }

}
