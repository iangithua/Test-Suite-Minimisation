package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

    public class NSGA2Test {

        @Test
        void testFindSolutionReturnsNonNullParetoFront() {

            NSGA2<DummyChromosome> nsga2 = new NSGA2<>(
                    new DummyChromosomeGenerator(),
                    new DummyMutation(),
                    new DummyCrossover(),
                    List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                    new DummyStoppingCondition(50),
                    new Random()
            );

            List<DummyChromosome> paretoFront = nsga2.findSolution();
            assertNotNull(paretoFront, "Pareto front must not be null");
        }

        @Test
        void testFindSolutionProducesAtLeastOneSolution() {

            NSGA2<DummyChromosome> nsga2 = new NSGA2<>(
                    new DummyChromosomeGenerator(),
                    new DummyMutation(),
                    new DummyCrossover(),
                    List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                    new DummyStoppingCondition(50),
                    new Random()
            );

            List<DummyChromosome> paretoFront = nsga2.findSolution();
            assertFalse(paretoFront.isEmpty(), "Pareto front should contain at least one solution");
        }

        @Test
        void testStoppingConditionIsRespected() {

            DummyStoppingCondition stoppingCondition = new DummyStoppingCondition(10);

            NSGA2<DummyChromosome> nsga2 = new NSGA2<>(
                    new DummyChromosomeGenerator(),
                    new DummyMutation(),
                    new DummyCrossover(),
                    List.of(new DummySizeFitness(), new DummyCoverageFitness()),
                    stoppingCondition,
                    new Random()
            );

            nsga2.findSolution();
            assertTrue(stoppingCondition.getEvaluations() >= 10,
                    "Stopping condition should have stopped after at least 10 evaluations");
        }


        static class DummyChromosome extends Chromosome<DummyChromosome> {

            final double size;
            final double coverage;

            DummyChromosome(double size, double coverage) {
                super(new DummyMutation(), new DummyCrossover());
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
                double size = random.nextDouble();     // smaller is better
                double coverage = random.nextDouble(); // larger is better
                return new DummyChromosome(size, coverage);
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

        static class DummyMutation implements Mutation<DummyChromosome> {
            @Override
            public DummyChromosome apply(DummyChromosome chromosome) {
                return chromosome.copy();
            }
        }

        static class DummyCrossover implements Crossover<DummyChromosome> {
            @Override
            public Pair<DummyChromosome> apply(DummyChromosome parent1, DummyChromosome parent2) {
                return Pair.of(parent1.copy(), parent2.copy());
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
