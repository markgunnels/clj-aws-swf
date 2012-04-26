(ns clj-aws-swf.utils
  (:require [clojure.java.io :as io]))

(defn get-property
  [prop-name]
  (System/getenv prop-name))

(defn uuid
  []
  (str (java.util.UUID/randomUUID)))