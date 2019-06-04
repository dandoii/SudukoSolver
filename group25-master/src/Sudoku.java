/**
 * The {@code Sudoku} class contains a single main method that supports
 * prefiltering, partially matched crossover, multiparental sorting crossover,
 * and the solving of Sudoku puzzles.
 */
public class Sudoku {

  /**
   * Perform operation, specified by mode, using data from input file.
   *
   * @param args commandline arguments specifying mode and input file path.
   */
  public static void main(String[] args) {
    int mode = Integer.valueOf(args[0]);
    int[][] input = SudokuIO.readInput(args[1], mode);

    switch (mode) {
      case 0:
        SudokuIO.print(PMX.cross(input[1], input[2],
                input[0][0] - 1, input[0][1] - 1), mode);
        break;
      case 1:
        int[][] aux = new int[input.length - 1][];
        System.arraycopy(input, 1, aux, 0, input.length - 1);
        SudokuIO.print(MPSX.cross(input[0], aux), mode);
        break;
      case 2:
        SudokuIO.print(Prefilt.domainsList(input), mode);
        break;
      case 3:
        int[][] solGrid = Solver.solve(input);
        if (solGrid != null) {
          SudokuIO.print(solGrid, mode);
        } else {
          System.out.println("MAX ITER EXCEEDED");
        }
        break;
      default:
        System.out.println("INVALID MODE");
    }
  }
}
