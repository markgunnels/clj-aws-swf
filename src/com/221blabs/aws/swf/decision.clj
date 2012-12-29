(ns com.221blabs.aws.swf.decision
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
(defprotocol Decision
  (as-attributes [this])
  (as-decision [this]))

(defrecord ScheduleActivityTask [name
                                 version
                                 id
                                 type
                                 task-list
                                 input]
  Decision
  (as-attributes [r]
    (let [activity-type (common/create-activity-type (:name r)
                                                     (:version r))]
      (doto (ScheduleActivityTaskDecisionAttributes.)
        (.setActivityId (:id r))
        (.setActivityType (:type r))
        (.setTaskList (:task-list r))
        (.setInput (:input r)))))
  (as-decision [r]
    (doto (Decision.) 
      (.setScheduleActivityTaskDecisionAttributes (as-attributes r))
      (.setDecisionType "ScheduleActivityTask"))))

(defrecord FailWorkflowExecution [reason
                                  details]
  Decision
  (as-attributes [r]
    (doto (FailWorkflowExecutionDecisionAttributes.)
      (.setReason (:reason r))
      (.setDetails (:details r))))
  (as-decision [r]
    (doto (Decision.) 
      (.setFailWorkflowExecutionDecisionAttributes (as-attributes r))
      (.setDecisionType "FailWorkflowExecution"))))

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
