(ns scoreboard.views.eval
  (:use [noir.core]
        [noir.response :only [redirect]]
        [net.cgrand.enlive-html]
        [clojure.tools.logging :only (info error)]
        [scoreboard.views.layout])
  (:require [scoreboard.models.institute :as institute]
            [scoreboard.models.project :as project]
            [scoreboard.models.player :as player]
            [scoreboard.models.score :as score]))

(defpage [:post "/scores"] {:as score}
  (score/save score)
  (redirect "/players"))

(defpage "/players/:seq/scores/new" {:keys [seq]}
  (let [institutes (institute/all)
        quantity (count institutes)
        page (html-resource "scoreboard/views/_eval.html")
        projects (project/all)
        pl (player/find-player seq)]
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

         [:#seq]
         (set-attr :value (:lottery pl))

         [(attr= :value (:institute_id pl))]
         (set-attr :selected true)

         [:#director]
         (set-attr :value (:directed_by pl))

         [:#username]
         (set-attr :value (:name pl)))
     [:.form-horizontal])))
