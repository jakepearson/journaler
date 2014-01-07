(ns journaler.handler
  (:require [compojure.core :refer [GET PUT POST DELETE PATCH ANY defroutes routes]]
            [compojure.route :as route]
            [journaler.response :as response]
            [ring.middleware.params :as params]
            [journaler.middleware :as middleware]
            [org.httpkit.server :as httpkit]
            [journaler.handlers.pages :as pages]))

(defroutes journaler-routes
  (GET "/" [] pages/home)

  (route/resources "/static")
  (GET "*" [] (fn [request] (response/not-found "Aw fiddlesticks"))))

(def handler
  (-> journaler-routes
      middleware/wrap-json-request
      middleware/wrap-json-response
      params/wrap-params
      middleware/wrap-handle-exception))

(defn- configuration [[port]]
  {:port   (if port (Long. port) 9999)
   :thread 20})

(defn- ascii [options]
  (println "
                                                ________  h___
         __        __      _____       ___     |        | |  L|_
       _/ L\\_    _| L\\_   |    L\\_    _/  L\\_  |        |_|     |
      '-o---o-' '-o---o-' '-O---O-' '=o----o-' '-OO----`OO----O-'
 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
  (println "Journaling on port: " (:port options)))

(defn -main [& args]
  (let [options (configuration args)]
    (httpkit/run-server handler options)
    (ascii options)))
