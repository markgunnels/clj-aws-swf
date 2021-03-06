(ns clj-aws-swf.test.workflow
  (:use [clj-aws-swf.workflow])
  (:use [clojure.test]))

(deftest test-start-workflow-execution
  (let [r (start-workflow-execution "HelloWorld"
                                    "1.1"
                                    "Yo"
                                    "HelloWorld"
                                    (str (rand-int 9999)))]
    (println r)
    (is (not= nil r))))

(deftest test-start-workflow-execution
  (let [workflow-id                 (str (rand-int 9999))
        swe-r (start-workflow-execution "HelloWorld"
                                    "1.1"
                                    "Yo"
                                    "HelloWorld"
                                    workflow-id)
        twe-r (terminate-workflow-execution "HelloWorld"
                                            (:runId (bean swe-r))
                                            workflow-id)]
    (println swe-r)
    (println twe-r)
    (is (= nil twe-r))))


(deftest test-register-and-deprecate
  (let [v (str (rand-int 9999999)) 
        r (register "HelloWorld" "wassup" v "what"
                    "1000" "1000" "sup")
        d (deprecate "HelloWorld" "wassup" v)]
    (is (= nil r))
    (is (= nil d))))
