package com.gcloud.demo.uploaddemo.exception;

/**
 * 公共异常类
 *
 * @author sean
 *
 */
public class GcException extends RuntimeException
{
    private static final long   serialVersionUID = 1L;
    private static final String CODE_SEP         = "::";
    private String              code;
    private Object[]            params;

    public static void throw_msg(String message, Object... args)
    {
        if (args != null && args.length > 0)
        {
            message = String.format(message, args);
        }
        throw new GcException(message);
    }

    public GcException(String msg)
    {
        super(msg);
    }

    public GcException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GcException(String message, Throwable cause, Object... args)
    {
        super(String.format(message, args), cause);
    }

    public GcException(Exception e)
    {
        super(e);
    }

    public GcException(String code, String message, Object[] params)
    {
        super(code + CODE_SEP + message);
        this.code = code;
        this.params = params;
    }
    public Object[] getParams()
    {
        return params;
    }

    public GcException setParams(Object... params)
    {
        this.params = params;
        return this;
    }

    public String getCode()
    {
        if (code == null)
        {
            if (getMessage().contains(CODE_SEP))
            {
                code = getMessage().substring(0, getMessage().indexOf(CODE_SEP));
            }
            else
            {
                code = getMessage();
            }
        }
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

}
