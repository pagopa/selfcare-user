env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-user"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 1
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
}

app_settings = [
  {
    name  = "USER_REGISTRY_URL"
    value = "https://api.uat.pdv.pagopa.it/user-registry/v1"
  },
  {
    name  = "KAFKA_BROKER"
    value = "selc-d-eventhub-ns.servicebus.windows.net:9093"
  },
  {
    name  = "KAFKA_USER_TOPIC"
    value = "sc-users"
  },
  {
    name  = "KAFKA_SASL_MECHANISM"
    value = "PLAIN"
  }
]

secrets_names = [
  "jwt-public-key",
  "mongodb-connection-string",
  "appinsights-instrumentation-key",
  "user-registry-api-key",
  "aws-ses-access-key-id",
  "aws-ses-secret-access-key",
  "eventhub-sc-users-selfcare-wo-connection-string-lc",
  "blob-storage-product-connection-string",
  "blob-storage-contract-connection-string"
  "eventhub-sc-users-selfcare-wo-connection-string-lc"
]

