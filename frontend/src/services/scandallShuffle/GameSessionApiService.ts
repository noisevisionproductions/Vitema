import { supabase } from '../../config/supabase';
import { GameSession } from "../../types/scandallShuffle/database";

export class GameSessionApiService {
    /**
     * Fetches recent game sessions.
     */
    static async getRecent(limit: number = 50): Promise<GameSession[]> {
        const { data, error } = await supabase
            .from('game_sessions')
            .select('*')
            .order('created_at', { ascending: false })
            .limit(limit);

        if (error) throw new Error(error.message);
        return data || [];
    }
}