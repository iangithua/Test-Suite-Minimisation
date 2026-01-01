package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.SearchAlgorithmType;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.BinaryChromosom;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;

// Test cases for AlgorithmBuilder
public class AlgorithmBuilderTest {

    // Dummy stopping condition for testing
    private static class DummyStoppingCondition implements StoppingCondition {
        @Override
        public void notifySearchStarted() {

        }

        @Override
        public void notifyFitnessEvaluation() {

        }

        @Override
        public boolean searchMustStop() {
            return false;
        }

        @Override
        public double getProgress() {
            return 0;
        }
    }

    // Dummy mutation operator
    private static class DummyMutation implements Mutation<BinaryChromosom> {
        @Override
        public BinaryChromosom apply(BinaryChromosom chromosome) {
            return chromosome;
        }
    }

    // Dummy crossover operator
    private static class DummyCrossover implements Crossover<BinaryChromosom> {
        @Override
        public Pair<BinaryChromosom> apply(BinaryChromosom p1, BinaryChromosom p2) {
            return Pair.of(p1, p2);
        }
    }


    // Test that NSGA_II dispatch reaches unimplemented method
    @Test
    void testBuildNSGA2ThrowsException() {
        boolean[][] coverageMatrix = {
                { true }
        };

        AlgorithmBuilder builder =
                new AlgorithmBuilder(
                        new Random(),
                        new DummyStoppingCondition(),
                        coverageMatrix
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> builder.buildAlgorithm(SearchAlgorithmType.NSGA_II)
        );
    }
}
