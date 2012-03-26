(ns clj-aws-swf.core
  (:import [com.amazonaws ClientConfiguration]
           [com.amazonaws.auth AWSCredentials BasicAWSCredentials]
           [com.amazonaws.services.simpleworkflow AmazonSimpleWorkflow AmazonSimpleWorkflowClient]
           [com.amazonaws.services.simpleworkflow.flow ActivityWorker]
           [com.amazonaws.services.simpleworkflow.flow.annotations Activities Activity ActivityRegistrationOptions
            Execute Workflow WorkflowRegistrationOptions]
           [com.amazonaws.services.simpleworkflow.model
            StartWorkflowExecutionRequest
            WorkflowType
            TaskList
            PollForActivityTaskRequest
            PollForDecisionTaskRequest]))

