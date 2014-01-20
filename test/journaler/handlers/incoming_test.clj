(ns journaler.handlers.incoming-test
  (:require [clojure.test :refer :all]
            [journaler.utils :as utils]
            [journaler.handlers.incoming :as incoming]
            [clj-time.core :as time]))

(def ^:private twitter-data "{\"user\":\"jake\",\"pass\":\"password\",\"title\":\"twitter☃My cat's breath smells like cat food.☃http://twitter.com/status/123\",\"categories\":[\"http://twitter.com/eddnerd/status/424745708109828096\"]}")

(def ^:private calendar-data "{\"user\":\"jake\",\"pass\":\"password\",\"title\":\"calendar☃brunch with the Andersons☃be there around 10☃7042 Quiet Retreat Ct Niwot☃January 19, 2014 at 10:00AM☃January 19, 2014 at 12:30PM☃http://ift.tt/1mqbgRs\"}")

(def ^:private instagram-data "{\"user\":\"jake\",\"pass\":\"password\",\"title\":\"instagram☃my picture is hipster☃http://instagram.com/foo.jpg☃http://instragram.com/foo\"}")

(deftest twitter
  (let [request {:body (utils/->map twitter-data)}]
    (is (= {:text "My cat's breath smells like cat food."
            :url  "http://twitter.com/status/123"
            :type :twitter}
           (-> request incoming/handle :body)))))

(deftest calendar
  (let [request {:body (utils/->map calendar-data)}]
    (is (= {:text        "brunch with the Andersons"
            :description "be there around 10"
            :where       "7042 Quiet Retreat Ct Niwot"
            :starts      (time/date-time 2014 1 19 10)
            :ends        (time/date-time 2014 1 19 12 30)
            :url         "http://ift.tt/1mqbgRs"
            :type        :calendar}
           (-> request incoming/handle :body)))))

(deftest instagram
  (let [request {:body (utils/->map instagram-data)}]
    (is (= {:text      "my picture is hipster"
            :image-url "http://instagram.com/foo.jpg"
            :url       "http://instragram.com/foo"
            :type      :instagram}
           (-> request incoming/handle :body)))))

(deftest ->timestamp
  (is (= (time/date-time 2014 1 19 22 11)
         (#'incoming/->timestamp "January 19, 2014 at 10:11PM"))))
