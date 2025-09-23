package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.petd.tiktok_system_be.entity.Auth.Setting;
import com.petd.tiktok_system_be.entity.Auth.SettingSystem;
import com.petd.tiktok_system_be.repository.SettingRepository;
import com.petd.tiktok_system_be.repository.SettingSystemRepository;
import com.petd.tiktok_system_be.service.Auth.SettingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleSheetService<T> {

    GoogleAuthService googleAuthService;
    SettingService settingService;
    SettingSystemRepository settingSystemRepository;


    public Sheets getSheetsService(SettingSystem setting) throws Exception {
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                googleAuthService.getCredential(setting)
        )
                .setApplicationName("tiktok_system_be")
                .build();
    }
    public <T> void exportOrdersToSheet(String spreadsheetId, String sheetName,
                                  List<T> exports, Class<T> clazz, String keyFieldName) throws Exception {

        SettingSystem setting = settingSystemRepository.findAll().get(0);
        Sheets sheetsService = getSheetsService(setting);

        // 1. Lấy dữ liệu hiện có từ sheet
        ValueRange existingData = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> sheetValues = existingData.getValues() != null
                ? existingData.getValues()
                : new ArrayList<>();

        // 2. Lấy header từ class
        List<String> headers = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toList();

        // Map header -> index
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndex.put(headers.get(i), i);
        }

        // 3. Đảm bảo header ở hàng đầu tiên
        if (sheetValues.isEmpty()) {
            sheetValues.add(new ArrayList<>(headers));
            updateHeader(spreadsheetId, sheetName, headers,sheetsService);
        } else {
            List<Object> firstRow = sheetValues.get(0);
            boolean headerMismatch = firstRow.size() != headers.size()
                    || !firstRow.equals(headers);

            if (headerMismatch) {
                updateHeader(spreadsheetId, sheetName, headers, sheetsService);
                sheetValues.set(0, new ArrayList<>(headers));
            }
        }

        // 4. Build map key -> rowIndex
        Map<String, Integer> keyToRowIndex = new HashMap<>();
        int keyColIndex = headerIndex.get(keyFieldName);

        for (int i = 1; i < sheetValues.size(); i++) { // bỏ qua header
            List<Object> row = sheetValues.get(i);
            if (row.size() > keyColIndex) {
                String keyVal = row.get(keyColIndex).toString();
                keyToRowIndex.put(keyVal, i);
            }
        }

        // 5. Duyệt dữ liệu export và ghi vào sheet
        for (T export : exports) {
            Field keyField = clazz.getDeclaredField(keyFieldName);
            keyField.setAccessible(true);
            Object keyObj = keyField.get(export);
            if (keyObj == null) continue;
            String key = keyObj.toString();

            // Build row data theo headers
            List<Object> row = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(export);
                row.add(value != null ? value.toString() : "");
            }

            if (keyToRowIndex.containsKey(key)) {
                // Update row đã tồn tại
                int rowIndex = keyToRowIndex.get(key);
                String range = sheetName + "!" + (rowIndex + 1) + ":" + (rowIndex + 1);
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, range, new ValueRange().setValues(List.of(row)))
                        .setValueInputOption("RAW")
                        .execute();
            } else {
                // Append mới
                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, sheetName, new ValueRange().setValues(List.of(row)))
                        .setValueInputOption("RAW")
                        .setInsertDataOption("INSERT_ROWS")
                        .execute();
            }
        }
    }

    // Helper: update header vào dòng đầu tiên
    private void updateHeader(String spreadsheetId, String sheetName, List<String> headers, Sheets sheetsService) throws Exception {
        String headerRange = sheetName + "!1:1"; // dòng 1
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, headerRange,
                        new ValueRange().setValues(List.of(new ArrayList<>(headers))))
                .setValueInputOption("RAW")
                .execute();
    }

    public static List<String> buildHeaders(Class<?> clazz, Object instance) throws IllegalAccessException {
        List<String> headers = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(instance);

            // Nếu field là List
            if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    headers.add(field.getName() + " - Option " + (i + 1));
                }
            } else {
                headers.add(field.getName());
            }
        }

        return headers;
    }
}
