(defproject clj-aws-swf "1.0.1-SNAPSHOT"
  :description "Clojure library to make use of Amazon's Simple Workflow Framework"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.amazonaws/aws-java-sdk "1.3.3"]]
  :plugins [[lein-swank "1.4.3"]]
  :jar-exclusions [#"aws.properties"])
  