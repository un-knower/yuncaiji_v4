package cn.uway.ucloude.exceptionhandler;

public enum PostHandlingAction {
	/// <summary>
    /// Indicates that no rethrow should occur.
    /// </summary>
    None,
    /// <summary>
    /// Notify the caller that a rethrow is recommended.
    /// </summary>
    NotifyRethrow,
    /// <summary>
    /// Throws the exception after the exception has been handled by all handlers in the chain.
    /// </summary>
    ThrowNewException
}
