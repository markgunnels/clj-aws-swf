(ns clj-aws-swf.workflow
  (:use clj-aws-swf.utils)
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.common :as common])
  (:import [com.amazonaws.services.simpleworkflow.model
            StartWorkflowExecutionRequest
            WorkflowType
            RegisterWorkflowTypeRequest
            DeprecateWorkflowTypeRequest
            TerminateWorkflowExecutionRequest
            GetWorkflowExecutionHistoryRequest
            WorkflowExecution
            WorkflowExecutionFilter
            CountClosedWorkflowExecutionsRequest
            CountOpenWorkflowExecutionsRequest
            ExecutionTimeFilter
            ChildPolicy
            TaskList
            DescribeWorkflowExecutionRequest
            GetWorkflowExecutionHistoryRequest
            ListOpenWorkflowExecutionsRequest
            ListClosedWorkflowExecutionsRequest]))

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

(defn- create-register-workflow-type-request
  [domain name version description default-execution-timeout
   default-task-timeout task-list-name]
  (let [request (RegisterWorkflowTypeRequest.)]
    (doto request
      (.setDomain domain)
      (.setName name)
      (.setVersion version)
      (.setDescription description)
      (.setDefaultExecutionStartToCloseTimeout default-execution-timeout)
      (.setDefaultTaskStartToCloseTimeout default-task-timeout)
      (.setDefaultTaskList (common/create-task-list task-list-name))
      (.setDefaultChildPolicy "TERMINATE"))))

(defn register
  [domain name version description default-execution-timeout
   default-task-timeout task-list-name]
  (let [service (c/create)
        request (create-register-workflow-type-request
                 domain name version description
                 default-execution-timeout
                 default-task-timeout task-list-name)]
    (.registerWorkflowType service request)))


(defn- create-deprecate-workflow-type-request
  [domain name version]
  (let [request (DeprecateWorkflowTypeRequest.)
        workflow-type (create-workflow-type name version)]
    (doto request
      (.setDomain domain)
      (.setWorkflowType workflow-type))))

(defn deprecate
  [domain name version ]
  (let [service (c/create)
        request (create-deprecate-workflow-type-request
                 domain name version)]
    (.deprecateWorkflowType service request)))

(defprotocol Execute
  (execute [this client]))

(extend ListOpenWorkflowExecutionsRequest
  Execute
  {:execute (fn [this client]
              (.listOpenWorkflowExecutions client this))})

(extend ListClosedWorkflowExecutionsRequest
  Execute
  {:execute (fn [this client]
              (.listClosedWorkflowExecutions client this))})

(defn list-workflow-executions
  [domain workflow-id request]
  (let [wf-filter (WorkflowExecutionFilter.)
        t-filter (ExecutionTimeFilter.)]
    (doto wf-filter
      (.setWorkflowId workflow-id))
    (doto t-filter
      (.setOldestDate (twenty-years-ago)))
    (doto request
      (.setDomain domain)
      (.setExecutionFilter wf-filter)
      (.setStartTimeFilter t-filter))
    (execute request (c/create))))

(defn list-open-workflow-executions
  [domain workflow-id]
  (list-workflow-executions domain
                            workflow-id
                            (ListOpenWorkflowExecutionsRequest.)))

(defn list-closed-workflow-executions
  [domain workflow-id]
  (list-workflow-executions domain
                            workflow-id
                            (ListClosedWorkflowExecutionsRequest.)))

(defn run-id-for-workflow-execution-info
  [wei]
  (.getRunId (.getExecution wei)))

(defn closed-run-ids-for-workflow-id
  [domain workflow-id]
  (let [weis (list-closed-workflow-executions domain workflow-id)
        wes (.getExecutionInfos weis)]
    (doall (map run-id-for-workflow-execution-info wes))))

(defn input-for-workflow-execution
  [domain workflow-id run-id]
  (-> (get-workflow-execution-history domain
                                      workflow-id
                                      run-id)
      .getEvents
      first
      .getWorkflowExecutionStartedEventAttributes
      .getInput))