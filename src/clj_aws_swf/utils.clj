(ns clj-aws-swf.utils
  (:require [clojure.java.io :as io]))

;; Borrowed from work by Gary Fredericks.
(def ^:private config-file-map
     (delay
      (with-open [^java.io.Reader reader (io/reader (io/resource "aws.properties"))] 
        (let [props (java.util.Properties.)]
          (.load props reader)
          (into {} (for [[k v] props] [(keyword k) (read-string v)]))))))

(defn get-property
  [prop-name]
  (or (when @config-file-map
        (get @config-file-map prop-name))
      (throw (new Exception
                  (str "Cannot find property value for "
                       prop-name
                       ". Try adding an \"aws.properties\" "
                       "file on the classpath.")))))

(defn uuid [] (str (java.util.UUID/randomUUID)))