(ns openscad-tryout.core
  (:require [scad-clj.model :as model]
            [scad-clj.scad :as scad]))

(def support-angle (/ model/pi 8/3))
(def laptop-thickness 17)
(def base-length 120)
(def support-length 80)
(def front-support-length 20)
(def base-thickness 8)
(def support-thickness 4)

(def support-distance 
  "Distance of support rods calculated from thickness of laptop"
  (/ (+ laptop-thickness support-thickness) (Math/sin support-angle)))

(def support-offset 
  "Position the front support rod to give a smooth transistion"
  (with-local-vars [dist (/ (- base-thickness support-thickness) 2)]
    [(- 0 (* @dist (Math/sin support-angle))) (* @dist (Math/cos support-angle))]))

(defn base-rod [center-dist base-thickness]
  "Base form of the stand"
  (model/hull
   (model/circle (/ base-thickness 2))
   (model/translate [center-dist 0 0]
                    (model/circle (/ base-thickness 2)))))

(defn arc [rad t a]
  "Draws an arc with length of 0 to π"
  (model/difference
   (model/circle (+ rad (/ t 2)))
   (model/circle (- rad (/ t 2)))
   (model/translate [(- (+ rad (/ t 2))) (- 0 (+ rad t)) 0] 
                    (model/square (+ (* rad 2) t) (+ rad t) :center false ))
   (model/rotate [0 0 support-angle] 
                 (model/translate [(- (+ rad (/ t 2))) 0 0] 
                                  (model/square (+ (* rad 2) t) (+ rad t) :center false )))))

(defn smoother [f & block]
  "Smooths the corners by doing offset both ways."
  (model/offset (- 0 f)
     (model/offset f block)))

(def cutouts
  "Cutouts to give a more airy feel to the stand and save some plastic"
  (model/union
   (model/circle (/ base-thickness 4))
   (model/translate 
    [(/ base-thickness 1.5) 0 0]
    (base-rod (- support-distance (/ base-thickness 0.75) (- 0  (first support-offset)) ) 
              (/ base-thickness 2.5)))
   (model/translate 
    [(- support-distance (- 0 (first support-offset))) (rest support-offset) 0]
    (model/circle (/ base-thickness 5)))
   (model/translate 
    [(+ support-distance (/ base-thickness 1.5)  (first support-offset)) 0 0] 
    (base-rod (- (* support-length 0.8) (/ base-thickness 0.75)) 
              (/ base-thickness 2.5)))
   (model/translate 
    [(+ support-distance (* support-length 0.8) (first support-offset)) (rest support-offset) 0] 
    (model/circle (/ base-thickness 5)))
   (model/translate 
    [(+ support-distance (* support-length 0.8) (/ base-thickness 1.5) (first support-offset) ) 0 0] 
    (base-rod (- (- base-length base-thickness  (first support-offset)) support-distance (* support-length 0.8) (/ base-thickness 1.5))
              (/ base-thickness 2.5)))))

(def stand
  (model/extrude-linear
   {:height 10 :center false}
   (model/difference
    (model/union
     (model/translate support-offset 
      (model/rotate [0 0 support-angle] 
       (base-rod front-support-length support-thickness)))
     (smoother 4
               (base-rod (- base-length base-thickness) base-thickness)
               (model/translate support-offset 
                                (model/translate [support-distance 0 0] 
                                                 (model/rotate [0 0 support-angle] 
                                                               (base-rod support-length support-thickness))))
               (model/translate [support-distance (/ base-thickness 2) 0]
                                (model/translate support-offset
                                                 (arc (* support-length 0.8) (* support-thickness 0.8) support-angle)))))
    cutouts)))

(spit "../scads/stand.scad"
      (str "$fn=300;\n" (scad/write-scad stand)))

