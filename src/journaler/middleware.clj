(ns journaler.middleware
  (:import [org.apache.commons.codec.binary Base64])
  (:require [journaler.utils :as utils]
            [clojure.string :as string]
            [slingshot.slingshot :refer [try+]]
            [journaler.response :as response]
            [org.httpkit.client :as http]))

;; Json middleware
(defn wrap-json-request
  "If there is a body in the request, it will be parsed as JSON and
   the resulting map stored in the :json-body key of the request."
  [handler]
  (fn [request]
    (handler (assoc request :json-body (if-let [body (:body request)]
                                         (utils/stream->map body))))))

(defn wrap-json-response
  "Serialize the response body as JSON if it is a map."
  [handler]
  (fn [request]
    (let [response (handler request)
          body     (:body response)]
      (merge response
             (if (map? body)
               {:body         (utils/to-string body)
                :content-type :json}))))) ; Depends on wrap-content-type


;;; Content-type middleware
(def known-content-types
  {:json "application/json"
   :html "text/html"})

(defn- content-type [type]
  (or (get known-content-types type)
      (name type)))

(defn wrap-content-type
  "Set the content-type response header if the repsonse contains
  a :content-type key. The value can be a keyword such as :json if it
  is in the map `known-content-types`."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if-let [type (:content-type response)]
        (assoc-in response [:headers "content-type"] (content-type type))
        response))))

;;; Authorization middleware
(defn- extract-basic-auth [request]
  (if-let [header (get-in request [:headers "authorization"])]
    (if-let [[_ ^String value] (re-matches #"\s*Basic (.*)$" header)]
      (try
        (let [[username password]
              (-> value
                  (.getBytes "UTF-8")
                  Base64/decodeBase64
                  (String. "UTF-8")
                  (string/split #":" 2))]
          {:username username
           :password password})
        (catch Exception e
          nil                           ; Decoding error?
          )))))

(defn wrap-basic-auth
  "Middleware that extracts HTTP Basic auth credentials from the
  authorization header, if present, and puts them into the request
  under the key :basic-auth, with the value being a map
  containing :username and :password."
  [handler]
  (fn [request]
    (handler
     (if-let [auth (extract-basic-auth request)]
       (assoc request :basic-auth auth)
       request))))

(defn validate-zuul-authentication
  "Middleware that will ask zuul if the request is being made with a valid
  zuul-key. If not, returns 401. If the user is validated, the user data
  returned from zuul will be added to the request at request/env/user. You
  must specify a :zuul-tenant and :endpoints in the options when using this
  middleware. :endpoints is a vecotor or regex patterns that match on
  endpoints where authentication is required."
  [handler {:keys [zuul-tenant endpoints]}]
  (fn [request]
    (if (some #(re-matches % (:uri request)) endpoints)
      (let [options       {:headers {"x-tenant" zuul-tenant}}
            url           (str "http://zuul1:3000/key/" (get-in request [:cookies "ZSESSIONID" :value]) ".js")
            zuul-response @(http/get url options)]
        (if (= (:status zuul-response) 200)
          (->> (:body zuul-response)
               utils/string->map
               utils/keywordify-map-keys
               (assoc-in request [:env :user])
               handler)
          (response/unauthorized "Unauthorized")))
      (handler request))))

;; Service Name

(def service-name (atom nil))

(defn wrap-server-response-header [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "service"] @service-name))))

;; Exception middleware

(defn stacktrace [throwable]
  (-> throwable
      utils/stacktrace
      (string/split #"\n")))

(defn error-information [{:keys [throwable cause environment]}]
  {:stack-trace (stacktrace throwable)
   :cause       cause
   :environment (zipmap (keys environment) (map str (vals environment)))})

(defn wrap-handle-exception
  "Middleware to catch an exception and return a stack trace in the response"
  [handler]
  (fn [request]
    (try+
     (let [response (handler request)
           body     (:body response)]
       (if (seq? body)
         (assoc response :body (doall body))
         response))
     (catch String s
       (response/bad-request {:errors (error-information &throw-context)}))
     (catch Exception e
                                        ;(.printStackTrace e)
       (response/bad-request {:errors (stacktrace e)})))))

