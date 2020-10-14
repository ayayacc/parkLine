package com.kl.parkLine.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kl.parkLine.util.Const;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("api通用返回数据")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResult<T>
{
    @ApiModelProperty("标识代码,0表示成功，非0表示出错")
    private Integer retCode;

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
        result.setRetCode(Const.RET_OK);
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
        result.setRetCode(Const.RET_OK);
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
        result.setRetCode(Const.RET_OK);
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
        result.setRetCode(Const.RET_OK);
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
        result.setRetCode(Const.RET_FAILED);
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
        result.setRetCode(Const.RET_FAILED);
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
        result.setRetCode(Const.RET_FAILED);
        result.setMsg(msg);
        return result;
    }
}
