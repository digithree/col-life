import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class colLifePattern extends PApplet {

/* Coloured Life - Cyclic CA - Pattern
 *
 * from http://kaytdek.trevorshp.com/projects/computer/neuralNetworks/gameOfLife2.htm
 *
 * "In cyclic cellular automata, an ordering of multiple colors is established.
 *    Whenever a cell is neighbored by a cell whose color is next in the cycle,
 *    it copies that neighbor's color--otherwise, it remains unchanged."
 *
 * Using different levels of thresholding, i.e. enforcing a minimum number of neighbours
 * in the next sequence to qualify cycle increment, this experiment intends to show
 * different behaviours within the cyclic CA system.
 *
 * Also added is randomised cycle sizes, i.e. a random number of states / colours.
 * This random value is between 3 and 64
 *
 * A final extra on this version is saving the stats of number of iterations for each
 * random threshold and cycle size run to a CVS file, for later analysis.
 *
 */

int cellSize = 10;
float cellSpacing = 0.0f;
int gridWidth = 60;
int gridHeight = 60;

Cell [][]cells;

float lastTime;

float MIN_COL_SIZE = 64.f;
float MIN_COL_DISTINCT = 30.f;

boolean blackAndWhiteFlag = false;

boolean doCreateCells = true;
int iterationCounter = 0;

int numCyclicColours = 12;
int []cyclicColours = null;

int minNeighbour = 0;
int maxIterations = 1000;

public void setup() {
  size( (int)((gridWidth * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)), 
  (int)((gridHeight * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)) );
  background(0);

  lastTime = (float)millis()/1000.f;
}

Cell trackCell = null;

float WAIT_TIMER_LENGTH = 0.05f;
float waitTimer = WAIT_TIMER_LENGTH;
boolean processNextFrame = false;
boolean change = false;
public void draw() {
  
  // time
  float curTime = (float)millis()/1000.f;
  float elapsedTime = curTime - lastTime;
  
  if( doCreateCells ) {
    createCells();
    doCreateCells = false;

  }

  // draw life cells
  if( processNextFrame ) {
    change = false;
  }
  for ( int j = 0 ; j < gridHeight ; j++ ) {
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      cells[j][i].draw( (cellSize*cellSpacing)+(i * (cellSize+(cellSize*cellSpacing))), 
                        (cellSize*cellSpacing)+(j * (cellSize+(cellSize*cellSpacing))), 
                        cellSize );
      if ( processNextFrame ) {
        cells[j][i].nextFrame();
      }
    }
  }
  if ( processNextFrame ) {
    if( !change ) {
      println( "    system is stable / dead, reseting" );
      doCreateCells = true;
    }
    processNextFrame = false;
  }

  // update depending on timer
  waitTimer -= elapsedTime;
  if ( waitTimer <= 0.f ) {
    waitTimer = WAIT_TIMER_LENGTH;
    processNextFrame = true;
  }
  
  iterationCounter++;
  
  // restart simulation if iteration gets over 150
  if( iterationCounter >= maxIterations ) {
    doCreateCells = true;
  } 

  // save last time
  lastTime = curTime;
}

public void keyPressed() {
  if( key == ' ' ) {
    blackAndWhiteFlag = !blackAndWhiteFlag;
  } else if( key == 'r' || key == 'R' ) {
    doCreateCells = true;
  }
}


