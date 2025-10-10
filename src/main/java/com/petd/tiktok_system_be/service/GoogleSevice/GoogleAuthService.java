package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GoogleAuthService {

    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            SheetsScopes.SPREADSHEETS
    );
    public GoogleCredentials getServiceAccountCredentials() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new RuntimeException("GOOGLE_APPLICATION_CREDENTIALS environment variable not set");
        }

        log.info("Loading Google Service Account credentials from {}", credentialsPath);

        return GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(SCOPES);
    }


    public Drive getDriveService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = getServiceAccountCredentials();

        return new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("TikTok System")
                .build();
    }

    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = getServiceAccountCredentials();

        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("TikTok System")
                .build();
    }
}
