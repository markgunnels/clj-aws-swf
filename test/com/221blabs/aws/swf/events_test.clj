(ns com.221blabs.aws.swf.events-test
  (:use com.221blabs.aws.swf.events
        midje.sweet
        clojure.test)
  (:require [clj-aws-swf.client :as c]
            [com.221blabs.aws.swf.common-test :as ct]
            [com.221blabs.aws.swf.workflow :as wf]
            [com.221blabs.aws.swf.decision :as d]
            [com.221blabs.aws.swf.activity :as a])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForActivityTaskRequest
            ActivityTask
            ActivityType]))

(deftest test-activity-series
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
        a (a/poll ct/test-client ct/domain "Add" "Me")
        c (a/complete ct/test-client (.getTaskToken a) "C")
        _ (wf/terminate ct/test-client ct/domain id (.getRunId r))
        h (wf/get-execution-history ct/test-client
                                    ct/domain
                                    id
                                    (.getRunId r))
        s (activity-series (.getEvents h))]
    (is (not ( nil? s)))))
