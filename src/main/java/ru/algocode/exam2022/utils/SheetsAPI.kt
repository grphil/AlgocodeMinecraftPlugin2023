@file:Suppress("DEPRECATION")

package ru.algocode.exam2022.utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException

class SheetsAPI(private val spreadsheetId: String, val plugin: Plugin) {
    private val credential: Credential?
        get() {
            val inputStream = File(plugin.dataFolder, "google_service_account.json").inputStream()
            try {
                return GoogleCredential.fromStream(inputStream)
                    .createScoped(setOf(SheetsScopes.SPREADSHEETS))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    private val service: Sheets

    init {
        var HTTP_TRANSPORT: NetHttpTransport? = null
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
        service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(plugin.config.getString("google_cloud_application_name"))
            .build()
    }

    operator fun get(range: String?): List<List<Any>>? {
        return try {
            val response = service.spreadsheets().values()[spreadsheetId, range].execute()
            response.getValues()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun update(range: String?, values: List<List<Any?>?>?) {
        try {
            val data = ValueRange()
            data.setValues(values)
            service.spreadsheets().values().update(spreadsheetId, range, data)
                .setValueInputOption("RAW")
                .execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}