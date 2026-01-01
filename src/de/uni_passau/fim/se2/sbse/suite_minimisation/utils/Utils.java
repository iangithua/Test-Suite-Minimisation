package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Utils {

    /**
     * Parses a coverage matrix from a string.
     *
     * @param matrixFile the string representation of the coverage matrix
     * @return the parsed coverage matrix
     * @throws IOException if the supplied file could not be read
     */
    public static boolean[][] parseCoverageMatrix(File matrixFile) throws IOException {
        List<String> matrix = Files.readAllLines(matrixFile.toPath());

        // Remove outer brackets
        matrix.removeFirst();
        matrix.removeLast();

        // Initialize 2D boolean array
        boolean[][] parsedMatrix = new boolean[matrix.size()][];

        for (int i = 0; i < matrix.size(); i++) {
            // Remove any remaining brackets and split by comma
            String[] values = matrix.get(i).replace("[", "").replace("]", "").split(", ");
            parsedMatrix[i] = new boolean[values.length];
            for (int j = 0; j < values.length; j++) {
                // Parse "true" or "false" as boolean
                parsedMatrix[i][j] = Boolean.parseBoolean(values[j]);
            }
        }


        return parsedMatrix;
    }

    /**
     * Computes the hyper-volume of the given Pareto {@code front}, using the given fitness
     * functions {@code f1} and {@code f2}, and {@code r1} and {@code r2} as coordinates of the
     * reference point. The fitness functions must produce normalized results between 0 and 1.
     *
     * @param front the front for which to compute the hyper-volume
     * @param f1    the first fitness function
     * @param f2    the second fitness function
     * @param r1    reference coordinate for {@code f1}
     * @param r2    reference coordinate for {@code f2}
     * @return the hyper volume of the given front w.r.t. the reference point
     * @apiNote The function uses ugly raw types because it seems the type system doesn't want to
     * let me express this in any other way :(
     * @implSpec In the implementation of this method you might need to cast or use raw types, too.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static double computeHyperVolume(
            final List front,
            final FitnessFunction f1,
            final FitnessFunction f2,
            final double r1,
            final double r2)
            throws IllegalArgumentException {
        // Validate input Pareto front
        if (front == null || front.isEmpty()) {
            throw new IllegalArgumentException("Pareto front must not be null or empty.");
        }

        // Create a modifiable copy of the Pareto front
        List<Object> sortedFront = new ArrayList<>(front);

        // Sort solutions by the first objective value (f1) in ascending order
        sortedFront.sort(Comparator.comparingDouble(c -> f1.applyAsDouble(c)));

        // Initialize hyper-volume accumulator
        double hyperVolume = 0.0;

        // Previous f1 value starts at the reference point r1
        double previousF1 = r1;

        // Iterate over the sorted Pareto front to compute hyper-volume
        for (Object chromosome : sortedFront) {

            // Evaluate objective values for the current solution
            double currentF1 = f1.applyAsDouble(chromosome);
            double currentF2 = f2.applyAsDouble(chromosome);

            // Compute rectangle dimensions
            // Width: difference along f1 axis
            // Height: difference from reference point r2 to current f2
            double width = currentF1 - previousF1;
            double height = r2 - currentF2;

            // Add rectangle area only if dimensions are valid
            if (width > 0.0 && height > 0.0) {
                hyperVolume += width * height;
            }

            // Update previous f1 value for the next rectangle
            previousF1 = currentF1;
        }

        // Return the computed hyper-volume value
        return hyperVolume;

    }
}
