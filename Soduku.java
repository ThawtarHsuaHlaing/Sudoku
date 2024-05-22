package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
public class Soduku extends JFrame {

    private JButton[][] buttons = new JButton[9][9];
    private int[][] board = new int[9][9];
    private int secondsElapsed = 0;
    private Timer timer;
    private JLabel timerLabel;
    private SudokuGenerator sudokuGenerator = new SudokuGenerator();

    public Soduku() {
        initializeGUI();

        timer = new Timer(1000, e -> {
            secondsElapsed++;
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            String timeString = String.format("Timer: %02d:%02d", minutes, seconds);
            timerLabel.setText(timeString);
        });
        timer.start();
    }

    private void updateButtonsWithGrid() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int cellValue = board[i][j];
                buttons[i][j].setText(cellValue == SudokuGenerator.EMPTY ? "" : String.valueOf(cellValue));
                if (cellValue != SudokuGenerator.EMPTY) {
                    buttons[i][j].setEnabled(false); // Disable clue buttons
                } else {
                    buttons[i][j].setEnabled(true); // Enable empty buttons for input
                }
            }
        }
    }

    private void initializeGUI() {
        setLayout(new BorderLayout());

        JPanel sudokuPanel = new JPanel(new GridLayout(9, 9));
        buttons = new JButton[9][9];

        // Initialize the buttons and add them to sudokuPanel
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 24));
                sudokuPanel.add(buttons[i][j]); // Add the buttons to the panel
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onButtonClick(row, col);
                    }
                });
            }
        }

        JButton createPuzzleButton = new JButton("Create Puzzle");
        createPuzzleButton.addActionListener(e -> {
            // Prompt the user to choose a difficulty level
            String[] options = { "Easy", "Medium", "Hard" };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Select a difficulty level:",
                    "Choose Difficulty",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1] // Default to Medium
            );

            if (choice != JOptionPane.CLOSED_OPTION) {
                // Map the choice to a difficulty level (1 for Easy, 2 for Medium, 3 for Hard)
                int difficultyLevel = choice + 1;
                sudokuGenerator.createPuzzle(difficultyLevel);
                board = sudokuGenerator.getGrid();
                updateButtonsWithGrid();
            }
        });

        JButton checkButton = new JButton("Check Answer");
        checkButton.addActionListener(e -> {
            checkSolution();
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            restartGame();
        });

        JButton showAnswerButton = new JButton("Show Answer");
        showAnswerButton.addActionListener(e -> {
            showAnswer();
        });

        timerLabel = new JLabel("Timer: 00:00");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = new JPanel();

        // Add timer label in the center
        topPanel.add(timerLabel, BorderLayout.CENTER);

        // Add create puzzle, check answer, and other buttons to the right
        controlPanel.add(createPuzzleButton);
        controlPanel.add(checkButton);
        // Create a new JPanel for the bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(resetButton);
        bottomPanel.add(showAnswerButton);

        // Add the bottomPanel to the bottom of the main frame
        add(bottomPanel, BorderLayout.SOUTH);

        topPanel.add(controlPanel, BorderLayout.EAST);

        // Add topPanel at the top
        add(topPanel, BorderLayout.NORTH);

        add(sudokuPanel, BorderLayout.CENTER); // Sudoku panel in the center

        setTitle("Sudoku");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void showAnswer() {
        int[][] solution = new int[9][9];
        // Copy the current board to a separate array for displaying the solution
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                solution[i][j] = board[i][j];
            }
        }

        // Solve the copied board to get the solution
        if (sudokuGenerator.solveSudoku(solution)) {
            // Display the solution on the buttons
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    buttons[i][j].setText(String.valueOf(solution[i][j]));
                }
            }
        } else {
            // Handle the case where there's no solution (shouldn't happen for a valid
            // puzzle)
            JOptionPane.showMessageDialog(this, "There is no valid solution for this puzzle.");
        }
    }

    private void restartGame() {
        if (timer != null) {
            timer.stop();
        }

        secondsElapsed = 0;
        timerLabel.setText("Timer: 00:00");
        clearSudokuGrid();

        // Start the timer again
        timer.start();
    }

    private void clearSudokuGrid() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = SudokuGenerator.EMPTY;
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
    }

    private void checkSolution() {
        boolean isValid = isSudokuSolved();
        String message = isValid ? "Congratulations! You've solved the Sudoku puzzle!" : "Invalid solution!";
        JOptionPane.showMessageDialog(this, message);
        if (isValid) {
            timer.stop();
        }
    }

    private boolean isSudokuSolved() {
        // Check if all cells are filled
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == SudokuGenerator.EMPTY) {
                    return false;
                }
            }
        }

        // Check if the filled board is a valid solution
        return sudokuGenerator.isValidSolution(board);
    }

    private void onButtonClick(int row, int col) {
        String val = JOptionPane.showInputDialog("Enter a value (1-9):");
        if (val == null || val.isEmpty()) {
            return;
        }

        try {
            int num = Integer.parseInt(val);

            if (num < 1 || num > 9) {
                JOptionPane.showMessageDialog(this, "Please enter a number between 1 and 9.");
                return;
            }

            if (sudokuGenerator.isValidMove(board, row, col, num)) {
                board[row][col] = num;
                buttons[row][col].setText(val);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid move. This number violates Sudoku rules.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        }
    }

    public class SudokuGenerator {
        private int[][] grid;
        private static final int SIZE = 9;
        public static final int EMPTY = 0;

        public SudokuGenerator() {
            grid = new int[SIZE][SIZE];
        }

        public int[][] getGrid() {
            return grid;
        }

        public void createPuzzle(int difficultyLevel) {
            clearGrid();
            solveSudoku();
            removeNumbers(difficultyLevel);
        }

        public boolean isValidSolution(int[][] puzzle) {
            return solveSudoku(puzzle);
        }

        public boolean solveSudoku() {
            return solveSudoku(grid);
        }

        private boolean solveSudoku(int[][] puzzle) {
            return solveSudoku(puzzle, 0, 0);
        }

        private boolean solveSudoku(int[][] puzzle, int row, int col) {
            if (row == SIZE) {
                row = 0;
                if (++col == SIZE) {
                    return true; // Successfully solved
                }
            }
            if (puzzle[row][col] != EMPTY) {
                return solveSudoku(puzzle, row + 1, col);
            }

            Random random = new Random();
            int[] numbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            for (int i = 0; i < SIZE; i++) {
                int randIndex = random.nextInt(SIZE - i);
                int num = numbers[randIndex];
                numbers[randIndex] = numbers[SIZE - 1 - i];

                if (isValidMove(puzzle, row, col, num)) {
                    puzzle[row][col] = num;
                    if (solveSudoku(puzzle, row + 1, col)) {
                        return true;
                    }
                    puzzle[row][col] = EMPTY;
                }
            }
            return false;
        }

        private boolean isValidMove(int[][] puzzle, int row, int col, int num) {
            return !usedInRow(puzzle, row, num) && !usedInCol(puzzle, col, num)
                    && !usedInBox(puzzle, row - row % 3, col - col % 3, num);
        }

        private boolean usedInRow(int[][] puzzle, int row, int num) {
            for (int col = 0; col < SIZE; col++) {
                if (puzzle[row][col] == num) {
                    return true;
                }
            }
            return false;
        }

        private boolean usedInCol(int[][] puzzle, int col, int num) {
            for (int row = 0; row < SIZE; row++) {
                if (puzzle[row][col] == num) {
                    return true;
                }
            }
            return false;
        }

        private boolean usedInBox(int[][] puzzle, int startRow, int startCol, int num) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (puzzle[row + startRow][col + startCol] == num) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void removeNumbers(int difficultyLevel) {
            Random random = new Random();
            int remainingCells = SIZE * SIZE;
            int numClues;

            switch (difficultyLevel) {
                case 1: // Easy
                    numClues = 30; // Adjust this number as needed
                    break;
                case 2: // Medium
                    numClues = 24; // Adjust this number as needed
                    break;
                case 3: // Hard
                    numClues = 18; // Adjust this number as needed
                    break;
                default: // Default to Medium difficulty if the level is not recognized
                    numClues = 24;
                    break;
            }

            while (remainingCells > numClues) {
                int row = random.nextInt(SIZE);
                int col = random.nextInt(SIZE);

                if (grid[row][col] != EMPTY) {
                    grid[row][col] = EMPTY;
                    remainingCells--;
                }
            }
        }

        private void clearGrid() {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    grid[i][j] = EMPTY;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Soduku();
        });
    }
}
