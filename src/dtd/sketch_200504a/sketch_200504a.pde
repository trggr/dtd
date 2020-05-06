int s = 100;
int h = s / 2;
float g = s * 0.6;
int MAXNUM = 60;
int num = 0;
int towersx[] = new int[MAXNUM];
int towersy[] = new int[MAXNUM];

void setup() {
  size(800, 600);
}

int tx(int x) {
  return x - h;
}

int ty(int y) {
  return y - h;
}


void drawTower(int i) {
      int x = towersx[i]; int y = towersy[i];

      // platform
      stroke(0, 0, 0);
      strokeWeight(2);
      fill(200, 200, 200);
      rect(tx(x), ty(y), s, s);
      
      // tower
      strokeWeight(4);
      ellipse(x, y, 50, 50);

      fill(0, 0, 0);
      text("5", tx(x + s - 20), ty(y + s - 20));
}

void drawTowers() {
      for (int i = 0; i < num; i++) {
              drawTower(i);
      }
}

int[] snapToGrid(int x, int y) {
  int xs = x % s;
  int ys = y % s;
  int x1 = x - xs; int x2 = x - xs + s;
  int y1 = y - ys; int y2 = y - ys + s;
  int[] rc = new int[2];
  float d, md;

  md = dist(x, y, x1, y1); rc[0] = x1; rc[1] = y1;
  
  d = dist(x, y, x2, y1);
  if (d < md) {
    md = d;
    rc[0] = x2; rc[1] = y1;
  }

  d = dist(x, y, x1, y2);
  if (d < md) {
    md = d;
    rc[0] = x1; rc[1] = y2;
  }

  d = dist(x, y, x2, y2);
  if (d < md) {
    md = d;
    rc[0] = x2; rc[1] = y2;
  }

  return rc;
}

//-----
void mouseClicked() {
  if (num >= MAXNUM)
      return;
      
  int[] tmp = snapToGrid(mouseX, mouseY);
  towersx[num] = tmp[0];
  towersy[num] = tmp[1];
  num++;
}

void draw() {
      int x = mouseX;
      int y = mouseY;
      clear();
      drawTowers();
 
      if (x > (x - (x % s)) && x < (x - (x % s) + s) && y > (y - (y % s)) && y < (y - (y % s) + s)) {
          fill(0, 200, 0);
      } else {
          fill(0, 200, 0);
      }        
      rect(tx(x), ty(y), s, s);

      // reach
      fill(180, 180, 180, 45);
      ellipse(x, y, 400, 400);
}
