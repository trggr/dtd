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
int XS[]      = new int[N];
int YS[]      = new int[M];
int field[][] = new int[N][M];
int GHOSTI = 1;
int GHOSTJ = 1;

void setup() {
  size(800, 600);
  frameRate(30);
  for (int i = 0; i < N; i++) {
    XS[i] = i*HS;
    for (int j = 0; j < M; j++) {
      field[i][j] = 0;  // unoccupied
    }
  }
  for (int j = 0; j < M; j++) {
    YS[j] = j*HS;
  }
}

void drawGrid() {
  strokeWeight(1);
  stroke(50, 50, 50);
  for (int i = 0; i < N; i++) {
    line(XS[i], 0, XS[i], HEIGHT);
  }
  for (int j = 0; j < M; j++) {
    line(0, YS[j], WIDTH, YS[j]);
  }
}

//----------------------------
void drawTower(int n) {
  int i = TOWERI[n];
  int j = TOWERJ[n];

  // platform
  stroke(0, 0, 0);
  strokeWeight(2);
  fill(200, 200, 200);
  rect(XS[i], YS[j], S, S);

  // tower
  strokeWeight(4);
  ellipse(XS[i+1], YS[j+1], 50, 50);

  fill(0, 0, 0);
  text("5", XS[i + 1] + 2, YS[j + 1] + 2);
}

void drawTowers() {
  for (int i = 0; i < NTOWERS; i++) {
    drawTower(i);
  }
}

//-----------------------
// maps x to index in XS
//-----------------------
int xi(int x) {
  if (x <= 0)
    return 0;
  if (x >= XS[N-2])
    return N-2;

  for (int i = 0; i < N-1; i++) {
    if (x <= XS[i])
      return i;
  }
  return N-2;
}

//-----------------------
// maps y to index in YS
//-----------------------
int yj(int y) {
  if (y <= 0)
    return 0;
  if (y >= YS[M-1])
    return M-2;

  for (int j = 0; j < M-1; j++) {
    if (y <= YS[j])
      return j;
  }
  return M-2;
}

//-----
void buildTower() {
  if (NTOWERS >= MAXTOWERS)
    return;

  TOWERI[NTOWERS] = GHOSTI - 1;
  TOWERJ[NTOWERS] = GHOSTJ - 1;
  NTOWERS++;
  
  field[GHOSTI-1][GHOSTJ-1] = 1; // occupy 4 fields
  field[GHOSTI]  [GHOSTJ-1] = 1; 
  field[GHOSTI-1][GHOSTJ]   = 1;
  field[GHOSTI]  [GHOSTJ]   = 1;
}

//-----
void mouseClicked() {
    buildTower();
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
      float tmp = dist(x, y, XS[i], YS[j]);
      if (tmp < md) {
        md = tmp;
        GHOSTI = i;
        GHOSTJ = j;
      }
    }
  }

  if (field[GHOSTI-1][GHOSTJ-1] == 1 || field[GHOSTI]  [GHOSTJ-1] == 1 || 
      field[GHOSTI-1][GHOSTJ]   == 1 || field[GHOSTI]  [GHOSTJ]   == 1) {
          fill(200, 0, 0, 45);
   } else { 
          fill(0, 200, 0, 45);
   }
   rect(XS[GHOSTI-1], YS[GHOSTJ-1], S, S);

   // reach
   fill(180, 180, 180, 45);
   ellipse(XS[GHOSTI], YS[GHOSTJ], 400, 400);
}

void draw() {
  clear();
  drawGrid();
  drawTowers();
  drawGhostTower();
}
