prefix    = "selc"
env_short = "u"
location  = "westeurope"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Uat"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-user"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

key_vault = {
  resource_group_name = "selc-u-sec-rg"
  name                = "selc-u-kv"
}


cidr_subnet_selc_user_fn        = ["10.1.144.0/24"] #change?

function_always_on = false

app_service_plan_info = {
  kind                         = "Linux"
  sku_size                     = "P1v3"
  sku_tier                     = "PremiumV3"
  maximum_elastic_worker_count = 1
  worker_count                 = 1
  zone_balancing_enabled       = false
}

storage_account_info = {
  account_kind                      = "StorageV2"
  account_tier                      = "Standard"
  account_replication_type          = "LRS"
  access_tier                       = "Hot"
  advanced_threat_protection_enable = false
}

app_settings = {
  "MONGODB_CONNECTION_URI" = "@Microsoft.KeyVault(SecretUri=https://selc-u-kv.vault.azure.net/secrets/mongodb-connection-string/)",
  "JWT_BEARER_TOKEN" = "@Microsoft.KeyVault(SecretUri=https://selc-u-kv.vault.azure.net/secrets/jwt-bearer-token-functions/)"
}
