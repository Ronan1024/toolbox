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
import java.util.*;

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
     * @param obj 待判断的对象
     * @return 空返回false，非空返回true
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 待判断的对象
     * @return 空返回true，非空返回false
     */
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


    /**
     * <p>设置对象指定字段的值</p>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code setProperty(user, User::getName, "张三")}</li>
     * </ul>
     *
     * @param bean  目标对象
     * @param fn    字段名
     * @param value 字段值
     * @param <T>   目标对象类型
     */
    public static <T> void setProperty(Object bean, SFunction<T, ?> fn, Object value) {
        String propertyName = BeanUtil.getFieldName(fn);
        setProperty(bean, propertyName, value);
    }

    /**
     * <p>设置对象指定字段的值</p>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code setProperty(user, "name", "张三")}</li>
     *     <li>{@code setProperty(user, "age", 18)}</li>
     * </ul>
     *
     * @param bean         目标对象
     * @param propertyName 字段名
     * @param value        字段值
     */
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


    /**
     * <p>获取对象指定字段的值</p>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code getProperty(user, User::getName)}</li>
     * </ul>
     *
     * @param bean 目标对象
     * @param fn   字段名
     * @param <T>  目标对象类型
     * @return 字段值
     */
    public static <T> Object getProperty(Object bean, SFunction<T, ?> fn) {
        String propertyName = getFieldName(fn);
        return getProperty(bean, propertyName);
    }

    /**
     * <p>获取对象指定字段的值</p>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code getProperty(user, "name")}</li>
     *     <li>{@code getProperty(user, "age")}</li>
     * </ul>
     *
     * @param bean         目标对象
     * @param propertyName 字段名
     * @return 字段值
     */
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

    /**
     * <p>获取Getter方法名对应的字段名称</p></br>
     * <p>例如： </p>
     * <ul>
     *     <li>{@code getFieldName(User::getName)  // name}</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param fn Getter或Setter方法名
     * @return 字段名称
     */
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        return getFieldNameStr(methodName);
    }

    /**
     * <p>获取Getter或Setter方法名对应的字段名称</p></br>
     * <p>例如： </p>
     * <ul>
     *     <li>{@code getFieldName("getName")  // name}</li>
     *     <li>{@code getFieldName("setName")  // name}</li>
     *     <li>{@code getFieldName("isName")   // name}</li>
     *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @param getOrSetName Getter或Setter方法名
     * @return 字段名称
     */
    public static String getFieldNameStr(String getOrSetName) {
        if (getOrSetName.startsWith("get") || getOrSetName.startsWith("set")) {
            getOrSetName = getOrSetName.substring(3);
        } else if (getOrSetName.startsWith("is")) {
            getOrSetName = getOrSetName.substring(2);
        } else {
            throw new IllegalArgumentException("Invalid Getter or Setter or is  name:" + getOrSetName);
        }
        return toLowerCaseFirstOne(getOrSetName);
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
            throw new IllegalStateException(e.getMessage());
        }
    }

}
