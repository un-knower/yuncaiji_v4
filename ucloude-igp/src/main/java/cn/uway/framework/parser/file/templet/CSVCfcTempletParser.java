package cn.uway.framework.parser.file.templet;

import org.w3c.dom.Node;

import cn.uway.util.StringUtil;

/**
 * @author yuy 2014.1.6 csv cfc模板解码器
 */
public class CSVCfcTempletParser extends TempletParser {

	/**
	 * 处理file属性
	 * 
	 * @param templet
	 * @param templetNode
	 * @throws Exception
	 */
	public void personalHandler(Templet templet, Node templetNode) throws Exception {
		Node classNameNode = templetNode.getAttributes().getNamedItem("className");
		if (classNameNode != null) {
			String className = classNameNode.getNodeValue();
			if (StringUtil.isEmpty(className))
				throw new Exception("className属性值不能为空");
			templet.setDataName(className.trim());
		} else
			throw new Exception("缺少className属性");
	}

}