public void createCells() {
  // first, recreate colour table
  // set random number of colours in table, between 3 and 24
  numCyclicColours = 15 + (int)random(11);
  cyclicColours = createColourTable_better( cyclicColours, numCyclicColours );

  // create
  cells = new Cell[gridHeight][];
  minNeighbour = 1 + (int)  random(2);
  int type = (int)random(10);

  println( "New simulation with "+numCyclicColours+" colour states and min "+minNeighbour+" neighbours, and type "+type );

  for ( int j = 0 ; j < gridHeight ; j++ ) {
    cells[j] = new Cell[gridWidth];
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      switch(type) {
        case 0:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i<(gridWidth/2)&&j<(gridHeight/2)) ? 1 : 0)
                                      + ((i>=(gridWidth/2)&&j>=(gridHeight/2)) ? 1 : 0) );
          break;
        case 1:
          cells[j][i] = (Cell)new LifeCellCyclicColour( minNeighbour );
          break;
        case 2:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + (int)random(2) );
          break;
        case 3:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i<(gridWidth/2)) ? 1 : 0)
                                      + ((j<(gridHeight/2)) ? 1 : 0) );
          break;
        case 4:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i>(gridWidth/2)) ? 1 : 0)
                                      + ((j>(gridHeight/2)) ? 1 : 0) );
          break;
        case 5:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i<(gridWidth/8)&&i>((gridWidth*6)/8)) ? 1 : 0)
                                      + ((j>((gridHeight*4)/8)&&j<((gridHeight*5)/8)) ? 1 : 0)
                                       );
          break;
        case 6:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i>((gridWidth*2)/8)&&i<((gridWidth*3)/8)) ? 1 : 0)
                                      + ((j>((gridHeight*6)/8)&&j<((gridHeight*7)/8)) ? 1 : 0)
                                       );
          break;
        case 7:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + ((random(2)<0.2f)?1:0) );
          break;
        case 8:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + ((random(2)<0.1f)?2:0) );
          break;
        default:
          cells[j][i] = (Cell)new LifeCellCyclicColour( minNeighbour );
      }
    }
  }
  // add adjacent cell list for each cell
  for ( int j = 0 ; j < gridHeight ; j++ ) {
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      Cell []adjacent = new Cell[8];
      int idx = 0;
      for ( int k = 0 ; k < 3 ; k++ ) {
        for ( int m = 0 ; m < 3 ; m++ ) {
          // calculate coordinate
          int y = (j+k-1);
          int x = (i+m-1);
          if ( y < 0 ) {
            y += gridHeight;
          } 
          else if ( y >= gridHeight ) {
            y -= gridHeight;
          }
          if ( x < 0 ) {
            x += gridWidth;
          } 
          else if ( x >= gridWidth ) {
            x -= gridWidth;
          }
          // skip middle cell (self)
          if ( !(k == m && k == 1) ) {
            adjacent[idx] = cells[y][x];
            idx++;
          }
        }
      }
      cells[j][i].addAdjacentCells( adjacent );
      //println( "cell "+j+", "+i+", has "+(idx+1)+"adjacent Cells Counted" );
    }
  }
  // pick random cell for test cell
  trackCell = cells[(int)random(cells.length)][(int)random(cells[0].length)];
  // reset iteration counter
  iterationCounter = 0;
}


public class Cell {
  int col;
  boolean doCommitNext = false;
  Cell []adjacentCells = new Cell[8];
  public int maxStillFrames = 0;
  int stillFrames = 0;
  
  Cell() {
    // initiallise self with default values
    col = color(255);
  }
  
  public void addAdjacentCells( Cell []_adjacentCells ) {
    adjacentCells = _adjacentCells;
  }
  
  public void nextFrame() {
    // modify seld based on adjacent cells
  }
  
  public void commitFrame() {
    // actually make changes
  }
  
  public void draw( float x, float y, float cellSize ) {
    if( doCommitNext ) {
      doCommitNext = false;
      commitFrame();
      stillFrames = 0;
    } else {
      stillFrames++;
      if( stillFrames > maxStillFrames ) {
        maxStillFrames = stillFrames;
      }
    }
    pushStyle();
      noStroke();
      fill( col );
      rect( x, y, cellSize, cellSize );
    popStyle();
  }
}

class LifeCell extends Cell {
  boolean isAlive, isAliveNext;
  
  LifeCell( boolean state ) {
    // initiallise self with default values
    isAlive = state;
    setColour();
    isAliveNext = false;
  }
  
