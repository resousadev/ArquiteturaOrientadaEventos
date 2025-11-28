#!/bin/bash
# ===========================================
# LocalStack AWS Resources Initialization
# ===========================================
# This script is automatically executed when LocalStack starts
# It creates all necessary AWS resources for local development

set -e

echo "============================================"
echo "🚀 Initializing LocalStack AWS resources..."
echo "============================================"

# Wait for LocalStack to be fully ready
echo "⏳ Waiting for LocalStack services..."
sleep 2

# -------------------------------------------
# EventBridge Setup
# -------------------------------------------
echo ""
echo "📡 Setting up EventBridge..."

# Create EventBridge Event Bus
awslocal events create-event-bus --name status-pedido-bus || echo "Event bus already exists"
echo "✅ Event bus 'status-pedido-bus' created"

# List event buses to verify
awslocal events list-event-buses

# -------------------------------------------
# SQS Setup
# -------------------------------------------
echo ""
echo "📨 Setting up SQS queues..."

# Create Dead Letter Queue (DLQ) first
awslocal sqs create-queue \
    --queue-name checkout-events-dlq \
    --attributes '{
        "MessageRetentionPeriod": "1209600"
    }' || echo "DLQ already exists"
echo "✅ DLQ 'checkout-events-dlq' created"

# Get DLQ ARN
DLQ_ARN=$(awslocal sqs get-queue-attributes \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/checkout-events-dlq \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

echo "📝 DLQ ARN: $DLQ_ARN"

# Create main SQS Queue with DLQ redrive policy
awslocal sqs create-queue \
    --queue-name checkout-events-queue \
    --attributes '{
        "VisibilityTimeout": "30",
        "MessageRetentionPeriod": "345600",
        "RedrivePolicy": "{\"deadLetterTargetArn\":\"'"$DLQ_ARN"'\",\"maxReceiveCount\":\"3\"}"
    }' || echo "Queue already exists"
echo "✅ Queue 'checkout-events-queue' created"

# Get SQS Queue ARN
QUEUE_ARN=$(awslocal sqs get-queue-attributes \
    --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/checkout-events-queue \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

echo "📝 Queue ARN: $QUEUE_ARN"

# List queues to verify
awslocal sqs list-queues

# -------------------------------------------
# EventBridge Rules Setup
# -------------------------------------------
echo ""
echo "🔗 Setting up EventBridge rules..."

# Create EventBridge Rule to capture all checkout events
awslocal events put-rule \
    --name checkout-to-sqs-rule \
    --event-bus-name status-pedido-bus \
    --event-pattern '{
        "source": ["web", "ms-checkout"],
        "detail-type": ["PENDING", "APPROVED", "REJECTED", "CANCELLED"]
    }' \
    --state ENABLED || echo "Rule already exists"
echo "✅ Rule 'checkout-to-sqs-rule' created"

# Set SQS as target for EventBridge rule
awslocal events put-targets \
    --rule checkout-to-sqs-rule \
    --event-bus-name status-pedido-bus \
    --targets '[{
        "Id": "sqs-target",
        "Arn": "'"$QUEUE_ARN"'"
    }]' || echo "Target already exists"
echo "✅ SQS target configured for rule"

# List rules to verify
awslocal events list-rules --event-bus-name status-pedido-bus

# -------------------------------------------
# CloudWatch Logs Setup
# -------------------------------------------
echo ""
echo "📊 Setting up CloudWatch Logs..."

# Create log group for the application
awslocal logs create-log-group --log-group-name /ms-checkout/events || echo "Log group already exists"
awslocal logs create-log-stream --log-group-name /ms-checkout/events --log-stream-name eventbridge || echo "Log stream already exists"
echo "✅ CloudWatch log group '/ms-checkout/events' created"

# -------------------------------------------
# Summary
# -------------------------------------------
echo ""
echo "============================================"
echo "✅ LocalStack initialization complete!"
echo "============================================"
echo ""
echo "📋 Resources created:"
echo "   • EventBridge bus: status-pedido-bus"
echo "   • SQS queue: checkout-events-queue"
echo "   • SQS DLQ: checkout-events-dlq"
echo "   • EventBridge rule: checkout-to-sqs-rule"
echo "   • CloudWatch log group: /ms-checkout/events"
echo ""
echo "🔗 Endpoints:"
echo "   • LocalStack: http://localhost:4566"
echo "   • EventBridge: http://localhost:4566"
echo "   • SQS: http://localhost:4566"
echo "============================================"
