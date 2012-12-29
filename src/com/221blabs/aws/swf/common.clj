(ns com.221blabs.aws.swf.common
  (:import [com.amazonaws.services.simpleworkflow.model
            ActivityType
            TaskList
            WorkflowType
            WorkflowExecution]))

(defn create-activity-type
  [name version]
  (doto (ActivityType.)
    (.setName name)
    (.setVersion version)))

(defn create-task-list
  [name]
  (doto (TaskList.)
    (.setName name)))

(defn create-workflow-type
  [name version]
  (doto (WorkflowType.)
    (.setName name)
    (.setVersion version)))

(defn create-workflow-execution
  [wf-id run-id]
  (doto (WorkflowExecution.)
    (.setWorkflowId wf-id)
    (.setRunId run-id)))