  public void setColour() {
    if( isAlive ) {
      col = color(255);
    } else {
      col = color(0);
    }
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // count alive neighbours
    int aliveNeighbours = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCell cell = (LifeCell)adjacentCells[i];
      if( cell.isAlive ) {
        aliveNeighbours++;
      }
    }
    // change state depending on neighbours
    if( isAlive ) {
      if( aliveNeighbours < 2 ) {
        isAliveNext = false;
      } else if( aliveNeighbours < 4 ) {
        isAliveNext = true;
      } else {
        isAliveNext = false;
      }
    } else {
      if( aliveNeighbours == 3 ) {
        isAliveNext = true;
      }
    }
    doCommitNext = true;
  }
  
  public void commitFrame() {
    isAlive = isAliveNext;
    setColour();
  }
}



class ColNeighbourCellRnd extends Cell {
  int nextCol;
 
  ColNeighbourCellRnd() {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // average neighbours colours
    float r = 0;
    float g = 0;
    float b = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      r += red(adjacentCells[i].col);
      g += green(adjacentCells[i].col);
      b += blue(adjacentCells[i].col);
    }
    nextCol = color( (int)(r / 8.f), (int)(g / 8.f), (int)(b / 8.f) );
    doCommitNext = true;
  }
  
  public void commitFrame() {
    col = nextCol;
  }
}


int SELECTION_TOP_LEFT = 1;
int SELECTION_TOP = 2;
int SELECTION_TOP_RIGHT = 4;
int SELECTION_LEFT = 8;
int SELECTION_RIGHT = 16;
int SELECTION_BOTTOM_LEFT = 32;
int SELECTION_BOTTOM = 64;
int SELECTION_BOTTOM_RIGHT = 128;

class ColNeighbourSelectedCellRnd extends Cell {
  int nextCol;
  int selection;
 
  ColNeighbourSelectedCellRnd( int _selection ) {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
    selection = _selection;
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // average neighbours colours
    float r = 0;
    float g = 0;
    float b = 0;
    int numSelected = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      int selectionNum = 1 << i;
      if( (selection & selectionNum) == selectionNum ) {
        //println( "selection Num "+selectionNum);
        r += red(adjacentCells[i].col);
        g += green(adjacentCells[i].col);
        b += blue(adjacentCells[i].col);
        //println( i+"  r:"+r+", g:"+g+", b:"+b);
        numSelected++;
      }
    }
    nextCol = color( (int)(r / (float)numSelected),
                     (int)(g / (float)numSelected),
                     (int)(b / (float)numSelected) );
    //println( "nextCol  r:"+red(nextCol)+", g:"+green(nextCol)+", b:"+blue(nextCol));
    doCommitNext = true;
  }
  
  public void commitFrame() {
    col = nextCol;
  }
}


class ColSizeNeighbourCellRnd extends Cell {
  int nextCol;
 
  ColSizeNeighbourCellRnd() {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // average neighbours colours
    float r = 0;
    float g = 0;
    float b = 0;
    float numCols = 0.f;
    for( int i = 0 ; i < 8 ; i++ ) {
      if( colSize(adjacentCells[i].col) > MIN_COL_SIZE ) {
        numCols += 1.f;
        r += adjacentCells[i].col >> 16 & 0xFF;
        g += adjacentCells[i].col >> 8 & 0xFF;
        b += adjacentCells[i].col & 0xFF;
      }
    }
    if( numCols > 0.f ) {
      nextCol = color( (int)(r / numCols), (int)(g / numCols), (int)(b / numCols) );
      //nextCol = raiseColToMinSize( nextCol, MIN_COL_SIZE );
      if( colDistinct( nextCol, MIN_COL_SIZE, MIN_COL_DISTINCT ) ) {
        doCommitNext = true;
      }
    }
  }
  
  public void commitFrame() {
    col = nextCol;
  }
}

float COL_CHANGE = 40.f;
class LifeCellColor extends Cell {
  boolean isAlive;
  int nextCol;
  
  LifeCellColor() {
    // initiallise self with default values
    /*
    col = color( (int)(MIN_COL_SIZE+random(255.F-MIN_COL_SIZE)),
                 (int)(MIN_COL_SIZE+random(255.F-MIN_COL_SIZE)),
                 (int)(MIN_COL_SIZE+random(255.F-MIN_COL_SIZE)) );
     */
    col = color( (int)random(255), (int)random(255), (int)random(255) );
    setAlive();
  }
  
