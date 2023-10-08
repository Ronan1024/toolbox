package com.ronan.toolbox.core.utils;

/**
 * @author L.J.Ran
 * @version 1.0
 */
public class StringUtil {


    /**
     * <p>判断字符串是否为空</p> </br>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code StrUtil.isEmpty(null)   // true}</li>
     *     <li>{@code StrUtil.isEmpty("")     // true}</li>
     *     <li>{@code StrUtil.isEmpty(" ")    // false}</li>
     *     <li>{@code StrUtil.isEmpty("str")  // false}</li>
     * </ul>
     *
     * @param str 字符串
     * @return 如果字符串为空返回True，否则返回False
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * <p>判断字符串是否为非空</p> </br>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code StrUtil.isNotEmpty(null)       // false}</li>
     *     <li>{@code StrUtil.isNotEmpty("")         // false}</li>
     *     <li>{@code StrUtil.isNotEmpty(" ")        // true}</li>
     *     <li>{@code StrUtil.isNotEmpty("str")      // true}</li>
     *     <li>{@code StrUtil.isNotEmpty("  str  ")  // true}</li>
     * </ul>
     *
     * @param str 字符串
     * @return 如果字符串为非空返回True，否则返回False
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * <p>判断字符串是否为空白</p> </br>
     * <p>例如：</p>
     * <ul>
     *     <li>{@code StrUtil.isBlank(null)       // true}</li>
     *     <li>{@code StrUtil.isBlank("")         // true}</li>
     *     <li>{@code StrUtil.isBlank(" ")        // true}</li>
     *     <li>{@code StrUtil.isBlank("str")      // false}</li>
     *     <li>{@code StrUtil.isBlank("  str  ")  // false}</li>
     * </ul>
     *
     * @param str 字符串
     * @return 如果字符串为空白返回True，否则返回False
     */
    public static boolean isEmptyCharacter(CharSequence str) {
        if (str == null || str.isEmpty()) {
            return true;
        } else {
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
