package com.petd.tiktok_system_be.controller;

import com.petd.tiktok_system_be.dto.response.ApiResponse;
import com.petd.tiktok_system_be.entity.Manager.Printer;
import com.petd.tiktok_system_be.service.Manager.PrinterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/printer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrinterController {

    PrinterService printerService;

    @PostMapping
    public ApiResponse<Printer> create(@RequestBody Printer printer) {
        return ApiResponse.<Printer>builder()
                .result(printerService.create(printer))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Printer> update(
            @PathVariable String id,
            @RequestBody Printer printer
    ) {
        return ApiResponse.<Printer>builder()
                .result(printerService.update(printer, id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<Printer>> get() {
        return ApiResponse.<List<Printer>>builder()
                .result(printerService.findAll())
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable String id) {
        printerService.delete(id);
        return ApiResponse.<String>builder()
                .result("Delete successfully")
                .build();
    }

}
