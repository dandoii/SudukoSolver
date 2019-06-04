import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code Solver} class is a library that contains one public method,
 * {@code solve}, that solves Sudoku puzzles.
 */
public class Solver {
  // Sudoku dimension.
  private static final int DIM = 9;
  // Useful constant.
  private static final int BIG_DIM = 10;
  // Number of solutions.
  private static final int SOL_NUM = 1000;
  // Time limit in seconds.
  private static final double MAX_TIME = 13.5;
  // Probabilities.
  private static final double PROB_PMX_MULTI = 0.9208;
  private static final double PROB_MUTATE = 0.5169;
  private static final double PROB_A = 0.9781;
  // Lists of cell domains.
  private static List<Integer>[][] domainListsGrid;
  // List to order cells according to domain size.
  private static List<SizeIndex>[] sizeIndexLists;
  // List of solutions.
  private static List<Solution> solutionsList;
  // Cost comparator object.
  private static Cost cost = new Cost();
  // Previous best solution.
  private static Solution lastBest;

  /**
   * The {@code SizeIndex} class serves as container for domain size and
   * column index of every cell in a row. Allows for iteration of row
   * cells in order of domain size.
   */
  private static class SizeIndex {
    int col;
    int size;

    /**
     * Constructor that takes cell column index and domain size.
     *
     * @param col cell column index.
     * @param size cell domain size.
     */
    SizeIndex(int col, int size) {
      this.col = col;
      this.size = size;
    }
  }

  /** {@code Comparator} for sorting {@code SizeIndex} objects. */
  private static class Size implements Comparator<SizeIndex> {

    /**
     * Compares {@code SizeIndex} objects with respect to domain size.
     *
     * @param s1 one {@code SizeIndex} object.
     * @param s2 other {@code SizeIndex} object.
     * @return same values as with {@code Integer} comparison.
     */
    @Override
    public int compare(SizeIndex s1, SizeIndex s2) {
      return Integer.compare(s1.size, s2.size);
    }
  }

  /**
   * The {@code Solution} class serves as container for potential solution,
   * including associated cost, and status.
   */
  private static class Solution {
    int[][] grid;
    int cost;
    boolean processed;

    /**
     * Constructor that takes a potential solution.
     *
     * @param grid potential solution.
     */
    Solution(int[][] grid) {
      this.grid = grid;
      getCost();
      processed = false;
    }

