package cn.uway.ucloude.data.dataaccess.model;

public enum LogicOptType {
	IsEqualTo("="),IsNotEqualTo("!="),IsLessThan("<"),IsLessThanOrEqualTo("<="),IsGreaterThanOrEqualTo(">="),IsGreaterThan(">");
	
	private String desc;
	
	private LogicOptType(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
//    /// <summary>
//    /// Left operand must be equal to the right one.
//    /// </summary>
//    //[Description("=")]
//    IsEqualTo,
//    /// <summary>
//    /// Left operand must be different from the right one.
//    /// </summary>
//    //[Description("!=")]
//    IsNotEqualTo,
//  /// <summary>
//    /// Left operand must be smaller than the right one.
//    /// </summary>
//    //[Description("<")]
//    IsLessThan(),
//    /// <summary>
//    /// Left operand must be smaller than or equal to the right one.
//    /// </summary>
//    //[Description("<=")]
//    IsLessThanOrEqualTo,
//    /// <summary>
//    /// Left operand must be larger than the right one.
//    /// </summary>
//    //[Description(">=")]
//    IsGreaterThanOrEqualTo,
//    /// <summary>
//    /// Left operand must be larger than or equal to the right one.
//    /// </summary>
//    //[Description(">")]
//    IsGreaterThan
}
