package dev.rodrigosambade.oca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GooseGameTest {

    private final GooseGame game = new GooseGame();

    @Test
    void gooseMovesPlayerAndGrantsExtraTurn() {
        GooseGame.Move move = game.apply(0, 4);

        assertEquals(8, move.to());
        assertTrue(move.extraTurn());
    }

    @Test
    void deathSquareResetsPlayer() {
        GooseGame.Move move = game.apply(53, 4);

        assertEquals(0, move.to());
    }

    @Test
    void overshootBouncesBackFromFinalSquare() {
        GooseGame.Move move = game.apply(60, 5);

        assertEquals(59, move.to());
    }
}
