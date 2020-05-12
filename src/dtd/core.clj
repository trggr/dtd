(ns dtd.core
  (:use [quil.core]
        [quil.middleware]))

(println "here")

(def S         42)
(def HS        (/ S 2))
(def QS        (/ S 4))
(def GS        (* S 0.6))
(def WIDTH     720)
(def HEIGHT    600)

(def MAXTOWERS 60)
(def NTOWERS   0)
(def N         (inc (/ (* 2 WIDTH) S)))
(def M         (inc (/ (* 2 HEIGHT) S)))
(def XS        (vec (for [i (range N)] (* i HS))))
(def YS        (vec (for [i (range M)] (* i HS))))
(def MAXZEEKS  100)
(def NZEEKS    10)
(def AMOUNT    100)
(def LIVES     20)
(def GHOSTI    1)
(def GHOSTJ    1)
(def TIMETOUPDATEZEEK 0)
(def TIMER     0)


; 
; //----------------------------
; void drawTower(int n) {
;   int i = TOWERI[n]; // upper-left corner
;   int j = TOWERJ[n];
; 
;   // platform
;   stroke(0, 0, 0);
;   strokeWeight(2);
;   fill(200, 200, 200);
;   rect(XS[i], YS[j], S, S);
; 
;   // bashnya
;   strokeWeight(4);
;   fill(50, 50, 50);
;   ellipse(XS[i+1], YS[j+1], GS, GS);
;   
;   // dulo
;   stroke(0, 0, 0);
;   strokeWeight(5);
;   line(XS[i+1], YS[j+1], XS[i] + 10, YS[j] + 10);
; 
;   text("5", XS[i+2] - 10, YS[j+2] - 10);
; }
; 
; void drawTowers() {
;   for (int i = 0; i < NTOWERS; i++) {
;     drawTower(i);
;   }
; }
; 
; 
; //-----
; void buildTower() {
;   if (AMOUNT <= 0 ||
;       NTOWERS >= MAXTOWERS ||
;      field[GHOSTI-1][GHOSTJ-1] == 1 || field[GHOSTI][GHOSTJ-1] == 1 || 
;      field[GHOSTI-1][GHOSTJ]   == 1 || field[GHOSTI][GHOSTJ]   == 1)
;   {
;      return;
;   }
; 
;   AMOUNT -= 10;
;   TOWERI[NTOWERS] = GHOSTI - 1;
;   TOWERJ[NTOWERS] = GHOSTJ - 1;
;   NTOWERS++;
;   
;   field[GHOSTI-1][GHOSTJ-1] = 1; // occupy 4 fields
;   field[GHOSTI]  [GHOSTJ-1] = 1; 
;   field[GHOSTI-1][GHOSTJ]   = 1;
;   field[GHOSTI]  [GHOSTJ]   = 1;
; }
; 
; //-----
; void mouseClicked() {
;     buildTower();
; }
; 
; void drawGhostTower() {
;   int x = mouseX;
;   int y = mouseY;
;   if (x <= 0 || y <= 0 || x >= WIDTH || y >= HEIGHT)
;     return;
; 
;   int k = xi(x);
;   int l = yj(y);
;   float md = 1000000; // big enough
; 
;   for (int i = k; i <= k+1; i++) {
;     for (int j = l; j <= l+1; j++) {
;       float tmp = dist(x, y, XS[i], YS[j]);
;       if (tmp < md) {
;         md = tmp;
;         GHOSTI = i;
;         GHOSTJ = j;
;       }
;     }
;   }
; 
;   if (field[GHOSTI-1][GHOSTJ-1] == 1 || field[GHOSTI]  [GHOSTJ-1] == 1 || 
;       field[GHOSTI-1][GHOSTJ]   == 1 || field[GHOSTI]  [GHOSTJ]   == 1 ||
;       AMOUNT <= 0) {
;           fill(200, 0, 0, 45);
;    } else { 
;           fill(0, 200, 0, 45);
;    }
;    stroke(0, 0, 0);
;    rect(XS[GHOSTI-1], YS[GHOSTJ-1], S, S);
; 
;    // reach
; 
;    fill(180, 180, 180, 45);
;    ellipse(XS[GHOSTI], YS[GHOSTJ], S*3, S*3);
; }
; 
; 
; void moveZeek(int n, int dx, int dy) {
;   int i = ZEEKI[n] + dx;
;   int j = ZEEKJ[n] + dy;
;   
;   if (i >= 2 && i <= N-2 && j >= 2 && j <= M-2 && field[i][j] == 0) {
;       field[ZEEKI[n]][ZEEKJ[n]] = 0;
;       ZEEKI[n] = i;
;       ZEEKJ[n] = j;
;       field[i][j] = 1;
;   }
;   else
;       ZEEKD[n] = int(random(1,8));
; }
;       
; void updateZeeks() {
;     for (int i = 0; i < NZEEKS; i++) {
;         switch (ZEEKD[i]) {
;           case 1:  moveZeek(i,  1, -1); break;
;           case 2:  moveZeek(i,  1,  0); break;
;           case 3:  moveZeek(i,  1,  1); break;
;           case 4:  moveZeek(i,  0,  1); break;
;           case 5:  moveZeek(i, -1,  1); break;
;           case 6:  moveZeek(i, -1,  0); break;
;           case 7:  moveZeek(i, -1, -1); break;
;           default: moveZeek(i,  0, -1);
;         }
;     }
; }
; 
; void updateTimer() {
;   TIMER = millis()/1000;
; }
; 

