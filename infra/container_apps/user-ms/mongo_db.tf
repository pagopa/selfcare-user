resource "azurerm_cosmosdb_mongo_database" "selc_user" {
  name                = "selcUser"
  resource_group_name = local.mongo_db.mongodb_rg_name
  account_name        = local.mongo_db.cosmosdb_account_mongodb_name
}

resource "azurerm_management_lock" "mongodb_selc_user" {
  name       = "mongodb-selc-user-lock"
  scope      = azurerm_cosmosdb_mongo_database.selc_user.id
  lock_level = "CanNotDelete"
  notes      = "This items can't be deleted in this subscription!"
}

module "mongodb_collection_user_institutions" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//cosmosdb_mongodb_collection?ref=v7.39.0"

  name                = "userInstitutions"
  resource_group_name = local.mongo_db.mongodb_rg_name

  cosmosdb_mongo_account_name  = local.mongo_db.cosmosdb_account_mongodb_name
  cosmosdb_mongo_database_name = azurerm_cosmosdb_mongo_database.selc_user.name

  indexes = [{
      keys   = ["_id"]
      unique = true
    },
    {
      keys   = ["userId","institutionId"]
      unique = false
    }
  ]

  lock_enable = true
}

module "mongodb_collection_user_info" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//cosmosdb_mongodb_collection?ref=v7.39.0"

  name                = "userInfo"
  resource_group_name = local.mongo_db.mongodb_rg_name

  cosmosdb_mongo_account_name  = local.mongo_db.cosmosdb_account_mongodb_name
  cosmosdb_mongo_database_name = azurerm_cosmosdb_mongo_database.selc_user.name

  indexes = [{
    keys   = ["_id"]
    unique = true
    }
  ]

  lock_enable = true
}
