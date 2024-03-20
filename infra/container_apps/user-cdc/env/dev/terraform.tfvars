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
  cpu          = 0.25
  memory       = "0.5Gi"
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
    name  = "USER-CDC-MONGODB-WATCH-ENABLED"
    value = "false"
  }
]


secrets_names = {
  "APPLICATIONINSIGHTS_CONNECTION_STRING"   = "appinsights-connection-string"
  "MONGODB-CONNECTION-STRING"               = "mongodb-connection-string"
}

