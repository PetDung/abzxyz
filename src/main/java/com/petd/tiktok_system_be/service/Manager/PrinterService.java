package com.petd.tiktok_system_be.service.Manager;

import com.petd.tiktok_system_be.entity.Manager.Printer;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.repository.PrinterRepository;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PrinterService {

    PrinterRepository printerRepository;
    OrderRepository orderRepository;

    public Printer findById(String id) {
        return  printerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    public List<Printer> findAll() {
        return printerRepository.findAll();
    }

    public Printer create(Printer printer) {
        if(StringUtils.isBlank(printer.getName())){
            throw new AppException(ErrorCode.RQ);
        }
        return printerRepository.save(printer);
    }

    public Printer update(Printer printerRq, String id) {
        if(StringUtils.isBlank(printerRq.getName())){
            throw new AppException(ErrorCode.RQ);
        }
        Printer printer = this.findById(id);
        printer.setName(printerRq.getName());
        printer.setDescription(printerRq.getDescription());
        return printerRepository.save(printer);
    }




    public void delete(String id) {
        Printer printer = this.findById(id);
        printer.getOrders().forEach(order -> {
            order.setPrinter(null);
        });
        orderRepository.saveAll(printer.getOrders());
        printerRepository.delete(printer);
    }
}
