(ns clj-aws-swf.activity
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.workflow :as w]
            [clj-aws-swf.common :as common])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForActivityTaskRequest
            ActivityType
            RespondActivityTaskCompletedRequest
            RespondActivityTaskFailedRequest
            RespondActivityTaskCanceledRequest
            RegisterActivityTypeRequest
            DeprecateActivityTypeRequest]))

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

(defn- create-task-list
  [name]
  (let [task-list (TaskList.)]
    (.setName task-list name)))

(defn- create-register-activity-type-request
  [domain name version description
   default-task-schedule-start
   default-task-schedule-end
   default-task-start-to-end
   task-list-name]
  (let [request (RegisterActivityTypeRequest.)]
    (doto request
      (.setDomain domain)
      (.setName name)
      (.setVersion version)
      (.setDescription description)
      (.setDefaultTaskHeartbeatTimeout default-execution-timeout)
      (.setDefaultTaskScheduleToStartTimeout default-task-schedule-start)
      (.setDefaultTaskScheduleToCloseTimeout default-task-schedule-end)
      (.setDefaultTaskStartToCloseTimeout default-task-start-to-end)
      (.setDefaultTaskList (create-task-list task-list-name)))))

(defn register
  [domain name version description
   default-task-schedule-start
   default-task-schedule-end
   default-task-start-to-end
   task-list-name]
  (let [service (c/create)
        request (create-register-activity-type-request
                 domain name version description
                 default-task-schedule-start
                 default-task-schedule-end
                 default-task-start-to-end
                 task-list-name)]
    (.registerActivityType service request)))

(defn- create-deprecate-activity-type-request
  [domain name version]
  (let [request (DeprecateActivityTypeRequest.)
        activity-type (common/create-activity-type name
                                                   version)]
    (doto request
      (.setDomain domain)
      (.setActivityType activity-type))))

(defn deprecate
  [domain name version ]
  (let [service (c/create)
        request (create-deprecate-activity-type-request
                 domain name version)]
    (.deprecateActivityType service request)))