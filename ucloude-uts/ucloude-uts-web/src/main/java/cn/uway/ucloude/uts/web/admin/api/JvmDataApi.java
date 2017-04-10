package cn.uway.ucloude.uts.web.admin.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.cmd.DefaultHttpCmd;
import cn.uway.ucloude.cmd.HttpCmd;
import cn.uway.ucloude.cmd.HttpCmdClient;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cmd.HttpCmdNames;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.support.Builder;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;
import cn.uway.ucloude.uts.web.support.I18nManager;

import java.util.Collections;

/**
 * @author uway
 */
@RestController
@RequestMapping("/jvm")
public class JvmDataApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("node-jvm-info-get")
    public RestfulResponse getNodeList(String identity) {

        RestfulResponse restfulResponse = new RestfulResponse();

        Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(identity);

        if (node == null) {
            return Builder.build(false, I18nManager.getMessage("node.dose.not.alive"));
        }

        HttpCmd cmd = new DefaultHttpCmd();
        cmd.setCommand(HttpCmdNames.HTTP_CMD_JVM_INFO_GET);
        cmd.setNodeIdentity(identity);

        HttpCmdResponse response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), cmd);
        if (response.isSuccess()) {
            restfulResponse.setSuccess(true);
            restfulResponse.setResults(1);
            restfulResponse.setRows(Collections.singletonList(response.getObj()));
        } else {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(response.getMsg());
        }

        return restfulResponse;
    }

}