(defn rnd [low high]    (+ low (rand (- high low))))

(defn xi [x]
   (cond (<= x 0) 0
         (>= x (nth XS (- N 2))) (- N 2)
         :else (some #(<= x (nth XS %)) (range (- N 1)))))

(defn yi [y]
   (cond (<= y 0) 0
         (>= y (nth YS (- M 1))) (- M 2)
         :else (some #(<= y (nth YS %)) (range (- M 1)))))

(def dirref {1  [1 -1]
             2  [1  0]
             3  [1  1]
             4  [0  1]
             5  [-1 1]
             6  [-1 0]
             7  [-1 -1]
             8  [0 -1]})

(defn move-zeek [zeek]
    (let [[dx dy] (dirref (zeek :dir))
          i       (+ dx (zeek :i))
          j       (+ dy (zeek :j))]
          (if (and (<= 2 i (- N 2))
                   (<= 2 j (- M 2))  ; need to check field[i][j] = 0
                   )
              (assoc zeek :i i :j j) ; also need field[i][j] = 1
              (assoc zeek :dir (int (random 1 8))))))

(defn update-state [state] 
	(-> state
	    (update-in [:zeeks] #(map move-zeek %))))

(defn key-pressed [state event] state)
(defn key-released [state event] state)

(defn draw-zeek [val]
   (let [{i :i j :j} val]
       (stroke 200 0 200)
       (fill 200 0 200)
       (stroke-weight 2)
       (ellipse (+ (XS i) QS) (+ (YS j) QS) HS HS)))

(defn draw-zeeks [state]
   (doseq [z (state :zeeks)]
	(draw-zeek z)))

(defn draw-grid [state]
   (stroke-weight 1)
   (stroke 50 50 50)
   (doseq [i (range N)]
       (line (XS i) 0 (XS i) HEIGHT))
   (doseq [j (range M)]
       (line 0 (YS j) WIDTH (YS j))))

(defn draw-status [state]
   (fill 255 255 0)
   (text (str "Amount: " AMOUNT "   Timer: " TIMER "   Lives: " LIVES) (XS 1), (YS 1)))

; void draw() {
;+   clear();
;   updateTimer();
;+   drawGrid();
;   drawTowers();
;   if (TIMETOUPDATEZEEK == 0 && TIMER >= 10)
;       updateZeeks();
;   TIMETOUPDATEZEEK = (TIMETOUPDATEZEEK + 1) % 10;
;   drawZeeks();
;+   drawStatus();
;   drawGhostTower();
; }

(defn draw [state]
	(clear)
	(draw-grid state)
	(draw-zeeks state)
	(draw-status state))

;        (no-stroke)
;        (let [pos [100 200]
;              ; cam (translate2 pos [(* -0.5 (width)) (* -0.5 (height))])
;              cam pos]
;              (doseq [n  (:natas state)] (draw-entity n cam))
;              (doseq [t (:towers state)] (draw-entity t cam))
;              (draw-entity (:amount state) cam)))

(defn setup []
        (frame-rate 30)
        {:tower-types []
         :levels      []
         :amount      100
         :level       1
         :field       {}
         :zeeks       (for [_ (range NZEEKS)] {:i 2 :j 10 :dir (int (rnd 1 3))})
         :towers      []})

(println "here 2")

(defsketch dtd
        :host         "host"
        :size         [720 600]
        :setup        setup
        :update       update-state
        :key-pressed  key-pressed
        :key-released key-released
        :draw         draw
        :middleware   [fun-mode])

(println "here 3")

(defn -main [& args] nil)
