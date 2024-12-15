import tester.Tester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//EXTRA CREDIT:
//timer, counts the amount of flags left, counts the amount of clicks,
//enhance the graphics

// utility functions
class Utils {
  // checks that the given dimensions are limited according to display
  boolean limitDims(Random rand, int w, int l, int m) {
    return m < w * l && w <= 30 && l <= 20;
  }
}

//represents a Minesweeper game
interface IMineSweeper {
  int SCREEN_WIDTH = 1000;
  int SCREEN_HEIGHT = 1000;
}

//represents a world class to display the minesweeper game
class MineSweeper extends World implements IMineSweeper {
  int rows;
  int cols;
  int mines;
  boolean win;
  ArrayList<ArrayList<Cell>> board;
  boolean gameOver;
  boolean showFinalBoard;
  int counter;
  int numClicks = 0;
  int winner;
  boolean testReveal;

  // main constructor
  MineSweeper(int rows, int cols, int mines, Random rand, boolean reveal) {
    // when dimensions given fit within limitations, assign the values to the fields
    // otherwise, use preset values (maximum board size)
    if (new Utils().limitDims(rand, rows, cols, mines)) {
      this.rows = rows;
      this.cols = cols;
      this.mines = mines;
      this.gameOver = false;
      this.showFinalBoard = reveal;
      this.winner = mines;
    }
    else {
      this.rows = 30;
      this.cols = 16;
      this.mines = 99;
      this.gameOver = false;
      this.showFinalBoard = reveal;
      this.win = false;
      this.winner = this.mines;
    }

    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    // create a board according to the given row and column dimensions
    for (int r = 0; r < rows; r++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int c = 0; c < cols; c++) {
        Cell cell = new Cell(new ArrayList<Cell>(), 0, false);
        row.add(cell);
      }
      board.add(row);
    }
    // randomly place the given number of mines all over the board
    int m = 0;
    while (m < mines) {
      int x = rand.nextInt(rows - 1);
      int y = rand.nextInt(cols - 1);

      if (!board.get(x).get(y).hasMine) {
        Cell pos = board.get(x).get(y);
        pos.placeMine();
        m++;
      }
    }
    // find the neighbors of all cells in the board
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        ArrayList<Cell> neighbors = new ArrayList<Cell>();
        Cell currentCell = board.get(r).get(c);
        if (r > 0) {
          neighbors.add(board.get(r - 1).get(c)); // above current cell
        }
        if (r < rows - 1) {
          neighbors.add(board.get(r + 1).get(c)); // below current cell
        }
        if (c > 0) {
          neighbors.add(board.get(r).get(c - 1)); // left of current cell
        }
        if (c < cols - 1) {
          neighbors.add(board.get(r).get(c + 1)); // right of current cell
        }
        if (r > 0 && c > 0) {
          neighbors.add(board.get(r - 1).get(c - 1)); // diagonal-left-top of current cell
        }
        if (r + 1 < rows - 1 && c + 1 < cols - 1) {
          neighbors.add(board.get(r + 1).get(c + 1)); // diagonal-right-bottom of current cell
        }
        if (r > 0 && c + 1 < cols - 1) {
          neighbors.add(board.get(r - 1).get(c + 1)); // diagonal-right-top of current cell
        }
        if (r + 1 < rows - 1 && c > 0) {
          neighbors.add(board.get(r + 1).get(c - 1)); // diagonal-left-bottom of current cell
        }
        currentCell.updateNeighbors(neighbors);
      }
    }
    this.board = board;
  }

  //
  public void onTick() {
    if (!this.gameOver && !this.win) {
      this.counter += 1;
    }
  }

  // displays the game on the screen
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(SCREEN_WIDTH, SCREEN_HEIGHT);
    return this.draw(ws, this.showFinalBoard, this.testReveal);
  }

  //
  public WorldScene lastScene(String msg) {
    WorldScene finalScene = new WorldScene(SCREEN_WIDTH, SCREEN_HEIGHT);
    return this.draw(finalScene, true, false);
  }

  // draws the board
  public WorldScene draw(WorldScene ws, boolean finalReveal, boolean testReveal) {
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.cols; c++) {
        Cell currentCell = this.board.get(r).get(c);
        if (finalReveal && currentCell.hasMine) {
          currentCell.revealHelp();
        }
        else if (testReveal) {
          currentCell.revealHelp();
        }
        ws.placeImageXY(currentCell.drawCell(), r * 20 + 10, c * 20 + 10);
      }
    }

    ws.placeImageXY(new OverlayImage(new TextImage("" + this.mines, 25, Color.RED),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, cols * 20 + 100 / 2);
    ws.placeImageXY(new TextImage("Click Count: " + this.numClicks, 25, Color.black),
        rows * 20 / 2 + 10, cols * 20 + 100 / 2 - 25);
    ws.placeImageXY(new TextImage("⏱︎", 30, Color.red), rows * 20 - 100, cols * 20 + 100 / 2);
    ws.placeImageXY(
        new OverlayImage(new TextImage(this.counter + "", 25, Color.red),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)),
        rows * 20 - 50, cols * 20 + 100 / 2);
    ws.placeImageXY(
        new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)),
        rows * 20 / 2 + 10, cols * 20 + 100 / 2 + 15);

    if (this.gameOver) {
      ws.placeImageXY(new TextImage("Game Over ☠︎", 90, Color.RED), rows * 20 / 2 - 10,
          cols * 20 / 2 - 10);
    }
    if (this.win) {
      ws.placeImageXY(new TextImage("You Win ᕙ(๑˘ω˘)ᕗ︎", 60, Color.RED), rows * 20 / 2 - 10,
          cols * 20 / 2 - 10);
    }
    return ws;
  }

  //
  public void onMouseClicked(Posn pos, String buttonName) {
    int cellDim = 20;
    int c = Math.floorDiv(pos.x, cellDim);
    int r = Math.floorDiv(pos.y, cellDim);

    // if the user clicks a location outside the board constraints
    if (c > rows - 1 || r > cols - 1) {
      return;
    }
    // as long as the game as not over yet/the user has not won, register
    // the mouse click
    if (!gameOver) {
      Cell cellPressed = this.board.get(c).get(r);
      this.numClicks += 1;
      if (buttonName.equals("RightButton")) {
        if (!cellPressed.isFlag) {
          this.mines -= 1;
          if (cellPressed.hasMine) {
            this.winner -= 1;
          }
        }
        else {
          this.mines += 1;
        }
        cellPressed.placeFlag();
      }
      else if (buttonName.equals("LeftButton")) {
        if (cellPressed.hasMine) {
          this.gameOver = true;
          this.lastScene("Game Over");
        }
        cellPressed.flood();
      }
      this.checkWin();
    }
  }

  void checkWin() {
    int winner = this.rows * this.cols - this.mines;
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.cols; c++) {
        Cell cell = this.board.get(r).get(c);
        if (!cell.hasMine && cell.revealed || cell.isFlag) {
          winner -= 1;
        }
      }
    }
    if (winner == 0 && this.winner == 0) {
      this.win = true;
    }
  }

}

