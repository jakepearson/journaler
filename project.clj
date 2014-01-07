(defproject jakepearson/journaler "0.1.0"
  :description "A service called journaler"
  :url "https://github.com/RallySoftware/journaler"
  :license {:name "Eclipse 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [slingshot "0.10.3"]
                 [cheshire "5.0.1"]
                 [compojure "1.1.5"]
                 [http-kit "2.1.16"]
                 [de.ubercode.clostache/clostache "1.3.1"]]
  :min-lein-version "2.0.0"
  :main journaler.handler)
