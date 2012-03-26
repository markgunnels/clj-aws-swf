(ns clj-aws-swf.activity
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.common :as common])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForActivityTaskRequest
            ActivityType
            RespondActivityTaskCompletedRequest
            RespondActivityTaskFailedRequest
            RespondActivityTaskCanceledRequest]))

(defn poll-for-activity-task
  [task-list-name domain identity]
  (let [swf-service (c/create)
        task-list (common/create-task-list task-list-name)
        activity-task-poller (PollForActivityTaskRequest.)]
    (doto activity-task-poller
      (.setTaskList task-list)
      (.setDomain domain)
      (.setIdentity identity))
    (.pollForActivityTask swf-service activity-task-poller)))

(defn complete-activity-task
  [task-token result]
  (let [swf-service (c/create)
        activity-task-completed (RespondActivityTaskCompletedRequest.)]
    (doto activity-task-completed
      (.setTaskToken task-token)
      (.setResult result))
    (.respondActivityTaskCompleted swf-service activity-task-completed)))

(defn fail-activity-task
  [task-token reason details]
  (let [swf-service (c/create)
        activity-task-failed (RespondActivityTaskFailedRequest.)]
    (doto activity-task-failed
      (.setTaskToken task-token)
      (.setReason reason)
      (.setDetails details))
    (.respondActivityTaskFailed swf-service activity-task-failed)))

(defn cancel-activity-task
  [task-token details]
  (let [swf-service (c/create)
        activity-task-canceled (RespondActivityTaskCanceledRequest.)]
    (doto activity-task-canceled
      (.setTaskToken task-token)
      (.setDetails details))
    (.respondActivityTaskCanceled swf-service activity-task-canceled)))
