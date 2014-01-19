(ns journaler.handlers.incoming
  (:require [clojure.string :as string]
            [journaler.response :as response]))

;; Posts come from https://github.com/captn3m0/ifttt-webhook
;; using a wacky app that makes a webhook look like wordpress

;; HEROKU_POSTGRESQL_WHITE_URL
;; heroku-postgresql:hobby-dev

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
   :starts      starts
   :ends        ends
   :url         url})

(def ^:private actions
  {:instagram instagram
   :twitter   twitter
   :calendar  calendar})

(defn handle [request]
  (let [content (-> request
                    (get-in [:body :title])
                    (string/split #"☃"))
        action  (-> content first keyword actions)
        data    (action (rest content))]
    (response/ok data)))
