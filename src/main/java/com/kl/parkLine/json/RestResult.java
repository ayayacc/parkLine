package com.kl.parkLine.json;

import com.kl.parkLine.enums.RetCode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("api通用返回数据")
public class RestResult<T>
{
    @ApiModelProperty("标识代码,0表示成功，非0表示出错")
    private RetCode retCode;

    @ApiModelProperty("提示信息,供报错时使用")
    private String msg;

    @ApiModelProperty("返回的数据")
    private T data;

    /**
     * 处理成功返回
     *
     * @return
     */
    public static <T> RestResult<T> success()
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.ok);
        result.setMsg("操作成功");
        return result;
    }
    
    /**
     * 处理成功返回
     *
     * @return
     */
    public static <T> RestResult<T> success(String msg) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.ok);
        result.setMsg(msg);
        return result;
    }

    /**
     * 处理成功返回
     *
     * @return
     */
    public static <T> RestResult<T> success(String msg, T obj) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.ok);
        result.setMsg("success");
        result.setData(obj);
        return result;
    }
    
    /**
     * 处理成功返回
     *
     * @return
     */
    public static <T> RestResult<T> success(T obj) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.ok);
        result.setMsg("success");
        result.setData(obj);
        return result;
    }
    
    /**
     * 处理失败返回
     *
     * @return
     */
    public static <T> RestResult<T> failed()
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.failed);
        result.setMsg("操作失败");
        return result;
    }
    
    /**
     * 处理失败返回
     *
     * @return
     */
    public static <T> RestResult<T> failed(T obj, String msg) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.failed);
        result.setMsg(msg);
        result.setData(obj);
        return result;
    }

    /**
     * 处理异常返回
     *
     * @param msg
     * @return
     */
    public static <T> RestResult<T> failed(String msg) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(RetCode.failed);
        result.setMsg(msg);
        return result;
    }
    
    /**
     * 处理异常返回
     *
     * @param msg
     * @return
     */
    public static <T> RestResult<T> failed(RetCode retCode, String msg) 
    {
        RestResult<T> result = new RestResult<T>();
        result.setRetCode(retCode);
        result.setMsg(msg);
        return result;
    }
}
