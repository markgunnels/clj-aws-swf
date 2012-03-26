(ns clj-aws-swf.test.decision
  (:use [clj-aws-swf.workflow]
        [clj-aws-swf.decision]
        [clj-aws-swf.activity]
        )
  (:use [clojure.test])
  (:require [clj-aws-swf.utils :as u]))

(deftest test-poll-for-decision-task
  (let [swe-r (start-workflow-execution "HelloWorld"
                                    "1.1"
                                    "Yo"
                                    "HelloWorld"
                                    (str (rand-int 9999)))
        pdt-r (poll-for-decision-task "HelloWorld"
                                      "HelloWorld"
                                      "Wilco")]
    (is (not= nil pdt-r))))

(deftest test-schedule-activity-task
  (let [swe-r (start-workflow-execution "HelloWorld"
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
        pat-r-b (bean pat-r)]
    (is (= "2" (:input pat-r-b)))))

(deftest test-fail-workflow-execution
  (let [swe-r (start-workflow-execution "HelloWorld"
                                    "1.1"
                                    "2"
                                    "HelloWorld"
                                    (str (rand-int 9999)))
        pdt-r (poll-for-decision-task "HelloWorld"
                                      "HelloWorld"
                                      "Wilco")
        pdt-r-b (bean pdt-r)
        fwe-r (fail-workflow-execution (:taskToken pdt-r-b)
                                       "Yo Mamma"
                                       "Yo Mamma so fat...")]
    (println fwe-r)
    (is (= nil fwe-r))))

(deftest test-complete-workflow-execution
  (let [swe-r (start-workflow-execution "HelloWorld"
                                        "1.1"
                                        "2"
                                        "HelloWorld"
                                        (str (rand-int 9999)))
        pdt-r (poll-for-decision-task "HelloWorld"
                                      "HelloWorld"
                                      "Wilco")
        pdt-r-b (bean pdt-r)
        cwe-r (complete-workflow-execution (:taskToken pdt-r-b)
                                       "Whatup?!")]
    (println cwe-r)
    (is (= nil cwe-r))))

