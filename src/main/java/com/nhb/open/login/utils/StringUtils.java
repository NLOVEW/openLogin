package com.nhb.open.login.utils;

import java.io.UnsupportedEncodingException;

/**
 * @author luck_nhb
 */
public class StringUtils {

    /**
     * 比较两个字符串（大小写敏感）
     *
     * @param str1 要比较的字符串1
     * @param str2 要比较的字符串2
     * @return 如果两个字符串相同, 或者都是null, 则返回true
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    /**
     * 判断字符串是否为null或者长度为0
     *
     * @param cs 字符串
     * @return 若为null或长度为0 则返回true 否则返回false
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断字符不为空或者长度不为0
     *
     * @param cs 字符串
     * @return 若字符串不为空或者字符串长度不为0  则返回true  否则为false
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !StringUtils.isEmpty(cs);
    }

    /**
     * 判断字符串是否为null或者空格或者长度为0
     *
     * @param cs 字符串
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 与isBlank(CharSequence cs)相反
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !StringUtils.isBlank(cs);
    }

    /**
     * @param content 需要加密的字符串
     * @param charset 字符集
     * @return 加密后的字节数组
     */
    public static byte[] getContentBytes(String content, String charset) {
        if (StringUtils.isEmpty(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("转码过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}
