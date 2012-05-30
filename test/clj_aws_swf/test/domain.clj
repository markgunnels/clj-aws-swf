(ns clj-aws-swf.test.domain
  (:use [clj-aws-swf.domain])
  (:use [clojure.test]))

;; (deftest test-register-and-deprecate
;;   (let [name (str "TestMe" (rand-int 9999))
;;         r (register name
;;                     "7")
;;         d (deprecate name)]
;;     (println r)
;;     (println d)
;;     (is (= nil r))
;;     (is (= nil d))))
