package ru.algocode.exam2022;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

class SheetsAPI {

    private Credential getCredential() {
        InputStream is = SheetsAPI.class
                .getResourceAsStream("/google_service_account.json");
        try {
            return GoogleCredential.fromStream(is)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String spreadsheetId;
    private Sheets service;

    SheetsAPI(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
        NetHttpTransport HTTP_TRANSPORT = null;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Algocode")
                .build();
    }

    List<List<Object>> get(String range) {
        try {
            ValueRange response = this.service.spreadsheets().values().get(this.spreadsheetId, range).execute();
            return response.getValues();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void update(String range, List<List<Object>> values) {
        try {
            ValueRange data = new ValueRange();
            data.setValues(values);
            this.service.spreadsheets().values().update(this.spreadsheetId, range, data)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

