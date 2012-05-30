(ns clj-aws-swf.domain
  (:require [clj-aws-swf.client :as c])
  (:import [com.amazonaws.services.simpleworkflow.model
            RegisterDomainRequest
            DeprecateDomainRequest]))

(defn- create-register-domain-request
  [name retention-period]
  (let [request (RegisterDomainRequest.)]
    (doto request
      (.setName name)
      (.setWorkflowExecutionRetentionPeriodInDays retention-period))))

(defn register
  [name retention-period]
  (let [service (c/create)
        request (create-register-domain-request name
                                                retention-period)]
    (.registerDomain service request)))

(defn- create-deprecate-domain-request
  [name]
  (let [request (DeprecateDomainRequest.)]
    (doto request
      (.setName name))))

(defn deprecate
  [name]
  (let [service (c/create)
        request (create-deprecate-domain-request name)]
    (.deprecateDomain service request)))

