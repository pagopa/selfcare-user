locals {
  project = "${var.prefix}-${var.env_short}"

  github = {
    org        = "pagopa"
    repository = "selfcare-user"
  }

  repo_secrets = {
    "AZURE_SUBSCRIPTION_ID" = data.azurerm_client_config.current.subscription_id
    "AZURE_TENANT_ID"       = data.azurerm_client_config.current.tenant_id,
    "SONAR_TOKEN"           = data.azurerm_key_vault_secret.key_vault_sonar.value,
  }

  env_cd_variables = {}

  env_ci_secrets = {
    "AZURE_CLIENT_ID_CI" = module.identity_ci.identity_client_id
  }

  env_cd_secrets = {
    "AZURE_CLIENT_ID_CD" = module.identity_cd.identity_client_id
  }
}
