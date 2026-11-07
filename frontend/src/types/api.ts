/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2026-11-07 19:44:47.

export interface Resource {
    id: string;
    name: string;
    type: ResourceType;
    zoneID: ZoneId;
}

export interface ZoneId extends Serializable {
}

export interface Serializable {
}

export type ResourceType = "ROOM" | "DESK" | "EQUIPMENT";
