(ns clj-aws-swf.test.events
  (:use 
        [clj-aws-swf.decision]
        [clj-aws-swf.activity]
        [clj-aws-swf.events])
  (:use [clojure.test])
  (:require [clj-aws-swf.utils :as u]
            [clj-aws-swf.workflow :as w]))


(deftest test-activity-series
  (let [swe-r (w/start-workflow-execution "HelloWorld"
                                    "1.1"
                                    "2"
                                    "HelloWorld"
                                    (str (rand-int 9999)))
        pdt-r (poll-for-decision-task "HelloWorld"
                                      "HelloWorld"
                                      "Wilco")
        pdt-r-b (bean pdt-r)
        sat-r (schedule-activity-task (:taskToken pdt-r-b)
                                      (u/uuid)
                                      "Double"
                                      "1.2"
                                      "2"
                                      "HelloWorld")
        pat-r (poll-for-activity-task "HelloWorld"
                                      "HelloWorld"
                                      "Wilco")
        pat-r-b (bean pat-r)
        cat-r (complete-activity-task (:taskToken pat-r-b)
                                      "4")]
    (println (bean swe-r))
    (println sat-r)
    (is (not= nil cat-r))))