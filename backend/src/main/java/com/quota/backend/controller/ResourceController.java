package com.quota.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quota.backend.model.Resource;
import com.quota.backend.service.ResourceService;


@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    
    final ResourceService rService;

    ResourceController(ResourceService rService) {
        this.rService = rService;
    }
    
    @GetMapping
    public List<Resource> getResources() {
        return rService.getResources();
    }

    @GetMapping("/{id}")
    public  ResponseEntity<Resource> getResourceById(@PathVariable  UUID id){
        Resource resource = rService.findById(id);
        if(resource!=null){
            return ResponseEntity.ok(resource);
        }
        return ResponseEntity.notFound().build();
    }
    
}
