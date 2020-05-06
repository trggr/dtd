int S = 100;
int HS = S / 2;
float GS = S * 0.6;
int WIDTH  = 800;
int HEIGHT = 600;
int N = 2*WIDTH/S + 1;
int M = 2*HEIGHT/S + 1;
int MAXTOWERS = 60;
int NTOWERS = 0;
int TOWERI[] = new int[MAXTOWERS];
int TOWERJ[] = new int[MAXTOWERS];
int xs[]      = new int[N];
int ys[]      = new int[M];
int field[][] = new int[N][M];
int GHOSTI = 1;
int GHOSTJ = 1;


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

int tx(int x) {
  return x - HS;
}

int ty(int y) {
  return y - HS;
}

void drawGrid() {
  strokeWeight(1);
  stroke(50, 50, 50);
  for (int i = 0; i < N; i++) {
    line(xs[i], 0, xs[i], HEIGHT);
  }
  for (int j = 0; j < M; j++) {
    line(0, ys[j], WIDTH, ys[j]);
  }
}

void drawTower(int n) {
  int i = TOWERI[n];
  int j = TOWERJ[n];

  // platform
  stroke(0, 0, 0);
  strokeWeight(2);
  fill(200, 200, 200);
  rect(xs[i], ys[j], S, S);

  // tower
  strokeWeight(4);
  ellipse(xs[i+1], ys[j+1], 50, 50);

  fill(0, 0, 0);
  text("5", xs[i] + 2, ys[j] + 2);
}

void drawTowers() {
  for (int i = 0; i < NTOWERS; i++) {
    drawTower(i);
  }
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
  if (NTOWERS >= MAXTOWERS)
    return;

  // int[] tmp = snapToGrid(mouseX, mouseY);
  TOWERI[NTOWERS] = GHOSTI - 1;
  TOWERJ[NTOWERS] = GHOSTJ - 1;
  NTOWERS++;
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
        GHOSTI = i;
        GHOSTJ = j;
      }
    }
  }

  fill(0, 200, 0);
  rect(xs[GHOSTI-1], ys[GHOSTJ-1], S, S);


  // reach
  fill(180, 180, 180, 45);
  ellipse(xs[GHOSTI], ys[GHOSTJ], 400, 400);
}

void draw() {
  clear();
  drawGrid();
  drawTowers();
  drawGhostTower();
}
