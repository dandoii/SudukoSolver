import java.util.ArrayList;
import java.util.List;

/**
 * The {@code PMX} class is a library with a single public method to perform
 * partially matched crossover.
 */
public class PMX {
  // Parent arrays length.
  private static final int DIM = 9;

  /**
   * Perform partially matched crossover.
   *
   * @param parent1 Sudoku row.
   * @param parent2 Sudoku row.
   * @param low lower bound of segment.
   * @param upp upper bound of segment.
   * @return row produced by crossover.
   */
  public static int[] cross(int[] parent1, int[] parent2,
                            int low, int upp) {
    // Create and populate vals list.
    List<Integer> vals = new ArrayList<>();
    for (int i = low; i <= upp; i++) {
      for (int j = low; j <= upp; j++) {
        if (parent2[i] == parent1[j]) {
          break;
        }
        if (j == upp) {
          vals.add(parent2[i]);
        }
      }
    }

    // Create list of second parent.
    List<Integer> parent2List = new ArrayList<>(DIM);
    for (int i = 0; i < DIM; i++) {
      parent2List.add(parent2[i]);
    }

    // Generate child.
    int[] child = new int[DIM];
    int placed = 0;
    boolean lvalPlaced = true;
    int val = 0;
    int lval = 0;
    while (placed < vals.size()) {
      if (lvalPlaced) {
        val = vals.get(placed);
        lval = val;
        lvalPlaced = false;
      }
      int bigV = parent1[parent2List.indexOf(val)];
      if (parent2List.indexOf(bigV) >= low
              && parent2List.indexOf(bigV) <= upp) {
        val = bigV;
        continue;
      }
      // Position found.
      child[parent2List.indexOf(bigV)] = lval;
      lvalPlaced = true;
      placed++;
    }

    // Copy segment and remaining to child.
    for (int i = 0; i < DIM; i++) {
      if (i >= low && i <= upp) {
        child[i] = parent1[i];
      } else if (child[i] == 0) {
        child[i] = parent2[i];
      }
    }
    return child;
  }
}
