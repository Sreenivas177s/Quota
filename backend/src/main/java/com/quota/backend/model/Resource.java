package com.quota.backend.model;

import java.time.ZoneId;
import java.util.UUID;

import jakarta.persistence.Entity;

@Entity 
public record Resource(UUID id, String name, ResourceType type,ZoneId zoneID) {
    
}

public class Resource {
    private UUID id;
    private String name;
    private ResourceType resourceType;
    private
}