  public void setAlive() {
    //isAlive = (strongestColour(col) >= MIN_COL_SIZE);
    isAlive = brightness(col) >= MIN_COL_SIZE;
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // count alive neighbours
    int aliveNeighbours = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCellColor cell = (LifeCellColor)adjacentCells[i];
      if( cell.isAlive ) {
        aliveNeighbours++;
      }
    }
    // change state depending on neighbours
    boolean isAliveNext = false;
    if( isAlive ) {
      if( aliveNeighbours < 2 ) {
        isAliveNext = false;
      } else if( aliveNeighbours < 4 ) {
        isAliveNext = true;
      } else {
        isAliveNext = false;
      }
    } else {
      if( aliveNeighbours == 3 ) {
        isAliveNext = true;
      }
    }
    if( isAliveNext ) {
      nextCol = changeStrongestColour( col, COL_CHANGE );    
      //nextCol = color(255);
    } else {
      nextCol = changeStrongestColour( col, -COL_CHANGE );
      //nextCol = color(0);
    }
    doCommitNext = true;
  }
  
  public void commitFrame() {
    col = nextCol;
    setAlive();
  }
  
  public void draw( float x, float y, float cellSize ) {
    if( doCommitNext ) {
      doCommitNext = false;
      commitFrame();
    }
    pushStyle();
      noStroke();
      fill( blackAndWhiteFlag?(isAlive?color(255):color(0)):col );
      rect( x, y, cellSize, cellSize );
    popStyle();
  }
}


final int TYPE_RED = 0;
final int TYPE_GREEN = 1;
final int TYPE_BLUE = 2;

class LifeCellColorized extends Cell {
  boolean isAlive, isAliveNext;
  int type;
  
  LifeCellColorized() {
    // initiallise self with default values
    isAlive = (random(1)<0.5f) ? true : false;
    type = (int)random(2.9f);
    setColour();
    isAliveNext = false;
  }
  
  public void setColour() {
    if( isAlive ) {
      switch( type ) {
        case TYPE_RED:
          col = color(255,0,0);
          break;
        case TYPE_GREEN:
          col = color(0,255,0);
          break;
        case TYPE_BLUE:
          col = color(0,0,255);
          break;
        default:
          col = color(255);
      }
    } else {
      col = color(0);
    }
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // count alive neighbours
    int []aliveNeighbours = new int[3];
    for( int i = 0 ; i < 3 ; i++ ) {
      aliveNeighbours[i] = 0;
    }
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCellColorized cell = (LifeCellColorized)adjacentCells[i];
      if( cell.isAlive ) {
        aliveNeighbours[cell.type]++;
      }
    }
    // change state depending on neighbours
    if( isAlive ) {
      if( aliveNeighbours[type] < 2 ) {
        isAliveNext = false;
      } else if( aliveNeighbours[type] < 4 ) {
        isAliveNext = true;
      } else {
        isAliveNext = false;
      }
    } else {
      if( aliveNeighbours[type] == 3 ) {
        isAliveNext = true;
      }
    }

    // now check other neighbours
    int changeToType = type;
    int []twoOtherTypes = new int[2];
    int idx = 0;
    for( int i = 0 ; i < 3 ; i++ ) {
      if( i != type ) {
        twoOtherTypes[idx] = i;
        idx++;
      }
    }
    if( isAliveNext ) {
      if( aliveNeighbours[twoOtherTypes[0]] >= 2
          && aliveNeighbours[twoOtherTypes[0]] >= aliveNeighbours[twoOtherTypes[1]]) {
        changeToType = twoOtherTypes[0];
      } else if( aliveNeighbours[twoOtherTypes[1]] >= 2
          && aliveNeighbours[twoOtherTypes[1]] >= aliveNeighbours[twoOtherTypes[0]]) {
        changeToType = twoOtherTypes[1];
      }
    } else {
      if( aliveNeighbours[twoOtherTypes[0]] >= 3
          && aliveNeighbours[twoOtherTypes[0]] >= aliveNeighbours[twoOtherTypes[1]]) {
        changeToType = twoOtherTypes[0];
        isAliveNext = true;
      } else if( aliveNeighbours[twoOtherTypes[1]] >= 3
          && aliveNeighbours[twoOtherTypes[1]] >= aliveNeighbours[twoOtherTypes[0]]) {
        changeToType = twoOtherTypes[1];
        isAliveNext = true;
      }
    }

