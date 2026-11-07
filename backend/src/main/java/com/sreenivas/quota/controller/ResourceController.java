package com.sreenivas.quota.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sreenivas.quota.model.Resource;
import com.sreenivas.quota.service.ResourceService;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/v1")
public class ResourceController {
    
    final ResourceService rService;

    ResourceController(ResourceService rService) {
        this.rService = rService;
    }
    
    @GetMapping("/resources")
    public List<Resource> getResources() {
        return rService.getResources();
    }

    @GetMapping("/resources/{id}")
    public  ResponseEntity<Resource> getResourceById(@PathVariable  UUID id){
        Resource resource = rService.findById(id);
        if(resource!=null){
            return ResponseEntity.status(200).body(resource);
        }
        return ResponseEntity.status(404).body(null);
    }
    
}
