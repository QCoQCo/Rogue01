package com.rogue01.game;

public enum GameState {
    MENU, // 메인 메뉴
    PLAYING, // 게임 진행 중
    PAUSED, // 일시정지
    INVENTORY, // 인벤토리 창
    MAP_VIEW, // 맵 뷰
    BATTLE, // 전투 중 (JRPG 스타일)
    BOSS_DOOR_PROMPT, // 보스방 문 앞 "들어가시겠습니까?" (F=진입, ESC=취소)
    GAME_OVER, // 게임 오버
    CHAPTER_TRANSITION, // "다음 챕터로 넘어가는중..." 연출 (1-3, 2-3 보스 처치 후)
    GAME_CLEAR // 게임 클리어 (3-3 탈출구 도달)
}