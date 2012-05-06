(ns scoreboard.models.player
  (:require [clojure.java.jdbc :as sql])
  (:use [scoreboard.models.config]
        [clojure.tools.logging :only (info error)]))

(defn save-player [player]
  (sql/with-connection db
    (sql/insert-records :players
                        {:name (:username player) 
                         :lottery (:seq player)
                         :directed_by (:director player)
                         :institute_id (:college player)})))

(defn find-player [player-seq]
  (sql/with-connection db
    (sql/with-query-results rows
      ["select * from players where lottery = ?" player-seq]
      (first rows))))

(defn all-players []
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.lottery,p.name,i.name as institute,p.directed_by as director from players p, institutes i where p.institute_id = i.id order by p.lottery"]
      (into [] rows))))

(defn update [player]
  (info "college is " (:id player))
  (sql/with-connection db
    (sql/update-values
     :players
     ["lottery=?" (:id player)]
     {:name (:username player) :institute_id (:college player) :directed_by (:director player)})))
