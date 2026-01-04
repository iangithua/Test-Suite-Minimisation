package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MinimizingFitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.*;
import java.util.Random;

public class RandomSearch<C extends Chromosome<C>> implements GeneticAlgorithm<C> {
    private final ChromosomeGenerator<C> chromosomeGenerator;
    private final List<FitnessFunction<C>> fitnessFunctions;
    private final StoppingCondition stoppingCondition;
    private final Random random;


    public RandomSearch(ChromosomeGenerator<C> chromosomeGenerator,
                        List<FitnessFunction<C>> fitnessFunctions,
                        StoppingCondition stoppingCondition) {

        this.chromosomeGenerator = chromosomeGenerator;
        this.fitnessFunctions = fitnessFunctions;
        this.stoppingCondition = stoppingCondition;
        this.random = new Random();
    }

    @Override
    public List<C> findSolution() {
        List<C> solutions = new ArrayList<>();
        stoppingCondition.notifySearchStarted();
        // Collect all solutions during the search
        while (!stoppingCondition.searchMustStop()) {
            C candidate;
            double randomValue = random.nextDouble();

            // 92% of time: generate using mutation-based exploration
            if (randomValue < 0.92) {
                // Generate base candidate
                C base = chromosomeGenerator.get();

                // Apply multiple mutations (2-5 times)
                int mutationCount = 2 + random.nextInt(4);
                candidate = base;
                for (int i = 0; i < mutationCount; i++) {
                    candidate = candidate.mutate();
                }
            } else {
                // 8% pure random for exploration
                candidate = chromosomeGenerator.get();
            }

            // Generate 3 candidates per iteration (like CODE 1)
            for (FitnessFunction<C> fitnessFunction : fitnessFunctions) {

                fitnessFunction.applyAsDouble(candidate);
            }
            solutions.add(candidate);
            stoppingCondition.notifyFitnessEvaluation();
        }

        // Build Pareto front from all collected solutions at the end
        List<List<C>> paretoFront = findParetoFront(solutions);
        return paretoFront.isEmpty() ? List.of(chromosomeGenerator.get()) : paretoFront.get(0);
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }
    private List<List<C>>  findParetoFront(List<C> solutions) {
        Map<C, Integer> dominationCount = new HashMap<>();
        Map<C, List<C>> dominatedSolutions = new HashMap<>();

        // Initialize maps
        for (C individual : solutions) {
            dominationCount.put(individual, 0);
            dominatedSolutions.put(individual, new ArrayList<>());
        }

        // Compare each pair of solutions
        for (int i = 0; i < solutions.size(); i++) {
            for (int j = i + 1; j < solutions.size(); j++) {
                C solutionA = solutions.get(i);
                C solutionB = solutions.get(j);

                if (dominates(solutionA, solutionB)) {
                    // A dominates B
                    dominationCount.put(solutionB, dominationCount.get(solutionB) + 1);
                    dominatedSolutions.get(solutionA).add(solutionB);
                } else if (dominates(solutionB, solutionA)) {
                    // B dominates A
                    dominationCount.put(solutionA, dominationCount.get(solutionA) + 1);
                    dominatedSolutions.get(solutionB).add(solutionA);
                }
            }
        }

        // Build fronts
        List<List<C>> fronts = new ArrayList<>();
        List<C> currentFront = new ArrayList<>();

        // First front: solutions with domination count = 0
        for (C individual : solutions) {
            if (dominationCount.get(individual) == 0) {
                currentFront.add(individual);
            }
        }

        // Build subsequent fronts
        while (!currentFront.isEmpty()) {
            fronts.add(new ArrayList<>(currentFront));
            List<C> nextFront = new ArrayList<>();

            for (C individual : currentFront) {
                for (C dominated : dominatedSolutions.get(individual)) {
                    dominationCount.put(dominated, dominationCount.get(dominated) - 1);
                    if (dominationCount.get(dominated) == 0) {
                        nextFront.add(dominated);
                    }
                }
            }

            currentFront = nextFront;
        }

        return fronts;
    }

    private boolean dominates(C solution1, C solution2) {
        boolean atLeastOneBetter = false;

        for (FitnessFunction<C> ff : fitnessFunctions) {
            double fitness1 = ff.applyAsDouble(solution1);
            double fitness2 = ff.applyAsDouble(solution2);

            if (ff instanceof MinimizingFitnessFunction) {
                // For minimization: solution1 must be <= solution2
                if (fitness1 > fitness2) return false;
                if (fitness1 < fitness2) atLeastOneBetter = true;
            } else {
                // For maximization: solution1 must be >= solution2
                if (fitness1 < fitness2) return false;
                if (fitness1 > fitness2) atLeastOneBetter = true;
            }
        }

        return atLeastOneBetter;
    }
    }
