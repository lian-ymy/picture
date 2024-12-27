package com.example.picture.utils;

public class ColorSimilarity {

    /**
     * 计算两个十六进制颜色之间的相似度。
     * @param color1 第一个颜色的十六进制表示，如 "0xFFFFFF"。
     * @param color2 第二个颜色的十六进制表示，如 "0xFF0000"。
     * @return 相似度分数，1表示完全相同，数值越小表示差异越大。
     */
    public static double calculateColorSimilarity(String color1, String color2) {
        // 将十六进制颜色字符串转换为RGB整数
        int rgb1 = Integer.parseInt(color1.substring(2), 16);
        int rgb2 = Integer.parseInt(color2.substring(2), 16);

        // 分解RGB值
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        // 计算RGB三个通道的差异
        double diffR = Math.pow(r1 - r2, 2);
        double diffG = Math.pow(g1 - g2, 2);
        double diffB = Math.pow(b1 - b2, 2);

        // 计算欧几里得距离
        double similarityScore = Math.sqrt(diffR + diffG + diffB);

        // 归一化相似度分数，这里假设最大差异为255^2*3
        double maxDifference = Math.pow(255, 2) * 3;
        return (similarityScore / maxDifference);
    }

    public static void main(String[] args) {
        String color1 = "0xFFFFFF"; // 白色
        String color2 = "0xFF0000"; // 红色
        double similarity = calculateColorSimilarity(color1, color2);
        System.out.println("Similarity score: " + similarity);
    }
}