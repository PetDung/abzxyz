package com.petd.tiktok_system_be.repository;
import com.petd.tiktok_system_be.entity.Manager.Printer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrinterRepository extends JpaRepository<Printer,String> {

    List<Printer> findByIdIn(List<String> ids);
}
