(ns clj-aws-swf.events
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

(def initiated-event-set #{"ActivityTaskScheduled" "StartChildWorkflowExecutionInitiated"})

(defn event-type
  [event]
  (.getEventType event))

(defmulti attributes event-type)

(defmethod attributes "WorkflowExecutionStarted"
  [event]
  (.getWorkflowExecutionStartedEventAttributes event))

(defmethod attributes "WorkflowExecutionCompleted"
  [event]
  (.getWorkflowExecutionCompletedEventAttributes event))

(defmethod attributes "WorkflowExecutionFailed"
  [event]
  (.getWorkflowExecutionFailedEventAttributes event))

(defmethod attributes "WorkflowExecutionTimedOut"
  [event]
  (.getWorkflowExecutionTimedOutEventAttributes event))

(defmethod attributes "WorkflowExecutionCanceled"
  [event]
  (.getWorkflowExecutionCanceledEventAttributes event))

(defmethod attributes "WorkflowExecutionTerminated"
  [event]
  (.getWorkflowExecutionTerminatedEventAttributes event))

(defmethod attributes "WorkflowExecutionContinuedAsNew"
  [event]
  (.getWorkflowExecutionContinuedAsNewEventAttributes event))

(defmethod attributes "WorkflowExecutionCancelRequested"
  [event]
  (.getWorkflowExecutionCancelRequestedEventAttributes event))

(defmethod attributes "DecisionTaskScheduled"
  [event]
  (.getDecisionTaskScheduledEventAttributes event))

(defmethod attributes "DecisionTaskStarted"
  [event]
  (.getDecisionTaskStartedEventAttributes event))

(defmethod attributes "DecisionTaskCompleted"
  [event]
  (.getDecisionTaskCompletedEventAttributes event))

(defmethod attributes "DecisionTaskTimedOut"
  [event]
  (.getDecisionTaskTimedOutEventAttributes event))

(defmethod attributes "ActivityTaskScheduled"
  [event]
  (.getActivityTaskScheduledEventAttributes event))

(defmethod attributes "ScheduleActivityTaskFailed"
  [event]
  (.getScheduleActivityTaskFailedEventAttributes event))

(defmethod attributes "ActivityTaskStarted"
  [event]
  (.getActivityTaskStartedEventAttributes event))

(defmethod attributes "ActivityTaskCompleted"
  [event]
  (.getActivityTaskCompletedEventAttributes event))

(defmethod attributes "ActivityTaskFailed"
  [event]
  (.getActivityTaskFailedEventAttributes event))

(defmethod attributes "ActivityTaskTimedOut"
  [event]
  (.getActivityTaskTimedOutEventAttributes event))

(defmethod attributes "ActivityTaskCanceled"
  [event]
  (.getActivityTaskCanceledEventAttributes event))

(defmethod attributes "ActivityTaskCancelRequested"
  [event]
  (.getActivityTaskCancelRequestedEventAttributes event))

(defmethod attributes "RequestCancelActivityTaskFailed"
  [event]
  (.getRequestCancelActivityTaskFailedEventAttributes event))

(defmethod attributes "WorkflowExecutionSignaled"
  [event]
  (.getWorkflowExecutionSignaledEventAttributes event))

(defmethod attributes "MarkerRecorded"
  [event]
  (.getMarkerRecordedEventAttributes event))

(defmethod attributes "TimerStarted"
  [event]
  (.getTimerStartedEventAttributes event))

(defmethod attributes "StartTimerFailed"
  [event]
  (.getStartTimerFailedEventAttributes event))

(defmethod attributes "TimerFired"
  [event]
  (.getTimerFiredEventAttributes event))

(defmethod attributes "TimerCanceled"
  [event]
  (.getTimerCanceledEventAttributes event))

(defmethod attributes "CancelTimerFailed"
  [event]
  (.getCancelTimerFailedEventAttributes event))

(defmethod attributes "StartChildWorkflowExecutionInitiated"
  [event]
  (.getStartChildWorkflowExecutionInitiatedEventAttributes event))

(defmethod attributes "StartChildWorkflowExecutionFailed"
  [event]
  (.getStartChildWorkflowExecutionFailedEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionStarted"
  [event]
  (.getChildWorkflowExecutionStartedEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionCompleted"
  [event]
  (.getChildWorkflowExecutionCompletedEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionFailed"
  [event]
  (.getChildWorkflowExecutionFailedEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionTimedOut"
  [event]
  (.getChildWorkflowExecutionTimedOutEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionCanceled"
  [event]
  (.getChildWorkflowExecutionCanceledEventAttributes event))

(defmethod attributes "ChildWorkflowExecutionTerminated"
  [event]
  (.getChildWorkflowExecutionTerminatedEventAttributes event))

(defmethod attributes "SignalExternalWorkflowExecutionInitiated"
  [event]
  (.getSignalExternalWorkflowExecutionInitiatedEventAttributes event))

(defmethod attributes "ExternalWorkflowExecutionSignaled"
  [event]
  (.getExternalWorkflowExecutionSignaledEventAttributes event))

(defmethod attributes "SignalExternalWorkflowExecutionFailed"
  [event]
  (.getSignalExternalWorkflowExecutionFailedEventAttributes event))

(defmethod attributes "RequestCancelExternalWorkflowExecutionInitiated"
  [event]
  (.getRequestCancelExternalWorkflowExecutionInitiatedEventAttributes event))

(defmethod attributes "ExternalWorkflowExecutionCancelRequested"
  [event]
  (.getExternalWorkflowExecutionCancelRequestedEventAttributes event))

(defmethod attributes "RequestCancelExternalWorkflowExecutionFailed"
  [event]
  (.getRequestCancelExternalWorkflowExecutionFailedEventAttributes event))

(defn activity-task-scheduled-events
  [events]
  (filter #(= "ActivityTaskScheduled" (event-type %)) events))

(defn scheduled-event-id
  [event]
  (let [attrs (bean (attributes event))]
    (if (contains? attrs :scheduledEventId)
      (:scheduledEventId attrs)
      (:initiatedEventId attrs ))))

(defn has-scheduled-event-id?
  [event event-id]
  (let [attrs (bean (attributes event))]
    (or (and (contains? attrs :scheduledEventId)
             (= (:scheduledEventId attrs) event-id))
        (and (contains? attrs :initiatedEventId)
             (= (:initiatedEventId attrs) event-id))
        )))


(defn initiated-event?
  [event]
  (contains? initiated-event-set (event-type event)))

(defn initiated-events
  [events]
  (filter initiated-event? events))

(defn outcome-for-an-event
  [events event-id]
  (last (filter #(and (has-scheduled-event-id? % event-id)
                       (not (initiated-event? %)))
                 events)))

(defn outcome-for-an-activity
  [events event-id]
  (last (filter #(and (has-scheduled-event-id? % event-id)
                       (not= "ActivityTaskStarted" (.getEventType %)))
                 events)))

(defn input-from-activity-event
  [activity-event]
  (.getInput (attributes activity-event)))

(defmulti status-of-activity
  (fn [events activity-event]
    (.getEventType
     (outcome-for-an-activity events
                              (.getEventId activity-event)))))

(defmethod status-of-activity "ActivityTaskCompleted"
  [events activity-event]
  :completed)

(defmethod status-of-activity "ActivityTaskFailed"
  [events activity-event]
  :failed)

(defmethod status-of-activity "ActivityTaskTimedOut"
  [events activity-event]
  :timed-out)

(defmethod status-of-activity "ActivityTaskCanceled"
  [events activity-event]
  :canceled)

(defmethod status-of-activity "ActivityTaskCancelRequested"
  [events activity-event]
  :cancel-requested)

(defmulti activity-outcome-details
  (fn [events activity-event]
    (.getEventType
     (outcome-for-an-activity
      events
      (scheduled-event-id activity-event)))))

(defmethod activity-outcome-details "ActivityTaskCompleted"
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:result (.getResult attrs)}))

(defmethod activity-outcome-details "ActivityTaskFailed"
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:reason (.getReason attrs)
     :details (.getDetails attrs)}))

(defmethod activity-outcome-details "ActivityTaskTimedOut"
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:details (.getDetails attrs)}))

(defmethod activity-outcome-details "ActivityTaskCanceled"
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:details (.getDetails attrs)}))

