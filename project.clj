(defproject scoreboard "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.1"]
                           [enlive "1.0.0"]
                           [com.h2database/h2 "1.3.166"]
                           [org.clojure/java.jdbc "0.2.0"]
                           [org.clojure/tools.logging "0.2.3"]]
            :plugins [[lein-swank "1.4.4"]]
            :main scoreboard.server)

