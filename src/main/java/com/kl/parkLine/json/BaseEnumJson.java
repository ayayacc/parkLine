package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@ApiModel("枚举类型字典")
public class BaseEnumJson
{
    @ApiModelProperty(required = true, name="值")
    private String value;
    
    @ApiModelProperty(required = true, name="文本")
    private String text;
}
