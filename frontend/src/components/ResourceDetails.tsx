import { useEffect, useState } from "react";
import type { Resource } from "../types/api";
import { apiClient } from "../api/client";


export interface ResourceDetailsProp {
    id: string | null
}
export function ResourceDetails({ id }: ResourceDetailsProp) {
    const [resource, setResource] = useState<Resource | null>(null);
    const [resourceLoaded, setResourceLoaded] = useState<boolean>(false);


    useEffect(() => {
        if (!id) {
            setResource(null);
            setResourceLoaded(false);
            return;
        }
        const controller = new AbortController();
        async function loadDetails() {
            let response: Response = await apiClient(`/api/v1/resources/${id}`, { signal: controller.signal });
            if (response.ok) {
                let data = await response.json()
                setResource(data)
                setResourceLoaded(true)
            } else {
                setResource(null)
                setResourceLoaded(false)
            }
        }
        loadDetails()
        return () => controller.abort();
    }, [id])
    return (
        <>
            {resourceLoaded
                ? <div id={`${resource?.id}-details`} >
                    <h4>{resource?.name}</h4>
                    <h4>{resource?.type}</h4>
                </div>
                : <p>No details</p>
            }
        </>
    );
}