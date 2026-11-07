package com.quota.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.quota.backend.model.Resource;
import com.quota.backend.repository.ResourceRepository;

@Service 
public class ResourceService {
    public List<Resource> getResources() {
        return ResourceRepository.getResources();
    }

    public Resource findById(UUID id){
        return ResourceRepository.getResourceById(id);
    }
}
