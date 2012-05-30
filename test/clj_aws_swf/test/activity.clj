(ns clj-aws-swf.test.activity
  (:use 
        [clj-aws-swf.decision]
        [clj-aws-swf.activity])
  (:use [clojure.test])
  (:require [clj-aws-swf.utils :as u]
            [clj-aws-swf.workflow :as w]))

(deftest test-complete-activity-task
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

(deftest test-cancel-workflow-task
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
        cat-r (cancel-activity-task (:taskToken pat-r-b)
                                  "FAIL!")]
    (println (bean swe-r))
    (println cat-r)
    (is (not= nil cat-r))))

(deftest test-fail-workflow-task
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
        fat-r (fail-activity-task (:taskToken pat-r-b)
                                  "FAIL!"
                                  "Who knows?")]
    (println (bean swe-r))
    (println fat-r)
    (is (not= nil fat-r))))

(deftest test-register-and-deprecate
  (let [v (str (rand-int 9999999)) 
        r (register "HelloWorld" "wassup" v "what"
                    "1000" "1000" "1000" "sup")
        d (deprecate "HelloWorld" "wassup" v)]
    (is (= nil r))
    (is (= nil d))))