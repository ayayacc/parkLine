package com.kl.parkLine.swagger.plugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.kl.parkLine.annotation.SwaggerDisplayEnum;

import springfox.documentation.builders.PropertySpecificationBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

/**
 * @author chenc
 * @date 2020/4/15
 */
@SuppressWarnings(value = "all")
public class EnumModelPropertyBuilderPlugin implements ModelPropertyBuilderPlugin
{

    @Override
    public void apply(ModelPropertyContext context)
    {
        Optional<BeanPropertyDefinition> optional = context
                .getBeanPropertyDefinition();
        if (!optional.isPresent())
        {
            return;
        }

        final Class<?> fieldType = optional.get().getField().getRawType();

        addDescForEnum(context, fieldType);
    }

    @Override
    public boolean supports(DocumentationType delimiter)
    {
        return true;
    }

    private void addDescForEnum(ModelPropertyContext context,
            Class<?> fieldType)
    {
        if (Enum.class.isAssignableFrom(fieldType))
        {
            SwaggerDisplayEnum annotation = AnnotationUtils
                    .findAnnotation(fieldType, SwaggerDisplayEnum.class);
            if (annotation != null)
            {
                String index = annotation.value();
                String name = annotation.text();

                Object[] enumConstants = fieldType.getEnumConstants();

                List<String> displayValues = Arrays.stream(enumConstants)
                        .filter(Objects::nonNull).map(item -> {
                            Class<?> currentClass = item.getClass();

                            Field indexField = ReflectionUtils
                                    .findField(currentClass, index);
                            ReflectionUtils.makeAccessible(indexField);
                            Object value = ReflectionUtils.getField(indexField,
                                    item);

                            Field descField = ReflectionUtils
                                    .findField(currentClass, name);
                            ReflectionUtils.makeAccessible(descField);
                            Object desc = ReflectionUtils.getField(descField,
                                    item);
                            return value + ":" + desc;

                        }).collect(Collectors.toList());

                PropertySpecificationBuilder builder = context.getSpecificationBuilder();
                Field descField = ReflectionUtils.findField(builder.getClass(), "description");
                ReflectionUtils.makeAccessible(descField);
                String joinText = ReflectionUtils.getField(descField, builder)
                        + " (" + String.join("; ", displayValues) + ")";
                builder.description(joinText);
            }
        }

    }
}