    doCommitNext = true;
  }
  
  public void commitFrame() {
    isAlive = isAliveNext;
    setColour();
  }
}

class LifeCellWithColour extends Cell {
  boolean isAlive, isAliveNext;
  int aliveCol;
  
  LifeCellWithColour( boolean state ) {
    // initiallise self with default values
    isAlive = state;
    aliveCol = color( (int)random(255), (int)random(255), (int)random(255) );
    setColour();
    isAliveNext = false;
  }
  
  public void setColour() {
    if( isAlive ) {
      col = aliveCol;
    } else {
      col = color(0);
    }
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // count alive neighbours
    // average neighbours colours
    float r = aliveCol >> 16 & 0xFF;
    float g = aliveCol >> 8 & 0xFF;
    float b = aliveCol & 0xFF;
    float numCols = 1.f;
    int aliveNeighbours = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCellWithColour cell = (LifeCellWithColour)adjacentCells[i];
      if( cell.isAlive ) {
        aliveNeighbours++;
        numCols += 1.f;
        r += cell.col >> 16 & 0xFF;
        g += cell.col >> 8 & 0xFF;
        b += cell.col & 0xFF;
      }
    }
    // change state depending on neighbours
    if( isAlive ) {
      if( aliveNeighbours < 2 ) {
        isAliveNext = false;
      } else if( aliveNeighbours < 4 ) {
        isAliveNext = true;
        aliveCol = color( r/numCols, g/numCols, b/numCols );
      } else {
        isAliveNext = false;
      }
    } else {
      if( aliveNeighbours == 3 ) {
        isAliveNext = true;
        aliveCol = color( (int)random(255), (int)random(255), (int)random(255) );
      }
    }
    doCommitNext = true;
  }
  
  public void commitFrame() {
    isAlive = isAliveNext;
    setColour();
  }
}


/*
color []cyclicColours = { color(255,0,0), color(255,255,0), color(0,255,0), color(0,255,255),
                          color(0,0,255), color(255,0,255) };



color []cyclicColours = { color(255,0,0), color(255,128,0), color(255,255,0), color(128,255,0), 
                          color(0,255,0), color(0,255,128), color(0,255,255), color(0,128,255),
                          color(0,0,255), color(128,0,255), color(255,0,255), color(255,0,128) };


color []cyclicColours = { color(255,0,0), color(255,128,0), color(255,255,0), color(128,255,0), 
                          color(0,255,0), color(0,255,128), color(0,255,255), color(0,128,255),
                          color(0,0,255), color(128,0,255), color(255,0,255), color(255,0,128),
                          color(255,0,0), color(255,128,0), color(255,255,0), color(128,255,0), 
                          color(0,255,0), color(0,255,128), color(0,255,255), color(0,128,255),
                          color(0,0,255), color(128,0,255), color(255,0,255), color(255,0,128) };
*/



class LifeCellCyclicColour extends Cell {
  boolean isImmortal;
  int curCyclicColour, nextCyclicColour;
  int minNeighbours;
  
  LifeCellCyclicColour( int _minNeighbours ) {
    // initiallise self with default values
    minNeighbours = _minNeighbours;
    curCyclicColour = (int)random(numCyclicColours);
    nextCyclicColour = curCyclicColour;
    setColour();
  }
  
  public void setColour() {
    col = cyclicColours[curCyclicColour];
  }
  
