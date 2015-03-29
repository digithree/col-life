Coloured Life
=============


Definition
----------
from <http://kaytdek.trevorshp.com/projects/computer/neuralNetworks/gameOfLife2.htm>

"In cyclic cellular automata, an ordering of multiple colors is established. Whenever a cell is neighbored by a cell whose color is next in the cycle, it copies that neighbor's color--otherwise, it remains unchanged."


Method
------

Using different levels of thresholding, i.e. enforcing a minimum number of neighbours in the next sequence to qualify cycle increment, this experiment intends to show different behaviours within the cyclic CA system.

Also added is randomised cycle sizes, i.e. a random number of states / colours. This random value is between 3 and 64.


Versions
--------

### Standard rules, rainbow coloured

This is the standard cyclic cycle cellular autonoma based on the Game of Life. 

The grid is randomised by state to begin with. There are also several configurations which set patterns in the grid by setting the minimum number of neighbours for a state change higher (higher values mean the cells resist change).

Two variables exist in the system, which are randomised: threshold, which means the number of neighbours need for a colour to proceed to the next colour in the palette, and number of colours, i.e. the size of the cycle. 

The program resets the grid (i.e. automatically regenerates) and colour scheme if the system becomes stable or old.

![Standard scrshot 1](/screenshots/scrshot-std-1.png)

![Standard scrshot 2](/screenshots/scrshot-std-2.png)

![Standard scrshot 3](/screenshots/scrshot-std-3.png)

### Pattern weighed, interesting coloured palette

Same as the standard rules with the additional rule of minimum number of neighbours for a state change higher (higher values mean the cells resist change). These are distributed in patterns across the space.

Additionally, a set of more interesting colour palettes were used for this version.

![Pattern scrshot 1](/screenshots/scrshot-pat-1.png)

![Pattern scrshot 2](/screenshots/scrshot-pat-2.png)


### Triangular Neighbourhood

Same as standard square neighbourhood except that there are 15 possible neighbours instead of 8. In addition, only three of these neighbours will have edge contact, the other 12 have vertex contact. Accordingly, edge contact is weighted to have higher effect that vertex contact.

There are two display modes:
	1. Ordered - vertices are distributed in a strict grid
	2. Randomised - vertices are distributed in a strict grid and then their position is randomised by a small offset

![Triangular scrshot 1](/screenshots/scrshot-tri-1.png)

![Triangular scrshot 1](/screenshots/scrshot-tri-2.png)

###  Greenberg-Hastings (GH) Model

from <http://www.mirekw.com/ca/rullex_cycl.html>

"... the Greenberg-Hastings (GH) Model, perhaps the simplest CA prototype for an excitable medium. A prescribed number of colors N are arranged cyclically in a 'color wheel.' Each color can only advance to the next, the last cycling to 0. Every update, cells change from color 0 (resting) to 1 (excited) if they have at least Threshold 1's in their neighbour set. All other colors (refractory) _advance automatically_. Starting from a uniform random soup of the available colors, the excitation dies out if the threshold is too large compated to the size of the neighbour set, while a disordered soup virtually indistinguishable from noise results if the threshold is too low. For intermediate thresholds, however, waves of excitation self-organise into large scale spiral pairs that stablize in a locally periodic state."

I found that the only threshold that works in any case is 4 in our system. This yields aemeba-like organisims or areas with rapidly oscilating fringes. Pretty cool but predictable.

![Greenberg-Hastings scrshot 1](/screenshots/scrshot-gh-1.png)


Thanks
------
Brendan Flynn for sparking interest in this project and bouncing ideas.