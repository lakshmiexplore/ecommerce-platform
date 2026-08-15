package com.store.inventory;

import com.store.inventory.entity.Inventory;
import com.store.inventory.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            if (inventoryRepository.count() == 0) {
                inventoryRepository.save(Inventory.builder().sku("PROD-HEADPHONE-01").quantity(100).reservedQuantity(0).build());
                inventoryRepository.save(Inventory.builder().sku("PROD-LG-TELEVISION-01").quantity(50).reservedQuantity(0).build());
                inventoryRepository.save(Inventory.builder().sku("PROD-LG-REFRIGERATOR-01").quantity(30).reservedQuantity(0).build());
                inventoryRepository.save(Inventory.builder().sku("PROD-SONY-BLUETOOTH-SPEAKERS-501").quantity(80).reservedQuantity(0).build());
            }
        };
    }
}