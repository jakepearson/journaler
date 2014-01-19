(ns journaler.storage
  (:require [clojure.java.jdbc :as sql]))

(def ^:private connection-string (System/getenv "DATABASE_URL"))

(defn create-event-table []
  (sql/with-connection connection-string
    (sql/create-table :event
                      [:id :serial "PRIMARY KEY"]
                      [:body :varchar "NOT NULL"]
                      [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

