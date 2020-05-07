int S         = 60;
int HS        = S/2;
int QS        = S/4;
float GS      = S*0.6;
int WIDTH     = 720;
int HEIGHT    = 600;

int MAXTOWERS = 60;
int NTOWERS   = 0;
int TOWERI[]  = new int[MAXTOWERS]; // x-ccord of tower
int TOWERJ[]  = new int[MAXTOWERS]; // y-coord of tower

int MAXZEEKS  = 100;
int NZEEKS    = 0;
int ZEEKI[]   = new int[MAXZEEKS]; //x-coord of zeek
int ZEEKJ[]   = new int[MAXZEEKS];
int ZEEKD[]   = new int[MAXZEEKS]; // direction of movement: 1-12 - like clock's arms  

int N         = 2*WIDTH/S + 1;
int M         = 2*HEIGHT/S + 1;
int XS[]      = new int[N];        // x-coordinates
int YS[]      = new int[M];        // y-coordinates
int field[][] = new int[N][M];
int GHOSTI    = 1;
int GHOSTJ    = 1;

int AMOUNT    = 100;

int TIMETOUPDATEZEEK = 0;


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

  NZEEKS = 10;
  
  for (int i = 0; i < NZEEKS; i++) {
    ZEEKI[i] = 2;
    ZEEKJ[i] = 10;
    ZEEKD[i] = int(random(1, 3));
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
  int i = TOWERI[n]; // upper-left corner
  int j = TOWERJ[n];

  // platform
  stroke(0, 0, 0);
  strokeWeight(2);
  fill(200, 200, 200);
  rect(XS[i], YS[j], S, S);

  // bashnya
  strokeWeight(4);
  fill(50, 50, 50);
  ellipse(XS[i+1], YS[j+1], GS, GS);
  
  // dulo
  stroke(0, 0, 0);
  strokeWeight(5);
  line(XS[i+1], YS[j+1], XS[i] + 10, YS[j] + 10);

  text("5", XS[i+2] - 10, YS[j+2] - 10);
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
  if (AMOUNT <= 0 ||
      NTOWERS >= MAXTOWERS ||
     field[GHOSTI-1][GHOSTJ-1] == 1 || field[GHOSTI][GHOSTJ-1] == 1 || 
     field[GHOSTI-1][GHOSTJ]   == 1 || field[GHOSTI][GHOSTJ]   == 1)
  {
     return;
  }

  AMOUNT -= 10;
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
   stroke(0, 0, 0);
   rect(XS[GHOSTI-1], YS[GHOSTJ-1], S, S);

   // reach

   fill(180, 180, 180, 45);
   ellipse(XS[GHOSTI], YS[GHOSTJ], S*3, S*3);
}

//----------------------------
void drawZeek(int n) {
  int i = ZEEKI[n]; // upper-left corner
  int j = ZEEKJ[n];

  stroke(200, 0, 200);
  fill(200, 0, 200);
  strokeWeight(2);
  ellipse(XS[i]+QS, YS[j]+QS, HS, HS);
}

void drawZeeks() {
    for (int i = 0; i < NZEEKS; i++) {
        drawZeek(i);
    }
}

void moveZeek(int n, int dx, int dy) {
  int i = ZEEKI[n] + dx;
  int j = ZEEKJ[n] + dy;
  
  if (i >= 2 && i <= N-2 && j >= 2 && j <= M-2 && field[i][j] == 0) {
      field[ZEEKI[n]][ZEEKJ[n]] = 0;
      ZEEKI[n] = i;
      ZEEKJ[n] = j;
      field[i][j] = 1;
  }
  else
      ZEEKD[n] = int(random(1,8));
}
      
void updateZeeks() {
    for (int i = 0; i < NZEEKS; i++) {
        switch (ZEEKD[i]) {
          case 1:  moveZeek(i,  1, -1); break;
          case 2:  moveZeek(i,  1,  0); break;
          case 3:  moveZeek(i,  1,  1); break;
          case 4:  moveZeek(i,  0,  1); break;
          case 5:  moveZeek(i, -1,  1); break;
          case 6:  moveZeek(i, -1,  0); break;
          case 7:  moveZeek(i, -1, -1); break;
          default: moveZeek(i,  0, -1);
        }
    }
}

void drawStatus() {
  text("Amount: " + AMOUNT, XS[2], YS[2]);
}
  
void draw() {
  clear();
  drawGrid();
  drawTowers();
  if (TIMETOUPDATEZEEK == 0)
      updateZeeks();
  TIMETOUPDATEZEEK = (TIMETOUPDATEZEEK + 1) % 4;
  drawZeeks();
  drawStatus();
  drawGhostTower();
}
