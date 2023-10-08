package com.ronan.toolbox.core.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


/**
 * Bean util
 *
 * @author L.J.Ran
 * @version 1.0
 */
public class BeanUtil {

    private BeanUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 判断对象是否为非空
     *
     * @param obj
     * @return
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return ((Optional<?>) obj).isEmpty();
        } else if (obj instanceof String) {
            return ((CharSequence) obj).isEmpty();
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        } else {
            return false;
        }
    }


    public static <T> void setProperty(Object bean, SFunction<T, ?> fn, Object value) {
        String propertyName = BeanUtil.getFieldName(fn);
        setProperty(bean, propertyName, value);
    }

    public static void setProperty(Object bean, String propertyName, Object value) {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean.getClass(), propertyName);
        assert propertyDescriptor != null;
        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (BeanUtil.isNotEmpty(writeMethod)) {
            invoke(writeMethod, bean, value);
        } else {
            throw new IllegalArgumentException("Property '" + propertyName + "' is not writable.");
        }
    }


    public static <T> Object getProperty(Object bean, SFunction<T, ?> fn) {
        String propertyName = getFieldName(fn);
        return getProperty(bean, propertyName);
    }

    public static Object getProperty(Object bean, String propertyName) {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(bean.getClass(), propertyName);
        assert propertyDescriptor != null;
        Method readMethod = propertyDescriptor.getReadMethod();
        if (BeanUtil.isNotEmpty(readMethod)) {
            return invoke(readMethod, bean);
        } else {
            throw new IllegalArgumentException("Property '" + propertyName + "' is not readable.");
        }
    }

    public static <T> String getFieldName(SFunction<T, ?> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();

        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            methodName = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            methodName = methodName.substring(2);
        } else {
            throw new IllegalArgumentException("Invalid Getter or Setter or is  name:" + methodName);
        }
        return toLowerCaseFirstOne(methodName);
    }

    private static SerializedLambda getSerializedLambda(Serializable fn) {
        Method method;
        try {
            method = fn.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to get writeReplace method");
        }
        return (SerializedLambda) invoke(method, fn);
    }

    private static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Failed to get bean info");
        }
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyName.equals(propertyDescriptor.getName())) {
                return propertyDescriptor;
            }
        }

        return null;
    }

    private static String toLowerCaseFirstOne(String field) {
        if (Character.isLowerCase(field.charAt(0))) {
            return field;
        } else {
            char firstOne = Character.toLowerCase(field.charAt(0));
            String other = field.substring(1);
            return firstOne + other;
        }
    }

    private static Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to invoke method");
        }
    }
}
