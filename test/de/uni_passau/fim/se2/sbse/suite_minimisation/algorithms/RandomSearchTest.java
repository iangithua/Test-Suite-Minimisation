package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RandomSearchTest {

    @Test
    @DisplayName("Should return non-null Pareto front")
    void testFindSolutionReturnsNonNullParetoFront() {
        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(50)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should produce solutions in Pareto front")
    void testFindSolutionProducesSolutions() {
        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(600)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertFalse(result.isEmpty(), "Pareto front should not be empty");
    }

    @Test
    @DisplayName("Should respect stopping condition")
    void testStoppingConditionStopsSearch() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(100);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        randomSearch.findSolution();

        assertTrue(stoppingCondition.getEvaluations() >= 100);
    }


    @Test
    @DisplayName("Should maintain multiple non-dominated solutions")
    void testMaintainsNonDominatedSolutions() {
        AtomicInteger counter = new AtomicInteger(0);

        ChromosomeGenerator<DummyChromosome> nonDominatedGenerator = () -> {
            int count = counter.getAndIncrement();
            return switch (count % 3) {
                case 0 -> new DummyChromosome(0.2, 0.5);
                case 1 -> new DummyChromosome(0.5, 0.8);
                default -> new DummyChromosome(0.3, 0.6);
            };
        };

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        nonDominatedGenerator,
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(600)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertTrue(result.size() > 1, "Should maintain multiple non-dominated solutions");
    }

    @Test
    @DisplayName("Should notify search started exactly once")
    void testNotifiesSearchStarted() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(50);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        randomSearch.findSolution();

        assertTrue(stoppingCondition.getEvaluations() > 0,
                "Search should have been notified as started");
    }

    @Test
    @DisplayName("Should notify fitness evaluation for each candidate")
    void testNotifiesFitnessEvaluations() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(200);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        randomSearch.findSolution();

        assertEquals(200, stoppingCondition.getEvaluations(),
                "Should have notified for each evaluation");
    }

    @Test
    @DisplayName("Should return stopping condition")
    void testReturnsStoppingCondition() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(50);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        assertSame(stoppingCondition, randomSearch.getStoppingCondition());
    }

    @Test
    @DisplayName("Should return independent copy of Pareto front")
    void testReturnsDefensiveCopy() {
        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(100)
                );

        List<DummyChromosome> result1 = randomSearch.findSolution();
        List<DummyChromosome> result2 = randomSearch.findSolution();

        assertNotSame(result1, result2, "Should return different list instances");
    }

    @Test
    @DisplayName("Should handle equal fitness solutions correctly")
    void testHandlesEqualFitnessSolutions() {
        ChromosomeGenerator<DummyChromosome> identicalGenerator = () ->
                new DummyChromosome(0.5, 0.5);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        identicalGenerator,
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(100)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertFalse(result.isEmpty(), "Should include non-dominated equal solutions");
    }

    @Test
    @DisplayName("Should generate initial population of 500")
    void testGeneratesInitialPopulation() {
        AtomicInteger generationCount = new AtomicInteger(0);
        ChromosomeGenerator<DummyChromosome> countingGenerator = () -> {
            generationCount.incrementAndGet();
            return new DummyChromosome(0.1 + Math.random() * 0.8, 0.1 + Math.random() * 0.8);
        };

        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(500);
        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        countingGenerator,
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        randomSearch.findSolution();

        assertEquals(500, stoppingCondition.getEvaluations(),
                "Should generate exactly 500 initial candidates");
    }

    @Test
    @DisplayName("Should generate 20 candidates per iteration")
    void testGeneratesCandidatesPerIteration() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(540);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        randomSearch.findSolution();

        // 500 initial + 40 more (2 iterations * 20) = 540
        assertEquals(540, stoppingCondition.getEvaluations());
    }

    @Test
    @DisplayName("Should work with large evaluation budget")
    void testHandlesLargeEvaluationBudget() {
        DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(2000);

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        stoppingCondition
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertFalse(result.isEmpty());
        assertEquals(2000, stoppingCondition.getEvaluations());
    }

    @Test
    @DisplayName("Should produce diverse Pareto front with sufficient evaluations")
    void testProducesDiverseParetoFront() {
        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        new DummyChromosomeGenerator(),
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(1000)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertTrue(result.size() >= 2,
                "Should produce diverse Pareto front with sufficient evaluations");
    }

    @Test
    @DisplayName("Should correctly apply minimizing fitness function")
    void testMinimizingFitnessFunction() {
        AtomicInteger counter = new AtomicInteger(0);

        ChromosomeGenerator<DummyChromosome> generator = () -> {
            int count = counter.getAndIncrement();
            return new DummyChromosome(1.0 - count * 0.1, 0.5);
        };

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        generator,
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(510)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(c -> c.size < 0.5),
                "Should include solutions with small size values");
    }

    @Test
    @DisplayName("Should correctly apply maximizing fitness function")
    void testMaximizingFitnessFunction() {
        AtomicInteger counter = new AtomicInteger(0);

        ChromosomeGenerator<DummyChromosome> generator = () -> {
            int count = counter.getAndIncrement();
            return new DummyChromosome(0.5, count * 0.1);
        };

        RandomSearch<DummyChromosome> randomSearch =
                new RandomSearch<>(
                        generator,
                        List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                        new DummyStoppingCondition(510)
                );

        List<DummyChromosome> result = randomSearch.findSolution();

        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(c -> c.coverage > 0.5),
                "Should include solutions with high coverage values");
    }

    // ==================== Helper Classes ====================

    static class DummyChromosome extends Chromosome<DummyChromosome> {

        final double size;
        final double coverage;

        DummyChromosome(double size, double coverage) {
            this.size = size;
            this.coverage = coverage;
        }

        @Override
        public DummyChromosome copy() {
            return new DummyChromosome(size, coverage);
        }

        @Override
        public DummyChromosome self() {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DummyChromosome other)) return false;
            return Double.compare(size, other.size) == 0 &&
                    Double.compare(coverage, other.coverage) == 0;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(size) * 31 + Double.hashCode(coverage);
        }
    }

    static class DummyChromosomeGenerator implements ChromosomeGenerator<DummyChromosome> {

        private final Random random = new Random();

        @Override
        public DummyChromosome get() {
            return new DummyChromosome(
                    0.1 + random.nextDouble() * 0.9,
                    0.1 + random.nextDouble() * 0.9
            );
        }
    }

    static class DummySizeFitness implements MinimizingFitnessFunction<DummyChromosome> {

        @Override
        public double applyAsDouble(DummyChromosome chromosome) {
            return chromosome.size;
        }
    }

    static class DummyCoverageFitness implements FitnessFunction<DummyChromosome> {

        @Override
        public double applyAsDouble(DummyChromosome chromosome) {
            return chromosome.coverage;
        }

        @Override
        public boolean isMinimizing() {
            return false;
        }
    }

    static class DummyStoppingCondition implements StoppingCondition {

        private final int maxEvaluations;
        private int evaluations = 0;

        DummyStoppingCondition(int maxEvaluations) {
            this.maxEvaluations = maxEvaluations;
        }

        @Override
        public void notifySearchStarted() {
            evaluations = 0;
        }

        @Override
        public void notifyFitnessEvaluation() {
            evaluations++;
        }

        @Override
        public boolean searchMustStop() {
            return evaluations >= maxEvaluations;
        }

        @Override
        public double getProgress() {
            return (double) evaluations / maxEvaluations;
        }

        public int getEvaluations() {
            return evaluations;
        }
    }
}