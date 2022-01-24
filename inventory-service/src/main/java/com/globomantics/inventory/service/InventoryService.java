package com.globomantics.inventory.service;

import com.globomantics.inventory.model.InventoryRecord;

import java.util.Optional;

public interface InventoryService {
    Optional<InventoryRecord> getInventoryRecord(Integer productId);
    Optional<InventoryRecord> purchaseProduct(Integer productId, Integer quantity);
}