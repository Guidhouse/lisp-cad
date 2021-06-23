(ns openscad-tryout.core
  (:require [scad-clj.model :as model]
            [scad-clj.scad :as scad]))


(def length 15.5)
(def width 5)
(def c-dist (- length width))
(def radius (/ width 2))

(defn base [dist rad h]
  (model/extrude-linear {:height h :center false}
                        (model/with-fn 200
                          (model/hull
                           (model/translate [(/ dist 2) 0]
                                            (model/circle rad))
                           (model/translate [(/ dist -2) 0]
                                            (model/circle rad))))))



(def pad
  (model/union
   (model/difference
    (base c-dist radius 1.8)
    (base  c-dist 2.1 0.9))
   (base  c-dist 1.3 0.9)))


(spit "../scads/pads.scad"
      (scad/write-scad pad))
