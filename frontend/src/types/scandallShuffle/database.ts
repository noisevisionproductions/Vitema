export interface Database {
    public: {
        Tables: {
            profiles: {
                Row: {
                    id: string
                    user_id: string
                    display_name: string | null
                    avatar_url: string | null
                    games_played: number | null
                    total_score: number
                    role: 'user' | 'admin' | 'owner'
                    created_at: string
                    updated_at: string
                }
                Insert: {
                    user_id: string
                    display_name?: string | null
                    avatar_url?: string | null
                    games_played?: number
                    total_score?: number
                    role?: 'user' | 'admin' | 'owner'
                }
                Update: {
                    display_name?: string | null
                    avatar_url?: string | null
                    games_played?: number
                    total_score?: number
                    updated_at?: string
                    role?: 'user' | 'admin' | 'owner'
                }
            }
            scenarios: {
                Row: {
                    id: string
                    name: string
                    description: string | null
                    suggested_players: number
                    max_players: number
                    difficulty: string
                    duration_minutes: number
                    image_url: string | null
                    solution: string | null
                    average_rating: number | null
                    total_ratings: number
                    created_at: string
                    updated_at: string
                }
                Insert: {
                    name: string
                    description?: string | null
                    suggested_players?: number
                    max_players?: number
                    difficulty?: string
                    duration_minutes?: number
                    image_url?: string | null
                    solution?: string | null
                }
                Update: {
                    name?: string
                    description?: string | null
                    suggested_players?: number
                    max_players?: number
                    difficulty?: string
                    duration_minutes?: number
                    image_url?: string | null
                    solution?: string | null
                    average_rating?: number | null
                    total_ratings?: number
                    updated_at?: string
                }
            }
            game_sessions: {
                Row: {
                    id: string
                    code: string
                    host_id: string
                    game_name: string
                    max_players: number
                    status: 'waiting' | 'playing' | 'ended'
                    current_round: number
                    cards_in_deck: number
                    discarded_count: number
                    game_phase: 'waiting' | 'starting' | 'distributing' | 'playing' | 'final-test' | 'results'
                    revealed_cards: any[]
                    quiz_score: number | null
                    final_score: number | null
                    scenario_id: string | null
                    current_player_turn: string | null
                    turn_start_time: string | null
                    actions_this_turn: number
                    duration_minutes: number | null
                    created_at: string
                    updated_at: string
                    ended_at: string | null
                }
            }
        }
    }
}

export type Scenario = Database['public']['Tables']['scenarios']['Row']
export type Profile = Database['public']['Tables']['profiles']['Row']
export type GameSession = Database['public']['Tables']['game_sessions']['Row']

export type {Database as default}