package com.ruoyi.project3.assembler.algorithm;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class AlgorithmParamReader
{
    private AlgorithmParamReader()
    {
    }

    public static Object first(Map<String, Object> source, String... names)
    {
        if (source == null)
        {
            return null;
        }
        for (String name : names)
        {
            if (source.containsKey(name))
            {
                return source.get(name);
            }
        }
        return null;
    }

    public static String requiredText(Map<String, Object> source, String fieldName, String... names)
    {
        String value = optionalText(source, names);
        if (value == null)
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        return value;
    }

    public static String optionalText(Map<String, Object> source, String... names)
    {
        return text(first(source, names));
    }

    @SuppressWarnings("unchecked")
    public static Object nested(Map<String, Object> source, String objectName, String fieldName)
    {
        Object value = first(source, objectName);
        if (value instanceof Map)
        {
            return ((Map<String, Object>) value).get(fieldName);
        }
        return null;
    }

    public static String text(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    public static Double requiredNumber(Map<String, Object> source, String fieldName, String... names)
    {
        Double value = optionalNumber(source, names);
        if (value == null)
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        return value;
    }

    public static Double optionalNumber(Map<String, Object> source, Double fallback, String... names)
    {
        Double value = optionalNumber(source, names);
        return value == null ? fallback : value;
    }

    public static Double optionalNumber(Map<String, Object> source, String... names)
    {
        Object value = first(source, names);
        if (value == null || String.valueOf(value).trim().isEmpty())
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        try
        {
            return Double.parseDouble(String.valueOf(value).trim());
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("算法参数格式错误：" + names[0]);
        }
    }

    public static Double number(Map<String, Object> source, Double fallback, String... names)
    {
        return optionalNumber(source, fallback, names);
    }

    public static Long requiredLong(Object value, String fieldName)
    {
        Long ret = optionalLong(value, fieldName);
        if (ret == null)
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        return ret;
    }

    public static Long optionalLong(Object value, String fieldName)
    {
        return longVal(value, fieldName);
    }

    public static Long longVal(Object value, String fieldName)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            String text = String.valueOf(value).trim();
            if (text.isEmpty())
            {
                return null;
            }
            return Long.parseLong(text);
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException(fieldName + "格式不正确");
        }
    }

    public static String rawId(String nodeId)
    {
        if (nodeId == null)
        {
            return null;
        }
        int idx = nodeId.indexOf(':');
        return idx < 0 || idx == nodeId.length() - 1 ? nodeId : nodeId.substring(idx + 1);
    }

    public static String nodeType(String nodeId)
    {
        if (nodeId == null)
        {
            return null;
        }
        int idx = nodeId.indexOf(':');
        return idx <= 0 ? null : nodeId.substring(0, idx);
    }

    public static List<Long> sampleIds(Object value, String fieldName)
    {
        return optionalLongList(value, fieldName);
    }

    public static List<Long> requiredLongList(Object value, String fieldName)
    {
        List<Long> ids = optionalLongList(value, fieldName);
        if (ids.isEmpty())
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        return ids;
    }

    public static List<Long> optionalLongList(Object value, String fieldName)
    {
        if (value == null)
        {
            return Collections.emptyList();
        }

        List<Long> ids = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                Long id = longVal(item, fieldName);
                if (id != null)
                {
                    ids.add(id);
                }
            }
            return ids;
        }

        String raw = text(value);
        if (raw == null)
        {
            return Collections.emptyList();
        }
        if (raw.startsWith("["))
        {
            try
            {
                return JSON.parseArray(raw, Long.class);
            }
            catch (Exception e)
            {
                throw new ServiceException(fieldName + "不是合法的数组");
            }
        }

        for (String part : raw.split(","))
        {
            String item = text(part);
            if (item != null)
            {
                ids.add(longVal(item, fieldName));
            }
        }
        return ids;
    }
}
