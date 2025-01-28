import React, {useMemo} from "react";
import {User} from "../../types/user";
import LoadingSpinner from "../common/LoadingSpinner";
import {useDietInfo} from "../../hooks/useDietInfo";
import {formatDate} from "../../utils/dateFormatters";

interface UserSelectorTableProps {
    users: User[];
    selectedUser: User | null;
    onUserSelect: (user: User | null) => void;
    loading: boolean;
}

const UserSelectorTable: React.FC<UserSelectorTableProps> = ({
                                                                 users,
                                                                 selectedUser,
                                                                 onUserSelect,
                                                                 loading
                                                             }) => {
    const userIds = useMemo(() => users.map(user => user.id), [users]);
    const { dietInfo, loading: dietLoading } = useDietInfo(userIds);

    if (loading || dietLoading) {
        return (
            <div className="flex justify-center p-4">
                <LoadingSpinner/>
            </div>
        );
    }

    const renderDietStatus = (userId: string) => {
        const info = dietInfo[userId];
        if (!info || !info.hasDiet) {
            return (
                <span className="text-gray-500 text-sm">
                    Brak przypisanej diety
                </span>
            );
        }

        return (
            <div className="text-sm">
                <div className="text-green-600 font-medium">
                    Dieta przypisana
                </div>
                {info.startDate && info.endDate && (
                    <div className="text-gray-500">
                        {formatDate(info.startDate)} - {formatDate(info.endDate)}
                    </div>
                )}
            </div>
        )
    }

    return (
        <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50 sticky top-0">
            <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Wybór
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Email/Nick
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status profilu
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status diety
                </th>
            </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
            {users.map((user) => (
                <tr
                    key={user.id}
                    className={`hover:bg-gray-50 ${
                        selectedUser?.id === user.id ? 'bg-blue-50' : ''
                    }`}
                >
                    <td className="px-6 py-4">
                        <input
                            type="radio"
                            name="selectedUser"
                            checked={selectedUser?.id === user.id}
                            onChange={() => onUserSelect(user)}
                            className="h-4 w-4 text-blue-600"
                        />
                    </td>
                    <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">
                            {user.email}
                        </div>
                        <div className="text-sm text-gray-500">
                            {user.nickname}
                        </div>
                    </td>
                    <td className="px-6 py-4">
                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                user.profileCompleted
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-yellow-100 text-yellow-800'
                            }`}>
                                {user.profileCompleted ? 'Kompletny' : 'Niekompletny'}
                            </span>
                    </td>
                    <td className="px-6 py-4">
                        {renderDietStatus(user.id)}
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};

export default UserSelectorTable;