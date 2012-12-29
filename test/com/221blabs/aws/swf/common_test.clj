(ns com.221blabs.aws.swf.common-test
  (:require [clj-aws-swf.client :as c]))

(def test-client (c/create (System/getenv "ACCESS_ID")
                           (System/getenv "SECRET_KEY")))

(def domain "MultiplyTest")
(def version "1.0")