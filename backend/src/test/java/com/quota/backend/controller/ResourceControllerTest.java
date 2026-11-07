package com.quota.backend.controller;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quota.backend.model.Resource;
import com.quota.backend.model.ResourceType;
import com.quota.backend.service.ResourceService;

@WebMvcTest(ResourceController.class)
public class ResourceControllerTest {

    private static final String UUIDKey = "quotaApplication";

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean 
    private ResourceService rService;

    @Test 
    public void validGetAllResources() throws Exception{
        Resource room = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Room 1", ResourceType.ROOM, ZoneId.of("America/Los_Angeles")); 
        Resource desk = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Desk 2", ResourceType.DESK, ZoneId.of("America/Los_Angeles"));
        Resource equipment = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Equipment 3", ResourceType.EQUIPMENT, ZoneId.of("America/Los_Angeles"));
        
        // define get behaviour
        when(rService.getResources()).thenReturn(List.of(room,desk,equipment));

        // asser and act
        mockMvc.perform(get("/api/v1/resources").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
    }

    @Test 
    public void getIndividualResources() throws Exception{
        Resource room = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Room 1", ResourceType.ROOM, ZoneId.of("America/Los_Angeles")); 
        Resource desk = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Desk 2", ResourceType.DESK, ZoneId.of("America/Los_Angeles"));
        Resource equipment = new Resource(UUID.nameUUIDFromBytes(UUIDKey.getBytes()), "Equipment 3", ResourceType.EQUIPMENT, ZoneId.of("America/Los_Angeles"));
        
        when(rService.findById(room.id())).thenReturn(room);
        when(rService.findById(desk.id())).thenReturn(desk);
        when(rService.findById(equipment.id())).thenReturn(equipment);

        mockMvc.perform(get("/api/v1/resources/"+room.id()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(room.id().toString()));

        mockMvc.perform(get("/api/v1/resources/806581b4-af01-4c59-9c63-d9d2d1332284")).andExpect(status().isNotFound());
    }
}
