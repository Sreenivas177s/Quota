

const BASE_URL = import.meta.env.API_SERVER_URL || '';

export async function apiClient(endpoint: string, options?: RequestInit){
    return fetch(`${BASE_URL}${endpoint}`,{
        ...options,
        headers:{
            'content-type':'application/json',
            ...options?.headers,
        }
    })
}