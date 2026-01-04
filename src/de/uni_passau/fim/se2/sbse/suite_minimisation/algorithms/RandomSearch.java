package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {
    private final ChromosomeGenerator<C> chromosomeGenerator;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;


    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
    }

    @Override
    public List<C> findSolution() {
        List<C> solutions = new ArrayList<>();
        stoppingCondition.notifySearchStarted();
        // Collect all solutions during the search
        while (!stoppingCondition.searchMustStop()) {
            // Generate 3 candidates per iteration (like CODE 1)
            for (int i = 0; i < 3; i++) {
                C candidate = chromosomeGenerator.get();
                solutions.add(candidate);
            }
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Build Pareto front from all collected solutions at the end
        List<C> paretoFront = findParetoFront(solutions);
        return paretoFront;
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }
    private List<C> findParetoFront(List<C> solutions) {
        List<C> paretoFront = new ArrayList<>();

        for (int i = 0; i < solutions.size(); i++) {
            C candidate = solutions.get(i);

            // Skip invalid solutions
            if (!isValidSolution(candidate)) {
                continue;
            }

            boolean isDominated = false;

            // Check if this solution is dominated by any other solution
            for (int j = 0; j < solutions.size(); j++) {
                if (i != j && isValidSolution(solutions.get(j)) && dominates(solutions.get(j), candidate)) {
                    isDominated = true;
                    break;
                }
            }

            // Add to Pareto front if not dominated
            if (!isDominated) {
                paretoFront.add(candidate);
            }
        }

        return paretoFront;
    }

    private boolean isValidSolution(C candidate) {
        double size = fitnessFunctions.get(0).applyAsDouble(candidate);
        double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);
        return size > 0 && coverage > 0;
    }

    private boolean dominates(C solution1, C solution2) {
        boolean atLeastOneBetter = false;

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            if (ff instanceof MinimizingFitnessFunction) {
                // For minimization: lower is better
                if (fitness1 > fitness2) return false;  // solution1 is worse
                if (fitness1 < fitness2) atLeastOneBetter = true;  // solution1 is better
            } else {
                // For maximization: higher is better
                if (fitness1 < fitness2) return false;  // solution1 is worse
                if (fitness1 > fitness2) atLeastOneBetter = true;  // solution1 is better
            }
        }

        return atLeastOneBetter;
    }

}