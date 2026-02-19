package com.rogue01.map.utils;

import java.util.Random;

/**
 * 랜덤 유틸리티 클래스 - 인스턴스 기반으로 변경하여 스레드 안전성 향상
 */
public class RandomUtils {
    private final Random random;
    
    /**
     * 기본 생성자 - 현재 시간을 시드로 사용
     */
    public RandomUtils() {
        this(System.currentTimeMillis());
    }
    
    /**
     * 시드를 지정하는 생성자
     */
    public RandomUtils(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * 기존 Random 인스턴스를 사용하는 생성자
     */
    public RandomUtils(Random random) {
        this.random = random;
    }
    
    /**
     * 시드 설정
     */
    public void setSeed(long seed) {
        random.setSeed(seed);
    }
    
    /**
     * min과 max 사이의 랜덤 정수 반환 (포함)
     */
    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * 0과 max 사이의 랜덤 정수 반환 (포함)
     */
    public int nextInt(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max cannot be negative");
        }
        return random.nextInt(max + 1);
    }
    
    /**
     * 확률에 따른 랜덤 선택
     */
    public boolean nextBoolean(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0");
        }
        return random.nextDouble() < probability;
    }
    
    /**
     * 배열에서 랜덤 요소 선택
     */
    public <T> T randomChoice(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }
    
    /**
     * 리스트에서 랜덤 요소 선택
     */
    public <T> T randomChoice(java.util.List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * 두 점 사이의 유클리드 거리 계산
     */
    public double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * 맨해튼 거리 계산
     */
    public int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }
    
    /**
     * 체비셰프 거리 계산 (체스에서 킹의 이동 거리)
     */
    public int chebyshevDistance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
    
    /**
     * 내부 Random 인스턴스 반환 (고급 사용자용)
     */
    public Random getRandom() {
        return random;
    }
} 