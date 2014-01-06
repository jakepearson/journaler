(ns journaler.utils
  (:require [cheshire.custom :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [ring.util.codec :as codec])
  (:import [com.fasterxml.jackson.core JsonGenerator]
           [java.util.concurrent TimeUnit]
           [org.joda.time DateTime]
           [java.io StringWriter]))

(defn query-string
  "Given a map, return a properly-escaped URL query-string.
   Keys will be passed through clojure.core/name, so keywords are
   fine.  As a special case, nil values will cause the key-value pair
   to be omitted. To include an empty value, set it to an empty
   string. "
  [params]
  (->> params
       (remove (comp nil? second))
       (map (fn [[k v]] (str (codec/url-encode (name k)) \= (codec/url-encode v))))
       (interpose \&)
       (apply str)))

(defn uri
  "Return the path as a url string, with the query parameters from the
  params map appended.
  eg: (uri \"/foo\" {:bar \"baz\"}) => \"/foo?bar=baz\"
  "
  [^String path params]
  (let [qs (query-string params)]
    (str path
         (if (not-empty qs)
           (str (cond
                 (re-find #"\?.+" path) "&"
                 (.endsWith path "?")   ""
                 :else                  "?")
                qs)))))

(defn absolute-url? [url]
  (re-find #"^https?://" url))

(defn parse-url [url]
  (try
    (let [url (java.net.URL. url)]
      {:url          (str url)
       :query-params (codec/form-decode (.getQuery url))
       :path         (.getPath url)
       :host         (.getHost url)})
    (catch java.net.MalformedURLException e
      nil)))

(defn- map-keys
  "given a map, do something to the keys"
  [m transform]
  (let [val-mapper (fn [val] (map-keys val transform))]
    (if (map? m)
      (zipmap (map transform (keys m)) (map val-mapper (vals m)))
      m)))

(defn stringify-map-keys
  "given a map, return an equivelent map with string keys"
  [m]
  (map-keys m name))

(defn keywordify-map-keys
  "give a map, return an equivalent map with keywords instead of strings"
  [m]
  (map-keys m keyword))

(defn stacktrace
  "Returns the stacktrace as a String."
  [^Throwable e]
  (with-out-str (.printStackTrace e (java.io.PrintWriter. *out*))))

(defn encode-date-time
  [^DateTime date ^JsonGenerator jg]
  (.writeNumber jg (.getMillis date)))

(json/add-encoder org.joda.time.DateTime encode-date-time)

(defn server-name [req]
  (let [headers (:headers req)]
    (first (.split ^String (headers "host") ":"))))

(defn ^String to-string [x]
  (cond
   (string? x) x
   x           (json/generate-string x)
   :else       nil))

(defn map->stream [value]
  (when value
    (clojure.java.io/input-stream (.getBytes (to-string value) "utf-8"))))

(def string->map #(json/parse-string % true))
(defn stream->map [stream]
  (json/parse-string (slurp stream) true))

(defn ->map [value]
  (cond
   (nil? value)    nil
   (string? value) (string->map value)
   :else           (stream->map value)))

