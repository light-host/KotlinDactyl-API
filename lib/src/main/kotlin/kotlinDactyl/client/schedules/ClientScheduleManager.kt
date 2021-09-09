package kotlinDactyl.client.schedules

import kotlinDactyl.client.details.ClientServerDetails
import kotlinDactyl.client.schedules.actions.ScheduleActions
import kotlinDactyl.client.schedules.actions.TaskActions
import kotlinDactyl.client.schedules.models.*
import kotlinDactyl.requests.BaseRequest
import kotlinDactyl.requests.RouteModels.ClientRoutes
import org.json.JSONObject
import java.time.OffsetDateTime

class ClientScheduleManager( private val server: ClientServerDetails, private val baseRequest: BaseRequest) {

    fun retrieveSchedules(): List<ClientScheduleModel> {
        val list:MutableList<ClientScheduleModel> = mutableListOf()
        JSONObject(baseRequest.executeRequest(ClientRoutes.SCHEDULES.getSchedules(server.identifier), null)).getJSONArray("data").forEach {
            it as JSONObject
            list.add(parseClientSchedule(it.getJSONObject("attributes").toString()))
        }
        return list
    }

    fun retrieveSchedulesById(id:Int): ClientScheduleModel {
        return parseClientSchedule(JSONObject(baseRequest.executeRequest(
            ClientRoutes.SCHEDULES.getSchedule(server.identifier, id), null))
            .getJSONObject("attributes").toString())
    }

    fun createSchedule(schedule:NewScheduleModel): ClientScheduleModel {
        val json = JSONObject().accumulate("name", schedule.name)
            .accumulate("is_active", schedule.isActive)
            .accumulate("minute", schedule.cron.minute)
            .accumulate("hour", schedule.cron.hour)
            .accumulate("day_of_week", schedule.cron.dayOfWeek)
            .accumulate("month", schedule.cron.month)
            .accumulate("day_of_month", schedule.cron.dayOfMonth)
            .accumulate("only_when_online", schedule.onlyWhenOnline)
        return parseClientSchedule(JSONObject(baseRequest.executeRequest(
            ClientRoutes.SCHEDULES.createSchedule(server.identifier), json.toString()))
            .getJSONObject("attributes").toString())
    }

        private fun parseClientSchedule(rawJson : String): ClientScheduleModel {
            val json = JSONObject(rawJson)
            val cronJson = json.getJSONObject("cron")
            val cron = ClientCronJobModel(
                cronJson.getString("day_of_week"),
                cronJson.getString("day_of_month"),
                cronJson.getString("month"),
                cronJson.getString("hour"),
                cronJson.getString("minute")
            )
            val updateModel = NewScheduleModel(json.getString("name"), json.getBoolean("is_active"), cron, json.getBoolean("only_when_online"))
            val tasks: MutableList<ClientTaskModel> = mutableListOf()
            json.getJSONObject("relationships").getJSONObject("tasks").getJSONArray("data").forEach {
                it as JSONObject
                tasks.add(parseClientTask(it.getJSONObject("attributes").toString(),json.getInt("id")))
            }
            return ClientScheduleModel(
                json.getInt("id"),
                json.getString("name"),
                cron,
                json.getBoolean("is_active"),
                json.getBoolean("is_processing"),
                json.getBoolean("only_when_online"),
                (if (json.get("last_run_at").toString() != "null") { OffsetDateTime.parse(json.getString("last_run_at")) } else{null}),
                OffsetDateTime.parse(json.getString("next_run_at")),
                OffsetDateTime.parse(json.getString("created_at")),
                OffsetDateTime.parse(json.getString("updated_at")),
                tasks,
                ScheduleActions(server, baseRequest,json.getInt("id")),
                updateModel)
        }

         fun parseClientTask(rawJson : String, scheduleId:Int): ClientTaskModel {
            val json = JSONObject(rawJson)
             val updateModel = NewTaskModel(json.getInt("sequence_id"), json.getString("action"),json.getString("payload"),json.getInt("time_offset"),json.getBoolean("continue_on_failure") )
             return ClientTaskModel(
                json.getInt("id"),
                json.getInt("sequence_id"),
                json.getString("action"),
                json.getString("payload"),
                json.getInt("time_offset"),
                json.getBoolean("is_queued"),
                json.getBoolean("continue_on_failure"),
                OffsetDateTime.parse(json.getString("created_at")),
                OffsetDateTime.parse(json.getString("updated_at")),
                TaskActions(server, baseRequest, json.getInt("id"), scheduleId ),
                updateModel)
        }

}