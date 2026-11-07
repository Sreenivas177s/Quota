package com.quota.backend.repository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.quota.backend.model.Resource;
import com.quota.backend.model.ResourceType;

public class ResourceRepository {
    

    private static List<Resource> resources = List.of(
            new Resource(UUID.randomUUID(), "Room 1", ResourceType.ROOM, "America/Los_Angeles"), 
            new Resource(UUID.randomUUID(), "Desk 2", ResourceType.DESK, "America/Los_Angeles"),
            new Resource(UUID.randomUUID(), "Equipment 3", ResourceType.EQUIPMENT, "America/Los_Angeles")
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
