(ns openscad-tryout.core
  (:use [scad-clj.scad])
  (:use [scad-clj.model])
  )

(def space 14.07)
(def c-dist 19.05)
(def kb-vec ["",""])

(def width (* c-dist (count kb-vec)))

(def plater 
  (union
   (plate 1.4 width space 5)  
   (plate 6.4 width (+ space 3 ) 0)
   (extrude-linear {:height 10 :center false}
                   (difference 
                    (square (+ width 2) (+ c-dist 2))
                    (square width  c-dist)))))


(def mount-plate 
  (extrude-linear {:height 1.5}
                  (difference 
                   (translate [(/ c-dist -2) (/ c-dist -2)] 
                              (square width c-dist :center false ))
                   (map-indexed #(holes %1 %2) kb-vec))))


(defn plate [h w s f]
  (extrude-linear {:height h :center false}
                  (difference 
                   (square (+ w f) (+ c-dist f) :center true )
                   (translate [(- (/ w -2) (/ c-dist -2)) 0 ] 
                              (for [x (range (count kb-vec))]
                                (translate [(* c-dist x) 0 ] 
                                           (square s s :center true)))))))


(defn holes [a b]
  (with-local-vars [main-hole-d (/ 4.2 2), connector-hole-d (/ 3.2 2), side-hole-d (/ 1.9 2)]
    (translate [(* c-dist a) 0 ]    
               (with-fn 120
                 (difference (circle @main-hole-d  )
                             (text b :halign "center" :valign "center" :size 3))  
                 (translate [-2.54 -5.08] (circle  @connector-hole-d))
                 (translate [3.81 -2.54] (circle @connector-hole-d))
                 (translate [-5 0] (circle @side-hole-d))
                 (translate [5 0] (circle @side-hole-d))))))

(def cap 
  (full-cap 9 6 round-cap-form)
  )

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


(spit "../../post-demo.scad"
      (write-scad cap ))
