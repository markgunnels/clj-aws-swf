(ns clj-aws-swf.decision
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.common :as common])
  (:import [com.amazonaws.services.simpleworkflow.model
            RespondDecisionTaskCompletedRequest]))

;;poller defmethod

(defprotocol SWFRequest
  (as-request [this]))

(defprotocol SWFType
  (as-type [this]))

(defprotocol SWFDoer
  (do [this client]))

;; (defprotocol SWFResponse
;;   (respond [request client]))

;; (defprotocol SWFStart
;;   (start [start client]))

;; (extend RespondDecisionTaskCompletedRequest
;;   SWFResponse
;;   (respond [request client]
;;            (.respondDecisionTaskCompleted client request)))