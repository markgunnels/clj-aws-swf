(defproject clj-aws-swf/clj-aws-swf "1.0.29-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.amazonaws/aws-java-sdk "1.3.21.1"]
                 [clj-time "0.4.4"]
                 [inflections "0.7.3"]]
  :plugins [[lein-swank "1.4.3"]]
  :description "Clojure library to make use of Amazon's Simple Workflow Framework"
  :pedantic :warn)