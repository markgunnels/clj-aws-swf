(ns com.221blabs.aws.swf.events
  (:require [clj-aws-swf.client :as c]
            [clj-aws-swf.common :as common]
            [inflections.core :as inflections])
  (:import [com.amazonaws.services.simpleworkflow.model
            TaskList
            PollForDecisionTaskRequest
            ScheduleActivityTaskDecisionAttributes
            Decision
            RespondDecisionTaskCompletedRequest
            ActivityType
            ActivityTask
            FailWorkflowExecutionDecisionAttributes
            CompleteWorkflowExecutionDecisionAttributes]))

;; alpha event set
(def alpha-event-type-set #{"ActivityTaskScheduled" "StartChildWorkflowExecutionInitiated"})

;; omega event set
(def omega-event-type-set #{"ActivityTaskCanceled" "ActivityTaskCompleted" "ActivityTaskFailed"
                            "ActivityTaskTimedOut"
                            "ChildWorkflowExecutionCompleted" "ChildWorkflowExecutionFailed"
                            "ChildWorkflowExecutionTimedOut" "ChildWorkflowExecutionCanceled"
                            "ChildWorkflowExecutionTerminated"})

(defn event-type
  [event]
  (.getEventType event))

(defn alpha-event?
  [event]
  (contains? alpha-event-type-set (event-type event)))

(defn omega-event?
  [event]
  (contains? omega-event-type-set (event-type event)))


(defmulti attributes event-type)

(defmethod attributes "ActivityTaskScheduled"
  [event]
  (let [a (.getActivityTaskScheduledEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getEventId event)
     :type (.getName (.getActivityType a))}))

(defmethod attributes "ActivityTaskCompleted"
  [event]
  (let [a (.getActivityTaskCompletedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getScheduledEventId a)
     :result (.getResult a)
     :status :completed}))

(defmethod attributes "ActivityTaskFailed"
  [event]
  (let [a (.getActivityTaskFailedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getScheduledEventId a)
     :details (.getDetails a)
     :reason (.getReason a)
     :status :failed}))

(defmethod attributes "ActivityTaskTimedOut"
  [event]
  (let [a (.getActivityTaskTimedOutEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getScheduledEventId a)
     :timeout-type (.getTimeoutType a)
     :status :timed-out}))

(defmethod attributes "ActivityTaskCanceled"
  [event]
  (let [a (.getActivityTaskCanceledEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getScheduledEventId a)
     :details (.getDetails a)
     :status :canceled}))

(defmethod attributes "StartChildWorkflowExecutionInitiated"
  [event]
  (let [a (.getStartChildWorkflowExecutionInitiatedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getEventId event)
     :type (.getName (.getWorkflowType a))}))

(defmethod attributes "ChildWorkflowExecutionCompleted"
  [event]
  (let [a (.getChildWorkflowExecutionCompletedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getInitiatedEvent a)
     :result (.getResult a)
     :status :completed}))

(defmethod attributes "ChildWorkflowExecutionFailed"
  [event]
  (let [a (.getChildWorkflowExecutionFailedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getInitiatedEvent a)
     :reason (.getReason a)
     :details (.getDetails a)
     :status :failed}))

(defmethod attributes "ChildWorkflowExecutionTimedOut"
  [event]
  (let [a (.getChildWorkflowExecutionTimedOutEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getInitiatedEvent a)
     :timeout-type (.getTimeoutType a)
     :status :timed-out}))

(defmethod attributes "ChildWorkflowExecutionCanceled"
  [event]
  (let [a (.getChildWorkflowExecutionCanceledEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getInitiatedEvent a)
     :details (.getDetails a)
     :status :canceled}))

(defmethod attributes "ChildWorkflowExecutionTerminated"
  [event]
  (let [a (.getChildWorkflowExecutionTerminatedEventAttributes event)]
    {:id (.getEventId event)
     :origin-id (.getInitiatedEvent a)
     :status :terminated}))

(defn extract-omega-event
  [events alpha-event]
  (attributes (first (filter #(and (omega-event? %)
                                   (= (:id (attributes alpha-event))
                                      (:origin-id (attributes %))))
                             events))))

(defn extract-alpha-events
  [events]
  (filter alpha-event? events))

(defn characterize-event
  [events alpha-event]
  (merge (attributes alpha-event)
         (extract-omega-event events alpha-event)))

(defn activity-series
  [events]
  (sort-by :origin-id (map #(characterize-event events %) (extract-alpha-events events))))