(ns clj-aws-swf.decision
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.common :as common])
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

(defn poll-for-decision-task
  [task-list-name domain identity]
  (let [swf-service (c/create)
        task-list (common/create-task-list task-list-name)
        decision-task-poller (PollForDecisionTaskRequest.)]
    (doto decision-task-poller
      (.setTaskList task-list)
      (.setDomain domain)
      (.setIdentity identity))
    (.pollForDecisionTask swf-service decision-task-poller)))

(defn create-schedule-activity-task-attributes
  [activity-id activity-type task-list input]
  (let [schedule-activity-task-attrs (ScheduleActivityTaskDecisionAttributes.)]
    (doto schedule-activity-task-attrs
      (.setActivityId activity-id)
      (.setActivityType activity-type)
      (.setTaskList task-list)
      (.setInput input))
    schedule-activity-task-attrs))

(defn create-activity-type
  [name version]
  (let [activity-type (ActivityType.)]
    (doto activity-type
      (.setName name)
      (.setVersion version))
    activity-type))

(defn schedule-activity-task
  [decision-task-token activity-id activity-type-name activity-type-version input task-list-name]
  (let [swf-service (c/create)
        task-list (common/create-task-list task-list-name)
        activity-type (create-activity-type activity-type-name
                                            activity-type-version)
        schedule-activity-task-attrs (create-schedule-activity-task-attributes activity-id
                                                                               activity-type
                                                                               task-list
                                                                               input)
        decision (Decision.)
        decision-task-completed (RespondDecisionTaskCompletedRequest.)]
    (doto decision
      (.setScheduleActivityTaskDecisionAttributes schedule-activity-task-attrs)
      (.setDecisionType "ScheduleActivityTask"))
    (doto decision-task-completed
      (.setTaskToken decision-task-token)
      (.setDecisions [decision]))
    (.respondDecisionTaskCompleted swf-service decision-task-completed)))

(defn create-fail-workflow-execution-attributes
  [reason details]
  (let [attrs (FailWorkflowExecutionDecisionAttributes.)]
    (doto attrs
      (.setReason reason)
      (.setDetails details))
    attrs))

(defn fail-workflow-execution
  [decision-task-token reason details]
  (let [swf-service (c/create)
        attrs (create-fail-workflow-execution-attributes reason details)
        decision (Decision.)
        decision-task-completed (RespondDecisionTaskCompletedRequest.)]
    (doto decision
      (.setFailWorkflowExecutionDecisionAttributes attrs)
      (.setDecisionType "FailWorkflowExecution"))
    (doto decision-task-completed
      (.setTaskToken decision-task-token)
      (.setDecisions [decision]))
    (.respondDecisionTaskCompleted swf-service decision-task-completed)))

(defn create-complete-workflow-execution-attributes
  [result]
  (let [attrs (CompleteWorkflowExecutionDecisionAttributes.)]
    (doto attrs
      (.setResult result))
    attrs))

(defn complete-workflow-execution
  [decision-task-token result]
  (let [swf-service (c/create)
        attrs (create-complete-workflow-execution-attributes result)
        decision (Decision.)
        decision-task-completed (RespondDecisionTaskCompletedRequest.)]
    (doto decision
      (.setCompleteWorkflowExecutionDecisionAttributes attrs)
      (.setDecisionType "CompleteWorkflowExecution"))
    (doto decision-task-completed
      (.setTaskToken decision-task-token)
      (.setDecisions [decision]))
    (.respondDecisionTaskCompleted swf-service decision-task-completed)))

(defn create-start-child-workflow-execution-attributes
  [wf-name wf-version input domain workflow-id]
  (let [attrs (StartChildWorkflowExecutionDecisionAttributes.)
        workflow-type (common/create-workflow-type wf-name
                                                   wf-version)]
    (doto attrs
      (.setInput input)
      (.setWorkflowType workflow-type)
      (.setDomain domain)
      (.setWorkflowId workflow-id))
    attrs))

(defn start-child-workflow-execution
  [decision-task-token wf-name wf-version input domain workflow-id]
  (let [swf-service (c/create)
        attrs (create-start-child-workflow-execution-attributes wf-name
                                                                wf-version
                                                                input
                                                                domain
                                                                workflow-id)
        decision (Decision.)
        decision-task-completed (RespondDecisionTaskCompletedRequest.)]
    (doto decision
      (.setStartChildWorkflowExecutionDecisionAttributes attrs)
      (.setDecisionType "StartChildWorkflowExecution"))
    (doto decision-task-completed
      (.setTaskToken decision-task-token)
      (.setDecisions [decision]))
    (.respondDecisionTaskCompleted swf-service decision-task-completed)))