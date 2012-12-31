(ns com.221blabs.aws.swf.activity-test
  (:use com.221blabs.aws.swf.activity
        midje.sweet
        clojure.test)
  (:require [clj-aws-swf.client :as c]
            [com.221blabs.aws.swf.common-test :as ct]
            [com.221blabs.aws.swf.workflow :as wf]
            [com.221blabs.aws.swf.decision :as d])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForActivityTaskRequest
            ActivityTask
            ActivityType]))

(deftest test-poll
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (d/poll ct/test-client ct/domain "add-and-multiply"
                  "Me" nil nil)
        d (d/create-schedule-activity-task-decision
           "Add" "1.0" tid "Add" (str [1 1 2 2]))
        _ (d/decision-task-completed ct/test-client
                                     (.getTaskToken t)
                                     d)
        a (poll ct/test-client ct/domain "Add" "Me")]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (instance? ActivityTask a))))

(deftest test-complete
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (d/poll ct/test-client ct/domain "add-and-multiply"
                  "Me" nil nil)
        d (d/create-schedule-activity-task-decision
           "Add" "1.0" tid "Add" (str [1 1 2 2]))
        _ (d/decision-task-completed ct/test-client
                                     (.getTaskToken t)
                                     d)
        a (poll ct/test-client ct/domain "Add" "Me")
        c (complete ct/test-client (.getTaskToken a) "C")]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (nil? c))))

(deftest test-fail
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (d/poll ct/test-client ct/domain "add-and-multiply"
                  "Me" nil nil)
        d (d/create-schedule-activity-task-decision
           "Add" "1.0" tid "Add" (str [1 1 2 2]))
        _ (d/decision-task-completed ct/test-client
                                     (.getTaskToken t)
                                     d)
        a (poll ct/test-client ct/domain "Add" "Me")
        c (fail ct/test-client (.getTaskToken a) "C" "D")]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (nil? c))))

(deftest test-cancel
  (let [id (str (java.util.UUID/randomUUID))
        tid (str (java.util.UUID/randomUUID))
        r (wf/start ct/test-client ct/domain "add-and-multiply"
                    ct/version id (str [1 1 2 2]))
        t (d/poll ct/test-client ct/domain "add-and-multiply"
                  "Me" nil nil)
        d (d/create-schedule-activity-task-decision
           "Add" "1.0" tid "Add" (str [1 1 2 2]))
        _ (d/decision-task-completed ct/test-client
                                     (.getTaskToken t)
                                     d)
        a (poll ct/test-client ct/domain "Add" "Me")
        c (cancel ct/test-client (.getTaskToken a) "D")]
    (wf/terminate ct/test-client ct/domain id (.getRunId r))
    (is (nil? c))))