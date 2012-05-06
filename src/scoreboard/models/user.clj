(ns scoreboard.models.user
  (:require [clojure.java.jdbc :as sql]
            [scoreboard.models.institute :as institute]
            [scoreboard.models.project :as project]
            [scoreboard.models.player :as player])
  (:use [scoreboard.models.config]))

(defn find-by-name-and-password [{:keys [username password]}]
  (sql/with-connection db
    (sql/with-query-results rows
      ["select * from users where name = ? and password = ?" username password]
      (first rows))))



