package dev.rodrigosambade.oca;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class GooseGameTest{@Test void gooseGivesExtraTurn(){var m=new GooseGame().apply(0,4);assertEquals(8,m.to());assertTrue(m.extraTurn());}@Test void deathResets(){assertEquals(0,new GooseGame().apply(53,4).to());}@Test void overshootBounces(){assertEquals(59,new GooseGame().apply(60,5).to());}}
