package com.quota.backend.model;

import java.time.ZoneId;
import java.util.UUID;

public record Resource(UUID id, String name, ResourceType type,ZoneId zoneID) {
    
}
