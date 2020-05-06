int S = 100;
int HS = S / 2;
float GS = S * 0.6;
int WIDTH  = 800;
int HEIGHT = 600;
int N = 2*WIDTH/S + 1;
int M = 2*HEIGHT/S + 1;

int MAXTOWERS = 60;
int tower_cnt = 0;
int towersx[] = new int[MAXTOWERS];
int towersy[] = new int[MAXTOWERS];
int xs[]      = new int[N];
int ys[]      = new int[M];
int field[][] = new int[N][M];
int GHOSTi = 1;
int GHOSTj = 1;


void setup() {
  size(800, 600);
  frameRate(20);
  for (int i = 0; i < N; i++) {
    xs[i] = i*HS;
    print(xs[i] + " ");
  }
  for (int j = 0; j < M; j++) {
    ys[j] = j*HS;
    print(ys[j] + " ");
  }
  
}

int tx(int x) {return x - HS;}

int ty(int y) {return y - HS;}

void drawGrid() {
  stroke(255, 255, 255);
  for (int i = 0; i < N; i++) {
    line(xs[i], 0, xs[i], HEIGHT);
  }
  for (int j = 0; j < M; j++) {
    line(0, ys[j], WIDTH, ys[j]);
  }
}

void drawTower(int i) {
      int x = towersx[i];
      int y = towersy[i];

      // platform
      stroke(0, 0, 0);
      strokeWeight(2);
      fill(200, 200, 200);
      rect(tx(x), ty(y), S, S);
      
      // tower
      strokeWeight(4);
      ellipse(x, y, 50, 50);

      fill(0, 0, 0);
      text("5", tx(x + S - 20), ty(y + S - 20));
}

void drawTowers() {
      for (int i = 0; i < tower_cnt; i++) {
              drawTower(i);
      }
}

int[] snapToGrid(int x, int y) {
  int xs = x % S;
  int ys = y % S;
  int x1 = x - xs; int x2 = x - xs + S;
  int y1 = y - ys; int y2 = y - ys + S;
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

//--------
int xi(int x) {
  if (x <= 0)
    return 0;
  if (x >= xs[N-2])
    return N-2;
    
  for (int i = 0; i < N-1; i++) {
    if (x <= xs[i])
        return i;
  }
  return N-2;
}

//--------
int yj(int y) {
  if (y <= 0)
      return 0;
  if (y >= ys[M-1])
      return M-2;
      
  for (int j = 0; j < M-1; j++) {
    if (y <= ys[j])
        return j;
  }
  return M-2;
}

  
//-----
void mouseClicked() {
  if (tower_cnt >= MAXTOWERS)
      return;
      
  int[] tmp = snapToGrid(mouseX, mouseY);
  towersx[tower_cnt] = tmp[0];
  towersy[tower_cnt] = tmp[1];
  tower_cnt++;
}

void drawGhostTower() {
     int x = mouseX;
     int y = mouseY;
     if (x <= 0 || y <= 0 || x >= WIDTH || y >= HEIGHT)
       return;
     
     int k = xi(x);
     int l = yj(y);
     float md = 1000000; // big enough

//     print("[" + x + " " + y + " " + k + " " + l + "]");
     for (int i = k; i <= k+1; i++) {
         for (int j = l; j <= l+1; j++) {
             float tmp = dist(x, y, xs[i], ys[j]);
             if (tmp < md) {
                   md = tmp;
                   GHOSTi = i;
                   GHOSTj = j;
             }
         }
     }

     fill(0, 200, 0);
     rect(xs[GHOSTi-1], ys[GHOSTj-1], HS, HS);
     rect(xs[GHOSTi],   ys[GHOSTj-1], HS, HS);
     rect(xs[GHOSTi-1], ys[GHOSTj],   HS, HS);
     rect(xs[GHOSTi],   ys[GHOSTj],   HS, HS);
     
      // reach
      fill(180, 180, 180, 45);
      ellipse(xs[GHOSTi], ys[GHOSTj], 400, 400);
}

void draw() {
      clear();
      drawGrid();
      drawTowers();
      drawGhostTower();
}
