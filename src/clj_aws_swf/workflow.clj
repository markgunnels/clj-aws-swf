(ns clj-aws-swf.workflow
  (:use clj-aws-swf.utils)
  (:require [clj-aws-swf.client :as c])
  (:import [com.amazonaws.services.simpleworkflow.model
            StartWorkflowExecutionRequest
            WorkflowType
            TerminateWorkflowExecutionRequest]))

(defn- create-workflow-type
  [name version]
  (let [workflow-type (WorkflowType.)]
    (doto workflow-type
      (.setName name)
      (.setVersion version))
    workflow-type))

(defn start-workflow-execution
  [workflow-type-name workflow-type-version input domain workflow-id]
  (let [swf-service (c/create)
        workflow-type (create-workflow-type workflow-type-name
                                            workflow-type-version)
        start-workflow-request (StartWorkflowExecutionRequest.)]
    (doto start-workflow-request
      (.setInput input)
      (.setWorkflowType workflow-type)
      (.setDomain domain)
      (.setWorkflowId workflow-id))
    (.startWorkflowExecution swf-service start-workflow-request)))

(defn terminate-workflow-execution
  [domain run-id workflow-id]
  (let [swf-service (c/create)
        terminate-workflow-request (TerminateWorkflowExecutionRequest.)]
    (doto terminate-workflow-request
      (.setDomain domain)
      (.setRunId run-id)
      (.setWorkflowId workflow-id))
    (.terminateWorkflowExecution swf-service terminate-workflow-request)))