    /**
     * Determine cost of solution by counting all occurrences of values
     * appearing more than once per column or sector.
     */
    private void getCost() {
      int cost = 0;
      boolean[] in;
      // Column check.
      for (int i = 0; i < DIM; i++) {
        in = new boolean[BIG_DIM];
        for (int j = 0; j < DIM; j++) {
          if (!in[grid[j][i]]) {
            in[grid[j][i]] = true;
          } else {
            cost++;
          }
        }
      }
      // Sector check.
      for (int i = 0; i <= 6; i += 3) {
        for (int j = 0; j <= 6; j += 3) {
          in = new boolean[BIG_DIM];
          for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
              if (!in[grid[k][l]]) {
                in[grid[k][l]] = true;
              } else {
                cost++;
              }
            }
          }
        }
      }
      this.cost = cost;
    }
  }

  /** {@code Comparator} for sorting {@code Solution} objects. */
  private static class Cost implements Comparator<Solution> {

    /**
     * Compares {@code Solution} objects with respect to cost.
     *
     * @param sol1 one {@code Solution} object.
     * @param sol2 other {@code Solution} object.
     * @return same values as with {@code Integer} comparison.
     */
    @Override
    public int compare(Solution sol1, Solution sol2) {
      return Integer.compare(sol1.cost, sol2.cost);
    }
  }

  /**
   * Solve Sudoku puzzles using prefiltered cuckoo search algorithm with
   * geometric operators.
   *
   * @param sudoku Sudoku representation.
   * @return Sudoku solution or {@code null}.
   */
  public static int[][] solve(int[][] sudoku) {
    // Take start time.
    long start = System.currentTimeMillis();

    // Initialise domainListsGrid.
    domainListsGrid = Prefilt.filter(sudoku);

    // Apply naked singles.
    for (int i = 0; i < DIM; i++) {
      for (int j = 0; j < DIM; j++) {
        List<Integer> domain = domainListsGrid[i][j];
        if (domain.size() == 1) {
          Integer temp = domain.get(0);
          sudoku[i][j] = temp;
        }
      }
    }

    // Initialise and populate sizeIndexLists.
    sizeIndexLists = (ArrayList<SizeIndex>[]) new ArrayList[DIM];
    Size size = new Size();
    for (int i = 0; i < DIM; i++) {
      sizeIndexLists[i] = new ArrayList<>(DIM);
      for (int j = 0; j < DIM; j++) {
        // Only process empty cell domains.
        if (domainListsGrid[i][j].size() > 1) {
          sizeIndexLists[i].add(new SizeIndex(j,
                  domainListsGrid[i][j].size()));
        }
      }
      // Sort in increasing size order.
      sizeIndexLists[i].sort(size);
    }

    // Initialise solutionsList.
    solutionsList = new ArrayList<>(SOL_NUM);
    // Generate initial solutions.
    generateSolutions(sudoku);
    lastBest = copyOf(0);

    // Solve.
    while ((System.currentTimeMillis() - start) / 1000.0 < MAX_TIME) {
      if (solutionsList.get(0).cost == 0) {
        return solutionsList.get(0).grid;
      }
      // Choose two solutions.
      int index1 = randomNum(SOL_NUM);
      int index2;
      do {
        index2 = randomNum(SOL_NUM);
      } while (index2 == index1);
      geoOps();
      // Then compare cost of chosen solutions.
      if (solutionsList.get(index1).cost
              <= solutionsList.get(index2).cost) {
        solutionsList.add(index2, copyOf(index1));
      }
      abandonWorst();
      generateSolutions(sudoku);
    }
    return null;
  }

  /**
   * Generate potential solutions.
   *
   * @param sudoku Sudoku representation.
   */
  private static void generateSolutions(int[][] sudoku) {
    // Always SOL_NUM solutions.
    while (solutionsList.size() < SOL_NUM) {
      int[][] grid = new int[DIM][DIM];
      // Construct row-by-row.
      for (int i = 0; i < DIM; i++) {
        // Shuffle domains.
        for (int j = 0; j < DIM; j++) {
          List<Integer> domain = domainListsGrid[i][j];
          if (domain.size() > 1) {
            Collections.shuffle(domain);
          }
        }
        // Copy fixed values.
        System.arraycopy(sudoku[i], 0, grid[i], 0, DIM);
        // Fill empty cells.
        insertNonFixed(grid[i], i);
      }
      solutionsList.add(new Solution(grid));
    }
    // Sort in ascending cost order.
    solutionsList.sort(cost);
  }

  /**
   * Populate row cells in ascending domain size order, using backtracking.
   *
   * @param grid solution row to populate.
   * @param row index of solution row to populate.
   * @return whether successful.
   */
  private static boolean insertNonFixed(int[] grid, int row) {
    for (SizeIndex index : sizeIndexLists[row]) {
      if (grid[index.col] == 0) {
        for (int val : domainListsGrid[row][index.col]) {
          if (isValid(grid, val)) {
            grid[index.col] = val;
            if (insertNonFixed(grid, row)) {
              return true;
            } else {
              grid[index.col] = 0;
            }
          }
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Determine whether legal to insert value in row.
   *
   * @param grid row into which to insert value.
   * @param val value to insert.
   * @return whether legal.
   */
  private static boolean isValid(int[] grid, int val) {
    for (int placedVal : grid) {
      if (val == placedVal) {
        return false;
      }
    }
    return true;
  }

  /**
   * Perform geometric operators on potential solutions.
   */
  private static void geoOps() {
    int pass = 0;
    while (pass < SOL_NUM - 1) {
      pass = 0;
      for (int i = 1; i < SOL_NUM; i++) {
        Solution solution = solutionsList.get(i);
        // If solution is unprocessed.
        if (!solution.processed) {
          // Mark solution as processed.
          solution.processed = true;
          // Perform geometric operators.
          if (Math.random() < PROB_PMX_MULTI) {
            pmxCross(solution.grid, solutionsList.get(0).grid);
          } else {
            mpsxCross(solution.grid, solutionsList.get(0).grid,
                    lastBest.grid);
          }
          if (Math.random() < PROB_MUTATE) {
            mutate(solution.grid);
          }
          // Save best.
          lastBest = copyOf(0);
          // Update cost.
          solution.getCost();
          // Update order.
          solutionsList.sort(cost);
        } else {
          pass++;
        }
      }
    }
    // Mark all solutions as unprocessed.
    for (Solution solution : solutionsList) {
      solution.processed = false;
    }
  }

  /**
   * Creates copy of solution at index {@code sol}.
   *
   * @param sol index of solution to copy.
   * @return copy of solution {@code sol}.
   */
  private static Solution copyOf(int sol) {
    int[][] grid = new int[DIM][DIM];
    int[][] solGrid = solutionsList.get(sol).grid;
    for (int i = 0; i < DIM; i++) {
      System.arraycopy(solGrid[i], 0, grid[i], 0, DIM);
    }
    return new Solution(grid);
  }

  /**
   * Perform partially matched crossover on all rows of solution and current
   * best solution to generate new solution.
   *
   * @param grid solution to change.
   * @param gridBest current best solution.
   */
  private static void pmxCross(int[][] grid, int[][] gridBest) {
    for (int i = 0; i < DIM; i++) {
      // Create bounds.
      int low = randomNum(DIM);
      int upp;
      do {
        upp = randomNum(DIM);
      } while (upp == low);
      if (low > upp) {
        int temp = low;
        low = upp;
        upp = temp;
      }
      // Create new row.
      grid[i] = PMX.cross(grid[i], gridBest[i], low, upp);
    }
  }

  /**
   * Perform multiparental sorting crossover on all rows of solution,
   * current best solution, and previous best solution to generate new solution.
   *
   * @param grid solution to change.
   * @param gridBest current best solution.
   * @param gridLastBest previous best solution.
   */
  private static void mpsxCross(int[][] grid, int[][] gridBest,
                                int[][] gridLastBest) {
    // Create mask.
    int[] mask = new int[DIM];
    for (int i = 0; i < DIM; i++) {
      mask[i] = randomNum(3) + 1;
    }

    // Create new rows.
    int[][] aux = new int[3][DIM];
    for (int i = 0; i < DIM; i++) {
      System.arraycopy(grid[i], 0, aux[0], 0, DIM);
      System.arraycopy(gridBest[i], 0, aux[1], 0, DIM);
      System.arraycopy(gridLastBest[i], 0, aux[2], 0, DIM);
      grid[i] = MPSX.cross(mask, aux);
    }
  }

  /**
   * Switch values of two cells in random number of random rows.
   *
   * @param grid solution to change.
   */
  private static void mutate(int[][] grid) {
    // Choose random rows.
    List<Integer> rows = new ArrayList<>(DIM);
    for (int i = 0; i < DIM; i++) {
      rows.add(i);
    }
    Collections.shuffle(rows);

    // Choose random number.
    int num = randomNum(DIM) + 1;

    // Mutate.
    int count = 0;
    while (count < num) {
      int i = rows.remove(rows.size() - 1);
      List<Integer> cells = new ArrayList<>(DIM);
      for (int j = 0; j < DIM; j++) {
        // Only consider empty cells.
        if (domainListsGrid[i][j].size() > 1) {
          cells.add(j);
        }
      }
      if (cells.size() > 1) {
        Collections.shuffle(cells);
        int index1 = cells.remove(cells.size() - 1);
        int index2 = cells.remove(cells.size() - 1);
        // Swap values.
        int temp = grid[i][index1];
        grid[i][index1] = grid[i][index2];
        grid[i][index2] = temp;
      }
      count++;
    }
  }

  /**
   * Remove worst solutions from list.
   */
  private static void abandonWorst() {
    solutionsList.sort(cost);
    // Get minimum cost.
    int minCost = solutionsList.get(0).cost;
    // Process list in reverse order.
    for (int i = SOL_NUM - 1; i > 0; i--) {
      // Remove higher cost solutions from list.
      if (solutionsList.get(i).cost > minCost) {
        if (Math.random() < PROB_A) {
          solutionsList.remove(i);
        }
      }
    }
  }

  /**
   * Generate random integer in range from {@code 0} to {@code n - 1}.
   *
   * @param max exclusive maximum of range.
   * @return random integer from {@code 0} to {@code n - 1}.
   */
  private static int randomNum(int max) {
    return (int) (max * Math.random());
  }
}
