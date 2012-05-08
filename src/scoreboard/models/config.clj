(ns scoreboard.models.config
  (:require [clojure.java.jdbc :as sql]))
  

(def db
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :user        "scoreboard"
   :password    "scoreboard"
   :subname     "tcp://localhost/~/scoreboard"})
