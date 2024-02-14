prefix    = "selc"
env_short = "u"
is_pnpg   = true

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

secrets_names = [
    "jwt-public-key",
    "mongodb-connection-string"
]
