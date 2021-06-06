(ns openscad-tryout.core
  (:require [clojure.data.json :as json]
            [scad-clj.model :as model]
            [scad-clj.scad :as scad]))


(def space 14.07)
(def c-dist 19.05)
(def kb-vec [["","", ""],["",""],["","",""]])

(def height (* c-dist (count kb-vec)))
(def width (* c-dist (apply max (map #(count %) kb-vec))))


(defn mount-holes [t]
  (with-local-vars [main-hole-d (/ 4.2 2), connector-hole-d (/ 3.2 2), side-hole-d (/ 1.9 2)]    
    (model/with-fn 120
      (model/circle @main-hole-d)
      (model/translate [-2.54 -5.08] (model/circle  @connector-hole-d))
      (model/translate [3.81 -2.54] (model/circle @connector-hole-d))
      (model/translate [-5 0] (model/circle @side-hole-d))
      (model/translate [5 0] (model/circle @side-hole-d)))))

(defn mount-rows [i row cut]
  (model/translate [0 (* (* c-dist i) -1)]
             (map-indexed #(model/translate [(* c-dist %1 ) 0] (cut %2)) row)))



(def mount-plate
  (model/extrude-linear {:height 1.5}  
                  (model/difference
                   (model/square width height )
                   (model/translate  [(+ (/ width -2) (/ c-dist 2)) (- (/ height 2) (/ c-dist 2))]
                               (map-indexed #(mount-rows %1 %2 mount-holes) kb-vec)))))

(def hole 
  (model/square 12 12))

(defn top-rows [i row method]
  (model/translate [0 (* (* c-dist i) -1)]
             (map-indexed #(model/translate [(* c-dist %1 ) 0] (method)) row)))


(defn t-plate [t w h frame s]
  (model/extrude-linear {:height t :center false}
                  (model/difference 
                   (model/square (+ w frame) (+ h frame))
                   (model/translate  [(+ (/ w -2) (/ c-dist 2)) (- (/ h 2) (/ c-dist 2))] 
                               (map-indexed #(top-rows %1 %1 hole) kb-vec)))))

(defn plate [t w h s f]
  (model/extrude-linear {:height t :center false}
                  (model/difference 
                   (model/square (+ w f) (+ h f) :center true )
                   (model/translate [(- (/ w -2) (/ c-dist -2)) (- (/ h 2) (/ c-dist 2) ) ] 
                              (for [x (range (count kb-vec))]
                                (model/translate [0 (* (* c-dist x) -1) ]
                                           (for [y (range (count (get kb-vec x)))] 
                                             (model/translate [ (* (* c-dist y) 1) 0]
                                                        (model/square s s :center true)))))))))

(def top-plate
  (model/union 
   (plate 1.4 width height space 5)  
   (plate 5.0 width height (+ space 3) 1)
   (model/extrude-linear {:height 10 :center false}          
                   (model/difference 
                    (model/square (+ width 2) (+ height 2))
                    (model/square width height)))))


(spit "../scads/top-plate.scad"
      (scad/write-scad top-plate))

(spit "../scads/mount-plate.scad"
       (scad/write-scad mount-plate ))
 
