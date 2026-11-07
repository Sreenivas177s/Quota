package com.quota.backend.repository;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.quota.backend.model.Resource;
import com.quota.backend.model.ResourceType;

public class ResourceRepository {
    
    private static final HashMap<UUID,Resource> map = new HashMap<>();
    static {
        UUID roomUuid = UUID.nameUUIDFromBytes("Room 1".getBytes());
        map.put(roomUuid, new Resource(roomUuid, "Room 1", ResourceType.ROOM, ZoneId.of("America/Los_Angeles")));

        UUID deskUuid = UUID.nameUUIDFromBytes("Desk 2".getBytes());
        map.put(deskUuid, new Resource(deskUuid, "Desk 2", ResourceType.DESK, ZoneId.of("America/Los_Angeles")));

        UUID eqUuid = UUID.nameUUIDFromBytes("Equipment 3".getBytes());
        map.put(eqUuid, new Resource(eqUuid, "Equipment 3", ResourceType.EQUIPMENT, ZoneId.of("America/Los_Angeles")));
        
    }

    public static List<Resource> getResources(){
        return new ArrayList<>(map.values());
    }
    public static Resource getResourceById(UUID id){
        return map.get(id);
    }

}
