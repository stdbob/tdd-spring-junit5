package com.globomantics.inventory.controller;

import com.globomantics.inventory.model.PurchaseRecord;
import com.globomantics.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@Slf4j
@RequestMapping(InventoryController.REQUEST_MAPPING)
public class InventoryController {

  public static final String REQUEST_MAPPING = "/inventory";

  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getInventoryRecord(@PathVariable Integer id) {

    return inventoryService.getInventoryRecord(id)
        .map(inventoryRecord -> {
          try {
            return ResponseEntity
                .ok()
                .location(new URI(REQUEST_MAPPING +"/" + inventoryRecord.getProductId()))
                .body(inventoryRecord);
          } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/purchase-record")
  public ResponseEntity<?> addPurchaseRecord(@RequestBody PurchaseRecord purchaseRecord) {
    log.info("Creating new purchase record: {}", purchaseRecord);

    return inventoryService.purchaseProduct(purchaseRecord.getProductId(), purchaseRecord.getQuantityPurchased())
        .map(inventoryRecord -> {
          try {
            return ResponseEntity
                .ok()
                .location(new URI(REQUEST_MAPPING + "/" + inventoryRecord.getProductId()))
                .body(inventoryRecord);
          } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .orElse(ResponseEntity.notFound().build());
  }

}