  public void nextFrame() {
    if( adjacentCells == null ) {
      return;
    }
    // count alive neighbours
    // average neighbours colours
    int numNextColourNeighbours = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCellCyclicColour cell = (LifeCellCyclicColour)adjacentCells[i];
      if( cell.curCyclicColour == ((curCyclicColour+1)%numCyclicColours) ) {
        numNextColourNeighbours++;
      }
    }
    if( numNextColourNeighbours >= minNeighbours ) {
      nextCyclicColour = ((curCyclicColour+1)%numCyclicColours);
      change = true;
    }
    // change state depending on neighbours
    doCommitNext = true;
  }
  
  public void commitFrame() {
    curCyclicColour = nextCyclicColour;
    setColour();
  }
}
public float colSize( int col ) {
  float r = abs((col >> 16 & 0xFF) - 128);
  float g = abs((col >> 8 & 0xFF) - 128);
  float b = abs((col & 0xFF) - 128);
  return (r+g+b) / 3.f;
}

public boolean colDistinct( int col, float minSize, float minDistinct ) {
  float r = abs((col >> 16 & 0xFF) - 128);
  float g = abs((col >> 8 & 0xFF) - 128);
  float b = abs((col & 0xFF) - 128);
  // get greatest dist
  float cSize = 0.f;
  float dist1 = 0.f;
  float dist2 = 0.f;
  // red biggest
  if( r > g && r > b ) {
    cSize = r;
    dist1 = r - g;
    dist2 = r - b;
  }
  // green biggest
  if( g > r && g > b ) {
    cSize = g;
    dist1 = g - r;
    dist2 = g - b;
  }
  // blue biggest
  if( b > r && b > g ) {
    cSize = b;
    dist1 = b - r;
    dist2 = b - g;
  }
  float greatestDist = 0.f;
  if( dist1 > dist2 ) {
    greatestDist = dist1;
  } else {
    greatestDist = dist2;
  }
  if( cSize >= minSize && (cSize - greatestDist) >= minDistinct ) {
    return true;
  }
  return false;
}


public int raiseColToMinSize( int col, float minSize ) {
  int c = col;
  float cSize = colSize(c);
  //println( cSize+ " < "+minSize );
  if( cSize < minSize ) {
    //println( "colour size too small!" );
    float colDif = minSize - cSize;
    float r = ((col >> 16 & 0xFF) - 128) + cSize;
    float g = ((col >> 8 & 0xFF) - 128) + cSize;
    float b = ((col & 0xFF) - 128) + cSize;
    if( r > 255.f ) {
      r = 255.f;
    }
    if( g > 255.f ) {
      g = 255.f;
    }
    if( b > 255.f ) {
      b = 255.f;
    }
    c = color(r, g, b);
  }
  return c;
}

public float strongestColour( int col ) {
  float r = (col >> 16 & 0xFF);
  float g = (col >> 8 & 0xFF);
  float b = (col & 0xFF);
  // red biggest
  if( r > g && r > b ) {
    return r;
  }
  // green biggest
  if( g > r && g > b ) {
    return g;
  }
  // blue biggest
  if( b > r && b > g ) {
    return b;
  }
  return (r+g+b)/3.f;
}

public int changeStrongestColour( int col, float change ) {
  float r = (col >> 16 & 0xFF);
  float g = (col >> 8 & 0xFF);
  float b = (col & 0xFF);
  // red biggest
  if( r > g && r > b ) {
    r += change;
    if( r > 255.f ) {
      r = 255.f;
    } else if( r < 0.f ) {
      r = 0.f;
    }
  }
  // green biggest
  if( g > r && g > b ) {
    g += change;
    if( g > 255.f ) {
      g = 255.f;
    } else if( g < 0.f ) {
      g = 0.f;
    }
  }
  // blue biggest
  if( b > r && b > g ) {
    b += change;
    if( b > 255.f ) {
      b = 255.f;
    } else if( b < 0.f ) {
      b = 0.f;
    }
  }
  return color(r, g, b);
}

public int[] createColourTable( int []colourTable, int numCols ) {
  colourTable = new int[numCols];
  for( int i = 0 ; i < numCols ; i++ ) {
    colourTable[i] = getRainbowColourFromLinearNumber( (1.f/(float)numCols) * (float)i );
  }
  return colourTable;
}



int INC_RED = 1;
int INC_GREEN = 2;
int INC_BLUE = 4;
int DEC_RED = 8;
int DEC_GREEN = 16;
int DEC_BLUE = 32;

