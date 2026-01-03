package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should parse valid coverage matrix with single row")
    void testParseCoverageMatrixSingleRow() throws IOException {
        String content = """
                [
                [true, false, true]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(1, result.length);
        assertEquals(3, result[0].length);
        assertTrue(result[0][0]);
        assertFalse(result[0][1]);
        assertTrue(result[0][2]);
    }

    @Test
    @DisplayName("Should parse valid coverage matrix with multiple rows")
    void testParseCoverageMatrixMultipleRows() throws IOException {
        String content = """
                [
                [true, false, true]
                [false, true, false]
                [true, true, false]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(3, result.length);
        assertEquals(3, result[0].length);

        // First row
        assertTrue(result[0][0]);
        assertFalse(result[0][1]);
        assertTrue(result[0][2]);

        // Second row
        assertFalse(result[1][0]);
        assertTrue(result[1][1]);
        assertFalse(result[1][2]);

        // Third row
        assertTrue(result[2][0]);
        assertTrue(result[2][1]);
        assertFalse(result[2][2]);
    }

    @Test
    @DisplayName("Should parse coverage matrix with all true values")
    void testParseCoverageMatrixAllTrue() throws IOException {
        String content = """
                [
                [true, true]
                [true, true]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(2, result.length);
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                assertTrue(result[i][j], "All values should be true");
            }
        }
    }

    @Test
    @DisplayName("Should parse coverage matrix with all false values")
    void testParseCoverageMatrixAllFalse() throws IOException {
        String content = """
                [
                [false, false, false]
                [false, false, false]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(2, result.length);
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                assertFalse(result[i][j], "All values should be false");
            }
        }
    }

    @Test
    @DisplayName("Should parse coverage matrix with varying row lengths")
    void testParseCoverageMatrixVaryingRowLengths() throws IOException {
        String content = """
                [
                [true, false]
                [true, true, false, true]
                [false]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(3, result.length);
        assertEquals(2, result[0].length);
        assertEquals(4, result[1].length);
        assertEquals(1, result[2].length);
    }

    @Test
    @DisplayName("Should parse large coverage matrix")
    void testParseCoverageMatrixLarge() throws IOException {
        StringBuilder content = new StringBuilder("[\n");
        for (int i = 0; i < 50; i++) {
            content.append("[");
            for (int j = 0; j < 20; j++) {
                content.append(i % 2 == 0 ? "true" : "false");
                if (j < 19) content.append(", ");
            }
            content.append("]\n");
        }
        content.append("]");

        File file = createTempFile(content.toString());
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(50, result.length);
        assertEquals(20, result[0].length);
    }

    @Test
    @DisplayName("Should handle coverage matrix with extra whitespace")
    void testParseCoverageMatrixWithWhitespace() throws IOException {
        String content = """
                [
                [  true  ,  false  ,  true  ]
                [false,true,false]
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(2, result.length);
        assertFalse(result[0][1]);
    }

    @Test
    @DisplayName("Should throw IOException for non-existent file")
    void testParseCoverageMatrixNonExistentFile() {
        File nonExistent = new File("non_existent_file.txt");

        assertThrows(IOException.class, () ->
                Utils.parseCoverageMatrix(nonExistent)
        );
    }

    @Test
    @DisplayName("Should handle empty matrix (only brackets)")
    void testParseCoverageMatrixEmpty() throws IOException {
        String content = """
                [
                ]
                """;

        File file = createTempFile(content);
        boolean[][] result = Utils.parseCoverageMatrix(file);

        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("Should return 0 for null front")
    void testComputeHyperVolumeNullFront() {
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(null, f1, f2, 1.0, 1.0);

        assertEquals(0.0, hyperVolume);
    }

    @Test
    @DisplayName("Should return 0 for empty front")
    void testComputeHyperVolumeEmptyFront() {
        List<TestSolution> emptyFront = new ArrayList<>();
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(emptyFront, f1, f2, 1.0, 1.0);

        assertEquals(0.0, hyperVolume);
    }


    @Test
    @DisplayName("Should compute hypervolume for two non-dominated solutions")
    void testComputeHyperVolumeTwoSolutions() {
        List<TestSolution> front = List.of(
                new TestSolution(0.2, 0.6),  // Better in obj1, worse in obj2
                new TestSolution(0.5, 0.3)   // Worse in obj1, better in obj2
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);
        assertTrue(hyperVolume > 0);
    }

    @Test
    @DisplayName("Should compute hypervolume for Pareto front with multiple solutions")
    void testComputeHyperVolumeMultipleSolutions() {
        List<TestSolution> front = List.of(
                new TestSolution(0.1, 0.8),
                new TestSolution(0.3, 0.6),
                new TestSolution(0.5, 0.4),
                new TestSolution(0.7, 0.2)
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);

        assertTrue(hyperVolume > 0);
        assertTrue(hyperVolume < 1.0); // Should be less than reference area
    }

    @Test
    @DisplayName("Should remove duplicates before computing hypervolume")
    void testComputeHyperVolumeWithDuplicates() {
        List<TestSolution> frontWithDuplicates = List.of(
                new TestSolution(0.3, 0.5),
                new TestSolution(0.3, 0.5),  // Duplicate
                new TestSolution(0.6, 0.3)
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolumeWithDuplicates = Utils.computeHyperVolume(
                frontWithDuplicates, f1, f2, 1.0, 1.0);

        List<TestSolution> frontWithoutDuplicates = List.of(
                new TestSolution(0.3, 0.5),
                new TestSolution(0.6, 0.3)
        );

        double hyperVolumeWithoutDuplicates = Utils.computeHyperVolume(
                frontWithoutDuplicates, f1, f2, 1.0, 1.0);

        assertEquals(hyperVolumeWithoutDuplicates, hyperVolumeWithDuplicates, 0.0001);
    }

    @Test
    @DisplayName("Should handle solutions at reference point")
    void testComputeHyperVolumeSolutionAtReferencePoint() {
        List<TestSolution> front = List.of(new TestSolution(1.0, 1.0));
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);

        // At reference point, no area contribution
        assertEquals(0.0, hyperVolume, 0.0001);
    }

    @Test
    @DisplayName("Should handle solutions beyond reference point")
    void testComputeHyperVolumeSolutionBeyondReference() {
        List<TestSolution> front = List.of(new TestSolution(1.2, 1.3));
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);

        // Beyond reference point in both objectives - should contribute 0 or be handled
        assertTrue(hyperVolume >= 0);
    }

    @Test
    @DisplayName("Should compute correct hypervolume with different reference point")
    void testComputeHyperVolumeDifferentReferencePoint() {
        List<TestSolution> front = List.of(
                new TestSolution(0.2, 0.4),
                new TestSolution(0.4, 0.2)
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hv1 = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);
        double hv2 = Utils.computeHyperVolume(front, f1, f2, 0.8, 0.8);

        // Different reference points should give different hypervolumes
        // Larger reference point should generally give larger hypervolume
        assertTrue(hv1 >= hv2);
    }

    @Test
    @DisplayName("Should sort solutions by first objective")
    void testComputeHyperVolumeSortsByFirstObjective() {
        // Provide solutions in reverse order
        List<TestSolution> front = List.of(
                new TestSolution(0.8, 0.2),
                new TestSolution(0.6, 0.3),
                new TestSolution(0.4, 0.5),
                new TestSolution(0.2, 0.7)
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);

        assertTrue(hyperVolume > 0);
    }

    @Test
    @DisplayName("Should handle normalized values between 0 and 1")
    void testComputeHyperVolumeNormalizedValues() {
        List<TestSolution> front = List.of(
                new TestSolution(0.0, 1.0),  // Best in obj1, worst in obj2
                new TestSolution(1.0, 0.0)   // Worst in obj1, best in obj2
        );
        FitnessFunction<TestSolution> f1 = new Objective1Function();
        FitnessFunction<TestSolution> f2 = new Objective2Function();

        double hyperVolume = Utils.computeHyperVolume(front, f1, f2, 1.0, 1.0);

        assertTrue(hyperVolume >= 0 && hyperVolume <= 1.0);
    }

    private File createTempFile(String content) throws IOException {
        Path file = tempDir.resolve("test_matrix.txt");
        Files.writeString(file, content);
        return file.toFile();
    }

    static class TestSolution {
        private final double objective1;
        private final double objective2;

        TestSolution(double objective1, double objective2) {
            this.objective1 = objective1;
            this.objective2 = objective2;
        }

        public double getObjective1() {
            return objective1;
        }

        public double getObjective2() {
            return objective2;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestSolution other)) return false;
            return Double.compare(objective1, other.objective1) == 0 &&
                    Double.compare(objective2, other.objective2) == 0;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(objective1) * 31 + Double.hashCode(objective2);
        }

        @Override
        public String toString() {
            return String.format("TestSolution(%.2f, %.2f)", objective1, objective2);
        }
    }

    // Fitness function implementations
    static class Objective1Function implements FitnessFunction<TestSolution> {
        @Override
        public double applyAsDouble(TestSolution solution) {
            return solution.getObjective1();
        }

        @Override
        public boolean isMinimizing() {
            return true; // Assuming minimization
        }
    }

    static class Objective2Function implements FitnessFunction<TestSolution> {
        @Override
        public double applyAsDouble(TestSolution solution) {
            return solution.getObjective2();
        }

        @Override
        public boolean isMinimizing() {
            return false; // Assuming maximization
        }
    }
}