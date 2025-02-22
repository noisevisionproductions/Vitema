import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import useUsers from '../../hooks/useUsers';
import { collection, getDocs, Timestamp } from 'firebase/firestore';
import { db } from '../../config/firebase';
import { Diet } from '../../types/diet';
import LoadingSpinner from "../common/LoadingSpinner";

const StatsPanel: React.FC = () => {
    const { users, loading: usersLoading } = useUsers();
    const [diets, setDiets] = React.useState<Diet[]>([]);
    const [loading, setLoading] = React.useState(true);

    React.useEffect(() => {
        const fetchDiets = async () => {
            try {
                const dietsCollection = collection(db, 'diets');
                const dietsSnapshot = await getDocs(dietsCollection);
                const dietsData = dietsSnapshot.docs.map(doc => ({
                    id: doc.id,
                    ...doc.data()
                })) as Diet[];
                setDiets(dietsData);
            } catch (error) {
                console.error('Error fetching diets:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDiets();
    }, []);

    if (loading || usersLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <LoadingSpinner />
            </div>
        );
    }

    // Obliczanie aktywnych użytkowników (z dietą)
    const activeUsers = new Set(diets.map(diet => diet.userId)).size;

    // Obliczanie średniej liczby dni w dietach
    const averageDietDays = diets.length > 0
        ? Math.round(diets.reduce((acc, diet) => acc + (diet.days?.length || 0), 0) / diets.length)
        : 0;

    // Dane do wykresu użytkowników
    const usersData = [
        { name: 'Wszyscy użytkownicy', value: users.length },
        { name: 'Aktywni użytkownicy', value: activeUsers },
    ];

    // Dane do wykresu miesięcznego przyrostu diet
    const monthlyDiets = diets.reduce((acc: { [key: string]: number }, diet) => {
        const date = new Date((diet.createdAt as Timestamp).toDate());
        const monthKey = `${date.getMonth() + 1}/${date.getFullYear()}`;
        acc[monthKey] = (acc[monthKey] || 0) + 1;
        return acc;
    }, {});

    const monthlyData = Object.entries(monthlyDiets).map(([month, count]) => ({
        month,
        count
    })).sort((a, b) => {
        const [monthA, yearA] = a.month.split('/').map(Number);
        const [monthB, yearB] = b.month.split('/').map(Number);
        return yearA !== yearB ? yearA - yearB : monthA - monthB;
    });

    return (
        <div className="space-y-6 p-6">
            <h2 className="text-2xl font-bold mb-6">Statystyki Systemu</h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-600 mb-2">Liczba Użytkowników</h3>
                    <p className="text-3xl font-bold">{users.length}</p>
                    <p className="text-sm text-gray-500 mt-1">Wszyscy zarejestrowani</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-600 mb-2">Aktywni Użytkownicy</h3>
                    <p className="text-3xl font-bold">{activeUsers}</p>
                    <p className="text-sm text-gray-500 mt-1">Z przypisaną dietą</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium text-gray-600 mb-2">Średnia Długość Diety</h3>
                    <p className="text-3xl font-bold">{averageDietDays} dni</p>
                    <p className="text-sm text-gray-500 mt-1">Średnia liczba dni</p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium mb-4">Stosunek Użytkowników</h3>
                    <div className="h-[300px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={usersData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="name" />
                                <YAxis />
                                <Tooltip />
                                <Bar dataKey="value" fill="#3b82f6" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-medium mb-4">Przyrost Diet (miesięcznie)</h3>
                    <div className="h-[300px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={monthlyData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="month" />
                                <YAxis />
                                <Tooltip />
                                <Line type="monotone" dataKey="count" stroke="#3b82f6" />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StatsPanel;