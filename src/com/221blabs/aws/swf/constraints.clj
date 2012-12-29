(ns com.221blabs.aws.swf.constraints
  (:use clj-aws-swf.utils)
  (:require [clojure.core.contracts :as ccc])
  (:import [com.amazonaws.services.simpleworkflow
            AmazonSimpleWorkflow]
           [com.amazonaws.services.simpleworkflow.model
            Run
            History]))

(defn swf-client?
  [a]
  (instance? AmazonSimpleWorkflow a))

(defn swf-history?
  [a]
  (instance? History a))

(defn swf-run?
  [a]
  (instance? Run a))