(ns journaler.handlers.pages
  (:require [clostache.parser :as clostache]))

(defn home [request]
  (clostache/render-resource "templates/index.html" {}))
