package com.rogue01;

import com.rogue01.game.Game;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Swing 창 생성과 게임 루프(Swing Timer)를 모두 EDT에서 실행
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.start();
        });
    }
}