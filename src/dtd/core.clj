; To run from REPL:
; (use 'dtd.core :reload-all)
;
(ns dtd.core
  (:use [quil.core]
        [quil.middleware]))

;; (millis) has a bug - workaround the null pointer exception
(defn milsec []
    (System/currentTimeMillis))

(def S         96)      ;; divisible by 2, 4, 6, 8
(def HS        (/ S 2))
(def QS        (/ S 4))
(def GS        (* 6 (/ S 8)))
(def S3        (* 3 S))

(def N         20)
(def M         18)
(def N-1       (dec N))
(def M-1       (dec M))
(def N+1       (inc N))
(def M+1       (inc M))
(def WIDTH     (* HS N))
(def HEIGHT    (* HS M))

(def XS        (vec (for [i (range N+1)] (* i HS))))
(def YS        (vec (for [i (range N+1)] (* i HS))))

(def MAXTOWERS 60)
(def NZEEKS    3)
(def TIMETOUPDATEZEEK 0)
(def DEBUG     true)

(println XS)
(println YS)

(defn DBG [s]
    (when DEBUG
        (println s)))

(defn spot-avail? [field i j]
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
    (let [[on-screen i, j, meets-reqs] (state :ghost-tower)]
        (if (and on-screen meets-reqs)
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
                          (>= x (XS N-1)) N-1
                          :else (some #(when (<= x (nth XS %)) (dec %)) (range N+1)))]
    	         rc))))
  
(def yi
    (memoize
       (fn [y]
         (cond (<= y 0)      0
               (>= y (YS M-1)) M-1
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
          meets-reqs (spot-avail? field i j)]
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
    (let [field (state :field)]
        (stroke-weight 1)
        (fill 0 0 0)
        (stroke 50 0 0)
        (doseq [i (range N+1)]
            (let [x (XS i)]
   	        (text (str i) x (- HEIGHT 15))
                (line x 0 x HEIGHT)))
        (doseq [j (range M+1)]
            (let [y (YS j)]
	        (text (str j) 15 y)
                (line 0 y WIDTH y)))
        (stroke 150 0 0)
        (stroke-weight 3)
	(doseq [i (range N) j (range M)]
	    (if (not (field [i j]))
		(line (XS i) (YS j) (XS (inc i)) (YS (inc j)))))))

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

(defn setup-field []
    (let [m (zipmap (for [i (range N) j (range M)] [i j]) (repeat true))]
        (reduce #(assoc %1 %2 false)
                m
                (concat 
                        (for [i (range N)] [i 0])
                        (for [i (range N)] [i M-1])
                        (for [j (range M)] [0 j])
                        (for [j (range M)] [N-1 j])))))

(defn setup []
        (frame-rate   30)
        {:tower-types []
         :levels      []
         :amount      100
         :level       1
         :field       (setup-field)
	 :stimer      (milsec)
	 :timer       (milsec)
         :elapsed     0
	 :lives       20
         :ghost-tower [false 0 0 false] ; [on-screen i, j, meets-reqs]
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
