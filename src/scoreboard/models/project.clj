(ns scoreboard.models.project
  (:require [clojure.java.jdbc :as sql])
  (:use [scoreboard.models.config]))

(defn all []
  (sql/with-connection db
    (sql/with-query-results rows ["select * from projects group by name order by id"] (into [] rows))))