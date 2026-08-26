package dev.rodrigosambade.oca;

import java.util.Map;

public final class GooseGame {

    public static final int LAST = 62;

    private static final Map<Integer, Integer> GEESE = Map.ofEntries(
            Map.entry(4, 8),
            Map.entry(8, 13),
            Map.entry(13, 17),
            Map.entry(17, 22),
            Map.entry(22, 26),
            Map.entry(26, 31),
            Map.entry(31, 35),
            Map.entry(35, 40),
            Map.entry(40, 44),
            Map.entry(44, 49),
            Map.entry(49, 53),
            Map.entry(53, 58));

    public record Move(
            int from,
            int rolled,
            int to,
            boolean extraTurn,
            int skipTurns,
            String message,
            boolean won) {
    }

    public Move apply(int from, int roll) {
        validateMove(from, roll);

        int position = bounceIfNecessary(from + roll);
        SpecialSquare specialSquare = applySpecialSquare(position);

        return new Move(
                from,
                roll,
                specialSquare.position(),
                specialSquare.extraTurn(),
                specialSquare.skipTurns(),
                specialSquare.message(),
                specialSquare.position() == LAST);
    }

    private static void validateMove(int from, int roll) {
        if (from < 0 || from > LAST) {
            throw new IllegalArgumentException("Invalid starting position: " + from);
        }
        if (roll < 1 || roll > 6) {
            throw new IllegalArgumentException("Die roll must be between 1 and 6");
        }
    }

    private static int bounceIfNecessary(int position) {
        if (position <= LAST) {
            return position;
        }
        return LAST - (position - LAST);
    }

    private static SpecialSquare applySpecialSquare(int position) {
        Integer gooseDestination = GEESE.get(position);
        if (gooseDestination != null) {
            return new SpecialSquare(
                    gooseDestination,
                    true,
                    0,
                    "De oca a oca y tiro porque me toca");
        }

        return switch (position) {
            case 5 -> new SpecialSquare(
                    11, true, 0,
                    "De puente a puente y tiro porque me lleva la corriente");
            case 11 -> new SpecialSquare(
                    5, true, 0,
                    "De puente a puente y tiro porque me lleva la corriente");
            case 25 -> new SpecialSquare(
                    52, true, 0,
                    "De dado a dado y tiro porque me ha tocado");
            case 52 -> new SpecialSquare(
                    25, true, 0,
                    "De dado a dado y tiro porque me ha tocado");
            case 18 -> new SpecialSquare(
                    position, false, 2,
                    "Posada: pierdes 2 turnos");
            case 30 -> new SpecialSquare(
                    position, false, 2,
                    "Pozo: pierdes 2 turnos");
            case 41 -> new SpecialSquare(
                    29, false, 0,
                    "Laberinto: vuelves a la casilla 30");
            case 51 -> new SpecialSquare(
                    position, false, 3,
                    "Cárcel: pierdes 3 turnos");
            case 57 -> new SpecialSquare(
                    0, false, 0,
                    "Muerte: vuelves a empezar");
            default -> new SpecialSquare(position, false, 0, "");
        };
    }

    private record SpecialSquare(
            int position,
            boolean extraTurn,
            int skipTurns,
            String message) {
    }
}
