(ns scoreboard.models.score
  (:require [clojure.java.jdbc :as sql]
            [scoreboard.models.institute :as institute]
            [scoreboard.models.project :as project]
            [scoreboard.models.player :as player]
            )
  (:use [scoreboard.models.config]))

(defn find-all []
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.name as player,i.name as institute,pr.name as project,s.score from players p, institutes i, projects pr, scores s
where s.player_id = p.lottery
and s.project_id = pr.id
and p.institute_id = i.id order by p.lottery"] (into [] rows))))

(defn save [score]
  (sql/with-connection db
    (sql/insert-records
     :scores
     {:player_id (:seq score) :project_id (:project score) :score (:score score)})))

(defn get-single-total []
  (let [sql "select sum(s.score) total_score,i.name as institute,pr.name as project,pr.id as project_id
from scores s, players p, projects pr,institutes i 
where s.player_id = p.lottery 
and s.project_id = pr.id 
and p.institute_id = i.id group by i.name,pr.name order by pr.id"]
    (sql/with-connection db
      (sql/with-query-results rows
        [sql] (into [] rows)))))

(defn get-total []
  (let [sql "select sum(s.score) as total_score,i.name as institute
from scores s, players p, projects pr,institutes i 
where s.player_id = p.lottery 
and s.project_id = pr.id 
and p.institute_id = i.id group by i.name order by total_score desc"]
    (sql/with-connection db
      (sql/with-query-results rows
        [sql] (into [] rows)))))

(defn get-score-of-an-institute [scores institute]
  (let [results (filter #(= institute (:institute %)) scores)]
    (vec (for [result results] (str (:total_score result))))))

(defn rank []
  (let [single-total (get-single-total)
        total (get-total)]
    (for [i (range (count total))]
      (into (into (into [(:institute (get total i))]
                        (get-score-of-an-institute single-total (:institute (get total i))))
                  (get-score-of-an-institute total (:institute (get total i)))) [(str (inc i))]))))

(defn get-score-of-player [player]
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.name as player,i.name as institute,pr.name as project,s.score from players p, institutes i, projects pr, scores s
where s.player_id = p.lottery
and s.project_id = pr.id
and p.institute_id = i.id and p.lottery = ?" player]
      (into [] rows))))

(defn get-theory-score [player]
  (let [scores (get-score-of-player player)
        ts (filter #(= "临床案例分析" (:project %)) scores)
        ts1 (for [t ts] (:score t))]
    (if (zero? (count ts1))
      0
      (/ (reduce + ts1) (count ts1)))))


(defn get-practice-score-for-project [player project]
  (let [scores (get-score-of-player player)
        ts (filter #(= (:name project) (:project %)) scores)]
    (if (zero? (count ts))
      0
      (with-precision 2 (/ (reduce + (for [t ts] (:score t))) (count ts))))))

(defn get-practice-score [player]
  (let [projects (filter #(not (= "临床案例分析" (:name %))) (project/all))
        scores (for [project projects] (get-practice-score-for-project player project))]
    (if (zero? (count scores))
      [0 0 0 0]
      (vec scores))))

(defn get-player-final-scores []
  (let [players (player/all-players)
        projects (project/all)]
    (for [player players]
      (let [theory-score (list (get-theory-score (:lottery player)))
            practice-score (get-practice-score (:lottery player))
            ]
        (into (into (into [(:name player) (:institute player)]
                          (vec theory-score))
                    (vec practice-score))
              (vec (list (+ (* 0.2 (nth theory-score 0))
                            (with-precision 2
                              (* 0.8
                                 (/ (reduce + practice-score) (count practice-score))))))))))))

(defn get-player-rank []
  (let [scores (get-player-final-scores)
        sorted (sort-by last > scores)]
    (for [i (range (count sorted))]
      (conj (nth sorted i) (str (inc i))))))