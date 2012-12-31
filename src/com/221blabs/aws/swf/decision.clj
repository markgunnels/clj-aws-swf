(ns com.221blabs.aws.swf.decision
  (:require [com.221blabs.aws.swf.common :as common]
            [com.221blabs.aws.swf.constraints :as cx])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForDecisionTaskRequest
            ScheduleActivityTaskDecisionAttributes
            Decision
            DecisionTask
            RespondDecisionTaskCompletedRequest
            ActivityType
            FailWorkflowExecutionDecisionAttributes
            CompleteWorkflowExecutionDecisionAttributes
            StartChildWorkflowExecutionDecisionAttributes]))

;;POLLER
(defn- create-poll-for-decision-task-request
  [domain task-list-name identity maximum-page-size next-page-token]
  (let [req (PollForDecisionTaskRequest.)]
    (if maximum-page-size (.setMaximumPageSize req (Integer. maximum-page-size)))
    (doto req 
      (.setTaskList (common/create-task-list task-list-name))
      (.setDomain domain)
      (.setIdentity identity)
      (.setNextPageToken next-page-token))))

(defn poll
  [client domain task-list-name identity maximum-page-size next-page-token]
  {:pre [(cx/swf-client? client)
         (every? #(or (string? %)
                      (nil? %))
                 [domain task-list-name identity next-page-token])
         (or (number? maximum-page-size)
             (nil? maximum-page-size))]
   :post [(instance? DecisionTask %)]}
  (.pollForDecisionTask client
                        (create-poll-for-decision-task-request domain
                                                               task-list-name
                                                               identity
                                                               maximum-page-size
                                                               next-page-token)))

;;DECISION
;;create decision
(defn- create-schedule-activity-task-attributes
  [name version id task-list-name input]
  (doto (ScheduleActivityTaskDecisionAttributes.)
    (.setActivityId id)
    (.setActivityType (common/create-activity-type name
                                                   version))
    (.setTaskList (common/create-task-list task-list-name))
    (.setInput input)))

(defn create-schedule-activity-task-decision
  [name version id task-list-name input]
  {:pre [(every? string? [name version id task-list-name input])]
   :post [(instance? Decision %)]}
  (doto (Decision.) 
    (.setScheduleActivityTaskDecisionAttributes
     (create-schedule-activity-task-attributes name version
                                               id task-list-name
                                               input))
    (.setDecisionType "ScheduleActivityTask")))

(defn- create-fail-workflow-execution-decision-attributes
  [reason details]
  (doto (FailWorkflowExecutionDecisionAttributes.)
    (.setReason reason)
    (.setDetails details)))

(defn create-fail-workflow-execution-decision
  [reason details]
  {:pre [(every? #(or (string? %) (nil? %)) [reason details])]
   :post [(instance? Decision %)]}
  (doto (Decision.) 
    (.setFailWorkflowExecutionDecisionAttributes
     (create-fail-workflow-execution-decision-attributes reason details))
    (.setDecisionType "FailWorkflowExecution")))

(defn- create-complete-workflow-execution-decision-attributes
  [result]
  (doto (CompleteWorkflowExecutionDecisionAttributes.)
    (.setResult result)))

(defn create-complete-workflow-execution-decision
  [result]
  {:pre [(every? #(or (string? %) (nil? %)) [result])]
   :post [(instance? Decision %)]}
  (doto (Decision.) 
    (.setCompleteWorkflowExecutionDecisionAttributes
     (create-complete-workflow-execution-decision-attributes result))
    (.setDecisionType "CompleteWorkflowExecution")))

(defn- start-child-workflow-execution-decision-attributes
  [name version id input]
  (doto (StartChildWorkflowExecutionDecisionAttributes.)
    (.setInput input)
    (.setWorkflowType (common/create-workflow-type name
                                                   version))
    (.setWorkflowId id)))

(defn start-child-workflow-execution-decision
  [name version id input]
  {:pre [(every? string? [name version id input])]
   :post [(instance? Decision %)]}
  (doto (Decision.) 
    (.setStartChildWorkflowExecutionDecisionAttributes
     (start-child-workflow-execution-decision-attributes name version
                                                         id input))
    (.setDecisionType "StartChildWorkflowExecution")))


(defn decision-task-completed
  [client task-token & decisions]
  {:pre [(cx/swf-client? client)
         (string? task-token)]
   :post [(nil? %)]}
  (.respondDecisionTaskCompleted client
                                 (doto (RespondDecisionTaskCompletedRequest.)
                                   (.setTaskToken task-token)
                                   (.setDecisions decisions))))