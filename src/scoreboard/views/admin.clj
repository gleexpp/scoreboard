(ns scoreboard.views.admin
  (:use [noir.core]
        [noir.response :only [redirect]]
        [net.cgrand.enlive-html]
        [clojure.tools.logging :only (info error)]
        [scoreboard.views.layout])
  (:require [scoreboard.models.institute :as institute]
            [scoreboard.models.player :as player]))

(defpage "/" []
  (redirect "/player/add"))

(defpage "/players" []
  (let [players (player/all-players)
        page (html-resource "scoreboard/views/_players.html")]
    (main
     (at page
         [:tbody :tr]
         (clone-for [player players]
                    [:.lottery :a]
                    (do->
                     (content (str (:lottery player)))
                     (set-attr :href (str "/players/" (:lottery player)))
                     )
                    [:.username]
                    (content (:name player))
                    [:.college]
                    (content (:institute player))
                    [:.category]
                    (content (:category player)))
         ))))

(defpage [:put "/players/:id"] {:keys [id] :as entity}
  (info entity)
  (player/update entity)
  (redirect (str "/players/" id)))

(defpage "/players/:seq" {:keys [seq]}
  (let [institutes (institute/all)
        quantity (count institutes)
        page (html-resource "scoreboard/views/_player.html")
        pl (player/find-player seq)]
    (main
     (at page
         [:form]
         (set-attr :action (str "/players/" (:lottery pl)))
         
         [:#college :option]
         (clone-for [institute institutes]
                    (do->
                     (content (:name institute))
                     (set-attr :value (:id institute))))

         [(attr= :value (:lottery pl))]
         (set-attr :selected true)

         [(attr= :value (:institute_id pl))]
         (set-attr :selected true)

         [:#seq]
         (set-attr :value (:lottery pl))

         [:#username]
         (set-attr :value (:name pl))

         [(attr= :value (:category pl))]
         (set-attr :selected true ))
     [:.form-horizontal])))

(defpage "/player/add" [] 
  (let [institutes (institute/all)
        quantity (count institutes)
        page (html-resource "scoreboard/views/_user_form.html")]
    (main
     (at page
         [:#college :option]
         (clone-for [i (range quantity)]
                    (do->
                     (content (:name (nth institutes i)))
                     (set-attr :value (:id (nth institutes i)))))

         [:#seq :option]
         (clone-for [i (range 30)]
                    (content (str i))))
     [:.form-horizontal])))

(defpage [:post "/player/add"] {:as player}
  (if player
    (player/save-player player))
  (redirect (str "/players/" (:seq player))))






