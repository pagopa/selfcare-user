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
  max_replicas = 1
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
}

app_settings = [

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
]

