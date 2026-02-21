package com.rogue01.map;

import com.rogue01.map.structures.Room;
import java.util.List;

public interface MapGenerator {
    /**
     * 맵을 생성합니다.
     * 
     * @param width  맵 너비
     * @param height 맵 높이
     * @return 생성된 타일 배열
     */
    Tile[][] generate(int width, int height);

    /**
     * 맵 생성 시 시드값을 설정합니다.
     * 
     * @param seed 랜덤 시드
     */
    void setSeed(long seed);

    /**
     * 생성된 맵의 정보를 반환합니다.
     * 
     * @return 맵 생성 정보
     */
    MapGenerationInfo getGenerationInfo();

    /**
     * 생성된 맵의 방 목록을 반환합니다.
     * 
     * @return 방 목록
     */
    List<Room> getRooms();
}