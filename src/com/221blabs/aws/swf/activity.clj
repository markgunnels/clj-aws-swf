(ns com.221blabs.aws.swf.activity
  (:require [com.221blabs.aws.swf.common :as common]
            [com.221blabs.aws.swf.constraints :as cx])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForActivityTaskRequest
            ActivityType
            ActivityTask
            RespondActivityTaskCompletedRequest
            RespondActivityTaskFailedRequest
            RespondActivityTaskCanceledRequest
            RegisterActivityTypeRequest
            DeprecateActivityTypeRequest]))

(defn- create-poll-for-activity-task-request
  [domain task-list-name identity]
  (doto (PollForActivityTaskRequest.)
    (.setTaskList (common/create-task-list task-list-name))
    (.setDomain domain)
    (.setIdentity identity)))

(defn poll
  [client domain task-list-name identity]
  {:pre [(cx/swf-client? client)
         (every? string? [domain task-list-name identity])]
   :post [(instance? ActivityTask %)]}
  (.pollForActivityTask client
                        (create-poll-for-activity-task-request domain
                                                               task-list-name
                                                               identity)))

;; complete

(defn complete
  [client task-token result]
  {:pre [(cx/swf-client? client)
         (every? string? [task-token result])]
   :post [(nil? %)]}
  (.respondActivityTaskCompleted client
                                 (doto (RespondActivityTaskCompletedRequest.)
                                   (.setTaskToken task-token)
                                   (.setResult result))))

(defn fail
  [client task-token reason details]
  {:pre [(cx/swf-client? client)
         (every? string? [task-token reason details])]
   :post [(nil? %)]}
  (.respondActivityTaskFailed client
                              (doto (RespondActivityTaskFailedRequest.)
                                (.setReason reason)
                                (.setTaskToken task-token)
                                (.setDetails details))))

(defn cancel
  [client task-token details]
  {:pre [(cx/swf-client? client)
         (every? string? [task-token details])]
   :post [(nil? %)]}
  (.respondActivityTaskCanceled client
                              (doto (RespondActivityTaskCanceledRequest.)
                                (.setTaskToken task-token)
                                (.setDetails details))))

;; TODO:
;; (defn- create-task-list
;;   [name]
;;   (let [task-list (TaskList.)]
;;     (.setName task-list name)))

;; (defn- create-register-activity-type-request
;;   [domain name version description
;;    default-task-schedule-start
;;    default-task-schedule-end
;;    default-task-start-to-end
;;    task-list-name]
;;   (let [request (RegisterActivityTypeRequest.)]
;;     (doto request
;;       (.setDomain domain)
;;       (.setName name)
;;       (.setVersion version)
;;       (.setDescription description)
;;       (.setDefaultTaskHeartbeatTimeout default-task-schedule-start)
;;       (.setDefaultTaskScheduleToStartTimeout default-task-schedule-start)
;;       (.setDefaultTaskScheduleToCloseTimeout default-task-schedule-end)
;;       (.setDefaultTaskStartToCloseTimeout default-task-start-to-end)
;;       (.setDefaultTaskList (create-task-list task-list-name)))))

;; (defn register
;;   [domain name version description
;;    default-task-schedule-start
;;    default-task-schedule-end
;;    default-task-start-to-end
;;    task-list-name]
;;   (let [service (c/create)
;;         request (create-register-activity-type-request
;;                  domain name version description
;;                  default-task-schedule-start
;;                  default-task-schedule-end
;;                  default-task-start-to-end
;;                  task-list-name)]
;;     (.registerActivityType service request)))

;; (defn- create-deprecate-activity-type-request
;;   [domain name version]
;;   (let [request (DeprecateActivityTypeRequest.)
;;         activity-type (common/create-activity-type name
;;                                                    version)]
;;     (doto request
;;       (.setDomain domain)
;;       (.setActivityType activity-type))))

;; (defn deprecate
;;   [domain name version ]
;;   (let [service (c/create)
;;         request (create-deprecate-activity-type-request
;;                  domain name version)]
;;     (.deprecateActivityType service request)))