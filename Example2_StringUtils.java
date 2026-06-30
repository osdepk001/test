package com.mycompany.myapp;

import java.util.*;

/**
 * 示例程序2：字符串处理工具类
 * 演示常见字符串操作和算法
 */
public class Example2_StringUtils {

    /**
     * 反转字符串
     */
    public static String reverse(String input) {
        if (input == null) {
            return null;
        }
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * 判断字符串是否为回文（忽略大小写和非字母数字字符）
     */
    public static boolean isPalindrome(String input) {
        if (input == null) {
            return false;
        }
        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }

    /**
     * 统计每个字符出现的次数
     */
    public static Map<Character, Integer> charCount(String input) {
        Map<Character, Integer> countMap = new HashMap<>();
        if (input == null) {
            return countMap;
        }
        for (char c : input.toCharArray()) {
            countMap.put(c, countMap.getOrDefault(c, 0) + 1);
        }
        return countMap;
    }

    /**
     * 移除字符串中所有空白字符
     */
    public static String removeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+", "");
    }

    /**
     * 找出字符串中第一个不重复的字符（返回字符，没有则返回null）
     */
    public static Character firstNonRepeatingChar(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        Map<Character, Integer> countMap = new LinkedHashMap<>();
        for (char c : input.toCharArray()) {
            countMap.put(c, countMap.getOrDefault(c, 0) + 1);
        }
        for (Map.Entry<Character, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 将字符串中的字母大小写互换
     */
    public static String swapCase(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            } else if (Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
