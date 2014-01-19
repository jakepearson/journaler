(ns journaler.handlers.incoming
  (:require [clojure.string :as string]
            [journaler.response :as response]))

(defn- instagram [text image-url url]
  {:text      text
   :image-url image-url
   :url       url})

(defn- twitter [[text url]]
  {:text text
   :url  url})

(defn- calendar [[title description where starts ends url]]
  {:title       title
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
