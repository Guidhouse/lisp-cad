(ns openscad-tryout.core
  (:require [openscad-tryout.keyboard :as kb]))


(defn main [] 
  (spit "../scads/top-plate.scad"
        (scad/write-scad kb/top-plate))

  (spit "../scads/mount-plate.scad"
        (scad/write-scad kb/mount-plate ))
  )

