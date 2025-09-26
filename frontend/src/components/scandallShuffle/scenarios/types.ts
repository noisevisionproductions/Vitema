export type ScenarioStatus = 'pending' | 'approved' | 'rejected';
export type StatusFilter = 'all' | 'pending' | 'approved' | 'rejected';
export type DifficultyFilter = 'all' | 'easy' | 'medium' | 'hard';

export interface FilterState {
    status: StatusFilter;
    search: string;
    language: string;
}