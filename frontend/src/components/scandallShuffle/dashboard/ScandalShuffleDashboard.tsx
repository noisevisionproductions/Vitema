import React, {useEffect, useState} from 'react';
import {Users, BookOpen, Play, TrendingUp} from 'lucide-react';
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import {toast} from '../../../utils/toast';
import SectionHeader from "../../shared/common/SectionHeader";
import {ScandalShuffleNav} from "../navigation/ScandalShuffleSidebar";
import {DashboardApiService} from "../../../services/scandallShuffle/DashboardApiService";

interface DashboardStats {
    totalUsers: number;
    totalScenarios: number;
    activeSessions: number;
}

interface ScandalShuffleDashboardProps {
    onNavigate?: (tab: ScandalShuffleNav) => void;
}

const ScandalShuffleDashboard: React.FC<ScandalShuffleDashboardProps> = ({onNavigate}) => {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats().catch(console.error);
    }, []);

    const fetchStats = async () => {
        try {
            const dashboardStats = await DashboardApiService.getStats();
            setStats(dashboardStats);
        } catch (error) {
            console.error('Error fetching dashboard stats:', error);
            toast.error('Błąd podczas pobierania statystyk');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner size="lg"/>
            </div>
        );
    }

    const statCards = [
        {
            title: 'Total Users',
            value: stats?.totalUsers || 0,
            icon: Users,
            color: 'bg-blue-500',
            bgColor: 'bg-blue-50'
        },
        {
            title: 'Scenarios',
            value: stats?.totalScenarios || 0,
            icon: BookOpen,
            color: 'bg-green-500',
            bgColor: 'bg-green-50'
        },
        {
            title: 'Active Game Sessions',
            value: stats?.activeSessions || 0,
            icon: Play,
            color: 'bg-purple-500',
            bgColor: 'bg-purple-50'
        }
    ];

    return (
        <div className="space-y-6">
            <SectionHeader
                title="Scandal Shuffle Dashboard"
                description="Manage game, users and scenarios"
            />

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {statCards.map((stat) => {
                    const Icon = stat.icon;
                    return (
                        <div key={stat.title} className={`${stat.bgColor} rounded-lg p-6`}>
                            <div className="flex items-center">
                                <div className={`${stat.color} rounded-lg p-3`}>
                                    <Icon className="h-6 w-6 text-white"/>
                                </div>
                                <div className="ml-4">
                                    <p className="text-sm font-medium text-gray-600">
                                        {stat.title}
                                    </p>
                                    <p className="text-2xl font-bold text-gray-900">
                                        {stat.value}
                                    </p>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-4">
                    Quick Actions
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <button
                        onClick={() => onNavigate?.('users')}
                        className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors hover:border-green-500 group">
                        <Users className="h-5 w-5 mr-2 text-gray-400 group-hover:text-green-600"/>
                        <span className="text-sm font-medium group-hover:text-green-600">Manage Users</span>
                    </button>
                    <button
                        onClick={() => onNavigate?.('scenarios')}
                        className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors hover:border-green-500 group">
                        <BookOpen className="h-5 w-5 mr-2 text-gray-400 group-hover:text-green-600"/>
                        <span className="text-sm font-medium group-hover:text-green-600">Manage Scenarios</span>
                    </button>
                    <button
                        onClick={() => onNavigate?.('games')}
                        className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors hover:border-green-500 group">
                        <Play className="h-5 w-5 mr-2 text-gray-400 group-hover:text-green-600"/>
                        <span className="text-sm font-medium group-hover:text-green-600">Active Games</span>
                    </button>
                    <button
                        onClick={() => onNavigate?.('stats')}
                        className="flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors hover:border-green-500 group">
                        <TrendingUp className="h-5 w-5 mr-2 text-gray-400 group-hover:text-green-600"/>
                        <span className="text-sm font-medium group-hover:text-green-600">Statistics</span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ScandalShuffleDashboard;