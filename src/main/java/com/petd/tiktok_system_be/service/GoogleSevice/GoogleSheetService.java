package com.petd.tiktok_system_be.service.GoogleSevice;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.petd.tiktok_system_be.entity.Setting;
import com.petd.tiktok_system_be.repository.SettingRepository;
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
    SettingRepository settingRepository;


    public Sheets getSheetsService(Setting setting) throws Exception {
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                googleAuthService.getCredential(setting)
        )
                .setApplicationName("tiktok_system_be")
                .build();
    }


    public <T> void exportOrdersToSheet(String spreadsheetId, String sheetName, List<T> exports, Class<T> clazz, String keyFieldName) throws Exception {
        Setting setting = settingRepository.findAll().get(0);
        Sheets sheetsService = getSheetsService(setting);

        // 1. Lấy dữ liệu hiện tại trong sheet
        ValueRange existingData = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();

        List<List<Object>> sheetValues = existingData.getValues() != null ? existingData.getValues() : new ArrayList<>();

        // 2. Lấy tên field làm header
        List<String> headers = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toList();

        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndex.put(headers.get(i), i);
        }

        // 3. Nếu sheet trống, thêm header
        if (sheetValues.isEmpty()) {
            sheetValues.add(new ArrayList<>(headers));
        }

        // 4. Xây map key -> rowIndex trong sheet
        Map<String, Integer> keyToRowIndex = new HashMap<>();
        int keyColIndex = headerIndex.get(keyFieldName);
        for (int i = 1; i < sheetValues.size(); i++) { // Bỏ header
            List<Object> row = sheetValues.get(i);
            if (row.size() > keyColIndex) {
                keyToRowIndex.put(row.get(keyColIndex).toString(), i);
            }
        }

        // 5. Cập nhật hoặc thêm dữ liệu
        for (T export : exports) {
            // Lấy key từ từng phần tử
            Field keyField = clazz.getDeclaredField(keyFieldName);
            keyField.setAccessible(true);
            Object keyObj = keyField.get(export);
            if (keyObj == null) continue; // bỏ qua nếu key null
            String key = keyObj.toString();

            // Build row dữ liệu
            List<Object> row = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(export);
                row.add(value != null ? value.toString() : "");
            }

            if (keyToRowIndex.containsKey(key)) {
                // Update row
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


}