(defmethod activity-outcome-details "ActivityTaskCancelRequested"
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:details (.getDetails attrs)}))

(defmethod activity-outcome-details "ChildWorkflowExecutionCompleted"
  [events activity-event]
  (let [outcome (outcome-for-an-event events
                                      (scheduled-event-id activity-event))
        attrs (attributes outcome)]
    {:result (.getResult attrs)}))

(defmethod activity-outcome-details "ChildWorkflowExecutionStarted"
  [events activity-event]
  {})

(defn activity-event-details
  [events activity-event]
  {:id (.getEventId activity-event)
   :input (input-from-activity-event activity-event)
   :status (status-of-activity events activity-event)
   :activity-type (.getName (.getActivityType (attributes activity-event)))})

(defn activity-outcome
  [events activity-event]
  (let [outcome (outcome-for-an-activity
                 events
                 (.getEventId activity-event))
        activity-details (activity-event-details events
                                                 activity-event)
        outcome-details (activity-outcome-details events
                                                  outcome)]
    (conj activity-details outcome-details)))

(defn activity-outcomes
  [events]
  (for [activity-event (activity-task-scheduled-events events)]
    (activity-outcome events activity-event)))


(defn event-status
  [events activity-event]
  (-> (.getEventType
       (outcome-for-an-event events
                             (.getEventId activity-event)))
      inflections/hyphenize
      keyword))

(defmulti activity-type event-type)

(defmethod activity-type "StartChildWorkflowExecutionInitiated"
  [event]
  (-> event
      attributes
      .getWorkflowType
      .getName))

(defmethod activity-type :default
  [event]
  (.getName (.getActivityType attributes )))

(defn event-details
  [events activity-event]
  {:id (.getEventId activity-event)
   :input (input-from-activity-event activity-event)
   :status (event-status events activity-event)
   :activity-type (activity-type activity-event)})

(defn event-outcome
  [events initiated-event]
  (let [outcome (outcome-for-an-event
                 events
                 (.getEventId initiated-event))
        event-details (event-details events
                                     initiated-event)
        outcome-details (activity-outcome-details events
                                                  outcome)]
    (conj event-details outcome-details)))

(defn event-outcomes
  [events]
  (for [activity-event (initiated-events events)]
    (event-outcome events activity-event)))