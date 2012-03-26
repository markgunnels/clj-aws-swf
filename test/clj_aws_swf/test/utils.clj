(ns clj-aws-swf.test.core
  (:use [clj-aws-swf.utils])
  (:use [clojure.test]))

(deftest test-get-property
  (is "b" (get-property :a)))

