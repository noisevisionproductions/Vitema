export enum ApplicationType {
    NUTRILOG = 'nutrilog',
    YOUR_NEW_APP = 'scandal-shuffle'
}

export interface ApplicationConfig {
    type: ApplicationType;
    name: string;
    authMethod: 'firebase' | 'supabase';
}

export const APPLICATION_CONFIGS: Record<ApplicationType, ApplicationConfig> = {
    [ApplicationType.NUTRILOG]: {
        type: ApplicationType.NUTRILOG,
        name: 'NutriLog',
        authMethod: 'firebase'
    },
    [ApplicationType.YOUR_NEW_APP]: {
        type: ApplicationType.YOUR_NEW_APP,
        name: 'Scandal Shuffle',
        authMethod: 'supabase'
    }
};