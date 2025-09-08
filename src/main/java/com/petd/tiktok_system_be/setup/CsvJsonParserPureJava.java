package com.petd.tiktok_system_be.setup;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CsvJsonParserPureJava {
    public static void main(String[] args) throws Exception {
        String inputFile = "D:\\petd_coding\\tiktok_shop_system\\data-1757299869835.csv";       // file input
        String outputFile = "D:\\petd_coding\\tiktok_shop_system\\data-1757299869835.json"; // file output

        // Đọc toàn bộ file
        List<String> lines = Files.readAllLines(Paths.get(inputFile));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Ghi header
            writer.write("orders: {");
            writer.newLine();
            writer.write("[");
            writer.newLine();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // Bỏ thừa dấu " và chuẩn hóa
                String raw = line.trim()
                        .replace("\"\"", "\"")
                        .replaceFirst("^\"", "")
                        .replaceFirst("\"$", "");

                // Parse thủ công (vì JSON format cố định)
                String shopId = extractValue(raw, "shopId");
                String orderId = extractValue(raw, "orderId");
                String limit = extractValue(raw, "limit");

                String jsonLine = String.format(
                        "{\"shopId\":\"%s\",\"orderId\":\"%s\",\"limit\":%s}",
                        shopId, orderId, limit
                );
                if(i != lines.size()-1){
                    jsonLine += ",";
                }
                // Ghi ra CSV
                writer.write(jsonLine);
                writer.newLine();
            }
            writer.write("]");
            writer.newLine();
            writer.write("}");
        }

        System.out.println("✅ Xuất file thành công: " + outputFile);
    }

    // Hàm parse thủ công: tìm theo key trong JSON string
    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";

        int colon = json.indexOf(":", start);
        int comma = json.indexOf(",", colon);
        if (comma == -1) comma = json.indexOf("}", colon);

        String value = json.substring(colon + 1, comma).trim();

        // Bỏ dấu " nếu có
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }
}
