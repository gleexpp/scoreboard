(ns scoreboard.views.layout
  (:use [net.cgrand.enlive-html]))

(defn main [page-content & selector]
  (apply str
         (emit*
          (at (html-resource "scoreboard/views/layout.html")
              [:#main]
              (if selector
                (content (select page-content selector))
                (content page-content))))))

(defn mark [page-content & selector]
  (apply str
         (emit*
          (at (html-resource "scoreboard/views/mark.html")
              [:#main]
              (if selector
                (content (select page-content selector))
                (content page-content))))))