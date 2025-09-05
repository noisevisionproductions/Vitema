import { supabase } from '../../config/supabase';

export class DashboardApiService {
    /**
     * Fetches aggregated stats for the main dashboard.
     */
    static async getStats() {
        const [profilesCount, scenariosCount, activeSessions] = await Promise.all([
            supabase.from('profiles').select('*', { count: 'exact', head: true }),
            supabase.from('scenarios').select('*', { count: 'exact', head: true }),
            supabase.from('game_sessions').select('*', { count: 'exact', head: true }).eq('status', 'playing')
        ]);

        return {
            totalUsers: profilesCount.count || 0,
            totalScenarios: scenariosCount.count || 0,
            activeSessions: activeSessions.count || 0
        };
    }
}