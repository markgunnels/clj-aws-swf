(ns com.221blabs.aws.swf.workflow-test
  (:use com.221blabs.aws.swf.workflow
        midje.sweet
        clojure.test)
  (:require [clj-aws-swf.client :as c]
            [com.221blabs.aws.swf.common-test :as ct])
  (:import [com.amazonaws.services.simpleworkflow.model
            Run
            History
            ExecutionTimeFilter
            WorkflowExecutionFilter
            TagFilter
            WorkflowTypeFilter
            WorkflowExecutionCount
            WorkflowExecutionInfos
            WorkflowExecutionDetail
            WorkflowTypeDetail]))

(deftest test-start-and-terminate-a-workflow
  (let [id (str (java.util.UUID/randomUUID))
        r (start ct/test-client ct/domain "add-and-multiply"
                 ct/version id (str [1 1 2 2]))]
    (is (instance? Run r))
    (is (= nil
           (terminate ct/test-client ct/domain id (.getRunId r))))))

(deftest test-get-history-execution-history
  (let [id (str (java.util.UUID/randomUUID))
        r (start ct/test-client ct/domain "add-and-multiply"
                 ct/version id (str [1 1 2 2]))
        _ (terminate ct/test-client ct/domain id (.getRunId r))
        h (get-execution-history ct/test-client ct/domain id (.getRunId r))]
    (is (= (instance? History h)))))

(deftest test-filter-creation
  (let [wfef (create-execution-filter "B")
        timef (create-time-filter (java.util.Date.)  nil)
        tagf (create-tag-filter "B")
        typef (create-type-filter "B" "B")]
    (is (instance? WorkflowExecutionFilter wfef))
    (is (instance? ExecutionTimeFilter timef))
    (is (instance? TagFilter tagf))
    (is (instance? WorkflowTypeFilter typef))))

(deftest test-count-executions
  (let [id (str (java.util.UUID/randomUUID))
        d (java.util.Date.)
        r (start ct/test-client ct/domain "add-and-multiply"
                 ct/version id (str [1 1 2 2]))
        typef (create-type-filter "add-and-multiply" ct/version)
        timef (create-time-filter d nil)
        open-count (count-open-executions ct/test-client ct/domain
                                          {:type-filter typef
                                           :start-time-filter timef})
        _ (terminate ct/test-client ct/domain id (.getRunId r))
        closed-count (count-closed-executions ct/test-client ct/domain
                                            {:type-filter typef
                                             :start-time-filter timef})]
    (is (= (instance? WorkflowExecutionCount open-count)))    
    (is (= (instance? WorkflowExecutionCount closed-count)))    
    (is (= (> (.getCount open-count) 0)))    
    (is (= (> (.getCount closed-count) 0)))))

(deftest test-list-executions
  (let [id (str (java.util.UUID/randomUUID))
        d (java.util.Date.)
        r (start ct/test-client ct/domain "add-and-multiply"
                 ct/version id (str [1 1 2 2]))
        typef (create-type-filter "add-and-multiply" ct/version)
        timef (create-time-filter d nil)
        open-list (list-open-executions ct/test-client
                                        ct/domain
                                        100
                                        nil
                                        {:type-filter typef
                                         :start-time-filter timef})
        _ (terminate ct/test-client ct/domain id (.getRunId r))
        closed-list (list-closed-executions ct/test-client
                                            ct/domain
                                            100
                                            nil
                                            {:type-filter typef
                                             :start-time-filter timef})]
    (is (= (instance? WorkflowExecutionInfos open-list)))    
    (is (= (instance? WorkflowExecutionInfos closed-list)))    
    (is (= (> (count (.getExecutionInfos open-list)) 0)))    
    (is (= (> (count (.getExecutionInfos closed-list)) 0)))))

(deftest test-describe-execution
  (let [id (str (java.util.UUID/randomUUID))
        r (start ct/test-client ct/domain "add-and-multiply"
                 ct/version id (str [1 1 2 2]))
        _ (terminate ct/test-client ct/domain id (.getRunId r))
        h (describe-execution ct/test-client ct/domain id (.getRunId r))]
    (is (= (instance? WorkflowExecutionDetail h)))))

(deftest test-describe-type
  (let [t (describe-type ct/test-client ct/domain "add-and-multiply"
                         ct/version)]
    (is (= (instance? WorkflowTypeDetail t)))))