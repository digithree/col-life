/* Coloured Life - Triangular Neighbourhood
 *
 * by Simon Kenny
 */
 
import org.processing.wiki.triangulate.*;

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
color []cyclicColours = null;

int minNeighbour = 0;
int maxIterations = 500; // was 5000

boolean directContact = false;
boolean addRandomVertexVariation = false;

void setup() {
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
void draw() {
  noStroke();

  // time
  float curTime = (float)millis()/1000.f;
  float elapsedTime = curTime - lastTime;
  
  if( doCreateCells ) {
    createCellsTriangular( directContact );
    doCreateCells = false;
    directContact = (random(1) < 0.5) ? true : false;
    addRandomVertexVariation = (random(1) < 0.5) ? true : false;
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

void keyPressed() {
  doCreateCells = true;
}

void mousePressed() {
  doCreateCells = true;
}


void createCells() {
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
                                      ((random(1)<0.6)? 0 : 
                                          ((random(1)<0.6)? 1 : 
                                             ((random(1)<0.6? 1 :
                                                ((random(1)<0.6? 1 : 2)))) )) );
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
void createCellsTriangular( boolean fullContactNeighbourhood ) {
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
