package com.kl.parkLine.component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.enums.BaseEnum;

@Component
public class EnumConvertFactory implements ConverterFactory<String, BaseEnum>
{

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(
            Class<T> targetType)
    {
        return new StringToEnum<>(targetType);
    }
    
    @SuppressWarnings("all")
    private static class StringToEnum<T extends BaseEnum> implements Converter<String, T> 
    {
        private Class<T> targerType;
        public StringToEnum(Class<T> targerType) 
        {
            this.targerType = targerType;
        }

        @Override
        public T convert(String source)
        {
            if (StringUtils.isEmpty(source)) 
            {
                return null;
            }
            return (T) EnumConvertFactory.getBaseEnum(this.targerType, source);
        }
    }
    
     public static <T extends BaseEnum> Object getBaseEnum(Class<T> targerType, String source) 
     {
        for (T enumObj : targerType.getEnumConstants()) 
        {
            if (source.equals(String.valueOf(enumObj.getValue()))) 
            {
                return enumObj;
            }
        }
        return null;
    }
}
