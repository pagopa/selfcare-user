prefix    = "selc"
env_short = "d"
is_pnpg   = true

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
  cpu    = 0.5
  memory = "1Gi"
}

app_settings = [

]

key_vault = {
  resource_group_name = "selc-d-sec-rg"
  name                = "selc-d-kv"
  secrets_names = [
    "jwt-public-key",
    "mongodb-connection-string",
    "appinsights-connection-string"
  ]
}
