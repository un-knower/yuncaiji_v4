package cn.uway.ucloude.uts.web.admin;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import cn.uway.ucloude.uts.web.admin.support.DateEditor;
import cn.uway.ucloude.uts.web.admin.support.MapEditor;

import java.util.Date;
import java.util.Map;

/**
 * @author magic.s.g.xie
 */
public class AbstractMVC {

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws Exception {
        //对于需要转换为Date类型的属性，使用DateEditor进行处理
        //binder.registerCustomEditor(Date.class, new DateEditor());
        //binder.registerCustomEditor(Map.class, "extParams", new MapEditor());
    }

}
