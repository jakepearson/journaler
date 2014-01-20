(ns journaler.handlers.incoming
  (:require [clojure.string :as string]
            [journaler.response :as response]
            [journaler.storage :as storage]
            [clj-time.core :as time]
            [clj-time.format :as format]))

;; Posts come from https://github.com/captn3m0/ifttt-webhook
;; using a wacky app that makes a webhook look like wordpress

;; HEROKU_POSTGRESQL_WHITE_URL
;; heroku-postgresql:hobby-dev

(defn- ->timestamp [text]
 (let [formatter (format/formatter "MMM d, YYYY 'at' hh:mmaa")]
   (format/parse formatter text)))

(defn- instagram [[text image-url url]]
  {:text      text
   :image-url image-url
   :url       url})

(defn- twitter [[text url]]
  {:text text
   :url  url})

(defn- calendar [[text description where starts ends url]]
  {:text       text
   :description description
   :where       where
   :starts      (->timestamp starts)
   :ends        (->timestamp ends)
   :url         url})

(def ^:private actions
  {:instagram instagram
   :twitter   twitter
   :calendar  calendar})

(defn- parse [body]
  (let [content     (-> body
                        :title
                        (string/split #"☃"))
        action-type (-> content first keyword)
        action      (action-type actions)]
    (-> content
        rest
        action
        (assoc :type action-type))))

(defn handle [{:keys [body]}]
  (let [event (-> body parse)]
    (storage/insert-event event)
    (response/ok event)))