int []interpolateColourTable = { color(255,0,0), color(255,255,0), color(0,255,0), color(0,255,255),
                          color(0,0,255), color(255,0,255) };
int []colourMovementTable = { INC_GREEN, DEC_RED, INC_BLUE, DEC_GREEN, INC_RED, DEC_BLUE };                         
                          
public int getRainbowColourFromLinearNumber( float num ) {
  int idx = -1;
  float scaledNum = 0.f;
  for( int i = 1 ; i <= 6 ; i++ ) {
    if( num < ((1.f / 6.f)*((float)i)) ) {
      scaledNum = num - ((1.f / 6.f)*((float)i-1));
      scaledNum *= 6f;
      //println( num + " is in group "+i+", "+scaledNum+" in it" );
      idx = i - 1;
      break;
    }
  }
  if( idx >= 0 ) {
    return color( red(interpolateColourTable[idx])
                        + (((colourMovementTable[idx]&INC_RED)==INC_RED) ? (int)(255.f * scaledNum) : 0)
                        - (((colourMovementTable[idx]&DEC_RED)==DEC_RED) ? (int)(255.f-(255.f * (1.f-scaledNum))) : 0),
                  green(interpolateColourTable[idx])
                        + (((colourMovementTable[idx]&INC_GREEN)==INC_GREEN) ? (int)(255.f * scaledNum) : 0)
                        - (((colourMovementTable[idx]&DEC_GREEN)==DEC_GREEN) ? (int)(255.f-(255.f * (1.f-scaledNum))) : 0),
                  blue(interpolateColourTable[idx])
                        + (((colourMovementTable[idx]&INC_BLUE)==INC_BLUE) ? (int)(255.f * scaledNum) : 0)
                        - (((colourMovementTable[idx]&DEC_BLUE)==DEC_BLUE) ? (int)(255.f-(255.f * (1.f-scaledNum))) : 0) );
  }
  return color(255);
}


