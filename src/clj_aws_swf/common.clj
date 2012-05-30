(ns clj-aws-swf.common
  (:require [clj-aws-swf.client :as c])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            ActivityType]))

(defn create-task-list
  [name]
  (let [task-list (TaskList.)]
    (.setName task-list name)
    task-list))

(defn create-activity-type
  [name version]
  (let [activity-type (ActivityType.)]
    (doto activity-type
      (.setName name)
      (.setVersion version))
    activity-type))