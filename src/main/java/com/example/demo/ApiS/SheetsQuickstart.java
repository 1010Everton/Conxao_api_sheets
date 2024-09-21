package com.example.demo.ApiS;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "Ignore/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SheetsQuickstart.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static Credential SheetData() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1PphyVyiS967Lzx1lAKnOzd9JPLayhc0Bl-zstGy8uBY";
        final String range = "página1!A:F";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Read data from columns A to F
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            List<ValueRange> dataToUpdate = new ArrayList<>();
            int rowIndex = 2; // Assuming row 1 is headers, and the data starts from row 2.

            for (List<Object> row : values) {
                // Ignore rows that do not have at least five columns (i.e., complete data)
                if (row.size() > 4) {
                    Object ageValue = row.get(4); // "idade" is in the fifth column (index 4)

                    try {
                        // Verify if the age is a valid integer
                        int age = Integer.parseInt(ageValue.toString().trim());
                        if (age > 20) {
                            // Prepare the update for column F (index 5)
                            List<Object> rowData = new ArrayList<>();
                            rowData.add("TRUE"); // Set the cell to true

                            // Create a ValueRange object for the row to be updated in column F
                            ValueRange valueRange = new ValueRange()
                                    .setRange("página1!F" + rowIndex) // Update the F column of the current row
                                    .setValues(Collections.singletonList(rowData));
                            dataToUpdate.add(valueRange);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid age format in row " + rowIndex);
                    }
                }
                rowIndex++;
            }

            // If there are updates to be made, apply them
            if (!dataToUpdate.isEmpty()) {
                BatchUpdateValuesRequest batchUpdateRequest = new BatchUpdateValuesRequest()
                        .setValueInputOption("RAW")
                        .setData(dataToUpdate);
                service.spreadsheets().values().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
                System.out.println("Updates applied to the sheet.");
            } else {
                System.out.println("No updates needed.");
            }
        }
        return (Credential) values;
    }
}
