(ns clj-aws-swf.common
  (:require [clj-aws-swf.client :as c])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList]))

(defn create-task-list
  [name]
  (let [task-list (TaskList.)]
    (.setName task-list name)
    task-list))