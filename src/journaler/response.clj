(ns journaler.response
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [ring.util.response :as response]))


(defn response
  ([status]
     (response status nil nil))
  ([status body]
     (response status body nil))
  ([status body headers]
     {:status status :body body :headers headers}))

(defmacro defresponse
  ([status name]
     `(defresponse ~status ~name nil))
  ([status name default-body]
     (let [uppercase-name (-> name clojure.core/name (string/upper-case) symbol)]
       `(do
          (defn ~name
            ([] (~name ~default-body))
            ([~'body] (response ~status ~'body)))
          (def ~uppercase-name ~status)))))


(defresponse 200 ok)
(defresponse 400 bad-request)
(defresponse 204 no-content)
(defresponse 400 bad-serialization)
(defresponse 401 unauthorized)
(defresponse 403 forbidden)
(defresponse 404 not-found)
(defresponse 404 file-not-found)
(defresponse 405 not-allowed)
(defresponse 405 method-not-allowed)
(defresponse 409 conflict)
(defresponse 409 username-conflict)
(defresponse 500 internal-server-error)
(defresponse 501 not-implemented)
(defresponse 503 service-unavailable)

(defn redirect
  "Returns a 302 Redirect response."
  [redirect-uri]
  (response/redirect redirect-uri))
