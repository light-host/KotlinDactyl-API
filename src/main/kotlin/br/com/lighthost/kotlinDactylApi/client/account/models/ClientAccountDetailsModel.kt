package br.com.lighthost.kotlinDactylApi.client.account.models

import br.com.lighthost.kotlinDactylApi.client.account.actions.ClientAccountActions

data class ClientAccountDetailsModel(
    val id:Int,
    val isAdmin:Boolean,
    val username:String,
    val email:String,
    val firstName:String,
    val lastName:String,
    val language:String,
    val actions:ClientAccountActions)
