package com.mycompany.myapp;

/**
 * 示例程序1：数学计算工具类
 * 提供基本的加减乘除运算及扩展功能
 */
public class Example1_Calculator {

    /**
     * 加法
     */
    public static int add(int a, int b) {
        return a + b;
    }

    /**
     * 减法
     */
    public static int subtract(int a, int b) {
        return a - b;
    }

    /**
     * 乘法
     */
    public static int multiply(int a, int b) {
        return a * b;
    }

    /**
     * 除法（整数除法，自动向下取整）
     * @throws ArithmeticException 当除数为0时
     */
    public static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return a / b;
    }

    /**
     * 计算数组平均值（整数）
     */
    public static double average(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return (double) sum / numbers.length;
    }

    /**
     * 阶乘计算（递归示例）
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("阶乘不允许负数");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    /**
     * 判断是否为质数
     */
    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
