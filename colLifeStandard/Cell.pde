
class Cell {
  color col;
  boolean doCommitNext = false;
  Cell []adjacentCells = new Cell[8];
  
  Cell() {
    // initiallise self with default values
    col = color(255);
  }
  
  void addAdjacentCells( Cell []_adjacentCells ) {
    adjacentCells = _adjacentCells;
  }
  
  void nextFrame() {
    // modify seld based on adjacent cells
  }
  
  void commitFrame() {
    // actually make changes
  }
  
  void draw( float x, float y, float cellSize ) {
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
  
  void setColour() {
    if( isAlive ) {
      col = color(255);
    } else {
      col = color(0);
    }
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
    isAlive = isAliveNext;
    setColour();
  }
}



class ColNeighbourCellRnd extends Cell {
  color nextCol;
 
  ColNeighbourCellRnd() {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
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
  color nextCol;
  int selection;
 
  ColNeighbourSelectedCellRnd( int _selection ) {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
    selection = _selection;
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
    col = nextCol;
  }
}


class ColSizeNeighbourCellRnd extends Cell {
  color nextCol;
 
  ColSizeNeighbourCellRnd() {
    // initiallise self with default values
    col = color( (int)random(255), (int)random(255), (int)random(255) );
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
    col = nextCol;
  }
}

float COL_CHANGE = 40.f;
class LifeCellColor extends Cell {
  boolean isAlive;
  color nextCol;
  
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
  
  void setAlive() {
    //isAlive = (strongestColour(col) >= MIN_COL_SIZE);
    isAlive = brightness(col) >= MIN_COL_SIZE;
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
    col = nextCol;
    setAlive();
  }
  
  void draw( float x, float y, float cellSize ) {
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
  
  void setColour() {
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
  
  void nextFrame() {
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
  
  void commitFrame() {
    isAlive = isAliveNext;
    setColour();
  }
}

class LifeCellWithColour extends Cell {
  boolean isAlive, isAliveNext;
  color aliveCol;
  
  LifeCellWithColour( boolean state ) {
    // initiallise self with default values
    isAlive = state;
    aliveCol = color( (int)random(255), (int)random(255), (int)random(255) );
    setColour();
    isAliveNext = false;
  }
  
  void setColour() {
    if( isAlive ) {
      col = aliveCol;
    } else {
      col = color(0);
    }
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
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
  
  void setColour() {
    col = cyclicColours[curCyclicColour];
  }
  
  void nextFrame() {
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
  
  void commitFrame() {
    curCyclicColour = nextCyclicColour;
    setColour();
  }
}
