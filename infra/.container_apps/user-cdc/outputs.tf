output "container_app_resource_group_name" {
  value = azapi_resource.container_app_user_cdc.location
}

output "container_app_environment_name" {
  value = data.azurerm_container_app_environment.container_app_environment.name
}

output "container_app_name" {
  value = azapi_resource.container_app_user_ms.name
}