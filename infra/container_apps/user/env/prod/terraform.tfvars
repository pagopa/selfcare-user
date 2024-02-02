prefix    = "selc"
env_short = "p"

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
  scale_rules  = [
    {
      custom = {
        metadata = {
          "desiredReplicas" = "3"
          "start"           = "0 8 * * MON-FRI"
          "end"             = "0 19 * * MON-FRI"
          "timezone"        = "Europe/Rome"
        }
        type = "cron"
      }
      name = "cron-scale-rule"
    }
  ]
  cpu = 0.2
  memory = "0.25Gi"
}

app_settings = [
  {
    name  = "USER-CDC-MONGODB-WATCH-ENABLED"
    value = "false"
  }
]

key_vault = {
  resource_group_name = "selc-p-sec-rg"
  name                = "selc-p-kv"
  secrets_names = [
    "mongodb-connection-string",
    "user-appinsights-connection-string"
  ]
}