// represents a cell on the board
class Cell {
  ArrayList<Cell> neighbors; // arraylist of cells bordering this cell
  int numMines; // number of mines in the surrounding cells
  boolean hasMine; // if the cell contains a mine
  boolean revealed = false; // if the cell has been clicked on
  boolean isFlag;

  Cell(ArrayList<Cell> neighbors, int numMines, boolean isFlag, boolean hasMine) {
    this.neighbors = neighbors;
    this.numMines = numMines;
    this.hasMine = hasMine;
    this.isFlag = isFlag;
    this.revealed = false;
  }

  Cell(ArrayList<Cell> neighbors, int numMines, boolean hasMine) {
    this.neighbors = neighbors;
    this.numMines = numMines;
    this.hasMine = hasMine;
  }

  // method to mutate the hasMine field of a given cell
  public Cell placeMine() {
    this.hasMine = true;
    return this;
  }

  // method to mutate the neighbors field of a given cell
  // mutates the numMines field if the given cell does NOT contain a mine
  void updateNeighbors(ArrayList<Cell> n) {
    this.neighbors = n;
    if (!this.hasMine) {
      this.numMines = this.countMines();
    }
  }

  // method to count the number of mines surrounding the current cell
  public int countMines() {
    int count = 0;
    for (int i = 0; i < this.neighbors.size(); i++) {
      if (this.neighbors.get(i).hasMine) {
        count += 1;
      }
    }
    return count;
  }

  //
  void placeFlag() {
    if (!this.isFlag && !this.revealed) {
      this.isFlag = true;
    }
    else {
      this.isFlag = false;
    }
  }

  void revealHelp() {
    this.revealed = true;
  }

  void flood() {
    this.revealed = true;
    if (this.countMines() == 0) {
      for (Cell n : neighbors) {
        if (n.countMines() > 0 && !this.hasMine) {
          n.revealHelp();
        }
        if (!n.revealed && n.countMines() == 0) {
          n.revealHelp();
          n.flood();
        }
      }
    }
  }

