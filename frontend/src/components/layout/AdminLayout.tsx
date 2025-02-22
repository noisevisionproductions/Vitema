import {TabName} from "../../types/navigation";
import React from "react";
import Sidebar from '../navigation/Sidebar';

interface AdminLayoutProps {
    children: React.ReactNode;
    activeTab: TabName;
    onTabChange: (tab: TabName) => void;
}

const AdminLayout: React.FC<AdminLayoutProps> = ({
                                                     children,
                                                     activeTab,
                                                     onTabChange
                                                 }) => {
    return (
        <div className="flex h-screen bg-gray-100">
            <Sidebar
                activeTab={activeTab}
                onTabChange={onTabChange}
            />
            <main className="flex-1 p-8 overflow-auto">
                {children}
            </main>
        </div>
    );
};

export default AdminLayout;