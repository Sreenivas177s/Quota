package com.quota.backend.repository;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.quota.backend.model.Resource;
import com.quota.backend.model.ResourceType;

public class ResourceRepository {
    
    private static String UUIDKey = "quotaApplication";
    private static List<Resource> resources = List.of(
            new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Room 1", ResourceType.ROOM, ZoneId.of("America/Los_Angeles")), 
            new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Desk 2", ResourceType.DESK, ZoneId.of("America/Los_Angeles")),
            new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Equipment 3", ResourceType.EQUIPMENT, ZoneId.of("America/Los_Angeles"))
        );
    private static HashMap<UUID,Resource> map = resources.stream().collect(Collectors.toMap(
        Resource::id,               // Key mapper
        user -> user,                  // Value mapper
        (existing, replace) -> existing, // Merge function (handles duplicate keys if any)
        HashMap::new                   // Maps supplier explicitly to a HashMap
    ));

    public static List<Resource> getResources(){
        return resources;
    }
    public static Resource getResourceById(UUID id){
        return map.get(id);
    }

}
