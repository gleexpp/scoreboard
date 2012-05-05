(ns scoreboard.models.player
  (:require [clojure.java.jdbc :as sql])
  (:use [scoreboard.models.config]
        [clojure.tools.logging :only (info error)]))

(defn save-player [player]
  (sql/with-connection db
    (sql/insert-records :players
                        {:name (:username player) :category (:category player)
                         :lottery (:seq player)
                         :institute_id (:college player)})))

(defn find-player [player-seq]
  (sql/with-connection db
    (sql/with-query-results rows
      ["select * from players where lottery = ?" player-seq]
      (first rows))))

(defn all-players []
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.lottery,p.name,p.category,i.name as institute from players p, institutes i where p.institute_id = i.id order by p.lottery"]
      (into [] rows))))

(defn update [player]
  (info "college is " (:id player))
  (sql/with-connection db
    (sql/update-values
     :players
     ["lottery=?" (:id player)]
     {:name (:username player) :institute_id (:college player) :category (:category player)})))
