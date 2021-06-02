(ns openscad-tryout.core
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  )

(def space 14.07)
(def c-dist 19.05)
(def kb-vec [["","",""],["","S"],["","",""]])

(def height (* c-dist (count kb-vec)))
(def width (* c-dist (apply max (map #(count %) kb-vec))))


(defn mount-holes [t]
  (with-local-vars [main-hole-d (/ 4.2 2), connector-hole-d (/ 3.2 2), side-hole-d (/ 1.9 2)]    
    (with-fn 120
      (circle @main-hole-d)
      (translate [-2.54 -5.08] (circle  @connector-hole-d))
      (translate [3.81 -2.54] (circle @connector-hole-d))
      (translate [-5 0] (circle @side-hole-d))
      (translate [5 0] (circle @side-hole-d)))))

(defn mount-rows [i row cut]
  (translate [0 (* (* c-dist i) -1)]
             (map-indexed #(translate [(* c-dist %1 ) 0] (cut %2)) row)))


(def mount-plate 
  (extrude-linear {:height 1.5}  
                  (difference
                   (square width height )
                   (translate  [(+ (/ width -2) (/ c-dist 2)) (- (/ height 2) (/ c-dist 2))]
                               (map-indexed #(mount-rows %1 %2 mount-holes) kb-vec)))))


(def hole 
  (square 12 12))

(defn top-rows [i row method]
  (translate [0 (* (* c-dist i) -1)]
             (map-indexed #(translate [(* c-dist %1 ) 0] (method)) row)))


(defn t-plate [t w h frame s]
  (extrude-linear {:height t :center false}
                  (difference 
                   (square (+ w frame) (+ h frame))
                   (translate  [(+ (/ w -2) (/ c-dist 2)) (- (/ h 2) (/ c-dist 2))] 
                               (map-indexed #(top-rows %1 %1 hole) kb-vec)))))

(defn plate [t w h s f]
  (extrude-linear {:height t :center false}
                  (difference 
                   (square (+ w f) (+ h f) :center true )
                   (translate [(- (/ w -2) (/ c-dist -2)) (- (/ h 2) (/ c-dist 2) ) ] 
                              (for [x (range (count kb-vec))]
                                (translate [0 (* (* c-dist x) -1) ]
                                           (for [y (range (count (get kb-vec x)))] 
                                             (translate [ (* (* c-dist y) 1) 0]
                                                        (square s s :center true)))))))))

(def top-plate
  (union 
   (plate 1.4 width height space 5)  
   (plate 5.0 width height (+ space 3) 1)
   (extrude-linear {:height 10 :center false}          
                   (difference 
                    (square (+ width 2) (+ height 2))
                    (square width height)))))

(defn full-cap [r h base-form]
  (with-fn 120
    (difference
     (base-form r h)
     (translate [0 0 (/ r -10)]
                (base-form (- r (/ r 10)) h )))
    (difference 
     (cylinder  2.75  (- h 1.3) :center false)
     (cube 4.1 1.4 (- h 0.4)) 
     (cube 1.4 4.1 (- h 0.4)))
    (for [i (range 0 4)]
      (rotate [0 0 (* i 300)] 
              (translate [5 0 (- h 1.8)] 
                         (cube 6 0.6 1)))
      )))

(defn round-cap-form [r h]
  (difference
   (extrude-linear  {:height h :center false :scale 0.93 }
                    (circle  r ))
   (translate [0 0 (* r 4)] 
              (with-fn 200 
                (sphere (* r 3.44))))))

(defn square-cap-form [r h]
  (with-local-vars [w (- (* r 2) 4 )]
    (with-fn 200    (difference
                     (extrude-linear  {:height h :center false :scale 0.93 :fn 200}
                                      (offset   2 (square  @w @w ))) 
                     (translate [0 0 (* r 5)] 
                                (sphere (* r 4.47)))))))

(def cap 
  (full-cap 9 6 round-cap-form)
  )

(spit "../scads/top-plate.scad"
      (write-scad top-plate ))

(spit "../scads/mount-plate.scad"
       (write-scad mount-plate ))
 
