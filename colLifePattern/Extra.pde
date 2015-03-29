float colSize( color col ) {
  float r = abs((col >> 16 & 0xFF) - 128);
  float g = abs((col >> 8 & 0xFF) - 128);
  float b = abs((col & 0xFF) - 128);
  return (r+g+b) / 3.f;
}

boolean colDistinct( color col, float minSize, float minDistinct ) {
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


color raiseColToMinSize( color col, float minSize ) {
  color c = col;
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

float strongestColour( color col ) {
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

color changeStrongestColour( color col, float change ) {
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

color[] createColourTable( color []colourTable, int numCols ) {
  colourTable = new color[numCols];
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

color []interpolateColourTable = { color(255,0,0), color(255,255,0), color(0,255,0), color(0,255,255),
                          color(0,0,255), color(255,0,255) };
int []colourMovementTable = { INC_GREEN, DEC_RED, INC_BLUE, DEC_GREEN, INC_RED, DEC_BLUE };                         
                          
color getRainbowColourFromLinearNumber( float num ) {
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
    { #5D5C4A,#686752,#8D8B6B,#CEC8B7,#6B7257,#7D866C,#302E4A,#313046,#313345,#373748,#3A3A4B,#49445A,#817A85,#B2A7AB,#A49EA5 },                    // Pennys & Polaroids - SeeFig
    { #433D29,#50442C,#5C4E2C,#7B5D2E,#836231,#88744B,#8D7A4E,#A18847,#AA8F54,#B39B5C },                                                            // Emers Garden (wood) - SeeFig
    { #947D72,#A08E8D,#A38280,#977977,#AB8F8D,#A18582,#B2ABA9,#CAD0C5,#919195,#998694,#8B6E7B,#846F7C,#7E6B77,#9B808C,#A08F9B,#A78896,#AB939C,#6B4852,#BA9EA3,#B19196,#BA9599,#9C7374,#976C6B }, //Blossoms - SeeFig
    { #21431C,#103113,#144217,#736E39,#867238,#588D35,#7E9644,#B0A345,#D0B93E,#DFCE46,#EDE951,#E2D597 },                                            // Spring - SeeFig
    { #E5B9B9,#B78C78,#CC9F82,#E1B57F,#C69547,#E6D1A6,#D8BB7C,#E1C679,#E6D488,#EADEAE,#EEE8C4,#F5F5EC,#ABA9BC,#DA3B88,#DD7FA2,#DB9DA9,#DF8D95 },    // Colourblocc (ice cream) - SeeFig
    { #120602,#220B03,#360E02,#3C1102,#431302,#4E1401,#5F2A07,#742F07,#7D2D01,#843A07,#A1621D },                                                    // Galway Cathedral (dark fire) - SeeFig
    { #532700,#914F0B,#512C04,#994710,#503010,#6B451A,#A27D4A,#CAAA7D,#BA8840,#E2B46D,#6A5017,#89681F,#989A40,#7E7B12,#5C5C19,#4A5117,#343A1E },    // Salad Fingers(wood and grass) - SeeFig
    { #AF2E35,#C35132,#B43E18,#B76E3C,#724524,#C69060,#D58E1D,#A27C2C,#907121,#997B24,#988D49,#CCC071 },                                            // Untitled (natural tasty) - Alison
    { #671C2C,#73162A,#B33A54,#C4526B,#C16175,#CC7487,#C7798A,#E0768D,#FE6686,#EA7A92,#D8899A,#F37B95,#E4889B,#DA91A1,#E593A5,#DC98A6,#DC98A7,
      #E197A7,#EA94A6,#DE9DAB,#DE9FAC,#E2ABB7,#E8B3BE,#EEB1BE,#E9BAC4,#E8C1CA,#FEBAC9,#FEC1CE,#FEEEF2,#F9F9F9,#FEFEFE},                             // Samuari Hack
    { #946D52,#776746,#63612E,#4D5528,#37420F,#4E5B22,#465826,#374D18,#576841,#354D29,#71A559,#457A4F,#538F60 },                                    // Moschops
    { #333933,#444D47,#48534F,#515D59,#ABB7AC,#CDDAD2,#D0DBD3,#DCE6DF,#E4EFE7,#F6F0EE,#F8FBF9,#FCFEFC,#FDFFFD },                                    // Poster on poll
    { #6E4A38,#C3A795,#886B58,#342418,#7E5C42,#BAA38F,#4C3928,#4F4134,#5F5A55,#9A988E,#5D5B4E,#62675A,#64675E,#6A6F6A },                            // Anne
    { #141B90,#12E940,#123750,#2104E0,#20307B,#2E3468,#104350,#505340,#202320,#48456A,#363253,#514462,#3C3147,#7C6469 },                                   // Jellyfish
    { #C08814,#D69E13,#D09910,#D49C10,#CD9812,#9C750D,#956D01,#957106,#434501,#C4C681,#9DCFFE,#9AC6CD,#C9D2B4,#B1BFAD },                            // Buttercup
    { #F5A630,#4EBBC9,#64C4CF,#65C4D0,#69C5D0,#6BC3CE,#6EC7D2,#8DD4DC,#89CCD4,#90D5DD,#A5DDE3,#ABDFE5,#B7E4E8,#CAEAEC },                             // Ice
    { #F97C44,#F77C38,#F57D2B,#F68131,#F48326,#F78E37,#F68D30,#F69232,#F6982E,#F59729,#F3921D,#F3981E,#F49C24,#F4AE1D }                             // Sun
  };


color[] createColourTable_better( color []colourTable, int numCols ) {
  colourTable = new color[numCols];
  int table = (int)random(colours.length);
  int tableLen = colours[table].length;
  for( int i = 0 ; i < numCols ; i++ ) {
    colourTable[i] = colours[table][(int)(((float)i/(float)numCols)*(float)tableLen)];
  }
  return colourTable;
}