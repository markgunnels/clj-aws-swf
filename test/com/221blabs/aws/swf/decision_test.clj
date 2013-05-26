(ns com.221blabs.aws.swf.decision-test
  (:use com.221blabs.aws.swf.decision
        midje.sweet
        clojure.test)
  (:require [clj-aws-swf.client :as c]
            [com.221blabs.aws.swf.common-test :as ct]
            [com.221blabs.aws.swf.workflow :as wf])
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

(deftest test-poll
  (let [id (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (poll ct/test-client ct/domain "add-and-multiply"
                "Me" nil nil)]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (instance? DecisionTask t))))

(deftest test-decision-creation
  (let [satd (create-schedule-activity-task-decision
              "add-and-multiply" "1.0" "1" "add-and-multiply" (str [1 1 2 2]))
        fwed (create-fail-workflow-execution-decision "A" "A")
        cwed (create-complete-workflow-execution-decision "A")
        sced (start-child-workflow-execution-decision "A" "1.0" "1" (str [1 1 2 2]))]
    (is (instance? Decision satd))
    (is (instance? Decision fwed))
    (is (instance? Decision cwed))
    (is (instance? Decision sced))))

(deftest test-decision-task-completed
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (poll ct/test-client ct/domain "add-and-multiply"
                "Me" nil nil)
        d (create-schedule-activity-task-decision
           "Add" "1.0" tid "Add" (str [1 1 2 2]))
        result (decision-task-completed ct/test-client
                                        (.getTaskToken t)
                                        d)]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (nil? result))))

(deftest test-fail-workflow-execution
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (poll ct/test-client ct/domain "add-and-multiply"
                "Me" nil nil)
        result (fail-workflow-execution ct/test-client
                                        t
                                        "cuz")]
    (is (nil? result))))

