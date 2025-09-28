package com.petd.tiktok_system_be.sdk.printSdk;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class PrinterFactory {

    private final Map<String, PrintSupplier> printerMap = new HashMap<>();


    public PrinterFactory(List<PrintSupplier> providers) {
        for (PrintSupplier p : providers) {
            printerMap.put(p.getCode(), p);
        }
    }

    public PrintSupplier getProvider(String code) {
        return printerMap.get(code);
    }
}
