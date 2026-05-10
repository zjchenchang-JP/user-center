package com.zjcc.usercenter.utils;

import java.util.List;
import java.util.Objects;

/**
* 算法工具类
*
* @author yupi
*/
public class AlgorithmUtils {

/**
* 编辑距离算法（用于计算最相似的两组标签）
* 距离值越小 越相似
* @param tagList1
* @param tagList2
* @return
*/
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        // 直接取出所有用户，依次和当前用户计算分数，取 TOP N（54 秒）
        // 优化方法：
        // 1. 切忌不要在数据量大的时候循环输出日志（取消掉日志后 20 秒）
        // 2. Map 存了所有的分数信息，占用内存解决：维护一个固定长度的有序集合（sortedSet），只保留分数最高的几个用户（时间换空间）
        // e.g.【3, 4, 5, 6, 7】取 TOP 5，id 为 1 的用户就不用放进去了
        // 3. 细节：剔除自己 √
        // 4. 尽量只查需要的数据：
        //   a. 过滤掉标签为空的用户 √
        //   b. 根据部分标签取用户（前提是能区分出来哪个标签比较重要）
        //   c. 只查需要的数据（比如 id 和 tags） √（7.0s）
        // 5. 提前查？（定时任务）
        //   a. 提前把所有用户给缓存（不适用于经常更新的数据）
        //   b. 提前运算出来结果，缓存（针对一些重点用户，提前缓存）

        // 大数据推荐，比如说有几亿个商品，难道要查出来所有的商品？
        // 难道要对所有的数据计算一遍相似度？
        // 匹配通用
        //  检索 => 召回 => 粗排 => 精排 => 重排序等等
        // 检索：尽可能多地查符合要求的数据（比如按记录查）
        // 召回：查询可能要用到的数据（不做运算）
        // 粗排：粗略排序，简单地运算（运算相对轻量）
        // 精排：精细排序，确定固定排位

        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }

/**
* 编辑距离算法（用于计算最相似的两个字符串）
* 原理：https://blog.csdn.net/DBC_121/article/details/104198838
*
* @param word1
* @param word2
* @return
*/
    public static int minDistance(String word1, String word2) {
        int n = word1.length();
        int m = word2.length();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (word1.charAt(i - 1) != word2.charAt(j - 1)) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }
}