package br.com.lighthost.kotlinDactylApi.client.server.managers.backups.models

import br.com.lighthost.kotlinDactylApi.client.server.managers.backups.actions.ClientBackupActions
import java.time.OffsetDateTime

data class ClientBackupModel(
    val uuid:String,
    val isSuccessful:Boolean,
    val isLocked:Boolean,
    val name:String,
    val ignoredFiles:List<String>?,
    val checksum: String?,
    val bytes:Long,
    val createdAt:OffsetDateTime,
    val completedAt: OffsetDateTime?,
    val actions: ClientBackupActions)