(ns openscad-tryout.core
  (:require [scad-clj.model :as model]
            [scad-clj.scad :as scad]))


(def length 15.5)
(def width 5)
(def c-dist (- length width))
(def radius (/ width 2))

(defn base [dist rad h]
  (model/extrude-linear {:height h :center false}
                        (model/hull
                         (model/translate [(/ dist 2) 0]
                                          (model/circle rad))
                         (model/translate [(/ dist -2) 0]
                                          (model/circle rad)))))

(defn intersector [dist rad]
  (with-local-vars [sf 2]
    (model/hull
     (model/translate [(/ dist 2) 0]
                      (model/scale [1.8 1.8 1]
                                   (model/sphere rad)))
     (model/translate [(/ dist -2) 0]
                      (model/scale [1.8 1.8 1]
                                   (model/sphere rad))))))


(def pad
  (model/with-fn 150
    (model/intersection
     (model/union
      (model/difference
       (base c-dist radius 1.8)
       (base  c-dist 2.1 0.9))
      (base  c-dist 1.2 1))
     (intersector c-dist 1.8))
))


(spit "../scads/pads.scad"
      (scad/write-scad pad))
