import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { ResourceCard } from "./ResourceCard";
import type { Resource } from "../types/api";
import { ResourceDetails } from "./ResourceDetails";

export function ResourceDashboard() {
    const [resources, setResources] = useState<Resource[]>([]);
    // 1. Maintain a clean string layout enum state rather than binary booleans
    const [viewState, setViewState] = useState<'loading' | 'success' | 'error'>('loading');
    // 2. Align type schema parameter definition contract with matching state union types
    const [resourceDetailID, setResourceDetailID] = useState<string | null>(null);

    useEffect(() => {
        const abort: AbortController = new AbortController();
        
        async function triggerApi() {
            try {
                setViewState('loading');
                const response: Response = await apiClient('/api/v1/resources', { signal: abort.signal });
                
                if (response.ok) {
                    let data = await response.json();
                    setResources(data);
                    setViewState('success');
                } else {
                    setResources([]);
                    setViewState('error');
                }
            } catch (error: any) {
                if (error.name === 'AbortError') return; // Exit safely on component unmount
                console.error(error);
                setResources([]);
                setViewState('error');
            }
        }
        
        triggerApi();
        return () => abort.abort();
    }, []);

    return (
        <div className="dashboard-layout" style={{ display: 'flex', gap: '2rem', padding: '1rem' }}>
            {/* Left Side Panel: Collection Workspace List View Layout */}
            <main style={{ flex: 1 }}>
                <h2>Resources</h2>
                <div id="resource-list" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {viewState === 'loading' && <p role="status">Loading resources...</p>}
                    
                    {viewState === 'error' && (
                        <p className="error-msg">Failed to load infrastructure data. Please try again.</p>
                    )}
                    
                    {viewState === 'success' && resources.length === 0 && (
                        <p>No active resources found in this project environment.</p>
                    )}

                    {viewState === 'success' && resources.length > 0 && (
                        resources.map((resource) => (
                            <ResourceCard 
                                resource={resource} 
                                key={String(resource.id)} // React consumes key safely here
                                detailsIDSetter={setResourceDetailID} 
                            />
                        ))
                    )}
                </div>
            </main>

            {/* Right Side Panel: Isolated Dynamic Inspection Metrics View Block */}
            <aside style={{ width: '320px', borderLeft: '1px solid #ccc', paddingLeft: '2rem' }}>
                <h3>Resource Details</h3>
                {/* 3. Keep details container always mounted so its internal lifecycle effects manage data loading fetching blocks safely */}
                <ResourceDetails id={resourceDetailID} />
            </aside>
        </div>
    );
}
