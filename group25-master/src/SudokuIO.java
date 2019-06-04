import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.out;

/**
 * The {@code SudokuIO} class is a library that manages input and output of
 * Sudoku puzzles.
 */
class SudokuIO {
  // General input array length.
  private static final int DIM = 9;

  /**
   * Read input data from file into a dynamically sized 2D {@code int} array.
   *
   * @param path input file path.
   * @param mode determines row and column configuration to use for array.
   * @return 2D {@code int} array of puzzle data.
   */
  static int[][] readInput(String path, int mode) {
    // Declare input array and allocate default memory for rows.
    int[][] input;
    if (mode == 0) {
      input = new int[3][];
    } else if (mode == 1) {
      input = new int[4][];
    } else {
      input = new int[DIM][];
    }

    // Initialise Scanner.
    Scanner reader = null;
    try {
      reader = new Scanner(new File(path));
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Read input lines.
    if (reader != null) {
      for (int i = 0; reader.hasNextLine(); i++) {
        // Allocate column memory.
        if (i == 0 && mode == 0) {
          input[i] = new int[2];
        } else {
          // Allocate additional row memory as needed.
          if (i >= input.length) {
            input = Arrays.copyOf(input, input.length + 1);
          }
          input[i] = new int[DIM];
        }
        // Convert string input to int array.
        int auxIndex = 0;
        int tempIndex = 0;
        int[] temp = input[i];
        String[] aux = reader.nextLine().split("");
        while (tempIndex != temp.length) {
          if (aux[auxIndex].matches("[1-9]")) {
            temp[tempIndex++] = Integer.parseInt(aux[auxIndex]);
          } else if (aux[auxIndex].equals(".")) {
            temp[tempIndex++] = 0;
          }
          auxIndex++;
        }
      }
    }
    return input;
  }

  /**
   * Print {@code int} array on single line.
   *
   * @param output {@code int} array to print.
   * @param mode determines if spaces should be printed between elements.
   */
  static void print(int[] output, int mode) {
    for (int i = 0; i < output.length; i++) {
      out.print(output[i]);
      // Don't print spaces when printing solver output.
      if (mode != 3 && i < output.length - 1) {
        out.print(" ");
      }
    }
  }

  /**
   * Print 2D {@code int} array as grid.
   *
   * @param output 2D {@code int} array to print.
   * @param mode determines if spaces should be printed between elements.
   */
  static void print(int[][] output, int mode) {
    for (int i = 0; i < output.length; i++) {
      print(output[i], mode);
      // Don't print new line after final output line.
      if (i != output.length - 1) {
        out.println();
      }
    }
  }
}
