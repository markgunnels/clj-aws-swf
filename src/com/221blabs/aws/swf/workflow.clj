(ns {:description "Wraps the AWS SWF Workflow calls."
     :author "Mark Gunnels"}
  com.221blabs.aws.swf.workflow
  (:use clj-aws-swf.utils)
  (:require [clj-aws-swf.client :as c]
            [com.221blabs.aws.swf.common :as common]
            [com.221blabs.aws.swf.constraints :as cx]
            [clj-time.coerce :as time.coerce]
            [clj-time.core :as time.core])
  (:import [com.amazonaws.services.simpleworkflow.model
            History
            StartWorkflowExecutionRequest
            WorkflowType
            RegisterWorkflowTypeRequest
            DeprecateWorkflowTypeRequest
            TerminateWorkflowExecutionRequest
            GetWorkflowExecutionHistoryRequest
            WorkflowExecution
            WorkflowExecutionDetail
            WorkflowExecutionFilter
            WorkflowExecutionCount
            WorkflowExecutionInfos
            WorkflowTypeFilter
            WorkflowTypeDetail
            CountClosedWorkflowExecutionsRequest
            CountOpenWorkflowExecutionsRequest
            ExecutionTimeFilter
            TagFilter
            ChildPolicy
            TaskList
            DescribeWorkflowExecutionRequest
            DescribeWorkflowTypeRequest
            GetWorkflowExecutionHistoryRequest
            ListOpenWorkflowExecutionsRequest
            ListClosedWorkflowExecutionsRequest]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WORKFLOW EXECUTION ACTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn start
  "Starts a workflow execution."
  [client domain name version id input]
  {:pre [(cx/swf-client? client)
         (every? string? [domain name version id input])]
   :post [(cx/swf-run? %)]}
  (let [workflow-type (common/create-workflow-type name
                                                   version)]
    (.startWorkflowExecution client (doto (StartWorkflowExecutionRequest.)
                                      (.setInput input)
                                      (.setWorkflowType workflow-type)
                                      (.setDomain domain)
                                      (.setWorkflowId id)))))

(defn terminate
  "Terminates a workflow execution."
  [client domain workflow-id run-id]
  {:pre [(cx/swf-client? client)
         (every? string? [domain workflow-id run-id])]
   :post [(nil? %)]}
  (.terminateWorkflowExecution client (doto (TerminateWorkflowExecutionRequest.)
                                        (.setDomain domain)
                                        (.setRunId run-id)
                                        (.setWorkflowId workflow-id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WORKFLOW EXECUTION INTEROGATIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- create-workflow-execution-request
  [domain wf-execution]
  (doto (GetWorkflowExecutionHistoryRequest.)
    (.setDomain domain)
    (.setExecution wf-execution)))


(defn get-execution-history
  "Gets the execution history for a particular Run."
  [client domain wf-id run-id]
  {:pre [(cx/swf-client? client)
         (every? string? [domain wf-id run-id])]
   :post [(cx/swf-history? %)]}
  (.getWorkflowExecutionHistory client
                                (create-workflow-execution-request
                                 domain
                                 (common/create-workflow-execution wf-id
                                                                   run-id))))
(defn input-for-workflow-execution
  [client domain wf-id run-id]
  (-> (get-execution-history domain
                             workflow-id
                             run-id)
      .getEvents
      first
      .getWorkflowExecutionStartedEventAttributes
      .getInput))

;;filter functions
(defn create-execution-filter
  [id]
  {:pre [(string? id)]}
  (doto (WorkflowExecutionFilter.)
    (.setWorkflowId id)))

(defn create-time-filter
  [oldest-date latest-date]
  {:pre [(every? #(or (instance? java.util.Date %) (nil? %))
                 [oldest-date latest-date])]}
  (doto (ExecutionTimeFilter.)
    (.setOldestDate oldest-date)
    (.setLatestDate latest-date)))

(defn create-tag-filter
  [tag]
  {:pre [(string? tag)]}
  (doto (TagFilter.)
    (.setTag tag)))

(defn create-type-filter
  [name version]
  {:pre [(every? string? [name version])]}
  (doto (WorkflowTypeFilter.)
    (.setName name)
    (.setVersion version)))

(defn- set-count-filters
  [request filters]
  (if (contains? filters :execution-filter)
    (.setExecutionFilter request (:execution-filter filters)))
  (if (contains? filters :start-time-filter)
    (.setStartTimeFilter request (:start-time-filter filters)))
  (if (contains? filters :tag-filter)
    (.setTagFilter request (:tag-filter filters)))
  (if (contains? filters :type-filter)
    (.setTypeFilter request (:type-filter filters)))
  (if (contains? filters :close-time-filter)
    (.setCloseTimeFilter request (:close-time-filter filters)))
  (if (contains? filters :close-status-filter)
    (.setCloseStatusFilter request (:close-status-filter filters)))
  request)

(defn- populate-domain-and-filters
  [request domain filters]
  (.setDomain request domain)
  (set-count-filters request filters))

(defn count-open-executions
  "Counts the open executions selected by the filters"
  [client domain filters]
  {:pre [(cx/swf-client? client)
         (string? domain)
         (map? filters)]
   :post [(instance? WorkflowExecutionCount %)]}
  (.countOpenWorkflowExecutions client
                                (populate-domain-and-filters (CountOpenWorkflowExecutionsRequest.)
                                                             domain
                                                             filters)))

;;count-closed
;;execution-filter close-status-filter start-time-filter
;;close-time-filter tag-filter type-filter
(defn count-closed-executions
  "Counts the closed executions selected by the filters"
  [client domain filters]
  {:pre [(cx/swf-client? client)
         (string? domain)
         (map? filters)]
   :post [(instance? WorkflowExecutionCount %)]}
  (.countClosedWorkflowExecutions client
                                  (populate-domain-and-filters (CountClosedWorkflowExecutionsRequest.)
                                                               domain
                                                               filters)))
;; LIST
(defn populate-list-request
  [req domain max-page-size next-page-token filters]
  (doto (populate-domain-and-filters req domain filters)
    (.setMaximumPageSize (Integer. max-page-size))
    (.setNextPageToken next-page-token)))

;;list-open
(defn list-open-executions
  "Returns the open executions selected by the filters"
  [client domain max-page-size next-page-token filters]
  {:pre [(cx/swf-client? client)
         (every? #(or (string? %) (nil? %))
                 [domain next-page-token])
         (integer? max-page-size)
         (map? filters)]
   :post [(instance? WorkflowExecutionInfos %)]}
  (.listOpenWorkflowExecutions client
                               (populate-list-request (ListOpenWorkflowExecutionsRequest.)
                                                      domain max-page-size
                                                      next-page-token filters)))

;;list-closed
(defn list-closed-executions
  "Returns the closed executions selected by the filters"
  [client domain max-page-size next-page-token filters]
  {:pre [(cx/swf-client? client)
         (every? #(or (string? %) (nil? %))
                 [domain next-page-token])
         (integer? max-page-size)
         (map? filters)]
   :post [(instance? WorkflowExecutionInfos %)]}
  (.listClosedWorkflowExecutions client
                                 (populate-list-request (ListClosedWorkflowExecutionsRequest.)
                                                        domain max-page-size
                                                        next-page-token filters)))


;;describe-execution
;;DescribeWorkflowExecutionRequest
(defn describe-execution
  "Describes a workflow execution."
  [client domain wf-id run-id]
  {:pre [(cx/swf-client? client)
         (every? string? [domain wf-id run-id])]
   :post [(instance? WorkflowExecutionDetail %)]}
  (.describeWorkflowExecution client
                              (doto (DescribeWorkflowExecutionRequest.)
                                (.setDomain domain)
                                (.setExecution (common/create-workflow-execution
                                                wf-id
                                                run-id)))))


;;describe-type
(defn describe-type
  "Describes a workflow type."
  [client domain name version]
  {:pre [(cx/swf-client? client)
         (every? string? [domain name version])]
   :post [(instance? WorkflowTypeDetail %)]}
  (.describeWorkflowType client
                         (doto (DescribeWorkflowTypeRequest.)
                           (.setDomain domain)
                           (.setWorkflowType (common/create-workflow-type
                                              name
                                              version)))))



;;META
;;list types

;;register
;; default-execution-start-to-close-timeout
;;
;;description domain name version
(defn- create-register-workflow-type-request
  [domain name version description default-task-list-name default-child-policy
   default-execution-timeout default-task-timeout]
  (doto (RegisterWorkflowTypeRequest.)
    (.setDomain domain)
    (.setName name)
    (.setVersion version)
    (.setDescription description)
    (.setDefaultExecutionStartToCloseTimeout default-execution-timeout)
    (.setDefaultTaskStartToCloseTimeout default-task-timeout)
    (.setDefaultTaskList (common/create-task-list default-task-list-name))
    (.setDefaultChildPolicy default-child-policy)))

(defn register
  [client domain name version description default-task-list default-child-policy
   default-execution-timeout default-task-timeout]
  {:pre [(cx/swf-client? client)
         (every? string? [domain name version description default-task-list default-child-policy
                          default-execution-timeout default-task-timeout])]
   :post [(nil? %)]}
  (.registerWorkflowType client
                         (create-register-workflow-type-request
                          domain name version description default-task-list
                          default-child-policy default-execution-timeout
                          default-task-timeout)))

;;deprecate
(defn- create-deprecate-workflow-type-request
  [domain name version]
  (let [request (DeprecateWorkflowTypeRequest.)
        workflow-type (common/create-workflow-type name version)]
    (doto request
      (.setDomain domain)
      (.setWorkflowType workflow-type))))

(defn deprecate
  [client domain name version]
  (.deprecateWorkflowType client
                          (create-deprecate-workflow-type-request domain
                                                                  name
                                                                  version)))

;;signal
;;TODO: Implement later.

;;interogate
