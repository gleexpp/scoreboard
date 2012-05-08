(ns scoreboard.models.score
  (:require [clojure.java.jdbc :as sql]
            [scoreboard.models.institute :as institute]
            [scoreboard.models.project :as project]
            [scoreboard.models.player :as player])
  (:use [scoreboard.models.config]
        [clojure.tools.logging :only (info error)]))
(defn find-all []
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.name as player,i.name as institute,pr.name as project,s.score from players p, institutes i, projects pr, scores s
where s.player_id = p.lottery
and s.project_id = pr.id
and p.institute_id = i.id order by p.lottery"] (into [] rows))))

(defn find-project-score-by-referee
  "根据选手号，项目id和评委id获得某项比赛的成绩"
  [player project referee]
  (sql/with-connection db
    (sql/with-query-results rows
      ["select project_id,score from scores where player_id = ? and project_id = ? and referee_id = ?" player project referee]
      (into [] rows))))

(defn save [score]
  (info "referee is is " (:referee_id score))
  (sql/with-connection db
    (sql/update-or-insert-values
     :scores
     ["referee_id=? and player_id=? and project_id=?"
      (:referee_id score) (:seq score) (:project score)]
     {:player_id (:seq score) :project_id (:project score) :score (:score score)
      :referee_id (:referee_id score)})))

(defn get-single-total []
  (let [sql "select sum(s.score) as total_score,i.name as institute,pr.name as project,pr.id as project_id
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
      0M
      (/ (reduce + ts1) (count ts1)))))


(defn get-practice-score-for-project [player project]
  (let [scores (get-score-of-player player)
        ts (filter #(= (:name project) (:project %)) scores)]
    (if (zero? (count ts))
      0
      (with-precision 5 (/ (reduce + (for [t ts] (:score t))) (count ts))))))

(defn get-practice-score [player]
  (let [projects (filter #(not (= "临床案例分析" (:name %))) (project/all))
        scores (for [project projects] (get-practice-score-for-project player project))]
    (if (zero? (count scores))
      [0M 0M 0M 0M]
      (vec scores))))

(defn get-player-final-scores []
  (let [players (player/all-players)
        projects (project/all)]
    (for [player players]
      (let [theory-score (list (get-theory-score (:lottery player)))
            practice-score (get-practice-score (:lottery player))
            ]
        (into (into (into [(:lottery player) (:name player) (:institute player)]
                          (vec theory-score))
                    (vec practice-score))
              (vec (list (+ (with-precision 5
                              (* 0.2M (nth theory-score 0)))
                            (with-precision 5
                              (* 0.8M
                                 (with-precision 5
                                   (/ (reduce + practice-score) (count practice-score)))))))))))))

(defn get-player-rank []
  (let [scores (get-player-final-scores)
        sorted (sort-by last > scores)]
    (for [i (range (count sorted))]
      (conj (nth sorted i) (str (inc i))))))

(defn sum-of-nth [n values]
  (reduce + (map #(nth % n) values)))

(defn get-single-project-total [college pr]
  (let [scores (get-player-final-scores)
        college-total (filter #(= college (nth % 1)) scores)]
    (sum-of-nth pr college-total)))

(defn- vec-str [value]
  (vec (str value)))

(defn rank []
  (let [institutes (institute/all)]
    (for [institute institutes]
      (into (into (into (into (into [institute]
                                    (vec-str (get-single-project-total institute 2)))
                              (vec-str (get-single-project-total institute 3)))
                        (vec-str (get-single-project-total institute 4)))
                  (vec-str (get-single-project-total institute 5)))
            (vec-str (get-single-project-total institute 6))))))

(defn- select-practice-scores []
  (sql/with-connection db
    (sql/with-query-results rows
      ["select p.lottery,p.name as player,pr.name as project, s.score from players p, scores s, projects pr
where p.lottery = s.player_id
and s.project_id = pr.id
group by pr.name,p.lottery,s.score order by pr.id,p.lottery"] (into [] rows))))

(defn get-practice-project-score []
  (let [players (player/all-players)
        projects (filter #(not (= "临床案例分析" (:name %))) (project/all))
        scores (select-practice-scores)]
    (for [player players]
      (let [player-scores (filter #(= (:lottery player) (:lottery %)) scores)
            result [(:lottery player) (:name player)]]
        (into result (for [project projects]
                       (let [score (filter #(= (:name project) (:project %)) player-scores)]
                         (if (seq score) (:score (nth score 0)) 0))))))))

(defn get-final-project-scores []
  (let [sql "(select p.lottery,p.name,i.name as institute,pr.name as project,score from
 (select player_id,project_id,sum(s.score) / count(s.score) as score from scores s group by project_id,player_id), 
 players p,projects pr,institutes i
 where p.lottery = player_id and project_id = pr.id and p.institute_id = i.id)"]
    (sql/with-connection db
      (sql/with-query-results rows
        [sql]
        (into [] rows)))))

(defn get-final-rank []
  (let [players (player/all-players)
        projects (project/all)
        scores (get-final-project-scores)]
    (for [player players]
      (let [player-scores (filter #(= (:lottery player) (:lottery %)) scores)
            result [(:name player) (:institute player)]]
        (into
         (into result (for [project projects]
                       (let [score (filter #(= (:name project) (:project %)) player-scores)]
                         (if (seq score) (:score (nth score 0)) 0))))
         [(str (:lottery player))])))))

(defn do-internal-sort [scores pos]
  (let [sorted (vec (sort-by #(nth % pos) > scores))]
    (for [i (range (count sorted))]
      (into (into (into []
                        (subvec (nth sorted i) 0 (inc pos)))
                  [(str (inc i))])
            (subvec (nth sorted i) (inc pos))))))

(defn get-total-ranks []
  (let [scores (get-final-rank)]
    (do-internal-sort (do-internal-sort (do-internal-sort (do-internal-sort scores 2)
                                                          4)
                                        6)
                      8)))

(defn sum-of-vecs [[a b c d]]
  (map + a b c d))

;(defn get-institute-final-scores []
;  (let [scores (get-player-final-scores)
;        college-scores (group-by #(nth % 2) scores)
;        colleges (filter #(not (empty? (.trim %))) (keys college-scores))]
;   (for [i (range (count colleges))]
;      (into [(nth colleges i)] (sum-of-vecs ;;TODO 预期[[] [] [] []]的形式，如不满足则结果为nil
;                               (map #(subvec % 3) (college-scores (nth colleges i))))))))

(defn get-institute-final-scores []
  (let [scores (get-player-final-scores)
        college-scores (group-by #(nth % 2) scores)
        colleges (filter #(not (empty? (.trim %))) (keys college-scores))]
        (for [college colleges]
          (into [college]
                (reduce #(map + %1 %2) (map #(subvec % 3) (college-scores college)))))))

(defn get-institute-final-rank []
  (let [scores (get-institute-final-scores)
        sorted (sort-by #(nth % 5) > scores)]
    (for [i (range (count sorted))]
      (into (nth sorted i) [(str (inc i))]))))