  // draws the cell
  public WorldImage drawCell() {
    WorldImage start = new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK);
    WorldImage opened = new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY);
    WorldImage normal = new OverlayImage(new TextImage("" + this.numMines, Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
    WorldImage mine = new OverlayImage(new TextImage("☢︎", 20, Color.BLACK),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
    WorldImage flag = new OverlayImage(new TextImage("▶", 15, Color.RED),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
    if (this.hasMine && this.revealed) {
      return mine;
    }
    else if (this.revealed && this.countMines() == 0) {
      return opened;
    }
    else if (this.numMines >= 0 && this.revealed) {
      return normal;
    }
    else if (this.isFlag) {
      return flag;
    }
    else if (!this.isFlag) {
      return start;
    }
    else {
      return opened;
    }
  }
}

class ExamplesMineSweeper {
  // minesweeper
  MineSweeper m1;
  MineSweeper m2;
  MineSweeper m3;
  MineSweeper m4;
  // tile types
  WorldImage start = new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK);
  WorldImage opened = new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY);
  WorldImage normal = new OverlayImage(new TextImage("2", Color.BLUE),
      new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
  WorldImage mine = new OverlayImage(new TextImage("☢︎", 20, Color.BLACK),
      new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
  WorldImage flag = new OverlayImage(new TextImage("▶", 15, Color.RED),
      new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK));
  // makeScene testing
  MineSweeper ms1;
  MineSweeper ms6;
  MineSweeper ms2;
  MineSweeper ms5;
  MineSweeper ms3;
  MineSweeper ms4;
  MineSweeper ms7 = new MineSweeper(2, 2, 1, new Random(15), false);
  // display
  WorldScene ws1 = new WorldScene(1000, 1000);
  ArrayList<Cell> mt = new ArrayList<Cell>();
  // 2 x 2 board
  // +---+---+
  // | M | 1 |
  // +---+---+
  // | 1 | 1 |
  // +---+---+
  Cell b1c1 = new Cell(mt, 0, true);
  Cell b1c2 = new Cell(mt, 1, false);
  Cell b1c3 = new Cell(mt, 1, false);
  Cell b1c4 = new Cell(mt, 1, false);
  Cell cb1 = new Cell(mt, 0, true);
  Cell cb2 = new Cell(mt, 1, false);
  Cell cb3 = new Cell(mt, 1, false);
  Cell cb4 = new Cell(mt, 1, false);
  // 2 x 2 board addNeighbors test
  ArrayList<Cell> ab1c1 = new ArrayList<Cell>(Arrays.asList(this.b1c2, this.b1c3, this.b1c4));
  ArrayList<Cell> ab1c2 = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c3, this.b1c4));
  ArrayList<Cell> ab1c3 = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c2, this.b1c4));
  ArrayList<Cell> ab1c4 = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c2, this.b1c3));
  // duplicate for placeMine tests
  Cell pcb1 = new Cell(mt, 0, true);
  Cell pcb2 = new Cell(mt, 0, false);
  Cell pcb3 = new Cell(mt, 0, false);
  Cell pcb4 = new Cell(mt, 0, false);
  // countingMines test
  Cell mb1c1;
  Cell mb1c2;
  Cell mb1c3;
  Cell mb1c4;
  // 3 x 3 board
  // +---+---+---+ +----+----+----+
  // | M | 2 | 1 | | c1 | c2 | c3 |
  // +---+---+---+ +----+----+----+
  // | 2 | M | 1 | | c4 | c5 | c6 |
  // +---+---+---+ +----+----+----+
  // | 1 | 1 | 1 | | c7 | c8 | c9 |
  // +---+---+---+ +----+----+----+
  Cell b2c1 = new Cell(new ArrayList<Cell>(), 0, true);
  Cell b2c2 = new Cell(new ArrayList<Cell>(), 2, false);
  Cell b2c3 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell b2c4 = new Cell(new ArrayList<Cell>(), 2, false);
  Cell b2c5 = new Cell(new ArrayList<Cell>(), 0, true);
  Cell b2c6 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell b2c7 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell b2c8 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell b2c9 = new Cell(new ArrayList<Cell>(), 1, false);
  // duplicates for addNeighbor tests
  Cell bc1 = new Cell(new ArrayList<Cell>(), 0, true);
  Cell bc2 = new Cell(new ArrayList<Cell>(), 2, false);
  Cell bc3 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell bc4 = new Cell(new ArrayList<Cell>(), 2, false);
  Cell bc5 = new Cell(new ArrayList<Cell>(), 0, true);
  Cell bc6 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell bc7 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell bc8 = new Cell(new ArrayList<Cell>(), 1, false);
  Cell bc9 = new Cell(new ArrayList<Cell>(), 1, false);
  // addNeighbors mutation items
  ArrayList<Cell> ab2c1 = new ArrayList<Cell>(Arrays.asList(this.b2c2, this.b2c4, this.b2c5));
  ArrayList<Cell> ab2c2 = new ArrayList<Cell>(
      Arrays.asList(this.b2c1, this.b2c3, this.b2c4, this.b2c5, this.b2c6));
  ArrayList<Cell> ab2c3 = new ArrayList<Cell>(Arrays.asList(this.b2c2, this.b2c5, this.b2c6));
  ArrayList<Cell> ab2c4 = new ArrayList<Cell>(
      Arrays.asList(this.b2c1, this.b2c2, this.b2c5, this.b2c7, this.b2c8));
  ArrayList<Cell> ab2c5 = new ArrayList<Cell>(Arrays.asList(this.b2c1, this.b2c2, this.b2c3,
      this.b2c4, this.b2c6, this.b2c7, this.b2c8, this.b2c9));
  ArrayList<Cell> ab2c6 = new ArrayList<Cell>(
      Arrays.asList(this.b2c2, this.b2c3, this.b2c5, this.b2c8, this.b2c9));
  ArrayList<Cell> ab2c7 = new ArrayList<Cell>(Arrays.asList(this.b2c4, this.b2c5, this.b2c8));
  ArrayList<Cell> ab2c8 = new ArrayList<Cell>(
      Arrays.asList(this.b2c4, this.b2c5, this.b2c6, this.b2c7, this.b2c9));
  ArrayList<Cell> ab2c9 = new ArrayList<Cell>(Arrays.asList(this.b2c5, this.b2c6, this.b2c8));
  // countingMines test
  Cell mb2c2;
  Cell mb2c9;

  // cell mutation tests
  void init() {
    m1 = new MineSweeper(3, 3, 2, new Random(15), true);
    m2 = new MineSweeper(2, 2, 1, new Random(15), true);
    m3 = new MineSweeper(3, 3, 1, new Random(10), true);
    m4 = new MineSweeper(4, 4, 3, new Random(10), false);
    ms6 = new MineSweeper(2, 2, 1, new Random(15), false);
    ms6 = new MineSweeper(2, 2, 1, new Random(15), false);
    ms1 = new MineSweeper(2, 2, 1, new Random(15), false);
    ms2 = new MineSweeper(3, 3, 2, new Random(15), true);
    ms5 = new MineSweeper(3, 3, 2, new Random(15), true);
    ms3 = new MineSweeper(2, 2, 1, new Random(15), false);
    ms4 = new MineSweeper(3, 3, 1, new Random(10), false);
    this.b1c1.neighbors = new ArrayList<Cell>(Arrays.asList(this.b1c2, this.b1c3, this.b1c4));
    this.b1c2.neighbors = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c4, this.b1c3));
    this.b1c3.neighbors = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c4, this.b1c2));
    this.b1c4.neighbors = new ArrayList<Cell>(Arrays.asList(this.b1c1, this.b1c3, this.b1c2));
    this.mb1c1 = new Cell(this.b1c1.neighbors, 0, true);
    this.mb1c2 = new Cell(this.b1c2.neighbors, 0, false);
    this.mb1c3 = new Cell(this.b1c3.neighbors, 0, false);
    this.mb1c4 = new Cell(this.b1c4.neighbors, 0, false);
    this.b2c1.neighbors = new ArrayList<Cell>(Arrays.asList(this.b2c2, this.b2c4, this.b2c5));
    this.b2c2.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.b2c1, this.b2c3, this.b2c4, this.b2c5, this.b2c6));
    this.b2c3.neighbors = new ArrayList<Cell>(Arrays.asList(this.b2c2, this.b2c5, this.b2c6));
    this.b2c4.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.b2c1, this.b2c2, this.b2c5, this.b2c7, this.b2c8));
    this.b2c5.neighbors = new ArrayList<Cell>(Arrays.asList(this.b2c1, this.b2c2, this.b2c3,
        this.b2c4, this.b2c6, this.b2c7, this.b2c8, this.b2c9));
    this.b2c6.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.b2c2, this.b2c3, this.b2c5, this.b2c8, this.b2c9));
    this.b2c7.neighbors = new ArrayList<Cell>(Arrays.asList(this.b2c4, this.b2c5, this.b2c8));
    this.b2c8.neighbors = new ArrayList<Cell>(
        Arrays.asList(this.b2c4, this.b2c5, this.b2c6, this.b2c7, this.b2c9));
    this.b2c9.neighbors = new ArrayList<Cell>(Arrays.asList(this.b2c5, this.b2c6, this.b2c8));
    this.mb2c2 = new Cell(this.b2c2.neighbors, 0, false);
    this.mb2c9 = new Cell(this.b2c9.neighbors, 0, false);
  }

  // TESTS
  void testBigBang(Tester t) {
    MineSweeper world = new MineSweeper(30, 16, 5, new Random(), false);
    int worldWidth = world.rows * 20;
    int worldHeight = world.cols * 20 + 100;
    double tickRate = 1;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // UTILS METHOD(S) TESTS -----------------
  // checks if the given dimensions match the limits set for the game
  boolean testLimitDims(Tester t) {
    return t.checkExpect(new Utils().limitDims(new Random(), 30, 16, 99), true)
        && t.checkExpect(new Utils().limitDims(new Random(), 20, 20, 83), true)
        && t.checkExpect(new Utils().limitDims(new Random(), 20, 20, 70), true)
        && t.checkExpect(new Utils().limitDims(new Random(), 20, 25, 100), false)
        && t.checkExpect(new Utils().limitDims(new Random(), 10, 10, 101), false);
  }

  // MINESWEEPER METHOD(S) TESTS -------------
  //
  void testOnTick(Tester t) {
    this.init();
    this.ms3.counter = 10;
    t.checkExpect(this.ms3.counter, 10);
    ms3.onTick();
    t.checkExpect(this.ms3.counter, 11);
    ms3.onTick();
    t.checkExpect(this.ms3.counter, 12);
  }

  void testMakeScene(Tester t) {
    this.init();
    // 3 x 3 board
    WorldScene ws10 = new WorldScene(1000, 1000);
    WorldScene wsb1 = new WorldScene(1000, 1000);
    wsb1.placeImageXY(mine, 10, 10);
    wsb1.placeImageXY(new OverlayImage(new TextImage("2", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 50);
    wsb1.placeImageXY(new OverlayImage(new TextImage("2", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 10);
    wsb1.placeImageXY(mine, 30, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 50);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 10);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 50);
    wsb1.placeImageXY(new OverlayImage(new TextImage("2", 25, Color.RED),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, 3 * 20 + 100 / 2);
    wsb1.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 3 * 20 / 2 + 10,
        3 * 20 + 100 / 2 - 25);
    wsb1.placeImageXY(new TextImage("⏱︎", 30, Color.red), 3 * 20 - 100, 3 * 20 + 100 / 2);
    wsb1.placeImageXY(
        new OverlayImage(new TextImage("0", 25, Color.red),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)),
        3 * 20 - 50, 3 * 20 + 100 / 2);
    wsb1.placeImageXY(
        new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)),
        3 * 20 / 2 + 10, 3 * 20 + 100 / 2 + 15);
    t.checkExpect(this.m1.draw(ws10, true, true), wsb1);

    // 3 x 3
    WorldScene w2 = new WorldScene(1000, 1000);
    w2.placeImageXY(mine, 10, 10);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 10, 30);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 10, 50);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 30, 10);
    w2.placeImageXY(mine, 30, 30);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 30, 50);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 50, 10);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 50, 30);
    w2.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black), 50, 50);
    w2.placeImageXY(new OverlayImage(new TextImage("2", 25.0, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, 110);
    w2.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 40, 85);
    w2.placeImageXY(new TextImage("⏱︎", 30, Color.red), -40, 110);
    w2.placeImageXY(new OverlayImage(new TextImage("0", 25.0, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 10, 110);
    w2.placeImageXY(new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)), 40, 125);
    t.checkExpect(this.ms5.makeScene(), w2);
  }

  // checks that the given state of the game is drawn on the display
  void testDraw(Tester t) {
    this.init();
    // 3 x 3 board
    WorldScene ws10 = new WorldScene(1000, 1000);
    WorldScene wsb1 = new WorldScene(1000, 1000);
    wsb1.placeImageXY(mine, 10, 10);
    wsb1.placeImageXY(new OverlayImage(new TextImage("2", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 10);
    wsb1.placeImageXY(new OverlayImage(new TextImage("2", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 30);
    wsb1.placeImageXY(mine, 30, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 30);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 50);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 50);
    wsb1.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 50);
    // t.checkExpect(this.m1.draw(ws10, true), wsb1);
    // MineSweeper m1 = new MineSweeper(3, 3, 2, new Random(15), true);
    this.init();
    // 2 x 2 board
    WorldScene ws20 = new WorldScene(1000, 1000);
    WorldScene wsb2 = new WorldScene(1000, 1000);
    wsb2.placeImageXY(mine, 10, 10);
    wsb2.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 30);
    wsb2.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 10);
    wsb2.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 30);
    wsb2.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 40, 90);
    wsb2.placeImageXY(new OverlayImage(new TextImage("1", 25.0, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, 90);
    wsb2.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 30, 65);
    wsb2.placeImageXY(new TextImage("⏱︎", 30, Color.red), -60, 90);
    wsb2.placeImageXY(new OverlayImage(new TextImage("0", 25, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), -10, 90);
    wsb2.placeImageXY(new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)), 30, 105);
    t.checkExpect(this.m2.draw(ws20, false, true), wsb2);

    // 3 x 3 board with flags included
    WorldScene ws30 = new WorldScene(1000, 1000);
    WorldScene wsb3 = new WorldScene(1000, 1000);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 10);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 10, 30);
    wsb3.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY), 10, 50);
    wsb3.placeImageXY(mine, 30, 10);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 30, 30);
    wsb3.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY), 30, 50);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 10);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", Color.BLUE),
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.black)), 50, 30);
    wsb3.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY), 50, 50);
    wsb3.placeImageXY(new OverlayImage(new TextImage("1", 25, Color.RED),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.black)), 40, 110);
    wsb3.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 40, 85);
    wsb3.placeImageXY(new TextImage("⏱︎", 30, Color.red), -40, 110);
    wsb3.placeImageXY(new OverlayImage(new TextImage("0", 25, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 10, 110);
    wsb3.placeImageXY(new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)), 40, 125);
    t.checkExpect(this.m3.draw(ws30, true, true), wsb3);
    this.init();
    // 4 x 4 starter board
    WorldScene ws40 = new WorldScene(1000, 1000);
    WorldScene wsb4 = new WorldScene(1000, 1000);
    wsb4.placeImageXY(start, 10, 10);
    wsb4.placeImageXY(start, 10, 30);
    wsb4.placeImageXY(start, 10, 50);
    wsb4.placeImageXY(start, 10, 70);
    wsb4.placeImageXY(start, 30, 10);
    wsb4.placeImageXY(start, 30, 30);
    wsb4.placeImageXY(start, 30, 50);
    wsb4.placeImageXY(start, 30, 70);
    wsb4.placeImageXY(start, 50, 10);
    wsb4.placeImageXY(start, 50, 30);
    wsb4.placeImageXY(start, 50, 50);
    wsb4.placeImageXY(start, 50, 70);
    wsb4.placeImageXY(start, 70, 10);
    wsb4.placeImageXY(start, 70, 30);
    wsb4.placeImageXY(start, 70, 50);
    wsb4.placeImageXY(start, 70, 70);
    wsb4.placeImageXY(new OverlayImage(new TextImage("3", 25, Color.RED),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, 4 * 20 + 100 / 2);
    wsb4.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 4 * 20 / 2 + 10,
        4 * 20 + 100 / 2 - 25);
    wsb4.placeImageXY(new TextImage("⏱︎", 30, Color.red), 4 * 20 - 100, 4 * 20 + 100 / 2);
    wsb4.placeImageXY(
        new OverlayImage(new TextImage("0", 25, Color.red),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)),
        4 * 20 - 50, 4 * 20 + 100 / 2);
    wsb4.placeImageXY(
        new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
            new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)),
        4 * 20 / 2 + 10, 4 * 20 + 100 / 2 + 15);
    t.checkExpect(this.m4.draw(ws40, false, false), wsb4);

  }

  // tests the lastScene
  void testLastScene(Tester t) {
    this.init();
    RectangleImage rec = new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK);
    WorldScene lastScene = new WorldScene(1000, 1000);
    lastScene.placeImageXY(new OverlayImage(new TextImage("☢︎", 20, Color.BLACK), rec), 10, 10);
    lastScene.placeImageXY(rec, 10, 30);
    lastScene.placeImageXY(rec, 30, 10);
    lastScene.placeImageXY(rec, 30, 30);
    lastScene.placeImageXY(new OverlayImage(new TextImage("1", 25.0, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), 40, 90);
    lastScene.placeImageXY(new TextImage("Click Count: 0", 25, Color.black), 30, 65);
    lastScene.placeImageXY(new TextImage("⏱︎", 30, Color.red), -60, 90);
    lastScene.placeImageXY(new OverlayImage(new TextImage("0", 25, Color.red),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.BLACK)), -10, 90);
    lastScene.placeImageXY(new OverlayImage(new TextImage("☺︎", 30, Color.YELLOW),
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.LIGHT_GRAY)), 30, 105);
    t.checkExpect(this.ms1.lastScene("game over"), lastScene);

  }

  // test the onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.init();
    // 2 x 2 board (ms1)
    // +---+---+
    // | M | 1 |
    // +---+---+
    // | 1 | 1 |
    // +---+---+

    // 3 x 3 board (ms4)
    // +---+---+---+
    // | 1 | M | 1 |
    // +---+---+---+
    // | 1 | 1 | 1 |
    // +---+---+---+
    // | 0 | 0 | 0 |
    // +---+---+---+

    // RIGHT
    // flag and unflag
    t.checkExpect(this.ms1.board.get(0).get(0).isFlag, false);
    t.checkExpect(this.ms1.board.get(0).get(0).revealed, false);
    this.ms1.onMouseClicked(new Posn(15, 15), "RightButton");
    t.checkExpect(this.ms1.board.get(0).get(0).isFlag, true);
    t.checkExpect(this.ms1.board.get(0).get(0).revealed, false);
    this.ms1.onMouseClicked(new Posn(15, 15), "RightButton");
    t.checkExpect(this.ms1.board.get(0).get(0).isFlag, false);
    t.checkExpect(this.ms1.board.get(0).get(0).revealed, false);

    // LEFT
    // user clicked on cell (that is not a mine nor flood) and reveals it contents
    // (i.e. cell that is located near 1 or more mines)
    t.checkExpect(this.ms1.board.get(1).get(1).revealed, false);
    this.ms1.onMouseClicked(new Posn(30, 30), "LeftButton");
    t.checkExpect(this.ms1.board.get(1).get(1).revealed, true);
    // user clicked on tile that cause a flood
    t.checkExpect(this.ms4.board.get(0).get(2).revealed, false);
    t.checkExpect(this.ms4.board.get(1).get(2).revealed, false);
    t.checkExpect(this.ms4.board.get(2).get(2).revealed, false);
    this.ms4.onMouseClicked(new Posn(30, 50), "LeftButton");
    t.checkExpect(this.ms4.board.get(0).get(2).revealed, true);
    t.checkExpect(this.ms4.board.get(1).get(2).revealed, true);
    t.checkExpect(this.ms4.board.get(2).get(2).revealed, true);
    // RIGHT -> user tries to put a flag on a cell that is already revealed (but is
    // not a mine)
    t.checkExpect(this.ms1.board.get(1).get(1).isFlag, false);
    this.ms1.onMouseClicked(new Posn(30, 30), "RightButton");
    t.checkExpect(this.ms1.board.get(1).get(1).isFlag, false);
    // user clicked on mine
    t.checkExpect(this.ms1.board.get(0).get(0).revealed, false);
    t.checkExpect(this.ms1.gameOver, false);
    this.ms1.onMouseClicked(new Posn(15, 15), "LeftButton");
    t.checkExpect(this.ms1.gameOver, true);

    // OTHER
    // off the screen
    this.ms1.onMouseClicked(new Posn(20, 60));
    t.checkExpect(this.ms1, this.ms1);
    // after the game ends - no changes made
    this.ms1.onMouseClicked(new Posn(15, 15), "LeftButton");
    t.checkExpect(this.ms1, this.ms1);
  }

  /*
   * void checkWin() { int winner = this.rows * this.cols - this.mines; for (int r
   * = 0; r < this.rows; r++) { for (int c = 0; c < this.cols; c++) { Cell cell =
   * this.board.get(r).get(c); if(!cell.hasMine && cell.revealed || cell.isFlag) {
   * winner -= 1; } } } if(winner == 0 && this.winner == 0) { this.win = true; } }
   */

  // CELL METHODS TESTS -----------------
  // checks that the given cell is drawn based on its type
  // [i.e. mine, flagged, normal (w/ num mines on top), starting cell (blank)]
  void testDrawCell(Tester t) {
    this.init();
    Cell testc1 = new Cell(mt, 0, false, true);
    Cell testc3 = new Cell(mt, 0, false, false);
    Cell testc2 = new Cell(new ArrayList<Cell>(Arrays.asList(testc1, testc3)), 2, true, false);
    // draw a cell that is not revealed
    t.checkExpect(testc1.drawCell(), start);
    // draw a cell that is flagged
    t.checkExpect(testc2.drawCell(), flag);
    // draw a cell that contains a mine
    testc1.revealHelp();
    t.checkExpect(testc1.drawCell(), mine);
    // draw a cell that is bordered by at least one mine
    testc2.revealHelp();
    t.checkExpect(testc2.drawCell(), normal);
    // draw a cell that does not border any mines
    testc3.revealHelp();
    t.checkExpect(testc3.drawCell(), opened);
  }

  // checks that the hasMine parameter of a cell is switched to true if called on
  void testPlaceMine(Tester t) {
    this.init();
    t.checkExpect(this.pcb1.placeMine(), new Cell(this.mt, 0, true));
    t.checkExpect(this.pcb2.placeMine(), new Cell(this.mt, 0, true));
    t.checkExpect(this.pcb3.placeMine(), new Cell(this.mt, 0, true));
  }

  // checks that the given cell counts the number of neighbors surrounding it that
  // contains a mine
  void testCountMines(Tester t) {
    this.init();
    // 2 x 2 board tests
    t.checkExpect(this.mb1c1.countMines(), 0);
    t.checkExpect(this.mb1c2.countMines(), 1);
    // 3 x 3 board tests
    t.checkExpect(this.mb2c2.countMines(), 2);
    t.checkExpect(this.mb2c9.countMines(), 1);
    // 4 x 4 board tests
  }

  // checks that the neighbors field of a given cell is updated
  void testUpdateNeighbors(Tester t) {
    this.init();
    // 2 x 2 board tests
    Cell b1c1Test = new Cell(this.ab1c1, 0, true);
    Cell b1c2Test = new Cell(this.ab1c2, 1, false);
    Cell b1c3Test = new Cell(this.ab1c3, 1, false);
    Cell b1c4Test = new Cell(this.ab1c4, 1, false);
    this.cb1.updateNeighbors(this.ab1c1);
    this.cb2.updateNeighbors(this.ab1c2);
    this.cb3.updateNeighbors(this.ab1c3);
    this.cb4.updateNeighbors(this.ab1c4);
    t.checkExpect(this.cb1, b1c1Test);
    t.checkExpect(this.cb2, b1c2Test);
    t.checkExpect(this.cb3, b1c3Test);
    t.checkExpect(this.cb4, b1c4Test);
    // 3 x 3 board tests
    Cell b2c1Test = new Cell(this.ab2c1, 0, true);
    Cell b2c2Test = new Cell(this.ab2c2, 2, false);
    Cell b2c3Test = new Cell(this.ab2c3, 1, false);
    Cell b2c4Test = new Cell(this.ab2c4, 2, false);
    Cell b2c5Test = new Cell(this.ab2c5, 0, true);
    Cell b2c6Test = new Cell(this.ab2c6, 1, false);
    Cell b2c7Test = new Cell(this.ab2c7, 1, false);
    Cell b2c8Test = new Cell(this.ab2c8, 1, false);
    Cell b2c9Test = new Cell(this.ab2c9, 1, false);
    this.bc1.updateNeighbors(this.ab2c1);
    this.bc2.updateNeighbors(this.ab2c2);
    this.bc3.updateNeighbors(this.ab2c3);
    this.bc4.updateNeighbors(this.ab2c4);
    this.bc5.updateNeighbors(this.ab2c5);
    this.bc6.updateNeighbors(this.ab2c6);
    this.bc7.updateNeighbors(this.ab2c7);
    this.bc8.updateNeighbors(this.ab2c8);
    this.bc9.updateNeighbors(this.ab2c9);
    t.checkExpect(this.bc1, b2c1Test);
    t.checkExpect(this.bc2, b2c2Test);
    t.checkExpect(this.bc3, b2c3Test);
    t.checkExpect(this.bc4, b2c4Test);
    t.checkExpect(this.bc5, b2c5Test);
    t.checkExpect(this.bc6, b2c6Test);
    t.checkExpect(this.bc7, b2c7Test);
    t.checkExpect(this.bc8, b2c8Test);
    t.checkExpect(this.bc9, b2c9Test);
  }

  // tests the placeFlag
  void testPlaceFlag(Tester t) {
    this.init();
    Cell testc1 = new Cell(mt, 0, false, true);
    testc1.placeFlag();
    t.checkExpect(testc1.isFlag, true);
    testc1.placeFlag();
    t.checkExpect(testc1.isFlag, false);
  }

  void testCheckWin(Tester t) {
    this.init();
    // 2 x 2 board (ms1)
    // +---+---+
    // | M | 1 |
    // +---+---+
    // | 1 | 1 |
    // +---+---+

    // win condition :
    // flagging mines and
    // clicking all other cells
    t.checkExpect(this.ms1.win, false);
    // flagging cell with mine
    this.ms1.onMouseClicked(new Posn(0, 0), "RightButton");
    // clicking everything else
    this.ms1.onMouseClicked(new Posn(20, 0), "LeftButton");
    this.ms1.onMouseClicked(new Posn(0, 20), "LeftButton");
    this.ms1.onMouseClicked(new Posn(20, 20), "LeftButton");
    // won
    t.checkExpect(this.ms1.win, true);
  }

  // tests the flood method
  void flood(Tester t) {
    this.init();
    t.checkExpect(this.bc1.revealed, false);
    this.bc1.flood();
    t.checkExpect(this.bc1.revealed, true);
    this.bc1.revealed = true;
    this.bc2.revealed = false;
    t.checkExpect(this.bc2.revealed, false);
    this.bc2.flood();
    this.bc4.revealed = false;
    t.checkExpect(this.bc4.revealed, true);
    this.bc4.flood();
    t.checkExpect(this.bc1.revealed, false);
  }

}
