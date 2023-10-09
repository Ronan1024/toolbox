package io.github.Ronan1024.toolbox.core.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * @author L.J.Ran
 * @version 1.0
 */
public class Convert {

    private Convert() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> void copyProperties(Object source, T target) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        copyProperties(source, target, false);
    }

    public static void copyProperties(Object source, Object target, boolean ignoreNull) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        copyProperties(source, target, ignoreNull, new SFunction[0]);
    }

    public static <T> T copyBean(Object source, T target) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return copyBean(source, target, false);
    }

    @SafeVarargs
    public static <T, TG> TG copyBean(Object source, TG target, boolean ignoreNull, SFunction<T, ?>... ignoreProperties) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        copyProperties(source, target, ignoreNull, ignoreProperties);
        return target;
    }


    @SafeVarargs
    public static <T> void copyProperties(Object source, Object target, boolean ignoreNull, SFunction<T, ?>... ignoreProperties) throws RuntimeException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Set<String> ignoredProperties = new HashSet<>();
        // 处理被忽略的属性
        for (SFunction<T, ?> sf : ignoreProperties) {
            String propertyName = BeanUtil.getFieldName(sf);
            ignoredProperties.add(propertyName);
        }
        PropertyDescriptor[] propertyDescriptors;
        try {
            propertyDescriptors = Introspector.getBeanInfo(source.getClass()).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Failed to get bean info.");
        }
        // 如果需要忽略null属性，将源对象中为null的属性添加到忽略列表中
        if (ignoreNull) {
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method readMethod = propertyDescriptor.getReadMethod();
                Object value = readMethod.invoke(source);
                if (value == null) {
                    ignoredProperties.add(propertyDescriptor.getName());
                }
            }
        }

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            // 如果属性在忽略列表中，则跳过
            if (ignoredProperties.contains(propertyName)) {
                continue;
            }
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (readMethod != null && writeMethod != null) {
                Object value = readMethod.invoke(source);
                writeMethod.invoke(target, value);
            }
        }
    }
    public static Map<String, Object> beanToMap(Object bean) throws IntrospectionException {
        Map<String, Object> map = new HashMap<>(16);
        BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            Object value = BeanUtil.getProperty(bean, propertyName);
            map.put(propertyName, value);
        }

        return map;
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IntrospectionException {
        T bean = beanClass.getDeclaredConstructor().newInstance();
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            if (map.containsKey(propertyName)) {
                Object value = map.get(propertyName);
                BeanUtil.setProperty(bean, propertyName, value);
            }
        }
        return bean;
    }

}
