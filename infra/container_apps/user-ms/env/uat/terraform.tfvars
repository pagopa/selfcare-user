env_short = "u"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Uat"
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
    name  = "USER-CDC-MONGODB-WATCH-ENABLED"
    value = "false"
  },
  {
    name  = "USER_REGISTRY_URL"
    value = "https://api.uat.pdv.pagopa.it/user-registry/v1"
  },
  {
    name  = "EVENT_HUB_BASE_PATH"
    value = "https://selc-u-eventhub-ns.servicebus.windows.net/sc-users"
  },
  {
    name = "SHARED_ACCESS_KEY_NAME"
    value = "selfcare-wo"
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
  "blob-storage-contract-connection-string",
  "eventhub-sc-users-selfcare-wo-key-lc"
]
