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
        List<C> goodSolutions = new ArrayList<>();  // Track high-quality solutions

        stoppingCondition.notifySearchStarted();

        // Phase 1: Build initial pool of diverse solutions
        int initialSamples = 50;
        for (int i = 0; i < initialSamples && !stoppingCondition.searchMustStop(); i++) {
            C candidate = chromosomeGenerator.get();

            // Evaluate fitness
            for (FitnessFunction<C> ff : fitnessFunctions) {
                ff.applyAsDouble(candidate);
            }

            solutions.add(candidate);

            // Track if it has reasonable coverage
            double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);
            if (coverage > 0.5) {  // At least 50% coverage
                goodSolutions.add(candidate);
            }

            stoppingCondition.notifyFitnessEvaluation();
        }


        // Phase 2:  Collect all solutions during the search
        while (!stoppingCondition.searchMustStop()) {
            C candidate;
            double randomValue = random.nextDouble();

            // 92% of time: generate using mutation-based exploration
            if (randomValue < 0.85 && !goodSolutions.isEmpty()) {
                // Generate base candidate
                C base = goodSolutions.get(random.nextInt(goodSolutions.size()));

                // Apply multiple mutations (3 - 8 times)
                int mutationCount = 3 + random.nextInt(6);
                candidate = base;
                for (int i = 0; i < mutationCount; i++) {
                    candidate = candidate.mutate();
                }
            } else if (randomValue < 0.95 && solutions.size() > 10) {
                // 10% of time: mutate from any previous solution
                C base = solutions.get(random.nextInt(solutions.size()));
                int mutationCount = 2 + random.nextInt(5);
                candidate = base;
                for (int i = 0; i < mutationCount; i++) {
                    candidate = candidate.mutate();
                }}else  {
                // 8% pure random for exploration
                candidate = chromosomeGenerator.get();
            }

            // Generate 3 candidates per iteration (like CODE 1)
            for (FitnessFunction<C> fitnessFunction : fitnessFunctions) {

                fitnessFunction.applyAsDouble(candidate);
            }
            solutions.add(candidate);
            // Update good solutions pool periodically
            double coverage = fitnessFunctions.get(1).applyAsDouble(candidate);
            double size = fitnessFunctions.get(0).applyAsDouble(candidate);

            // Add to good pool if high coverage OR small size with decent coverage
            if (coverage > 0.6 || (size < 0.3 && coverage > 0.4)) {
                goodSolutions.add(candidate);

                // Keep pool size manageable
                if (goodSolutions.size() > 100) {
                    goodSolutions.remove(0);
                }
            }

            stoppingCondition.notifyFitnessEvaluation();
        }

        // Build Pareto front using non-dominated sorting
        List<List<C>> fronts = findParetoFront(solutions);
        return fronts.isEmpty() ? new ArrayList<>() : fronts.get(0);
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
