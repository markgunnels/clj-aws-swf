(ns clj-aws-swf.client
  (:use [clj-aws-swf.utils])
  (:import [com.amazonaws ClientConfiguration]
           [com.amazonaws.auth AWSCredentials BasicAWSCredentials]
           [com.amazonaws.services.simpleworkflow AmazonSimpleWorkflow AmazonSimpleWorkflowClient]))

(defn- create-client-configuration
  [socket-timeout]
  (let [config (new ClientConfiguration)]
    (.withSocketTimeout config socket-timeout)
    config))

(defn create
  ([]
     (create (get-property :access_id)
             (get-property :secret_key)))
  ([access-id secret-key]
     (create access-id secret-key (* 70 1000)))
  ([access-id secret-key socket-timeout]
     (let [config (create-client-configuration socket-timeout)
           aws-credentials (BasicAWSCredentials. access-id secret-key)
           client (AmazonSimpleWorkflowClient. aws-credentials config)]
       (.setEndpoint client (get-property :endpoint))
       client)))
