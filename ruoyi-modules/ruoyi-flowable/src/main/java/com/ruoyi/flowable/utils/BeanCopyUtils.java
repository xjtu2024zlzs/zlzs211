package com.ruoyi.flowable.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean copy utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanCopyUtils {

    public static <T, V> V copy(T source, Class<V> desc) {
        if (ObjectUtil.isNull(source) || ObjectUtil.isNull(desc)) {
            return null;
        }
        final V target = ReflectUtil.newInstanceIfPossible(desc);
        return copy(source, target);
    }

    public static <T, V> V copy(T source, V desc) {
        if (ObjectUtil.isNull(source) || ObjectUtil.isNull(desc)) {
            return null;
        }
        BeanUtil.copyProperties(source, desc);
        return desc;
    }

    public static <T, V> List<V> copyList(List<T> sourceList, Class<V> desc) {
        if (ObjectUtil.isNull(sourceList)) {
            return null;
        }
        if (CollUtil.isEmpty(sourceList)) {
            return CollUtil.newArrayList();
        }
        return StreamUtils.toList(sourceList, source -> copy(source, desc));
    }

    public static <T> Map<String, Object> copyToMap(T bean) {
        if (ObjectUtil.isNull(bean)) {
            return null;
        }
        return BeanUtil.beanToMap(bean);
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) {
        if (MapUtil.isEmpty(map) || ObjectUtil.isNull(beanClass)) {
            return null;
        }
        T bean = ReflectUtil.newInstanceIfPossible(beanClass);
        return mapToBean(map, bean);
    }

    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        if (MapUtil.isEmpty(map) || ObjectUtil.isNull(bean)) {
            return null;
        }
        BeanUtil.fillBeanWithMap(map, bean, true);
        return bean;
    }

    public static <T, V> Map<String, V> mapToMap(Map<String, T> map, Class<V> clazz) {
        if (MapUtil.isEmpty(map) || ObjectUtil.isNull(clazz)) {
            return null;
        }
        Map<String, V> copyMap = new LinkedHashMap<>(map.size());
        map.forEach((k, v) -> copyMap.put(k, copy(v, clazz)));
        return copyMap;
    }
}
