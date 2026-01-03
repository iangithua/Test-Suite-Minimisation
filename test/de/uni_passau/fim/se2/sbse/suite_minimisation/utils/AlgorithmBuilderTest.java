package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.GeneticAlgorithm;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.NSGA2;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.RandomSearch;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.SearchAlgorithmType;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosomGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MaximizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlgorithmBuilderTest {

    private Random random;
    private StoppingCondition mockStoppingCondition;
    private boolean[][] coverageMatrix;
    private AlgorithmBuilder builder;

    @BeforeEach
    void setUp() {
        random = new Random(42);
        mockStoppingCondition = mock(StoppingCondition.class);

        // Create a simple 3x4 coverage matrix (3 test cases, 4 lines)
        coverageMatrix = new boolean[][]{
                {true, false, true, false},   // Test 0 covers lines 0, 2
                {false, true, true, false},   // Test 1 covers lines 1, 2
                {false, false, true, true}    // Test 2 covers lines 2, 3
        };
    }

    @Test
    @DisplayName("Should create AlgorithmBuilder with valid parameters")
    void testConstructorWithValidParameters() {
        assertDoesNotThrow(() ->
                new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix)
        );
    }

    @Test
    @DisplayName("Should initialize with correct dimensions")
    void testConstructorInitializesCorrectDimensions() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        assertNotNull(builder);
        assertNotNull(builder.getSizeFF());
        assertNotNull(builder.getCoverageFF());
    }

    @Test
    @DisplayName("Should handle single test case matrix")
    void testConstructorWithSingleTestCase() {
        boolean[][] singleTestMatrix = {{true, false, true}};

        assertDoesNotThrow(() ->
                new AlgorithmBuilder(random, mockStoppingCondition, singleTestMatrix)
        );
    }

    @Test
    @DisplayName("Should handle single line matrix")
    void testConstructorWithSingleLine() {
        boolean[][] singleLineMatrix = {
                {true},
                {false},
                {true}
        };

        assertDoesNotThrow(() ->
                new AlgorithmBuilder(random, mockStoppingCondition, singleLineMatrix)
        );
    }

    @Test
    @DisplayName("Should handle large coverage matrix")
    void testConstructorWithLargeMatrix() {
        boolean[][] largeMatrix = new boolean[100][50];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 50; j++) {
                largeMatrix[i][j] = (i + j) % 2 == 0;
            }
        }

        assertDoesNotThrow(() ->
                new AlgorithmBuilder(random, mockStoppingCondition, largeMatrix)
        );
    }

    @Test
    @DisplayName("Should return non-null size fitness function")
    void testGetSizeFFReturnsNonNull() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();

        assertNotNull(sizeFF);
    }

    @Test
    @DisplayName("Should return non-null coverage fitness function")
    void testGetCoverageFFReturnsNonNull() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MaximizingFitnessFunction<? extends Chromosome<?>> coverageFF = builder.getCoverageFF();

        assertNotNull(coverageFF);
    }

    @Test
    @DisplayName("Should return MinimizingFitnessFunction for size")
    void testSizeFitnessIsMinimizing() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();

        assertTrue(sizeFF.isMinimizing());
    }

    @Test
    @DisplayName("Should return MaximizingFitnessFunction for coverage")
    void testCoverageFitnessIsMaximizing() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MaximizingFitnessFunction<? extends Chromosome<?>> coverageFF = builder.getCoverageFF();

        assertFalse(coverageFF.isMinimizing());
    }

    @Test
    @DisplayName("Size fitness should calculate partial coverage correctly")
    void testSizeFitnessPartialCoverage() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);
        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();

        // Select only first test (covers lines 0 and 2, so 2 out of 4 = 0.5)
        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        chromosome.getGenes()[0] = true;
        chromosome.getGenes()[1] = false;
        chromosome.getGenes()[2] = false;

        double fitness = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);

        assertEquals(0.5, fitness, 0.001); // 2 lines out of 4
    }


    @Test
    @DisplayName("Coverage fitness should calculate partial selection correctly")
    void testCoverageFitnessPartialSelection() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);
        MaximizingFitnessFunction<? extends Chromosome<?>> coverageFF = builder.getCoverageFF();

        // Select 1 out of 3 tests
        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        chromosome.getGenes()[0] = true;
        chromosome.getGenes()[1] = false;
        chromosome.getGenes()[2] = false;

        double fitness = ((MaximizingFitnessFunction) coverageFF).applyAsDouble(chromosome);

        assertEquals(1.0 / 3.0, fitness, 0.001);
    }

    @Test
    @DisplayName("Should build RandomSearch algorithm")
    void testBuildRandomSearchAlgorithm() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertNotNull(algorithm);
        assertInstanceOf(RandomSearch.class, algorithm);
    }

    @Test
    @DisplayName("Should build NSGA2 algorithm")
    void testBuildNSGA2Algorithm() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertNotNull(algorithm);
        assertInstanceOf(NSGA2.class, algorithm);
    }

    @Test
    @DisplayName("Built RandomSearch should have correct stopping condition")
    void testRandomSearchHasCorrectStoppingCondition() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertSame(mockStoppingCondition, algorithm.getStoppingCondition());
    }

    @Test
    @DisplayName("Built NSGA2 should have correct stopping condition")
    void testNSGA2HasCorrectStoppingCondition() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertSame(mockStoppingCondition, algorithm.getStoppingCondition());
    }

    @Test
    @DisplayName("Should create RandomSearch with valid components")
    void testBuildRandomSearchCreatesValidAlgorithm() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertNotNull(algorithm);
        assertNotNull(algorithm.getStoppingCondition());
    }

    @Test
    @DisplayName("RandomSearch should be executable")
    void testRandomSearchIsExecutable() {
        when(mockStoppingCondition.searchMustStop()).thenReturn(true);

        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);
        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertDoesNotThrow(() -> algorithm.findSolution());
    }

    @Test
    @DisplayName("Should create NSGA2 with valid components")
    void testBuildNSGA2CreatesValidAlgorithm() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertNotNull(algorithm);
        assertNotNull(algorithm.getStoppingCondition());
    }

    @Test
    @DisplayName("NSGA2 should be executable")
    void testNSGA2IsExecutable() {
        when(mockStoppingCondition.searchMustStop()).thenReturn(true);

        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);
        GeneticAlgorithm<?> algorithm = builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertDoesNotThrow(() -> algorithm.findSolution());
    }



    @Test
    @DisplayName("Both algorithms should handle same coverage matrix")
    void testBothAlgorithmsHandleSameMatrix() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> rs = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);
        GeneticAlgorithm<?> nsga2 = builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertNotNull(rs);
        assertNotNull(nsga2);
        assertNotSame(rs, nsga2);
    }

    @Test
    @DisplayName("Fitness functions should be normalized")
    void testFitnessFunctionsAreNormalized() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();
        MaximizingFitnessFunction<? extends Chromosome<?>> coverageFF = builder.getCoverageFF();

        // Test with all tests selected
        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        for (int i = 0; i < 3; i++) {
            chromosome.getGenes()[i] = true;
        }

        double sizeFitness = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);
        double coverageFitness = ((MaximizingFitnessFunction) coverageFF).applyAsDouble(chromosome);

        // Both should be between 0 and 1 (normalized)
        assertTrue(sizeFitness >= 0.0 && sizeFitness <= 1.0);
        assertTrue(coverageFitness >= 0.0 && coverageFitness <= 1.0);
    }

    @Test
    @DisplayName("Should handle matrix with all false values")
    void testHandlesAllFalseMatrix() {
        boolean[][] allFalseMatrix = new boolean[3][4];

        builder = new AlgorithmBuilder(random, mockStoppingCondition, allFalseMatrix);
        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();

        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        for (int i = 0; i < 3; i++) {
            chromosome.getGenes()[i] = true;
        }
        double fitness = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);

        assertEquals(0.0, fitness, 0.001); // No lines covered
    }

    @Test
    @DisplayName("Should handle matrix with all true values")
    void testHandlesAllTrueMatrix() {
        boolean[][] allTrueMatrix = new boolean[3][4];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                allTrueMatrix[i][j] = true;
            }
        }

        builder = new AlgorithmBuilder(random, mockStoppingCondition, allTrueMatrix);
        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();

        // Even one test should cover all lines
        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        chromosome.getGenes()[0] = true;
        chromosome.getGenes()[1] = false;
        chromosome.getGenes()[2] = false;
        double fitness = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);

        assertEquals(1.0, fitness, 0.001); // All lines covered
    }

    @Test
    @DisplayName("Fitness functions should be consistent across multiple calls")
    void testFitnessFunctionsAreConsistent() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF = builder.getSizeFF();
        BinaryChromosomGenerator generator = new BinaryChromosomGenerator(3);
        BinaryChromosom chromosome = generator.get();
        chromosome.getGenes()[0] = true;
        chromosome.getGenes()[1] = false;
        chromosome.getGenes()[2] = true;

        double fitness1 = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);
        double fitness2 = ((MinimizingFitnessFunction) sizeFF).applyAsDouble(chromosome);

        assertEquals(fitness1, fitness2, 0.0001);
    }

    @Test
    @DisplayName("Should create independent algorithm instances")
    void testCreatesIndependentAlgorithms() {
        builder = new AlgorithmBuilder(random, mockStoppingCondition, coverageMatrix);

        GeneticAlgorithm<?> algorithm1 = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);
        GeneticAlgorithm<?> algorithm2 = builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertNotSame(algorithm1, algorithm2);
    }
}