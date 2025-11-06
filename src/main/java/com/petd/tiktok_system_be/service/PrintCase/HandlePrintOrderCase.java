package com.petd.tiktok_system_be.service.PrintCase;
import com.petd.tiktok_system_be.constant.PrintStatus;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.exception.AppException;
import com.petd.tiktok_system_be.exception.ErrorCode;
import com.petd.tiktok_system_be.repository.OrderRepository;
import com.petd.tiktok_system_be.sdk.printSdk.PrintSupplier;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;
import com.petd.tiktok_system_be.sdk.printSdk.PrinterFactory;
import com.petd.tiktok_system_be.service.Lib.TelegramService;
import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class HandlePrintOrderCase {

    PrinterFactory printerFactory;
    TelegramService telegramService;
    OrderRepository orderRepository;
    @Transactional
    public Order printOrder(Order order){
      try{

          if(StringUtils.isBlank(order.getLabel())){
              log.error("Label is empty");
              throw new AppException(4000, "Label is empty");
          }

          PrintSupplier supplier = printerFactory.getProvider(order.getPrinter().getCode());
          OrderResponse orderResponse = supplier.print(order);
          order.setPrintStatus(PrintStatus.PRINT_REQUEST_SUCCESS.toString());
          order.setCost(orderResponse.getAmount());
          order.setOrderFulfillId(orderResponse.getOrderFulfillId());
          order.setOriginPrintStatus(orderResponse.getOriginPrintStatus());
          telegramService.sendMessage("Đặt in thành công cho đơn " + order.getId());
      }catch (Exception e){
          log.error(e.getMessage());
          order.setPrintStatus(PrintStatus.PRINT_REQUEST_FAIL.toString());
          order.setErrorPrint(e.getMessage());
          throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
      }finally {
          long timestamp = Instant.now().getEpochSecond();
          order.setCreatePrintTime(timestamp);
          orderRepository.save(order);
      }
      return order;
    }

    public Order cancel(Order order){
        try {
            PrintSupplier supplier = printerFactory.getProvider(order.getPrinter().getCode());
            supplier.cancel(order);
            order.setPrintStatus(PrintStatus.PRINT_CANCEL.toString());
            telegramService.sendMessage("Hủy thành công cho đơn " + order.getId());
        }catch (AppException e){
            log.error(e.getMessage());
            order.setErrorPrint(e.getMessage());
            throw new AppException(409, e.getMessage());
        }catch (Exception e){
            log.error(e.getMessage());
            order.setErrorPrint("Lỗi hệ thống khi hủy");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }finally {
            orderRepository.save(order);
        }
        return order;
    }
    public Order synchronize(Order order){
        try {
            PrintSupplier supplier = printerFactory.getProvider(order.getPrinter().getCode());
            OrderResponse orderResponse = supplier.synchronize(order);
            order.setCost(orderResponse.getAmount());
            order.setOrderFulfillId(orderResponse.getOrderFulfillId());
            order.setOriginPrintStatus(orderResponse.getOriginPrintStatus());
        }catch (AppException e){
            log.error(e.getMessage());
            order.setErrorPrint(e.getMessage());
            throw new AppException(409, e.getMessage());
        }catch (Exception e){
            log.error(e.getMessage());
            order.setErrorPrint("Lỗi hệ thống khi hủy");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }finally {
            orderRepository.save(order);
        }
        return order;
    }
}
