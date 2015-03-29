import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.processing.wiki.triangulate.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class colLifeTri extends PApplet {

/* Coloured Life - Experiment 15
 *
 * Coloured Life - Triangular Neighbourhood
 *
 * 
 *
 */
 


int cellSize = 30;
float cellSpacing = 0.0f;
int gridWidth = 20;
int gridHeight = 20;

Cell [][]cells = null;

float lastTime;

float MIN_COL_SIZE = 64.f;
float MIN_COL_DISTINCT = 30.f;

boolean blackAndWhiteFlag = false;

boolean doCreateCells = true;
int iterationCounter = 0;

int numCyclicColours = 12;
int []cyclicColours = null;

int minNeighbour = 0;
int maxIterations = 500; // was 5000

boolean directContact = false;
boolean addRandomVertexVariation = false;

public void setup() {
  size( (int)((gridWidth * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)), 
  (int)((gridHeight * cellSize * (1.f+cellSpacing))+(cellSize*cellSpacing)) );
  background(0);
  
  createCellsTriangular( directContact );

  lastTime = (float)millis()/1000.f;
}

float WAIT_TIMER_LENGTH = 0.1f;
float waitTimer = WAIT_TIMER_LENGTH;
boolean processNextFrame = false;
boolean change = false;
public void draw() {
  noStroke();

  // time
  float curTime = (float)millis()/1000.f;
  float elapsedTime = curTime - lastTime;
  
  if( doCreateCells ) {
    createCellsTriangular( directContact );
    doCreateCells = false;
    directContact = (random(1) < 0.5f) ? true : false;
    addRandomVertexVariation = (random(1) < 0.5f) ? true : false;
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
  
  // restart simulation if iteration gets over max
  if( iterationCounter >= maxIterations ) {
    doCreateCells = true;
  }

  // save last time
  lastTime = curTime;
}

public void keyPressed() {
  doCreateCells = true;
}

public void mousePressed() {
  doCreateCells = true;
}


public void createCells() {
  // first, recreate colour table
  // set random number of colours in table, between 3 and 24
  numCyclicColours = 3 + (int)random(61);
  cyclicColours = createColourTable( cyclicColours, numCyclicColours );

  // create
  cells = new Cell[gridHeight][];
  minNeighbour = 4; // this is the only threshold that gives a stable pattern

  println( "New simulation with "+numCyclicColours+" colour states and min "+minNeighbour+" neighbours" );
  
  int type = (int)random(11);
  println("type = "+type);

  for ( int j = 0 ; j < gridHeight ; j++ ) {
    cells[j] = new Cell[gridWidth];
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      switch(type) {
        case 0:
          cells[j][i] = (Cell)new LifeCell( (random(1) < 0.5f) ? false : true );
          break;
        case 1:
          cells[j][i] = (Cell)new ColNeighbourCellRnd();
          break;
        case 2:
          cells[j][i] = (Cell)new ColNeighbourSelectedCellRnd( SELECTION_TOP_LEFT + SELECTION_RIGHT );
          break;
        case 3:
          cells[j][i] = (Cell)new ColSizeNeighbourCellRnd();
          break;
        case 4:
          cells[j][i] = (Cell)new LifeCellColor();
          break;
        case 5:
          cells[j][i] = (Cell)new LifeCellColorized();
          break;
        case 6:
          cells[j][i] = (Cell)new LifeCellWithColour( (random(1) < 0.5f) ? false : true );
          break;
        case 7:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 
                                      + ((i<(gridWidth/2)&&j<(gridHeight/2)) ? 1 : 0)
                                      + ((i>=(gridWidth/2)&&j>=(gridHeight/2)) ? 2 : 0) );
          break;
        case 8:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + 
                                      ((random(1)<0.6f)? 0 : 
                                          ((random(1)<0.6f)? 1 : 
                                             ((random(1)<0.6f? 1 :
                                                ((random(1)<0.6f? 1 : 2)))) )) );
          break;
        case 9:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + (int)random(4) );
          break;
        case 10:
          cells[j][i] = (Cell)new LifeCellGHCyclicColour( minNeighbour );
          break;
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
  // reset iteration counter
  iterationCounter = 0;
}


// settings
float randomVariationX = .5f; // 50 percent
float randomVariationY = .5f; // 50 percent
// other
ArrayList triangleList = new ArrayList();
int oldGridWidth, oldGridHeight;
boolean firstTime = true;
public void createCellsTriangular( boolean fullContactNeighbourhood ) {
  if( firstTime ) {
    oldGridWidth = gridWidth;
    oldGridHeight = gridHeight;
    firstTime = false;
  }
  
  // first, recreate colour table
  // set random number of colours in table, between 3 and 24
  numCyclicColours = 3 + (int)random(21);
  cyclicColours = createColourTable( cyclicColours, numCyclicColours );
  
  // create point field
  //     note, must have extra points that double for the wrap
  ArrayList points = new ArrayList();
  // border points first
  float unitX = (float)width / (float)(oldGridWidth-1);
  float unitY = (float)height / (float)(oldGridHeight-1);
  for( int j = 0 ; j < oldGridHeight ; j++ ) {
    for( int i = 0 ; i < oldGridWidth ; i++ ) {
      float x = ((float)i) * unitX;
      float y = ((float)j) * unitY;
      if( addRandomVertexVariation ) {
        if( i != 0 && i != (oldGridWidth-1) && j != 0 && j != (oldGridHeight-1)) {
          x += random(unitX*randomVariationX*2.f) - (unitX*randomVariationX);
          y += random(unitY*randomVariationY*2.f) - (unitY*randomVariationY);
        }
      }
      points.add( new PVector(x,y) );
    }
  }
  // get triangles
  triangleList = Triangulate.triangulate(points);
  
  // create grid
  gridHeight = 1;
  gridWidth = triangleList.size();
  cells = new Cell[gridHeight][];
  for ( int j = 0 ; j < gridHeight ; j++ ) {
    cells[j] = new Cell[gridWidth];
    for ( int i = 0 ; i < gridWidth ; i++ ) {
      cells[j][i] = (Cell)new LifeCellTriangularCyclicColour( minNeighbour, (Triangle)triangleList.get(i) );
    }
  }

  // choose minimum neighbour
  minNeighbour = 1;
  if( fullContactNeighbourhood ) {
    minNeighbour += (int)random(3);
  }
  // create neighbourhoods
  print( "Creating Neighbourhoods for "+triangleList.size()+" cells " );
  for( int k = 0 ; k < triangleList.size() ; k++ ) {
    Triangle t1 = (Triangle)triangleList.get(k);
    Cell []adjacent = new Cell[ fullContactNeighbourhood ? 21 : 3 ];
    int adjCount = 0;
    for( int i = 0 ; i < triangleList.size() ; i++ ) {
      if( k != i ) {
        Triangle t2 = (Triangle)triangleList.get(i);
        PVector []vert1 = { t1.p1, t1.p2, t1.p3 };
        PVector []vert2 = { t2.p1, t2.p2, t2.p3 };
        int numSameVertices = 0;
        for( int v1 = 0 ; v1 < 3 ; v1++ ) {
          for( int v2 = 0 ; v2 < 3 ; v2++ ) {
            if( (((int)vert1[v1].x) % width) == (((int)vert2[v2].x) % width)
                  && (((int)vert1[v1].y) % height) == (((int)vert2[v2].y) % height) ) {
              numSameVertices++;
            }
          }
        }
        if( (!fullContactNeighbourhood && numSameVertices >= 2)
            || (fullContactNeighbourhood && numSameVertices >= 1) ) {
          adjacent[adjCount] = cells[0][i];
          adjCount++;
        }
      }
    }
    cells[0][k].addAdjacentCells( adjacent );
  }
  println( " done!" );

  println( "New simulation with "+numCyclicColours+" colour states and min "+minNeighbour+" neighbours" );
  
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


class LifeCellGHCyclicColour extends Cell {
  boolean isAlive, isAliveNext;
  int curCyclicColour, nextCyclicColour;
  int minNeighbours;
  
  LifeCellGHCyclicColour( int _minNeighbours ) {
    // initiallise self with default values
    isAlive = (random(1) < 0.4f ? true : false);
    isAliveNext = false;
    minNeighbours = _minNeighbours;
    //curCyclicColour = (int)random(numCyclicColours);
    curCyclicColour = 0;
    nextCyclicColour = curCyclicColour;
    setColour();
  }
  
  public void setColour() {
    if( isAlive ) {
      col = cyclicColours[curCyclicColour];
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
    int numAliveNeighbours = 0;
    for( int i = 0 ; i < 8 ; i++ ) {
      LifeCellGHCyclicColour cell = (LifeCellGHCyclicColour)adjacentCells[i];
      if( cell.isAlive ) {
        numAliveNeighbours++;
      }
    }
    if( numAliveNeighbours >= minNeighbours ) {
      if( isAlive ) {
        nextCyclicColour = ((curCyclicColour+1)%numCyclicColours);
      } else {
        isAliveNext = true;
      }
    } else {
      isAliveNext = false;
    }
    // change state depending on neighbours
    /*
    if( isAlive != isAliveNext && isAlive) {
      change = true;
    }
    */
    if( isAliveNext ) 
      change = true;
    doCommitNext = true;
  }
  
  public void commitFrame() {
    if( isAlive ) {
      if( isAliveNext ) {
        curCyclicColour = ((curCyclicColour+1)%numCyclicColours);
      } else {
        curCyclicColour = 0;
        isAlive = false;
      }
    } else if( isAliveNext ) {
      isAlive = true;
    }
    setColour();
  }
}


class LifeCellTriangularCyclicColour extends Cell {
  boolean isImmortal;
  int curCyclicColour, nextCyclicColour;
  int minNeighbours;
  Triangle tri;
  
  LifeCellTriangularCyclicColour( int _minNeighbours, Triangle _tri ) {
    // initiallise self with default values
    minNeighbours = _minNeighbours;
    curCyclicColour = (int)random(numCyclicColours);
    nextCyclicColour = curCyclicColour;
    setColour();
    tri = _tri;
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
    for( int i = 0 ; i < adjacentCells.length ; i++ ) {
      LifeCellTriangularCyclicColour cell = (LifeCellTriangularCyclicColour)adjacentCells[i];
      if( cell != null ) {
        if( cell.curCyclicColour == ((curCyclicColour+1)%numCyclicColours) ) {
          numNextColourNeighbours++;
        }
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
  
  public void draw( float x, float y, float cellSize ) {
    if( doCommitNext ) {
      doCommitNext = false;
      commitFrame();
    }
    pushStyle();
      noStroke();
      fill( col );
      beginShape(TRIANGLES);
      vertex( tri.p1.x, tri.p1.y );
      vertex( tri.p2.x, tri.p2.y );
      vertex( tri.p3.x, tri.p3.y );
      endShape();
    popStyle();
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
    String[] appletArgs = new String[] { "colLifeTri" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
