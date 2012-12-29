(ns {:description "Wraps the AWS SWF decision calls."
     :author "Mark Gunnels"}
    com.221blabs.aws.swf.decision
  (:require [com.221blabs.aws.swf.common :as common])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForDecisionTaskRequest
            ScheduleActivityTaskDecisionAttributes
            Decision
            RespondDecisionTaskCompletedRequest
            ActivityType
            FailWorkflowExecutionDecisionAttributes
            CompleteWorkflowExecutionDecisionAttributes
            StartChildWorkflowExecutionDecisionAttributes]))

;;POLLER
(defn create-poll
  [domain task-list identity]
  (doto (PollForDecisionTaskRequest.)
    (.setTaskList task-list)
    (.setDomain domain)
    (.setIdentity identity)))

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
  (doto (Decision.) 
    (.setScheduleActivityTaskDecisionAttributes
     (create-schedule-activity-task-attributes name version
                                               id task-list-name
                                               input))
    (.setDecisionType "ScheduleActivityTask")))

(defprotocol Decision
  (as-attributes [this])
  (as-decision [this]))

(defrecord ScheduleActivityTask []
  Decision
  (as-attributes [r]
    (let [activity-type ]
))
  (as-decision [r]
))

(defrecord FailWorkflowExecution [reason
                                  details]
  Decision
  (as-attributes [r]
    (doto (FailWorkflowExecutionDecisionAttributes.)
      (.setReason (:reason r))
      (.setDetails (:details r))))
  (as-decision [r]
))

(defrecord CompleteWorkflowExecution [result]
  (as-attributes [r]
    (doto (CompleteWorkflowExecutionDecisionAttributes.)
      (.setResult (:result r))))
  (as-decision [r]
    (doto (Decision.) 
      (.setCompleteWorkflowExecutionDecisionAttributes (as-attributes r))
      (.setDecisionType "CompleteWorkflowExecution"))))

(defrecord StartChildWorkflowExecution [name
                                        version
                                        workflow-id
                                        input]
  (as-attributes [r]
    (let [workflow-type (common/create-workflow-type wf-name
                                                     wf-version)]
      (doto (StartChildWorkflowExecutionDecisionAttributes.)
        (.setInput input)
        (.setWorkflowType workflow-type)
        (.setWorkflowId workflow-id))))
  (as-decision [r]
    (doto (Decision.) 
      (.setStartChildWorkflowExecutionDecisionAttributes (as-attributes r))
      (.setDecisionType "StartChildWorkflowExecution"))))


(defn decision-task-completed
  [task-token & decisions]
  (doto (RespondDecisionTaskCompletedRequest.)
    (.setTaskToken task-token)
    (.setDecisions decisions)))
