(ns dtd.core
  (:use [quil.core]
        [quil.middleware]))

;; (millis) has a bug - workaround the null pointer exception
(defn milsec []
    (System/currentTimeMillis))

(def S         42)
(def HS        (/ S 2))
(def QS        (/ S 4))
(def GS        (* S 0.6))
(def WIDTH     720)
(def HEIGHT    600)

(def MAXTOWERS 60)
(def NTOWERS   0)
(def N         (int (inc (/ (* 2 WIDTH) S))))
(def M         (int (inc (/ (* 2 HEIGHT) S))))
(def XS        (vec (for [i (range N)] (int (* i HS)))))
(def YS        (vec (for [i (range M)] (int (* i HS)))))
(def MAXZEEKS  100)
(def NZEEKS    10)
(def AMOUNT    100)
(def LIVES     20)
(def GHOSTI    1)
(def GHOSTJ    1)
(def TIMETOUPDATEZEEK 0)

(println XS)
(println YS)


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

; random number between low and high, both are inclusive
(defn rnd [low high]
       (+ low (random (- high low -0.5))))

(defn xi [x]
       (cond (<= x 0) 0
             (>= x (nth XS (- N 2))) (- N 2)
             :else (some #(when (<= x (nth XS %)) %) (range (- N 1)))))

(defn yi [y]
       (cond (<= y 0) 0
             (>= y (nth YS (- M 2))) (- M 2)
             :else (some #(when (<= y (nth YS %)) %) (range (- M 1)))))

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
              (assoc zeek :dir (int (rnd 1 8))))))

(defn update-state [state]
        (let [now (milsec)]
       	    (-> state
                (assoc :timer       now)
                (assoc :elapsed     (int (/ (- now (state :stimer)) 1000)))
    	        (update-in [:zeeks] #(map move-zeek %))
    	        (update-in [:ghost-tower] update-ghost-tower))))

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

(defn draw-tower [val] )

(defn draw-towers [tower]
        (doseq [t (state :towers)]
             (draw-tower t)))

(defn draw-grid [state]
        (stroke-weight 1)
        (stroke 50 50 50)
        (doseq [i (range N)]
            (line (XS i) 0 (XS i) HEIGHT))
        (doseq [j (range M)]
            (line 0 (YS j) WIDTH (YS j))))

(defn draw-ghost-tower [state]
        (let [[i j] (state :ghost-tower)]
            (when (and (pos? i) (pos? j))
                (fill 0 200 0 45)
                (stroke 0 0 0)
                (rect (XS (dec i)) (YS (dec j)) S S)
                (fill 180 180 180 45)
                (ellipse (XS i) (YS j) (* 3 S) (* 3 S)))))

(defn draw-status [state]
        (fill 255 255 0)
        (text (str "Amount: " AMOUNT
                   "   Timer: " (state :elapsed)
                   "   Lives: " LIVES) (XS 1), (YS 1)))

(defn update-ghost-tower [state]
    (let [x (mouse-x)
          y (mouse-y)]
        (if (or (<= x 0) (>= x WIDTH) (<= y 0) (>= y HEIGHT))
            [0 0]
            (let [k  (xi x)
                  l  (yi y)
                  xs (for [i [k (inc k)] j [l (inc l)]] [(dist x y (XS i) (YS j)), i, j])
                  rc (reduce (fn [acc b] 
                              (if (< (first b) (first acc))
                                  b
                                  acc))
                              [100000000 0 0]
                              xs)]
                  [(nth rc 1) (nth rc 2)]))))

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

; void draw() {
;+   clear();
;+   updateTimer();
;+   drawGrid();
;   drawTowers();
;   if (TIMETOUPDATEZEEK == 0 && TIMER >= 10)
;       updateZeeks();
;   TIMETOUPDATEZEEK = (TIMETOUPDATEZEEK + 1) % 10;
;+   drawZeeks();
;+   drawStatus();
;+   drawGhostTower();
; }

(defn draw [state]
	(clear)
	(println (state :ghost-tower))
	(draw-grid state)
	(draw-towers state)
	(draw-zeeks state)
	(draw-status state)
	(draw-ghost-tower state))


(defn setup []
        (frame-rate   30)
        {:tower-types []
         :levels      []
         :amount      100
         :level       1
         :field       {}
	 :stimer      (milsec)
	 :timer       (milsec)
         :elapsed     0
         :ghost-tower [0 0]
         :zeeks       (for [_ (range NZEEKS)] {:i 2 :j 10 :dir (int (rnd 1 3))})
         :towers      []})

(defsketch dtd
        :host         "host"
        :size         [720 600]
        :setup        setup
        :update       update-state
        :key-pressed  key-pressed
        :key-released key-released
        :draw         draw
        :middleware   [fun-mode])

(defn -main [& args] nil)
