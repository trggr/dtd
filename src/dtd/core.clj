; To run from REPL:
; (use 'dtd.core :reload-all)
;
(ns dtd.core
  (:use [quil.core]
        [quil.middleware]))

;; (millis) has a bug - workaround the null pointer exception
(defn milsec []
    (System/currentTimeMillis))

(def S         48)      ;; divisible by 2, 4, 6, 8
(def HS        (/ S 2))
(def QS        (/ S 4))
(def GS        (* 6 (/ S 8)))
(def S3        (* 3 S))

(def N         20)
(def M         18)
(def WIDTH     (* HS N))
(def HEIGHT    (* HS M))

(def XS        (vec (for [i (range (inc N))] (* i HS))))
(def YS        (vec (for [i (range (inc M))] (* i HS))))

(def MAXTOWERS 60)
(def NZEEKS    3)
(def TIMETOUPDATEZEEK 0)
(def DEBUG     true)

(println XS)
(println YS)

(defn DBG [s]
    (when DEBUG
        (println s)))

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

(defn spot-available? [field i j]
    (let [i1  (dec i)
          j1  (dec j)]
        (and (field [i1 j1]) (field [i j1]) (field [i1 j])  (field [i j]))))

(defn occupy-spot [field i j]
    (let [i1  (dec i)
          j1  (dec j)]
        (assoc field [i1 j1] false [i j1] false
                     [i1 j]  false [i j]  false)))

; tower takes four squares, i, j is a coord of its center
; prevent building it when not all 4 squares are visible
(defn build-tower [state]
    (let [[ok i j] (state :ghost-tower)
          i1 (dec i)
          j1 (dec j)]
        (if ok
            (-> state
                (update-in [:field]  occupy-spot i j)
                (update-in [:towers] conj [i j]))
            state)))

; random number between low and high, both are included in consideration
(defn rnd [low high]
       (+ low (random (- high low -0.5))))

(def xi
    (memoize
       (fn [x]
           (let [rc (cond (<= x 0) 0
                          (>= x (XS (dec N))) (dec N)
                          :else (some #(when (<= x (nth XS %)) (dec %)) (range (inc N))))]
    	         rc))))
  
(def yi
    (memoize
       (fn [y]
         (cond (<= y 0)      0
               (>= y (YS (dec M))) (dec M)
               :else (some #(when (<= y (nth YS %)) (dec %)) (range M))))))

(def dirref {1  [1 -1]
             2  [1  0]
             3  [1  1]
             4  [0  1]
             5  [-1 1]
             6  [-1 0]
             7  [-1 -1]
             8  [0 -1]})

(defn move-zeek [zeek]
;    (DBG "move-zeek")
    (let [[dx dy] (dirref (zeek :dir))
          i       (+ dx (zeek :i))
          j       (+ dy (zeek :j))]
          (if (and (<= 2 i (- N 2))
                   (<= 2 j (- M 2))  ; need to check field[i][j] = 0
                   )
              (assoc zeek :i i :j j) ; also need field[i][j] = 1
              (assoc zeek :dir (int (rnd 1 8))))))

(defn update-ghost-tower [field]
    (let [i          (xi (mouse-x))
          j          (yi (mouse-y))
          on-screen  (and (< 0 i N) (< 0 j M))
          meets-reqs (spot-available? field i j)]
        [on-screen i, j, meets-reqs]))


(defn update-state [state]
        (let [now (milsec)]
       	    (-> state
                (assoc     :timer       now)
                (assoc     :elapsed     (int (/ (- now (state :stimer)) 1000)))
    	        (update-in [:zeeks]     #(map move-zeek %))
    	        (assoc     :ghost-tower (update-ghost-tower (state :field))))))

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

(defn draw-tower [tower] 
	(let [[i j] tower     ;; i, j - center of tower
              nwx (XS (dec i))
              nwy (YS (dec j))
              cx  (XS i)
              cy  (XS j)]
	    (stroke 0 0 0)
	    (stroke-weight 2)
	    (fill 200 200 200)
	    (rect nwx nwy S S)

	    (stroke-weight 4)
	    (fill 50 50 50)
	    (ellipse cx cy GS GS)

	    (stroke 0 0 0)
	    (stroke-weight 5)
	    (line cx cy (+ nwx 10) (+ nwy 10))
            (text "5" cx (+ cy 20))))
	   
(defn draw-towers [state]
        (doseq [t (state :towers)]
             (draw-tower t)))

(defn draw-grid [state]
        (stroke-weight 1)
        (fill 150 150 150)
        (stroke 150 0 0)
        (doseq [i XS]
	    (text (str i) i (- HEIGHT 15))
            (line i 0 i HEIGHT))
        (doseq [j YS]
	    (text (str j) 15 j)
            (line 0 j WIDTH j)))

(defn draw-ghost-tower [state]
        (println "draw-ghost-tower " (state :ghost-tower))
        (let [[on-screen? i j meets-reqs] (state :ghost-tower)]
            (when on-screen?
                  (if meets-reqs
                      (fill 0  200 0 45)
                      (fill 200  0 0 45))
                  (stroke 0 0 0)
                  (rect (XS (dec i)) (YS (dec j)) S S)
                  (fill 180 180 180 45)
                  (ellipse (XS i) (YS j) S3 S3))))

(defn draw-status [state]
        (fill 255 255 0)
        (text (str "Amount: " (state :amount)
                   "   Timer: " (state :elapsed)
                   "   Lives: " (state :lives)) (XS 1), (YS 1)))

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
;+   drawTowers();
;   if (TIMETOUPDATEZEEK == 0 && TIMER >= 10)
;+       updateZeeks();
;   TIMETOUPDATEZEEK = (TIMETOUPDATEZEEK + 1) % 10;
;+   drawZeeks();
;+   drawStatus();
;+   drawGhostTower();
; }

(defn draw [state]
	(clear)
	(draw-grid state)
	(draw-towers state)
	(draw-zeeks state)
	(draw-status state)
	(draw-ghost-tower state))

(defn mouse-pressed [state event]
;	(DBG event)
	(build-tower state))

(defn setup []
        (frame-rate   30)
        {:tower-types []
         :levels      []
         :amount      100
         :level       1
         :field       (zipmap (for [i (range N) j (range M)] [i j]) (repeat true))
	 :stimer      (milsec)
	 :timer       (milsec)
         :elapsed     0
	 :lives       20
         :ghost-tower [false 0 0]
         :zeeks       (for [_ (range NZEEKS)] {:i 2 :j 10 :dir (int (rnd 1 3))})
         :towers      []})

(defsketch dtd
        :host          "host"
        :size          [WIDTH HEIGHT]
        :setup         setup
        :update        update-state
	:mouse-pressed mouse-pressed
        :key-pressed   key-pressed
        :key-released  key-released
        :draw          draw
        :middleware    [fun-mode])

(defn -main [& args] nil)
