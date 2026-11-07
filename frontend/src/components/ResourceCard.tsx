import type { Resource } from "../types/api";

interface ResourceCardProps {
    resource: Resource;
    detailsIDSetter: (id: string) => void;
}


export function ResourceCard({resource, detailsIDSetter} : ResourceCardProps){
    const handleClick = (elementID: string) =>{
        detailsIDSetter(elementID);
    }
    return (
        <div 
            id={resource.id} 
            onClick={() => handleClick(resource.id)} 
            role="button"
            tabIndex={0} // Allows keyboard navigation via Tab key
            style={{ cursor: 'pointer' }}
        >
            <h4>{resource.name}</h4>
        </div>
    );
}