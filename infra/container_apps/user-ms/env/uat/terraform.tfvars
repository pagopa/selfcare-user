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
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar",
  },
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "user-ms",
  },
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
    name  = "SHARED_ACCESS_KEY_NAME"
    value = "selfcare-wo"
  }

]

secrets_names = {
  "APPLICATIONINSIGHTS_CONNECTION_STRING"              = "appinsights-connection-string"
  "JWT-PUBLIC-KEY"                                     = "jwt-public-key"
  "MONGODB-CONNECTION-STRING"                          = "mongodb-connection-string"
  "USER-REGISTRY-API-KEY"                              = "user-registry-api-key"
  "AWS-SES-ACCESS-KEY-ID"                              = "aws-ses-access-key-id"
  "AWS-SES-SECRET-ACCESS-KEY"                          = "aws-ses-secret-access-key"
  "EVENTHUB-SC-USERS-SELFCARE-WO-CONNECTION-STRING-LC" = "eventhub-sc-users-selfcare-wo-connection-string-lc"
  "BLOB-STORAGE-PRODUCT-CONNECTION-STRING"             = "blob-storage-product-connection-string"
  "BLOB-STORAGE-CONTRACT-CONNECTION-STRING"            = "blob-storage-contract-connection-string"
  "EVENTHUB-SC-USERS-SELFCARE-WO-KEY-LC"               = "eventhub-sc-users-selfcare-wo-key-lc"
}
