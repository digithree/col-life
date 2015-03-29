import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.File; 
import java.io.BufferedWriter; 
import java.io.FileWriter; 
import java.io.IOException; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class colLifeStandard extends PApplet {

/* Coloured Life - Cyclic CA - Standard
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
int maxIterations = 500;

public void setup() {
  size( (int)((gridWidth * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)), 
  (int)((gridHeight * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)) );
  background(0);

  lastTime = (float)millis()/1000.f;
}

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
  numCyclicColours = 3 + (int)random(28);
  cyclicColours = createColourTable( cyclicColours, numCyclicColours );

  // create
  cells = new Cell[gridHeight][];
  minNeighbour = 1 + (int)  random(5);

  println( "New simulation with "+numCyclicColours+" colour states and min "+minNeighbour+" neighbours" );
  

  for ( int j = 0 ; j < gridHeight ; j++ ) {
    cells[j] = new Cell[gridWidth];
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      //cells[j][i] = (Cell)new LifeCell( (random(1) < 0.5f) ? false : true );
      //cells[j][i] = (Cell)new ColNeighbourCellRnd();
      //cells[j][i] = (Cell)new ColNeighbourSelectedCellRnd( SELECTION_TOP_LEFT + SELECTION_RIGHT );
      //cells[j][i] = (Cell)new ColSizeNeighbourCellRnd();
      //cells[j][i] = (Cell)new LifeCellColor();
      //cells[j][i] = (Cell)new LifeCellColorized();
      //cells[j][i] = (Cell)new LifeCellWithColour( (random(1) < 0.5f) ? false : true );
      /*
      cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i<(gridWidth/2)&&j<(gridHeight/2)) ? 1 : 0)
                                      + ((i>=(gridWidth/2)&&j>=(gridHeight/2)) ? 2 : 0) );
      */
      /*
      cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + 
                                      ((random(1)<0.6)? 0 : 
                                          ((random(1)<0.6)? 1 : 
                                             ((random(1)<0.6? 1 :
                                                ((random(1)<0.6? 1 : 2)))) )) );
      */
      //cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + (int)random(4) );
      cells[j][i] = (Cell)new LifeCellCyclicColour( minNeighbour );
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
    }
  }
  // reset iteration counter
  iterationCounter = 0;
}


class Cell {
  int col;
  boolean doCommitNext = false;
  Cell []adjacentCells = new Cell[8];
  
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "colLifeStandard" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
