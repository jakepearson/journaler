(ns journaler.storage
  (:require [clojure.java.jdbc :as sql]))

(def ^:private connection-string (System/getenv "DATABASE_URL"))

(defn create-event-table []
  (sql/with-connection connection-string
    (sql/create-table :event
                      [:id :serial "PRIMARY KEY"]
                      [:type :varchar]
                      [:text :varchar]
                      [:image-url :varchar]
                      [:url :varchar]
                      [:description :varchar]
                      [:where :varchar]
                      [:starts :timestamp]
                      [:ends :timestamp]
                      [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn insert-event [event]
  (sql/with-connection connection-string
    (sql/insert-record :event event)))
