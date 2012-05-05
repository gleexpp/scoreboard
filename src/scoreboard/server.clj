(ns scoreboard.server
  (:require [noir.server :as server])
  (:import (org.h2.tools Server)))

(server/load-views "src/scoreboard/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))
        db (. Server createTcpServer (seq ""))]
    (do
      (.start db)
      (server/start port {:mode mode
                          :ns 'scoreboard}))))

