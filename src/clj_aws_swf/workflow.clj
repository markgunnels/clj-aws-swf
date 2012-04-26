(ns clj-aws-swf.workflow
  (:use clj-aws-swf.utils)
  (:require [clj-aws-swf.client :as c])
  (:import [com.amazonaws.services.simpleworkflow.model
            StartWorkflowExecutionRequest
            WorkflowType
            TerminateWorkflowExecutionRequest
            GetWorkflowExecutionHistoryRequest
            WorkflowExecution
            WorkflowExecutionFilter
            CountClosedWorkflowExecutionsRequest
            CountOpenWorkflowExecutionsRequest
            ExecutionTimeFilter]))

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

(defn get-workflow-execution-history
  [domain workflow-id run-id]
  (let [swf-service (c/create)
        request (GetWorkflowExecutionHistoryRequest.)
        wf-execution (WorkflowExecution.)]
    (doto wf-execution
      (.setWorkflowId workflow-id)
      (.setRunId run-id))
    (doto request
      (.setDomain domain)
      (.setExecution wf-execution))
    (.getWorkflowExecutionHistory swf-service request)))

(defn twenty-years-ago
  []
  (let [d (java.util.Date.)]
    (.setYear d (- (.getYear d) 20))
    d))

(defn count-closed-workflow-executions
  [domain workflow-id]
  (let [swf-service (c/create)
        request (CountClosedWorkflowExecutionsRequest.)
        wf-filter (WorkflowExecutionFilter.)
        t-filter (ExecutionTimeFilter.)]
    (doto wf-filter
      (.setWorkflowId workflow-id))
    (doto t-filter
      (.setOldestDate (twenty-years-ago)))
    (doto request
      (.setDomain domain)
      (.setExecutionFilter wf-filter)
      (.setStartTimeFilter t-filter))
    (.countClosedWorkflowExecutions swf-service request)))

(defn count-open-workflow-executions
  [domain workflow-id]
  (let [swf-service (c/create)
        request (CountOpenWorkflowExecutionsRequest.)
        wf-filter (WorkflowExecutionFilter.)
        t-filter (ExecutionTimeFilter.)]
    (doto wf-filter
      (.setWorkflowId workflow-id))
    (doto t-filter
      (.setOldestDate (twenty-years-ago)))
    (doto request
      (.setDomain domain)
      (.setExecutionFilter wf-filter)
      (.setStartTimeFilter t-filter))
    (.countOpenWorkflowExecutions swf-service request)))

(defn count-workflow-executions
  [domain workflow-id]
  (let [oc (.getCount
            (count-open-workflow-executions domain workflow-id)) 
        cc (.getCount
            (count-closed-workflow-executions domain workflow-id))]
    (+ oc cc)))

