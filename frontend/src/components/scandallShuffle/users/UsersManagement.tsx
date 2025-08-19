import React, {useEffect, useState} from 'react';
import {Edit, MoreVertical, Trash2, User} from 'lucide-react';
import {ScandalShuffleApiService} from "../../../services/scandallShuffle/ApiService";
import {Profile} from "../../../types/scandallShuffle/database";
import LoadingSpinner from '../../shared/common/LoadingSpinner';
import {toast} from '../../../utils/toast';
import {formatDistanceToNow} from 'date-fns';
import SectionHeader from "../../shared/common/SectionHeader";

const UsersManagement: React.FC = () => {
    const [users, setUsers] = useState<Profile[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers().catch(console.error);
    }, []);

    const fetchUsers = async () => {
        try {
            const profiles = await ScandalShuffleApiService.getAllProfiles();
            setUsers(profiles);
        } catch (error) {
            console.error('Error fetching users:', error);
            toast.error('Błąd podczas pobierania użytkowników');
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

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <SectionHeader
                    title="User Management"
                    description="Browse and manage Scandal Shuffle user profiles"
                />
                <div className="text-md text-gray-500">
                    Total: {users.length} users
                </div>
            </div>

            {/* Users Table */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            User
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Game Statistics
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Role
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Last Activity
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Actions (To do)
                        </th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {users.map((user) => (
                        <tr key={user.id} className="hover:bg-gray-50">
                            <td className="px-6 py-4 whitespace-nowrap">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0 h-10 w-10">
                                        {user.avatar_url ? (
                                            <img
                                                className="h-10 w-10 rounded-full"
                                                src={user.avatar_url}
                                                alt=""
                                            />
                                        ) : (
                                            <div
                                                className="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                                                <User className="h-6 w-6 text-gray-600"/>
                                            </div>
                                        )}
                                    </div>
                                    <div className="ml-4">
                                        <div className="text-sm font-medium text-gray-900">
                                            {user.display_name || 'Unnamed'}
                                        </div>
                                        <div className="text-sm text-gray-500">
                                            ID: {user.user_id}
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                                <div className="text-sm text-gray-900">
                                    <div>Games: {user.games_played || 0}</div>
                                    <div>Points: {user.total_score || 0}</div>
                                </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                                user.role === 'owner'
                                    ? 'bg-purple-100 text-purple-800'
                                    : user.role === 'admin'
                                        ? 'bg-blue-100 text-blue-800'
                                        : 'bg-gray-100 text-gray-800'
                            }`}>
                                {user.role === 'owner' ? 'Owner' :
                                    user.role === 'admin' ? 'Administrator' : 'User'}
                            </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                {formatDistanceToNow(new Date(user.updated_at), {
                                    addSuffix: true
                                })}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                <div className="flex items-center space-x-2">
                                    <button className="text-indigo-600 hover:text-indigo-900">
                                        <Edit className="h-4 w-4"/>
                                    </button>
                                    <button className="text-red-600 hover:text-red-900">
                                        <Trash2 className="h-4 w-4"/>
                                    </button>
                                    <button className="text-gray-600 hover:text-gray-900">
                                        <MoreVertical className="h-4 w-4"/>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                {users.length === 0 && (
                    <div className="text-center py-12">
                        <User className="mx-auto h-12 w-12 text-gray-400"/>
                        <h3 className="mt-2 text-sm font-medium text-gray-900">
                            No users
                        </h3>
                        <p className="mt-1 text-sm text-gray-500">
                            Users will appear here after registering in the application.
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default UsersManagement;