int [][]colours = {
    { 0xff5D5C4A,0xff686752,0xff8D8B6B,0xffCEC8B7,0xff6B7257,0xff7D866C,0xff302E4A,0xff313046,0xff313345,0xff373748,0xff3A3A4B,0xff49445A,0xff817A85,0xffB2A7AB,0xffA49EA5 },                    // Pennys & Polaroids - SeeFig
    { 0xff433D29,0xff50442C,0xff5C4E2C,0xff7B5D2E,0xff836231,0xff88744B,0xff8D7A4E,0xffA18847,0xffAA8F54,0xffB39B5C },                                                            // Emers Garden (wood) - SeeFig
    { 0xff947D72,0xffA08E8D,0xffA38280,0xff977977,0xffAB8F8D,0xffA18582,0xffB2ABA9,0xffCAD0C5,0xff919195,0xff998694,0xff8B6E7B,0xff846F7C,0xff7E6B77,0xff9B808C,0xffA08F9B,0xffA78896,0xffAB939C,0xff6B4852,0xffBA9EA3,0xffB19196,0xffBA9599,0xff9C7374,0xff976C6B }, //Blossoms - SeeFig
    { 0xff21431C,0xff103113,0xff144217,0xff736E39,0xff867238,0xff588D35,0xff7E9644,0xffB0A345,0xffD0B93E,0xffDFCE46,0xffEDE951,0xffE2D597 },                                            // Spring - SeeFig
    { 0xffE5B9B9,0xffB78C78,0xffCC9F82,0xffE1B57F,0xffC69547,0xffE6D1A6,0xffD8BB7C,0xffE1C679,0xffE6D488,0xffEADEAE,0xffEEE8C4,0xffF5F5EC,0xffABA9BC,0xffDA3B88,0xffDD7FA2,0xffDB9DA9,0xffDF8D95 },    // Colourblocc (ice cream) - SeeFig
    { 0xff120602,0xff220B03,0xff360E02,0xff3C1102,0xff431302,0xff4E1401,0xff5F2A07,0xff742F07,0xff7D2D01,0xff843A07,0xffA1621D },                                                    // Galway Cathedral (dark fire) - SeeFig
    { 0xff532700,0xff914F0B,0xff512C04,0xff994710,0xff503010,0xff6B451A,0xffA27D4A,0xffCAAA7D,0xffBA8840,0xffE2B46D,0xff6A5017,0xff89681F,0xff989A40,0xff7E7B12,0xff5C5C19,0xff4A5117,0xff343A1E },    // Salad Fingers(wood and grass) - SeeFig
    { 0xffAF2E35,0xffC35132,0xffB43E18,0xffB76E3C,0xff724524,0xffC69060,0xffD58E1D,0xffA27C2C,0xff907121,0xff997B24,0xff988D49,0xffCCC071 },                                            // Untitled (natural tasty) - Alison
    { 0xff671C2C,0xff73162A,0xffB33A54,0xffC4526B,0xffC16175,0xffCC7487,0xffC7798A,0xffE0768D,0xffFE6686,0xffEA7A92,0xffD8899A,0xffF37B95,0xffE4889B,0xffDA91A1,0xffE593A5,0xffDC98A6,0xffDC98A7,
      0xffE197A7,0xffEA94A6,0xffDE9DAB,0xffDE9FAC,0xffE2ABB7,0xffE8B3BE,0xffEEB1BE,0xffE9BAC4,0xffE8C1CA,0xffFEBAC9,0xffFEC1CE,0xffFEEEF2,0xffF9F9F9,0xffFEFEFE},                             // Samuari Hack
    { 0xff946D52,0xff776746,0xff63612E,0xff4D5528,0xff37420F,0xff4E5B22,0xff465826,0xff374D18,0xff576841,0xff354D29,0xff71A559,0xff457A4F,0xff538F60 },                                    // Moschops
    { 0xff333933,0xff444D47,0xff48534F,0xff515D59,0xffABB7AC,0xffCDDAD2,0xffD0DBD3,0xffDCE6DF,0xffE4EFE7,0xffF6F0EE,0xffF8FBF9,0xffFCFEFC,0xffFDFFFD },                                    // Poster on poll
    { 0xff6E4A38,0xffC3A795,0xff886B58,0xff342418,0xff7E5C42,0xffBAA38F,0xff4C3928,0xff4F4134,0xff5F5A55,0xff9A988E,0xff5D5B4E,0xff62675A,0xff64675E,0xff6A6F6A },                            // Anne
    { 0xff141B90,0xff12E940,0xff123750,0xff2104E0,0xff20307B,0xff2E3468,0xff104350,0xff505340,0xff202320,0xff48456A,0xff363253,0xff514462,0xff3C3147,0xff7C6469 },                                   // Jellyfish
    { 0xffC08814,0xffD69E13,0xffD09910,0xffD49C10,0xffCD9812,0xff9C750D,0xff956D01,0xff957106,0xff434501,0xffC4C681,0xff9DCFFE,0xff9AC6CD,0xffC9D2B4,0xffB1BFAD },                            // Buttercup
    { 0xffF5A630,0xff4EBBC9,0xff64C4CF,0xff65C4D0,0xff69C5D0,0xff6BC3CE,0xff6EC7D2,0xff8DD4DC,0xff89CCD4,0xff90D5DD,0xffA5DDE3,0xffABDFE5,0xffB7E4E8,0xffCAEAEC },                             // Ice
    { 0xffF97C44,0xffF77C38,0xffF57D2B,0xffF68131,0xffF48326,0xffF78E37,0xffF68D30,0xffF69232,0xffF6982E,0xffF59729,0xffF3921D,0xffF3981E,0xffF49C24,0xffF4AE1D }                             // Sun
  };


public int[] createColourTable_better( int []colourTable, int numCols ) {
  colourTable = new int[numCols];
  int table = (int)random(colours.length);
  int tableLen = colours[table].length;
  for( int i = 0 ; i < numCols ; i++ ) {
    colourTable[i] = colours[table][(int)(((float)i/(float)numCols)*(float)tableLen)];
  }
  return colourTable;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "colLifePattern" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
