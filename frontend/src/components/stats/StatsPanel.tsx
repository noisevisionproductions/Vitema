import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import useUsers from '../../hooks/useUsers';
import { Diet } from '../../types';
import LoadingSpinner from "../common/LoadingSpinner";
import { DietService } from '../../services/DietService';
import { formatMonthYear } from '../../utils/dateFormatters';

interface MonthlyStats {
    month: string;
    count: number;
}

interface UserStats {
    name: string;
    value: number;
}

const StatsPanel: React.FC = () => {
    const { users, loading: usersLoading } = useUsers();
    const [diets, setDiets] = useState<Diet[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                setLoading(true);
                const dietsData = await DietService.getDiets();
                setDiets(dietsData);
                setError(null);
            } catch (error) {
                console.error('Error fetching diets:', error);
                setError(error as Error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats().catch(console.error);
    }, []);

    // Obliczanie statystyk
    const calculateStats = () => {
        // Aktywni użytkownicy (z dietą)
        const activeUsers = new Set(diets.map(diet => diet.userId)).size;

        // Średnia liczba dni w dietach
        const averageDietDays = diets.length > 0
            ? Math.round(diets.reduce((acc, diet) => acc + (diet.days?.length || 0), 0) / diets.length)
            : 0;

        // Dane do wykresu użytkowników
        const usersData: UserStats[] = [
            { name: 'Wszyscy użytkownicy', value: users.length },
            { name: 'Aktywni użytkownicy', value: activeUsers },
        ];

        // Dane do wykresu miesięcznego przyrostu diet
        const monthlyDiets = diets.reduce((acc: { [key: string]: number }, diet) => {
            if (!diet.createdAt) return acc;

            const date = new Date(diet.createdAt.seconds * 1000);
            const monthKey = formatMonthYear(date);
            acc[monthKey] = (acc[monthKey] || 0) + 1;
            return acc;
        }, {});

        const monthlyData: MonthlyStats[] = Object.entries(monthlyDiets)
            .map(([month, count]) => ({
                month,
                count
            }))
            .sort((a, b) => {
                const [monthA, yearA] = a.month.split('/').map(Number);
                const [monthB, yearB] = b.month.split('/').map(Number);
                return yearA !== yearB ? yearA - yearB : monthA - monthB;
            });

        return {
            activeUsers,
            averageDietDays,
            usersData,
            monthlyData
        };
    };

    if (loading || usersLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner />
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-red-500 text-center p-4">
                Wystąpił błąd podczas ładowania statystyk
            </div>
        );
    }

    const { activeUsers, averageDietDays, usersData, monthlyData } = calculateStats();

    const StatCard: React.FC<{ title: string; value: string | number; subtitle: string }> = ({ title, value, subtitle }) => (
        <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-600 mb-2">{title}</h3>
            <p className="text-3xl font-bold">{value}</p>
            <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
        </div>
    );

    const ChartCard: React.FC<{ title: string; children: React.ReactNode }> = ({ title, children }) => (
        <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium mb-4">{title}</h3>
            <div className="h-[300px]">
                {children}
            </div>
        </div>
    );

    return (
        <div className="space-y-6 p-6">
            <h2 className="text-2xl font-bold mb-6">Statystyki Systemu</h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <StatCard
                    title="Liczba Użytkowników"
                    value={users.length}
                    subtitle="Wszyscy zarejestrowani"
                />
                <StatCard
                    title="Aktywni Użytkownicy"
                    value={activeUsers}
                    subtitle="Z przypisaną dietą"
                />
                <StatCard
                    title="Średnia Długość Diety"
                    value={`${averageDietDays} dni`}
                    subtitle="Średnia liczba dni"
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <ChartCard title="Stosunek Użytkowników">
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={usersData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip />
                            <Bar dataKey="value" fill="#3b82f6" />
                        </BarChart>
                    </ResponsiveContainer>
                </ChartCard>

                <ChartCard title="Przyrost Diet (miesięcznie)">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={monthlyData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="month" />
                            <YAxis />
                            <Tooltip />
                            <Line type="monotone" dataKey="count" stroke="#3b82f6" />
                        </LineChart>
                    </ResponsiveContainer>
                </ChartCard>
            </div>
        </div>
    );
};

export default StatsPanel;