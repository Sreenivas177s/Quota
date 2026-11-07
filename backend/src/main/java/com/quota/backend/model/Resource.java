package com.quota.backend.model;

import java.util.UUID;

public record Resource(UUID id, String name, ResourceType type,String zoneID) {
    
}
