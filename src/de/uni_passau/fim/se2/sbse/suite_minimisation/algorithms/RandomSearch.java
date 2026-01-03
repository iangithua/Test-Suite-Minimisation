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

    // Generator used to create random candidate chromosomes
    private final ChromosomeGenerator<C> chromosomeGenerator;

    // List of fitness functions (e.g., size minimization, coverage maximization)
    private final List<FitnessFunction<C>> fitnessFunctions;

    // Stopping condition controlling when the search terminates
    private final StoppingCondition stoppingCondition;

    // List storing the current Pareto-optimal (non-dominated) solutions
    private final List<C> paretoFront;

    // Size of the initial random population used to bootstrap the Pareto front
    private final int initialPopulationSize = 600;

    // Number of candidates generated per iteration to improve exploration
    private final int candidatesPerIteration = 20;

    // Constructor initializes all required components
    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.paretoFront = new ArrayList<>();
    }

    @Override
    public List<C> findSolution() {

        // Notify the stopping condition that the search has started
        stoppingCondition.notifySearchStarted();

        // Generate an initial population to seed the Pareto front
        generateInitialPopulation();

        // Continue random sampling until the stopping condition is met
        while (!stoppingCondition.searchMustStop()) {

            // Generate multiple candidates per iteration to increase diversity
            for (int i = 0; i < candidatesPerIteration && !stoppingCondition.searchMustStop(); i++) {

                // Generate a random candidate solution
                C candidate = chromosomeGenerator.get();

                // Attempt to insert the candidate into the Pareto front
                updateParetoFront(candidate);

                // Notify that a fitness evaluation has occurred
                stoppingCondition.notifyFitnessEvaluation();
            }
        }

        // Return a defensive copy of the final Pareto front
        return new ArrayList<>(paretoFront);
    }

    // Generates the initial population of random solutions
    private void generateInitialPopulation() {

        for (int i = 0; i < initialPopulationSize && !stoppingCondition.searchMustStop(); i++) {

            // Generate a random candidate
            C candidate = chromosomeGenerator.get();

            // Update Pareto front with the candidate
            updateParetoFront(candidate);

            // Notify fitness evaluation
            stoppingCondition.notifyFitnessEvaluation();
        }
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    // Updates the Pareto front by applying dominance checks
    private void updateParetoFront(C candidate) {

        // Discard invalid or meaningless solutions
        if (!isValidSolution(candidate)) {
            return;
        }

        boolean candidateIsDominated = false;
        Iterator<C> iterator = paretoFront.iterator();

        // Compare candidate against all existing Pareto solutions
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

        // Add candidate if it is not dominated by any existing solution
        if (!candidateIsDominated) {
            paretoFront.add(candidate);
        }
    }

    // Checks whether a solution is meaningful
    private boolean isValidSolution(C candidate) {

        // Fitness function at index 0: size (minimization)
        double size = fitnessFunctions.get(0).applyAsDouble(candidate);

        // Fitness function at index 1: coverage (maximization)
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);

        // Require at least one selected test and some coverage
        return size > 0 && coverage > 0;
    }

    // Determines whether solution1 Pareto-dominates solution2
    private boolean dominates(C solution1, C solution2) {

        boolean atLeastOneBetter = false;

        // Compare both solutions across all fitness functions
        for (FitnessFunction<C> ff : fitnessFunctions) {

            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            // Handle minimizing objectives (e.g., test suite size)
            if (ff instanceof MinimizingFitnessFunction) {
                if (fitness1 > fitness2) return false;
                if (fitness1 < fitness2) atLeastOneBetter = true;
            }
            // Handle maximizing objectives (e.g., coverage)
            else {
                if (fitness1 < fitness2) return false;
                if (fitness1 > fitness2) atLeastOneBetter = true;
            }
        }

        // True only if solution1 is strictly better in at least one objective
        return atLeastOneBetter;
    }
}
