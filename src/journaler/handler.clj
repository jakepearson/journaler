(ns journaler.handler
  (:require [compojure.core :refer [GET PUT POST DELETE PATCH ANY defroutes routes]]
            [compojure.route :as route]
            [journaler.response :as response]
            [ring.middleware.params :as params]
            [journaler.middleware :as middleware]
            [org.httpkit.server :as httpkit]))

(defroutes journaler-routes
  (GET "*" [] (fn [request] (response/ok "Hello World!"))))

(def handler
  (-> journaler-routes
      middleware/wrap-json-request
      middleware/wrap-json-response
      params/wrap-params
      middleware/wrap-handle-exception))

(defn- configuration []
  {:port   9999
   :thread 20})

(defn- ascii []
  (println "
                                                ________  h___
         __        __      _____       ___     |        | |  L|_
       _/ L\\_    _| L\\_   |    L\\_    _/  L\\_  |        |_|     |
      '-o---o-' '-o---o-' '-O---O-' '=o----o-' '-OO----`OO----O-'
 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
  (println "Journaling on port: " (-> (configuration) :port)))

(defn -main [& args]
  (httpkit/run-server handler (configuration))
  (ascii))
