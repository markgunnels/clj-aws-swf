(defproject clj-aws-swf/clj-aws-swf "1.0.32-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.contracts "0.0.1"]                 
                 [com.amazonaws/aws-java-sdk "1.4.0.1"]
                 [clj-time "0.4.4"]
                 [inflections "0.7.3"]
                 [midje "1.4.0"]]
  :profiles {:dev {:plugins [[lein-midje "2.0.3"]]}}
  :plugins [[lein-swank "1.4.3"]]
  :description "Clojure library to make use of Amazon's Simple Workflow Framework"
  :pedantic :warn)