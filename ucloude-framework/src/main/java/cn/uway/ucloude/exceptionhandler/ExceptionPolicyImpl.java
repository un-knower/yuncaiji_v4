package cn.uway.ucloude.exceptionhandler;

import java.util.Map;

public class ExceptionPolicyImpl {
	
	private Map<Class<?>, ExceptionPolicyEntry> policyEntries;
	
	
    public ExceptionPolicyImpl(String policyName, Map<Class<?>, ExceptionPolicyEntry> policyEntries)
    {
        //if (policyEntries == null) throw new ("policyEntries");
        //if (string.IsNullOrEmpty(policyName)) throw new ArgumentException(Resources.ExceptionStringNullOrEmpty, "policyName");

        this.policyEntries = policyEntries;
        this.policyName = policyName;

        InjectPolicyNameIntoEntries();
    }
	
    public boolean handleException(Exception exceptionToHandle) throws Exception
    {
        //if (exceptionToHandle == null) throw new ArgumentNullException("exceptionToHandler");

        ExceptionPolicyEntry entry = GetPolicyEntry(exceptionToHandle);

        if (entry == null)
        {
            return true;
        }

        return entry.handle(exceptionToHandle);
    }
    
    private ExceptionPolicyEntry GetPolicyEntry(Exception ex)
    {
        Class<?> exceptionType = ex.getClass();
        ExceptionPolicyEntry entry = this.FindExceptionPolicyEntry(exceptionType);
        return entry;
    }
    
    /// <summary>
    /// Gets the policy entry associated with the specified key.
    /// </summary>
    /// <param name="exceptionType">Type of the exception.</param>
    /// <returns>The <see cref="ExceptionPolicyEntry"/> corresponding to this exception type.</returns>
    public ExceptionPolicyEntry GetPolicyEntry(Class<?> exceptionType)
    {
        if (policyEntries.containsKey(exceptionType))
        {
            return policyEntries.get(exceptionType);
        }
        return null;
    }

    /// <devDoc>
    /// Traverses the specified type's inheritance hiearchy
    /// </devDoc>
    private ExceptionPolicyEntry FindExceptionPolicyEntry(Class<?> exceptionType)
    {
        ExceptionPolicyEntry entry = null;

        while (exceptionType != Object.class.getClass())
        {
            entry = GetPolicyEntry(exceptionType);

            if (entry == null)
            {
                exceptionType = exceptionType.getSuperclass();
            }
            else
            {
                //we've found the handlers, now continue on
                break;
            }
        }

        return entry;
    }

    private void InjectPolicyNameIntoEntries()
    {
        for(ExceptionPolicyEntry entry : policyEntries.values())
        {
            entry.setPolicyName(this.getPolicyName());
        }
    }

    /// <summary>
    /// Name of this exception policy.
    /// </summary>
    private String policyName;


	public String getPolicyName() {
		return policyName;
	}

	private void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
}
