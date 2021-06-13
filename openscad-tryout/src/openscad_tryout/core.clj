(ns openscad-tryout.core
  (:require [clojure.data.json :as json]
            [scad-clj.model :as model]
            [scad-clj.scad :as scad]))

(def space 14.07)
(def c-dist 19.05)
(def kb-vec (json/read-json (slurp "input.json")))

(def height (* c-dist (count kb-vec)))

(defn row_width [v]
  (+ (apply + (map  #(get % :x 0) v))
     (count (filter #(= (type %) java.lang.String ) v))))

(def width (* c-dist (apply max (map #(row_width %) kb-vec))))

(def mount-holes
  (with-local-vars [main-hole-d (/ 4.2 2), connector-hole-d (/ 3.2 2), side-hole-d (/ 1.9 2)]    
    (model/with-fn 120
      (model/circle @main-hole-d)
      (model/translate [-2.54 -5.08] (model/circle  @connector-hole-d))
      (model/translate [3.81 -2.54] (model/circle @connector-hole-d))
      (model/translate [-5 0] (model/circle @side-hole-d))
      (model/translate [5 0] (model/circle @side-hole-d)))))


(defn s-row [x row cut holes]
  (def is_hole (= (type (first row)) java.lang.String))
  (def x_mod (if is_hole (+ x c-dist) (get-x-mods (first row))))
  (if (empty? row)
    holes
    (if is_hole
      (recur x_mod (rest row) cut (conj holes (model/translate [x_mod  0] cut)))      
      (recur  (+ x  (*(get (first row) :x) c-dist)) (rest row) cut holes)
      )))

(defn rows [y row cut]
  (model/translate [0 (* (* c-dist y) -1)]
                   (s-row 0 row cut ())))

(defn t-plate [t w h s f]
  (model/extrude-linear {:height t :center false}  
                        (model/difference
                         (model/square (+ w f) (+ h f) :center true )
                         (model/translate  [(- (/ w -2) (/ c-dist 2)) (- (/ h 2) (/ c-dist 2))]
                                           (map-indexed #(rows %1 %2 (top-hole s)) kb-vec)))))

(def mount-plate
  (model/extrude-linear {:height 1.5}  
                        (model/difference
                         (model/square width height )
                         (model/translate  [(+ (/ width -2) (/ c-dist 2)) (- (/ height 2) (/ c-dist 2))]
                                           (map-indexed #(rows %1 %2 mount-holes) kb-vec)))))

(defn top-hole [s]
  (model/square s s))

(def top-plate
  (model/union 
   (t-plate 1.4 width height space 5 )  
   (t-plate 5.0 width height (+ space 3) 1)
   (model/extrude-linear {:height 10 :center false}          
                         (model/difference 
                          (model/square (+ width 2) (+ height 2))
                          (model/square width height)))))

(spit "../scads/top-plate.scad"
      (scad/write-scad top-plate))

(spit "../scads/mount-plate.scad"
      (scad/write-scad mount-plate ))

