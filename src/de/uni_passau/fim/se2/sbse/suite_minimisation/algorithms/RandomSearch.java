package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {

    // Generates random chromosomes (candidate solutions)
    private final ChromosomeGenerator<C> chromosomeGenerator;

    // List of fitness functions (e.g., size minimization, coverage maximization)
    private final List<FitnessFunction<C>> fitnessFunctions;

    // Determines when the search should stop
    private final StoppingCondition stoppingCondition;

    // Stores the current Pareto-optimal (non-dominated) solutions
    private final List<C> paretoFront;

    // Size of the initial population used to bootstrap the Pareto front
    private final int initialPopulationSize = 500;

    // Constructor initializes all required components
    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.paretoFront = new ArrayList<>();
    }

    /**
     * Executes the random search and returns the final Pareto front.
     */
    @Override
    public List<C> findSolution() {

        // Notify stopping condition that the search has started
        stoppingCondition.notifySearchStarted();

        // Generate an initial diverse population
        generateInitialPopulation();

        // Continue sampling random solutions until stopping condition is met
        while (!stoppingCondition.searchMustStop()) {

            // Generate multiple candidates per iteration to improve diversity
            for (int i = 0; i < 5 && !stoppingCondition.searchMustStop(); i++) {

                // Generate a random candidate solution
                C candidate = chromosomeGenerator.get();

                // Try to add it to the Pareto front
                updateParetoFront(candidate);

                // Notify that a fitness evaluation has occurred
                stoppingCondition.notifyFitnessEvaluation();
            }
        }

        // Return a defensive copy of the Pareto front
        return new ArrayList<>(paretoFront);
    }

    /**
     * Generates an initial population to better explore the search space.
     */
    private void generateInitialPopulation() {

        for (int i = 0; i < initialPopulationSize && !stoppingCondition.searchMustStop(); i++) {

            // Generate random solution
            C candidate = chromosomeGenerator.get();

            // Update Pareto front
            updateParetoFront(candidate);

            // Notify stopping condition
            stoppingCondition.notifyFitnessEvaluation();
        }
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    /**
     * Updates the Pareto front by checking dominance relations.
     */
    private void updateParetoFront(C candidate) {

        // Discard invalid solutions
        if (!isValidSolution(candidate)) {
            return;
        }

        boolean candidateIsDominated = false;
        Iterator<C> iterator = paretoFront.iterator();

        // Compare candidate against existing Pareto solutions
        while (iterator.hasNext()) {
            C existing = iterator.next();

            // Existing solution dominates candidate → discard candidate
            if (dominates(existing, candidate)) {
                candidateIsDominated = true;
                break;
            }
            // Candidate dominates existing solution → remove existing
            else if (dominates(candidate, existing)) {
                iterator.remove();
            }
        }

        // Add candidate if it is not dominated
        if (!candidateIsDominated) {
            paretoFront.add(candidate);
        }
    }

    /**
     * Checks whether a solution is meaningful (non-empty and with coverage).
     */
    private boolean isValidSolution(C candidate) {

        // Fitness function 0: size
        double size = fitnessFunctions.get(0).applyAsDouble(candidate);

        // Fitness function 1: coverage
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);

        // Require at least one selected test and some coverage
        return size > 0 && coverage > 0;
    }

    /**
     * Determines whether solution1 Pareto-dominates solution2.
     */
    private boolean dominates(C solution1, C solution2) {

        boolean atLeastOneBetter = false;

        // Compare solutions across all fitness functions
        for (FitnessFunction<C> ff : fitnessFunctions) {

            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            // Minimization objective (e.g., test suite size)
            if (ff instanceof MinimizingFitnessFunction) {
                if (fitness1 > fitness2) return false;
                if (fitness1 < fitness2) atLeastOneBetter = true;
            }
            // Maximization objective (e.g., coverage)
            else {
                if (fitness1 < fitness2) return false;
                if (fitness1 > fitness2) atLeastOneBetter = true;
            }
        }

        // Must be at least strictly better in one objective
        return atLeastOneBetter;
    }
}
