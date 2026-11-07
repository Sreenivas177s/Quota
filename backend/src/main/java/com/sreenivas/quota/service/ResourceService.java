package com.sreenivas.quota.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sreenivas.quota.model.Resource;
import com.sreenivas.quota.repository.ResourceRepository;

@Service 
public class ResourceService {
    public List<Resource> getResources() {
        return ResourceRepository.getResources();
    }

    public Resource findById(UUID id){
        return ResourceRepository.getResourceById(id);
    }
}
