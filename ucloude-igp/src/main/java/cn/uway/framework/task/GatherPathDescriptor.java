package cn.uway.framework.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.uway.util.StringUtil;

/**
 * 采集路径描述符
 * 
 * @author MikeYang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see GatherPathEntry
 */
public class GatherPathDescriptor{

	public static final String SPLIT_SYMBOL = ";";

	private String rawData; // 原始配置

	private List<GatherPathEntry> paths; // 转换后的采集路对象

	/**
	 * 构造方法
	 * 
	 * @param rawData 原始采集路径配置数据
	 */
	public GatherPathDescriptor(String rawData){
		super();
		this.rawData = rawData.trim();

		// 填充paths
		this.paths = new ArrayList<GatherPathEntry>();
		if(this.rawData != null && !this.rawData.trim().isEmpty()){
			String [] strs = this.rawData.split(SPLIT_SYMBOL);
			for(int i = 0; i < strs.length; i++){
				if(StringUtil.isEmpty(strs[i]))
					continue;
				this.paths.add(new GatherPathEntry(strs[i]));
			}
		}
	}

	/**
	 * 获取采集路径对象的个数
	 */
	public int getSize(){
		return this.paths.size();
	}

	/**
	 * 获取采集路径对象列表
	 */
	public List<GatherPathEntry> getPaths(){
		return Collections.unmodifiableList(this.paths);
	}

	/**
	 * 获取原始采集路径配置数据
	 */
	public String getRawData(){
		return rawData;
	}

	/**
	 * 获取指定位置的采集路径实体
	 * 
	 * @param index 位置
	 * @return {@link GatherPathEntry}
	 */
	public GatherPathEntry getByIndex(int index){
		return this.paths.get(index);
	}
}
