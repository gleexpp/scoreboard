(ns scoreboard.views.admin
  (:use [noir.core]
        [noir.response :only [redirect]]
        [net.cgrand.enlive-html]
        [clojure.tools.logging :only (info error)]
        [scoreboard.views.layout])
  (:require [scoreboard.models.institute :as institute]
            [scoreboard.models.player :as player]
            [scoreboard.models.score :as score]
            [scoreboard.models.user :as person]
            [noir.session :as session]
            [noir.request :as request]))

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
    (if (= (session/get :role) "admin")
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
                      [:.director]
                      (content (:director player)))
           ) [:.table])
      (mark
       (at page
           [:tbody :tr]
           (clone-for [player players]
                      [:.lottery :a]
                      (do->
                       (content (str (:lottery player)))
                       (set-attr :href (if (not (= (session/get :role) "admin"))
                                         (str "/players/" (:lottery player) "/scores/new/1")
                                         (str "/players/" (:lottery player))
                                         )))
                      [:.username]
                      (content (:name player))
                      [:.college]
                      (content (:institute player))
                      [:.director]
                      (content (:director player)))
           ) [:.table]))))

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

         [:#director]
         (set-attr :value (:directed_by pl))

         [:#username]
         (set-attr :value (:name pl))

         [(attr= :value (:category pl))]
         (set-attr :selected true ))
     [:.form-horizontal])))

(defpage "/ranks" []
  (let [page (html-resource "scoreboard/views/_rank.html")
        scores (score/get-institute-final-score)]
    (main
     (at page
         [:#scores]
         (clone-for [score scores]
                    [:#college]
                    (content (get score 0))
                    [:#pr1]
                    (content (str (get score 1)))
                    [:#pr2]
                    (content (str (get score 2)))
                    [:#pr3]
                    (content (str (get score 3)))
                    [:#pr4]
                    (content (str (get score 4)))
                    [:#total]
                    (content (str (get score 5)))
                    [:#rank]
                    (content (get score 6))
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
  (let [page (html-resource "scoreboard/views/_awards.html")
        scores (score/get-total-ranks)]
    (main
     (at page
         [:#awards]
         (clone-for [score scores]
                    [:#username]
                    (content (nth score 0))
                    [:#college ]
                    (content (nth score 1))
                    [:#theory-score]
                    (content (str (nth score 2)))
                    [:#theory-rank]
                    (content (nth score 3))
                    [:#hand-score]
                    (content (str (nth score 4)))
                    [:#hand-rank]
                    (content (nth score 5))
                    [:#heart-score]
                    (content (str (nth score 6)))
                    [:#heart-rank]
                    (content (nth score 7))
                    [:#closed-score]
                    (content (str (nth score 8)))
                    [:#closed-rank]
                    (content (nth score 9))))
     [:.table])))

(defpage "/scores" []
  (let [page (html-resource "scoreboard/views/_scores.html")
        scores (score/get-practice-project-score)]
    (main
     (at page
         [:#scores]
         (clone-for [score scores]
                    [:#lottery]
                    (content (str (nth score 0)))
                    [:#username]
                    (content (nth score 1))
                    [:#hand]
                    (content (str (nth score 2)))
                    [:#heart]
                    (content (str (nth score 3)))
                    [:#closed]
                    (content (str (nth score 4)))))
     [:.table])))

(defpage "/users" []
  (let [page (html-resource "scoreboard/views/_login.html")]
    (main
     page
     [:.form-horizontal])))

(defpage [:post "/users"] {:as user}
  (let [u (person/find-by-name-and-password user)]
    (if (not u)
      (redirect "/users")
      (do
        (session/put! :user u)
        (if (:is_admin u)
          (do
            (session/put! :role "admin")
            (redirect "/players"))
          (redirect "/players"))))))

(pre-route "/*" {}
           (let [uri (:uri (request/ring-request))]
             (if-not (or (= "/users" uri) (.startsWith uri "/assets") (session/get :user))
               (redirect "/users"))))


(defpage "/logout" []
  (do
    (session/remove! :user)
    (session/remove! :role))
  (redirect "/users"))