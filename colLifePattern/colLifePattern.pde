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
 * 
 * by Simon Kenny
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
color []cyclicColours = null;

int minNeighbour = 0;
int maxIterations = 1000;

void setup() {
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
void draw() {
  
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

void keyPressed() {
  if( key == ' ' ) {
    blackAndWhiteFlag = !blackAndWhiteFlag;
  } else if( key == 'r' || key == 'R' ) {
    doCreateCells = true;
  }
}


void createCells() {
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
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + ((random(2)<0.2)?1:0) );
          break;
        case 8:
          cells[j][i] = (Cell)new LifeCellCyclicColour( 1 + ((random(2)<0.1)?2:0) );
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

