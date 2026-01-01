package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class UtilsTest {

    // Simple data holder for objective values
    private static class Point {
        final double f1;
        final double f2;

        Point(double f1, double f2) {
            this.f1 = f1;
            this.f2 = f2;
        }
    }

    // Objective functions
    private final ToDoubleFunction<Object> f1 =
            o -> ((Point) o).f1;

    private final ToDoubleFunction<Object> f2 =
            o -> ((Point) o).f2;

    // Test hyper-volume with a single Pareto-optimal point
    @Test
    void testSinglePointHyperVolume() {
        List<Object> front = List.of(
                new Point(2.0, 3.0)
        );

        double r1 = 1.0;
        double r2 = 5.0;

        double hv = computeHyperVolume(front, f1, f2, r1, r2);

        // Rectangle: width = 2 - 1 = 1, height = 5 - 3 = 2
        assertEquals(2.0, hv);
    }

    // Test hyper-volume with multiple Pareto points
    @Test
    void testMultiplePointsHyperVolume() {
        List<Object> front = List.of(
                new Point(2.0, 4.0),
                new Point(3.0, 2.0)
        );

        double r1 = 1.0;
        double r2 = 5.0;

        double hv = computeHyperVolume(front, f1, f2, r1, r2);

        // First rectangle: (2 - 1) * (5 - 4) = 1 * 1 = 1
        // Second rectangle: (3 - 2) * (5 - 2) = 1 * 3 = 3
        // Total = 4
        assertEquals(4.0, hv);
    }

    // Test that sorting by f1 is handled correctly
    @Test
    void testUnsortedInputFront() {
        List<Object> front = List.of(
                new Point(3.0, 2.0),
                new Point(2.0, 4.0)
        );

        double r1 = 1.0;
        double r2 = 5.0;

        double hv = computeHyperVolume(front, f1, f2, r1, r2);

        assertEquals(4.0, hv);
    }

    // Test that dominated or invalid rectangles are ignored
    @Test
    void testNegativeAreaIsIgnored() {
        List<Object> front = List.of(
                new Point(0.5, 6.0) // Produces negative width and height
        );

        double r1 = 1.0;
        double r2 = 5.0;

        double hv = computeHyperVolume(front, f1, f2, r1, r2);

        // No valid rectangle → hyper-volume is zero
        assertEquals(0.0, hv);
    }

    // Test that null Pareto front throws exception
    @Test
    void testNullFrontThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> computeHyperVolume(null, f1, f2, 0.0, 0.0)
        );
    }

    // Test that empty Pareto front throws exception
    @Test
    void testEmptyFrontThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> computeHyperVolume(List.of(), f1, f2, 0.0, 0.0)
        );
    }

    // -----------------------------
    // Reference implementation hook
    // -----------------------------
    // Replace this method call with your actual implementation
    private double computeHyperVolume(
            List<Object> front,
            ToDoubleFunction<Object> f1,
            ToDoubleFunction<Object> f2,
            double r1,
            double r2
    ) {

        if (front == null || front.isEmpty()) {
            throw new IllegalArgumentException(
                    "Pareto front must not be null or empty."
            );
        }

        front = front.stream()
                .sorted((a, b) ->
                        Double.compare(
                                f1.applyAsDouble(a),
                                f1.applyAsDouble(b)))
                .toList();

        double hyperVolume = 0.0;
        double previousF1 = r1;

        for (Object chromosome : front) {
            double currentF1 = f1.applyAsDouble(chromosome);
            double currentF2 = f2.applyAsDouble(chromosome);

            double width = currentF1 - previousF1;
            double height = r2 - currentF2;

            if (width > 0.0 && height > 0.0) {
                hyperVolume += width * height;
            }

            previousF1 = currentF1;
        }

        return hyperVolume;
    }
}