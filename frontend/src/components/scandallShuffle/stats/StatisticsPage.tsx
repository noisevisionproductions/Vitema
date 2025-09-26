import React, {useEffect, useState} from 'react';
import {supabase} from '../../../config/supabase';
import {Scenario, Profile} from '../../../types/scandallShuffle/database';
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import SectionHeader from '../../shared/common/SectionHeader';
import {BookOpen, Clock, Users, FileCheck, Star, UserCheck} from 'lucide-react';

// Interface to hold all calculated stats
interface StatsData {
    totalUsers: number;
    totalScenarios: number;
    pendingScenarios: number;
    gamesPlayed: number;
    statusDistribution: { approved: number; pending: number; rejected: number };
    languageDistribution: Record<string, number>;
    highestRatedScenario: Scenario | null;
    mostRatedScenario: Scenario | null;
    roleDistribution: Record<string, number>;
}

// Reusable component for displaying a single statistic
const StatCard: React.FC<{
    icon: React.ReactNode;
    title: string;
    value: string | number;
    description?: string
}> = ({icon, title, value, description}) => (
    <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
        <div className="flex items-center">
            <div className="bg-indigo-100 text-indigo-600 p-3 rounded-full">{icon}</div>
            <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">{title}</p>
                <p className="text-2xl font-bold text-gray-900">{value}</p>
            </div>
        </div>
        {description && <p className="text-xs text-gray-500 mt-2">{description}</p>}
    </div>
);

const StatisticsPage: React.FC = () => {
    const [stats, setStats] = useState<StatsData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                // Fetch all necessary data in parallel
                const [scenariosRes, profilesRes, gamesRes] = await Promise.all([
                    supabase.from('scenarios').select('*'),
                    supabase.from('profiles').select('role'),
                    supabase.from('game_sessions').select('id', {count: 'exact'})
                ]);

                const errorMessages = [
                    scenariosRes.error,
                    profilesRes.error,
                    gamesRes.error,
                ]
                    .map(error => error?.message)
                    .filter(Boolean) as string[];

                if (errorMessages.length > 0) {
                    const combinedMessage = errorMessages.join(', ');
                    setError(combinedMessage);
                    console.error("Failed to fetch statistics data:", combinedMessage);
                    return;
                }

                const scenarios: Scenario[] = scenariosRes.data || [];
                const profiles: Pick<Profile, 'role'>[] = profilesRes.data || [];

                // --- Process Data ---
                const statusDistribution = scenarios.reduce((acc, s) => {
                    acc[s.status] = (acc[s.status] || 0) + 1;
                    return acc;
                }, {} as Record<string, number>);

                const languageDistribution = scenarios.reduce((acc, s) => {
                    acc[s.language] = (acc[s.language] || 0) + 1;
                    return acc;
                }, {} as Record<string, number>);

                const roleDistribution = profiles.reduce((acc, p) => {
                    acc[p.role] = (acc[p.role] || 0) + 1;
                    return acc;
                }, {} as Record<string, number>);

                const highestRatedScenario = [...scenarios].sort((a, b) => (b.average_rating || 0) - (a.average_rating || 0))[0] || null;
                const mostRatedScenario = [...scenarios].sort((a, b) => (b.total_ratings || 0) - (a.total_ratings || 0))[0] || null;


                setStats({
                    totalUsers: profiles.length,
                    totalScenarios: scenarios.length,
                    pendingScenarios: statusDistribution['pending'] || 0,
                    gamesPlayed: gamesRes.count || 0,
                    statusDistribution: {
                        approved: statusDistribution['approved'] || 0,
                        pending: statusDistribution['pending'] || 0,
                        rejected: statusDistribution['rejected'] || 0,
                    },
                    languageDistribution,
                    highestRatedScenario,
                    mostRatedScenario,
                    roleDistribution,
                });

            } catch (err: any) {
                setError(err.message);
                console.error("Error fetching statistics:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats().catch(console.error);
    }, []);

    if (loading) {
        return <div className="flex justify-center items-center h-64"><LoadingSpinner size="lg"/></div>;
    }

    if (error || !stats) {
        return <div className="text-center py-12 text-red-500">Error loading
            statistics: {error || 'No data available'}</div>;
    }

    return (
        <div className="space-y-8">
            <SectionHeader title="Game Statistics" description="An overview of scenarios, users, and game activity."/>

            {/* General Overview */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard icon={<Users size={24}/>} title="Total Users" value={stats.totalUsers}/>
                <StatCard icon={<BookOpen size={24}/>} title="Total Scenarios" value={stats.totalScenarios}/>
                <StatCard icon={<Clock size={24}/>} title="Games Played" value={stats.gamesPlayed}/>
                <StatCard icon={<FileCheck size={24}/>} title="Pending Scenarios" value={stats.pendingScenarios}
                          description="Awaiting approval"/>
            </div>

            {/* Scenario & User Insights */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Scenario Details */}
                <div className="lg:col-span-2 bg-white p-6 rounded-lg shadow-md border border-gray-200 space-y-4">
                    <h3 className="text-lg font-semibold text-gray-800">Scenario Insights</h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <p className="font-medium mb-2">By Status</p>
                            <ul className="text-sm space-y-1 text-gray-600">
                                <li>Approved: <span className="font-bold">{stats.statusDistribution.approved}</span>
                                </li>
                                <li>Pending: <span className="font-bold">{stats.statusDistribution.pending}</span></li>
                                <li>Rejected: <span className="font-bold">{stats.statusDistribution.rejected}</span>
                                </li>
                            </ul>
                        </div>
                        <div>
                            <p className="font-medium mb-2">By Language</p>
                            <ul className="text-sm space-y-1 text-gray-600">
                                {Object.entries(stats.languageDistribution).map(([lang, count]) => (
                                    <li key={lang}>{lang.toUpperCase()}: <span className="font-bold">{count}</span></li>
                                ))}
                            </ul>
                        </div>
                    </div>
                    <div className="pt-4 border-t">
                        {stats.highestRatedScenario && (
                            <div className="text-sm">
                                <p className="font-medium mb-1 flex items-center"><Star size={16}
                                                                                        className="text-yellow-500 mr-2"/>Highest
                                    Rated</p>
                                <p className="text-gray-600">{stats.highestRatedScenario.name} (<span
                                    className="font-bold">{stats.highestRatedScenario.average_rating?.toFixed(1)} ★</span>)
                                </p>
                            </div>
                        )}
                    </div>
                </div>

                {/* User Details */}
                <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">User Insights</h3>
                    <div>
                        <p className="font-medium mb-2 flex items-center"><UserCheck size={16} className="mr-2"/>By Role
                        </p>
                        <ul className="text-sm space-y-1 text-gray-600">
                            {Object.entries(stats.roleDistribution).map(([role, count]) => (
                                <li key={role} className="capitalize">{role}: <span className="font-bold">{count}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StatisticsPage;