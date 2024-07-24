prefix    = "selc"
env_short = "p"
suffix_increment = "-002"
cae_name         = "cae-002"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-user"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 1
  scale_rules  = []
  cpu          = 1
  memory       = "2Gi"
}

app_settings = [
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar",
  },
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "user-cdc",
  },
  {
    name  = "USER_CDC_SEND_EVENTS_WATCH_ENABLED"
    value = "true"
  },
  {
    name  = "EVENT_HUB_BASE_PATH"
    value = "https://selc-p-eventhub-ns.servicebus.windows.net/sc-users"
  },
  {
    name  = "SHARED_ACCESS_KEY_NAME"
    value = "selfcare-wo"
  },
  {
    name  = "USER_REGISTRY_URL"
    value = "https://api.pdv.pagopa.it/user-registry/v1"
  }
]

secrets_names = {
  "APPLICATIONINSIGHTS_CONNECTION_STRING" = "appinsights-connection-string"
  "MONGODB-CONNECTION-STRING"             = "mongodb-connection-string"
  "STORAGE_CONNECTION_STRING"             = "blob-storage-product-connection-string"
  "EVENTHUB-SC-USERS-SELFCARE-WO-KEY-LC"  = "eventhub-sc-users-selfcare-wo-key-lc"
  "USER-REGISTRY-API-KEY"                 = "user-registry-api-key"
}

