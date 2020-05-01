(ns dtd.core
  (:use [quil.core]
        [quil.middleware]))

(defn rnd [low high]    (+ low (rand (- high low))))

(defn rnd-coord [size] [(rnd (- size) size), (rnd (- size) size)])

(defn translate2 [a b] (map + a b))

(defn render-tower [tower]
        (let [s  (:size tower)
              -s (- s)
              h  (/ s 2)
              g  (* s 0.6)]
              (stroke 255 0 0) ; red
              (fill 150) ; grey
              (rect 0 0 s s)
              (fill 50)  ; black
              (ellipse h h g g)
              (line 0 0    s  s)
              (line 0 s s  0)
              (fill 255)
              (text "NW" 0 0)
              (text "NE" s 0)
              (text "SE" s s)
              (text "SW" 0 s)))

(defn render-amount [amount]
         (text (str amount) 0 10))

(defn create-nata []
        {:pos       (rnd-coord 1000)
         :size      (+ 1.0 (rand 3.0))
         :dir       (rand TWO-PI)
         :color     [(rnd 0 255) (rnd 50 150) (rnd 50 150)]
         :z         (rnd 0.2 0.7)
         :render    #(let [size (:size %1)]
                           (fill 255)
                           (rect 0 0 size size))})

(defn create-tower []
        {:pos        (rnd-coord 1000)
         :level      1
         :dir        (rand TWO-PI)
         :size       60
         :color      [(rnd 0 255) (rnd 50 150) (rnd 50 150)]
         :z          1.0
         :render     render-tower})

(defn create-amount []
        {:pos        [10 20]
         :z          1.0
	 :amount     20
         :render     render-amount})

(defn setup []
;        (ellipse-mode :center)
        (frame-rate 30)
        {:tower-types []
         :levels      []
         :amount      (create-amount)
         :level       1
         :natas       (for [_ (range 300)] (create-nata))
         :towers      (for [_ (range 50)]  (create-tower))})

(defn update-state [state] state)
(defn key-pressed [state event] state)
(defn key-released [state event] state)

(defn on-screen? [x y]
    (and (<= -100  x (+ 100 (width)))
         (<= -100  y (+ 100 (height)))))

(defn draw-entity [entity [camx camy]]
  (let [[x y]    (:pos entity)
        z        (:z entity)
        f        (:render entity)]
        (when (on-screen? x y)
              (push-matrix)
              (translate x y)
              (f entity)
              (pop-matrix))))

(defn draw-state [state]
        (background 20 40 15.0)
        (no-stroke)
        (let [pos [100 200]
              ; cam (translate2 pos [(* -0.5 (width)) (* -0.5 (height))])
              cam pos]
              (doseq [n  (:natas state)] (draw-entity n cam))
              (doseq [t (:towers state)] (draw-entity t cam))
              (draw-entity (:amount state) cam)))

(defsketch dtd
        :host         "host"
        :size         [500 500]
        :setup        setup
        :update       update-state
        :key-pressed  key-pressed
        :key-released key-released
        :draw         draw-state
        :middleware   [fun-mode])

(defn -main [& args] nil)
