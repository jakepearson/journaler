(ns journaler.handlers.incoming-test
  (:require [clojure.test :refer :all]
            [journaler.utils :as utils]
            [journaler.handlers.incoming :as incoming]))

(def ^:private twitter-data "{\"user\":\"jake\",\"pass\":\"password\",\"title\":\"twitter☃My cat's breath smells like cat food.☃http://twitter.com/status/123\",\"categories\":[\"http://twitter.com/eddnerd/status/424745708109828096\"]}")

(deftest twitter
  (let [request {:body (utils/->map twitter-data)}]
    (is (= {:text "My cat's breath smells like cat food."
            :url  "http://twitter.com/status/123"}
           (-> request incoming/handle :body)))))
