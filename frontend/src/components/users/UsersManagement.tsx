import React, {useEffect, useState} from "react";
import UsersList from "./UsersList";
import UserDetailsModal from "./UserDetailsModal";
import {User} from "../../types/user";
import useUsers from "../../hooks/useUsers";
import LoadingSpinner from "../common/LoadingSpinner";

const UsersManagement: React.FC = () => {
    const {users, loading, fetchUsers} = useUsers();
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    useEffect(() => {
        const initFetch = async () => {
            try {
                await fetchUsers();
            } catch (error) {
                console.error('Error in initial fetch:', error);
            }
        };

        initFetch().catch();
    }, []);


    return (
        <div className="space-y-4">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold">Lista użytkowników</h2>
                <span className="text-gray-500">
                    Łącznie użytkowników: {users.length}
                </span>
            </div>

            {loading ? (
                <div className="flex justify-center items-center h-64">
                    <LoadingSpinner/>
                </div>
            ) : (
                <UsersList
                    users={users}
                    onUserSelect={setSelectedUser}
                    onUpdate={fetchUsers}
                />
            )}

            {selectedUser && (
                <UserDetailsModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={fetchUsers}
                />
            )}
        </div>
    );
};

export default UsersManagement;