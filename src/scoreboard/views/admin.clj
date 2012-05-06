(ns scoreboard.views.admin
  (:use [noir.core]
        [noir.response :only [redirect]]
        [net.cgrand.enlive-html]
        [clojure.tools.logging :only (info error)]
        [scoreboard.views.layout])
  (:require [scoreboard.models.institute :as institute]
            [scoreboard.models.player :as player]
            [scoreboard.models.score :as score]
            [noir.session :as session]))

(deftemplate base "scoreboard/views/layout.html"
  [{:keys [body]}]
  [:#main] (content body))

(defsnippet player-form "scoreboard/views/_player.html" [:form]
  [{:keys [data]}]
  [:form] (set-attr :action (str "/players/" (:lottery data)))
  [[:#college :option]] (clone-for [institute (:institutes data)]
                                  (do->
                                   (content (:name institute))
                                   (set-attr :value (:id institute))))
  [[:option (attr= :value (:lottery data))]] (set-attr :selected true)
  [[:option (attr= :value (:institute_id data))]] (set-attr :selected true)
  [:#seq] (set-attr :value (:lottery data))
  [:#username] (set-attr :value (:name data)))


(defpage "/" []
  (redirect "/players"))

(defpage "/players/new" []
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
         (clone-for [i (range 1 21)]
                    (content (str i))))
     [:.form-horizontal])))

(defpage [:post "/players"] {:as player}
  (player/save-player player)
  (redirect "/players"))

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
                     (set-attr :href (if (not (= (session/get :role) "admin"))
                                       (str "/players/" (:lottery player) "/scores/new")
                                       (str "/players/" (:lottery player))
                                       )))
                    [:.username]
                    (content (:name player))
                    [:.college]
                    (content (:institute player))
                    [:.category]
                    (content (:category player)))
         ) [:.table])))

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

(defpage "/ranks" []
  (let [page (html-resource "scoreboard/views/_rank.html")
        scores (score/rank)]
    (main
     (at page
         [:#scores]
         (clone-for [score scores]
                    [:#college]
                    (content (get score 0))
                    [:#pr1]
                    (if (> (count score) 2)
                      (content (get score 1))
                      (content "0"))
                    [:#pr2]
                    (if (> (count score) 3)
                      (content (get score 2))
                      (content "0"))
                    [:#pr3]
                    (if (> (count score) 4)
                      (content (nth score 3))
                      (content "0"))
                    [:#pr4]
                    (if (> (count score) 5)
                      (content (nth score 4))
                      (content "0"))
                    [:#total]
                    (content (get score (- (count score) 2)))
                    [:#rank]
                    (content (last score))
                    ))
     [:.table])))

(defpage "/ranks/players" []
  (let [page (html-resource "scoreboard/views/_prank.html")
        scores (score/get-player-rank)
        ]
    (main
     (at page
         [:#scores]
         (clone-for [score scores]
                    [:#username]
                    (content (get score 0))
                    [:#college]
                    (content (get score 1))
                    [:#pr1]
                    (if (> (count score) 3)
                      (content (str (get score 2)))
                      (content "0"))
                    [:#pr2]
                    (if (> (count score) 4)
                      (content (str (get score 3)))
                      (content "0"))
                    [:#pr3]
                    (if (> (count score) 5)
                      (content (str (get score 4)))
                      (content "0"))
                    [:#pr4]
                    (if (> (count score) 6)
                      (content (str (get score 5)))
                      (content "0"))
                    [:#total]
                    (content (str (get score (- (count score) 2))))
                    [:#rank]
                    (content (last score))
                    ))
     [:.table])))

(defpage "/awards" []
  (let [page (html-resource "scoreboard/views/_awards.html")]
    (main page [:.table])))

(defpage "/scores" []
  (let [page (html-resource "scoreboard/views/_scores.html")]
    (main page [:.table])))



