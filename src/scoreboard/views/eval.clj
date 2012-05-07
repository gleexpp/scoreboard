(ns scoreboard.views.eval
  (:use [noir.core]
        [noir.response :only [redirect]]
        [net.cgrand.enlive-html]
        [clojure.tools.logging :only (info error)]
        [scoreboard.views.layout])
  (:require [scoreboard.models.institute :as institute]
            [scoreboard.models.project :as project]
            [scoreboard.models.player :as player]
            [scoreboard.models.score :as score]
            [noir.session :as session]))

(defpage [:post "/scores"] {:as score}
  (score/save (assoc score :referee_id (:id (session/get :user))))
  (redirect "/players"))

(defpage "/players/:seq/scores/new/:pid" {:keys [seq pid]}
  (let [institutes (institute/all)
        quantity (count institutes)
        page (html-resource "scoreboard/views/_eval.html")
        projects (project/all)
        pl (player/find-player seq)
        scores (score/find-project-score-by-referee seq pid (:id (session/get :user)))
        match-score (filter #(= (Integer/valueOf pid) (:project_id %)) scores)
        pl-score (if (zero? (count match-score))
                   0
                   (:score (nth match-score 0)))]
    (mark
     (at page
         [:#college :option]
         (clone-for [institute institutes]
                    (do->
                     (content (:name institute))
                     (set-attr :value (:id institute))))

         [:#project :option]
         (clone-for [project projects]
                    (do->
                        (content (:name project))
                        (set-attr :value (:id project))))

         [[:#project (attr= :value pid)]]
         (set-attr :selected true)

         [:#seq]
         (set-attr :value (:lottery pl))

         [(attr= :value (:institute_id pl))]
         (set-attr :selected true)

         [:#director]
         (set-attr :value (:directed_by pl))

         [:#score]
         (set-attr :value (str pl-score))

         [:#username]
         (set-attr :value (:name pl)))
     [:.form-horizontal])))
