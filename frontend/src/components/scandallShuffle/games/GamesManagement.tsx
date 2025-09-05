import React, {useEffect, useState} from 'react';
import {Play, Users, X, Eye} from 'lucide-react';
import {GameSession} from "../../../types/scandallShuffle/database";
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import {toast} from '../../../utils/toast';
import {formatDistanceToNow} from 'date-fns';
import {pl} from 'date-fns/locale';
import SectionHeader from '../../shared/common/SectionHeader';
import {GameSessionApiService} from "../../../services/scandallShuffle/GameSessionApiService";

/**
 * Component for managing active and recent game sessions
 */
const GamesManagement: React.FC = () => {
    const [games, setGames] = useState<GameSession[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<'all' | 'active' | 'ended'>('all');

    useEffect(() => {
        let isMounted = true;

        const fetchGames = async () => {
            try {
                const gamesData = await GameSessionApiService.getRecent();
                if (isMounted) {
                    setGames(gamesData);
                }
            } catch (error) {
                console.error('Error fetching games:', error);
                if (isMounted) {
                    toast.error('Błąd podczas pobierania gier');
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        fetchGames().catch(console.error);

        return () => {
            isMounted = false;
        };
    }, []);

    /**
     * Get status badge styling based on game status
     */
    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'playing':
                return 'bg-green-100 text-green-800';
            case 'waiting':
                return 'bg-yellow-100 text-yellow-800';
            case 'ended':
                return 'bg-gray-100 text-gray-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status: string) => {
        switch (status) {
            case 'playing':
                return 'In game';
            case 'waiting':
                return 'Waiting';
            case 'ended':
                return 'Ended';
            default:
                return status;
        }
    };

    const filteredGames = games.filter(game => {
        if (filter === 'active') return game.status === 'playing' || game.status === 'waiting';
        if (filter === 'ended') return game.status === 'ended';
        return true;
    });

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner size="lg"/>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <SectionHeader
                title="Games management"
                description="Monitor active games and sessions (last 50 games)"
            />

            {/* Filter tabs */}
            <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg w-fit">
                {[
                    {key: 'all', label: 'All'},
                    {key: 'active', label: 'Active'},
                    {key: 'ended', label: 'Ended'}
                ].map((tab) => (
                    <button
                        key={tab.key}
                        onClick={() => setFilter(tab.key as any)}
                        className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                            filter === tab.key
                                ? 'bg-white text-green-600 shadow-sm'
                                : 'text-gray-600 hover:text-gray-900'
                        }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Games table */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Game
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Status
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Players (To do)
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Time
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Actions (To do)
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {filteredGames.map((game) => (
                        <tr key={game.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 whitespace-nowrap">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0 h-10 w-10">
                                        <div
                                            className="h-10 w-10 rounded-full bg-green-100 flex items-center justify-center">
                                            <Play className="h-5 w-5 text-green-600"/>
                                        </div>
                                    </div>
                                    <div className="ml-4">
                                        <div className="text-sm font-medium text-gray-900">
                                            {game.game_name}
                                        </div>
                                        <div className="text-sm text-gray-500">
                                            Code: {game.code}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                                    <span
                                        className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusBadge(game.status)}`}>
                                        {getStatusText(game.status)}
                                    </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                <div className="flex items-center">
                                    <Users className="h-4 w-4 mr-1 text-gray-400"/>
                                    {/* This would need to be fetched from a players table */}
                                    0/{game.max_players}
                                </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                {formatDistanceToNow(new Date(game.created_at), {
                                    addSuffix: true,
                                    locale: pl
                                })}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <div className="flex items-center space-x-2">
                                    <button className="text-indigo-600 hover:text-indigo-900">
                                        <Eye className="h-4 w-4"/>
                                    </button>
                                    {game.status !== 'ended' && (
                                        <button className="text-red-600 hover:text-red-900">
                                            <X className="h-4 w-4"/>
                                        </button>
                                    )}
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                {filteredGames.length === 0 && (
                    <div className="text-center py-12">
                        <Play className="mx-auto h-12 w-12 text-gray-400"/>
                        <h3 className="mt-2 text-sm font-medium text-gray-900">
                            No games
                        </h3>
                        <p className="mt-1 text-sm text-gray-500">
                            {filter === 'active'
                                ? 'Currently no active games.'
                                : 'Games will show up here when users will start one.'}
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default GamesManagement;