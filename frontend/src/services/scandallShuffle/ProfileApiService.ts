import {supabase} from '../../config/supabase';
import {Profile} from "../../types/scandallShuffle/database";

export class ProfileApiService {
    /**
     * Fetches all user profiles.
     */
    static async getAll(): Promise<Profile[]> {
        const {data, error} = await supabase
            .from('profiles')
            .select('*')
            .order('created_at', {ascending: false});

        if (error) throw new Error(error.message);
        return data || [];
    }

    /**
     * Fetches a single profile by its ID.
     */
    static async getById(id: string): Promise<Profile | null> {
        const {data, error} = await supabase
            .from('profiles')
            .select('*')
            .eq('id', id)
            .single();

        if (error) throw new Error(error.message);
        return data;
    }

    /**
     * Updates a profile.
     */
    static async update(id: string, updates: Partial<Profile>): Promise<Profile> {
        const {data, error} = await supabase
            .from('profiles')
            .update(updates)
            .eq('id', id)
            .select()
            .single();

        if (error) throw new Error(error.message);
        return data;
    }

    /**
     * Deletes a profile.
     */
    static async delete(id: string): Promise<void> {
        const {error} = await supabase
            .from('profiles')
            .delete()
            .eq('id', id);

        if (error) throw new Error(error.message);
    }
}