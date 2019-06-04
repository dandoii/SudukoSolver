import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Prefilt} class is a library used for prefiltering in Sudoku
 * puzzles. Prefiltering helps to speed up the generation of solutions by
 * reducing the number of possible values for every empty cell to just the
 * ones allowed by constraints.
 */
public class Prefilt {
  // Row and column length.
  private static final int DIM = 9;
  // Lists of cell domains.
  private static List<Integer>[][] domainListsGrid;
  // List of cell checks.
  private static List<Integer> checksList;

  /**
   * Filter domain for every empty cell.
   *
   * @param sudoku Sudoku representation.
   * @return 2D array of cell domain lists.
   */
  public static List<Integer>[][] filter(int[][] sudoku) {
    // Initialise and populate domainListsGrid.
    domainListsGrid = (ArrayList<Integer>[][]) new ArrayList[DIM][DIM];
    for (int i = 0; i < DIM; i++) {
      for (int j = 0; j < DIM; j++) {
        domainListsGrid[i][j] = new ArrayList<>(DIM);
        List<Integer> domain = domainListsGrid[i][j];
        if (sudoku[i][j] == 0) {
          for (int k = 1; k <= DIM; k++) {
            domain.add(k);
          }
        } else {
          domain.add(sudoku[i][j]);
        }
      }
    }

    // Filter cell domains.
    checksList = new ArrayList<>(DIM * DIM);
    // Do checks once for all cells.
    for (int i = 0; i < DIM; i++) {
      for (int j = 0; j < DIM; j++) {
        checkDomains(i, j);
      }
    }
    // Then do checks only for cells in list.
    while (!checksList.isEmpty()) {
      int check = checksList.remove(checksList.size() - 1);
      checkDomains(check / 10, check % 10);
    }
    return domainListsGrid;
  }

  /**
   * Filter every empty cell domain and return array of domains.
   *
   * @param sudoku Sudoku representation.
   * @return array of cell domains.
   */
  public static int[][] domainsList(int[][] sudoku) {
    filter(sudoku);
    return makeDomainsList();
  }

  /**
   * Helper method that converts 2D array of domain lists to array of domains.
   *
   * @return array of cell domains.
   */
  private static int[][] makeDomainsList() {
    // Create and populate domainsList.
    int[][] domainsList = new int[DIM * DIM][];
    int index = 0;
    for (int i = 0; i < DIM; i++) {
      for (int j = 0; j < DIM; j++) {
        List<Integer> domain = domainListsGrid[i][j];
        domainsList[index] = new int[domain.size()];
        for (int k = 0; k < domainsList[index].length; k++) {
          domainsList[index][k] = domain.get(k);
        }
        index++;
      }
    }
    return domainsList;
  }

  /**
   * Helper method to reduce domains.
   *
   * @param row row index.
   * @param col column index.
   */
  private static void checkDomains(int row, int col) {
    // Do checks if cell has fixed value.
    List<Integer> domain = domainListsGrid[row][col];
    if (domain.size() == 1) {
      // Get cell value.
      Integer val = domain.get(0);
      // Row and Column check.
      for (int i = 0; i < DIM; i++) {
        // Row check.
        domain = domainListsGrid[row][i];
        if (i != col && domain.contains(val)) {
          domain.remove(val);
          if (domain.size() == 1) {
            checksList.add(row * 10 + i);
          }
        }
        // Column check.
        domain = domainListsGrid[i][col];
        if (i != row && domain.contains(val)) {
          domain.remove(val);
          if (domain.size() == 1) {
            checksList.add(i * 10 + col);
          }
        }
      }
      // Sector check.
      int secRow = (row / 3) * 3;
      int secCol = (col / 3) * 3;
      for (int i = secRow; i < secRow + 3; i++) {
        for (int j = secCol; j < secCol + 3; j++) {
          domain = domainListsGrid[i][j];
          if (i != row && j != col
                  && domain.contains(val)) {
            domain.remove(val);
            if (domain.size() == 1) {
              checksList.add(i * 10 + j);
            }
          }
        }
      }
    }
  }
}
