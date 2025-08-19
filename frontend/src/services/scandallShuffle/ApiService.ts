import {supabase} from '../../config/supabase';
import {Database, GameSession, Profile, Scenario} from "../../types/scandallShuffle/database";

export class ScandalShuffleApiService {
    // Users/Profiles management
    static async getAllProfiles(): Promise<Profile[]> {
        const {data, error} = await supabase
            .from('profiles')
            .select('*')
            .order('created_at', {ascending: false});

        if (error) {
            throw new Error(error.message);
        }

        return data || [];
    }

    static async getProfileById(id: string): Promise<Profile | null> {
        const {data, error} = await supabase
            .from('profiles')
            .select('*')
            .eq('id', id)
            .single();

        if (error) {
            throw new Error(error.message);
        }

        return data;
    }

    static async updateProfile(id: string, updates: Partial<Profile>): Promise<Profile> {
        const {data, error} = await supabase
            .from('profiles')
            .update(updates)
            .eq('id', id)
            .select()
            .single();

        if (error) {
            throw new Error(error.message);
        }

        return data;
    }

    static async deleteProfile(id: string): Promise<void> {
        const {error} = await supabase
            .from('profiles')
            .delete()
            .eq('id', id);

        if (error) {
            throw new Error(error.message);
        }
    }

    // Scenarios management
    static async getAllScenarios(): Promise<Scenario[]> {
        const {data, error} = await supabase
            .from('scenarios')
            .select('*')
            .order('created_at', {ascending: false});

        if (error) {
            throw new Error(error.message);
        }

        return data || [];
    }

    static async getScenarioById(id: string): Promise<Scenario | null> {
        const {data, error} = await supabase
            .from('scenarios')
            .select('*')
            .eq('id', id)
            .single();

        if (error) {
            throw new Error(error.message);
        }

        return data;
    }

    static async createScenario(scenario: Database['public']['Tables']['scenarios']['Insert']): Promise<Scenario> {
        const {data, error} = await supabase
            .from('scenarios')
            .insert(scenario)
            .select()
            .single();

        if (error) {
            throw new Error(error.message);
        }

        return data;
    }

    static async updateScenario(id: string, updates: Database['public']['Tables']['scenarios']['Update']): Promise<Scenario> {
        const {data, error} = await supabase
            .from('scenarios')
            .update(updates)
            .eq('id', id)
            .select()
            .single();

        if (error) {
            throw new Error(error.message);
        }

        return data;
    }

    static async deleteScenario(id: string): Promise<void> {
        const {error} = await supabase
            .from('scenarios')
            .delete()
            .eq('id', id);

        if (error) {
            throw new Error(error.message);
        }
    }

    // Game Sessions (for stats/monitoring)
    static async getRecentGameSessions(limit: number = 50): Promise<GameSession[]> {
        const {data, error} = await supabase
            .from('game_sessions')
            .select('*')
            .order('created_at', {ascending: false})
            .limit(limit);

        if (error) {
            throw new Error(error.message);
        }

        return data || [];
    }

    // Dashboard stats
    static async getDashboardStats() {
        const [profilesCount, scenariosCount, activeSessions] = await Promise.all([
            supabase.from('profiles').select('*', {count: 'exact', head: true}),
            supabase.from('scenarios').select('*', {count: 'exact', head: true}),
            supabase.from('game_sessions').select('*', {count: 'exact', head: true}).eq('status', 'playing')
        ]);

        return {
            totalUsers: profilesCount.count || 0,
            totalScenarios: scenariosCount.count || 0,
            activeSessions: activeSessions.count || 0
        };
    }
}