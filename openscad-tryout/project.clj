(defproject openscad-tryout "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [scad-clj "0.5.3"]
                 [org.clojure/data.json "2.3.1"]]
  :main ^:skip-aot openscad-tryout.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
