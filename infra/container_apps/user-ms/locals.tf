locals {
  pnpg_suffix = var.is_pnpg == true ? "-pnpg" : ""
  project     = "${var.prefix}-${var.env_short}"

  key_vault_resource_group_name = "${local.project}${local.pnpg_suffix}-sec-rg"
  key_vault_name                = "${local.project}${local.pnpg_suffix}-kv"
}
