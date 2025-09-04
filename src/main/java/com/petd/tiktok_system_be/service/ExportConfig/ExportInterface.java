package com.petd.tiktok_system_be.service.ExportConfig;

import java.util.List;
import java.util.Map;

public interface ExportInterface {

    Map<String, String> run(List<String> items);
}
