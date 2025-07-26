package com.rogue01.map.utils;

import java.util.Random;

public class RandomUtils {
    private static Random random = new Random();
    
    /**
     * 시드 설정
     */
    public static void setSeed(long seed) {
        random.setSeed(seed);
    }
    
    /**
     * min과 max 사이의 랜덤 정수 반환 (포함)
     */
    public static int nextInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * 0과 max 사이의 랜덤 정수 반환 (포함)
     */
    public static int nextInt(int max) {
        return random.nextInt(max + 1);
    }
    
    /**
     * 확률에 따른 랜덤 선택
     */
    public static boolean nextBoolean(double probability) {
        return random.nextDouble() < probability;
    }
    
    /**
     * 배열에서 랜덤 요소 선택
     */
    public static <T> T randomChoice(T[] array) {
        if (array.length == 0) return null;
        return array[random.nextInt(array.length)];
    }
    
    /**
     * 두 점 사이의 거리 계산
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * 맨해튼 거리 계산
     */
    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }
} 