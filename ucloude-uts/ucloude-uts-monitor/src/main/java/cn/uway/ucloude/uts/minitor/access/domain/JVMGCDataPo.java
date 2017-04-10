package cn.uway.ucloude.uts.minitor.access.domain;

/**
 * @author uway
 */
public class JVMGCDataPo extends MDataPo {

	//年轻代已发生的回收的总次数
    private Long youngGCCollectionCount;
    //年轻代近似的累积回收时间（以毫秒为单位）
    private Long youngGCCollectionTime;
    //已发生的回收的总次数
    private Long fullGCCollectionCount;
    //近似的累积回收时间（以毫秒为单位）
    private Long fullGCCollectionTime;
    //年轻代已发生的回收的总次数差值
    private Long spanYoungGCCollectionCount;
    //年轻代年轻代近似的累积回收时间（以毫秒为单位）差值
    private Long spanYoungGCCollectionTime;
    //已发生的回收的总次数差值
    private Long spanFullGCCollectionCount;
    //近似的累积回收时间（以毫秒为单位）差值
    private Long spanFullGCCollectionTime;

    public Long getYoungGCCollectionCount() {
        return youngGCCollectionCount;
    }

    public void setYoungGCCollectionCount(Long youngGCCollectionCount) {
        this.youngGCCollectionCount = youngGCCollectionCount;
    }

    public Long getYoungGCCollectionTime() {
        return youngGCCollectionTime;
    }

    public void setYoungGCCollectionTime(Long youngGCCollectionTime) {
        this.youngGCCollectionTime = youngGCCollectionTime;
    }

    public Long getFullGCCollectionCount() {
        return fullGCCollectionCount;
    }

    public void setFullGCCollectionCount(Long fullGCCollectionCount) {
        this.fullGCCollectionCount = fullGCCollectionCount;
    }

    public Long getFullGCCollectionTime() {
        return fullGCCollectionTime;
    }

    public void setFullGCCollectionTime(Long fullGCCollectionTime) {
        this.fullGCCollectionTime = fullGCCollectionTime;
    }

    public Long getSpanYoungGCCollectionCount() {
        return spanYoungGCCollectionCount;
    }

    public void setSpanYoungGCCollectionCount(Long spanYoungGCCollectionCount) {
        this.spanYoungGCCollectionCount = spanYoungGCCollectionCount;
    }

    public Long getSpanYoungGCCollectionTime() {
        return spanYoungGCCollectionTime;
    }

    public void setSpanYoungGCCollectionTime(Long spanYoungGCCollectionTime) {
        this.spanYoungGCCollectionTime = spanYoungGCCollectionTime;
    }

    public Long getSpanFullGCCollectionCount() {
        return spanFullGCCollectionCount;
    }

    public void setSpanFullGCCollectionCount(Long spanFullGCCollectionCount) {
        this.spanFullGCCollectionCount = spanFullGCCollectionCount;
    }

    public Long getSpanFullGCCollectionTime() {
        return spanFullGCCollectionTime;
    }

    public void setSpanFullGCCollectionTime(Long spanFullGCCollectionTime) {
        this.spanFullGCCollectionTime = spanFullGCCollectionTime;
    }
}
