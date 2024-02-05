prefix    = "selc"
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
  resource_group_name = "selc-u-sec-rg"
  name                = "selc-u-kv"
  secrets_names = [
    "mongodb-connection-string",
    "user-cdc-appinsights-connection-string"
  ]
}
