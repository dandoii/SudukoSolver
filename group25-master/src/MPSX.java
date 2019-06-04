/**
 * The {@code MPSX} class is a library containing a single public method to
 * perform multiparental sorting crossover.
 */
class MPSX {
  // Parent arrays length.
  private static final int DIM = 9;

  /**
   * Perform multiparental sorting crossover.
   *
   * @param mask array of row indices.
   * @param parents array of Sudoku rows.
   * @return row produced by crossover.
   */
  static int[] cross(int[] mask, int[][] parents) {
    // Generate child.
    int[] child = new int[DIM];
    for (int i = 0; i < DIM; i++) {
      int indexMask = mask[i] - 1;
      child[i] = parents[indexMask][i];
      // Swap.
      for (int j = 0; j < parents.length; j++) {
        if (j != indexMask && parents[j][i] != child[i]) {
          for (int k = 0; k < DIM; k++) {
            if (parents[j][k] == child[i]) {
              parents[j][k] = parents[j][i];
              parents[j][i] = child[i];
            }
          }
        }
      }
    }
    return child;
  }
}
