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
  max_replicas = 2
  scale_rules  = []
  cpu    = 0.5
  memory = "1Gi"
}

app_settings = [

]

key_vault = {
  resource_group_name = "selc-u-sec-rg"
  name                = "selc-u-kv"
  secrets_names = [
    "jwt-public-key",
    "mongodb-connection-string"
  ]
}