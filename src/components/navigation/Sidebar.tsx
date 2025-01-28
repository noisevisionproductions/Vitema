import {TabName} from "../../types/navigation";
import {BarChart3, ChevronLeft, ChevronRight, FileSpreadsheet, LogOut, Upload, Users} from "lucide-react";
import NavButton from "./NavButton";
import React, {useState} from "react";
import {cn} from "../../utils/cs";
import {useAuth} from "../../contexts/AuthContext";
import {useNavigate} from "react-router-dom";
import {toast} from "sonner";

interface SidebarProps {
    activeTab: TabName;
    onTabChange: (tab: TabName) => void;
}

const navigationItems = [
    {id: 'upload', label: 'Upload Excel', icon: Upload},
    {id: 'data', label: 'Zarządzanie Dietami', icon: FileSpreadsheet},
    {id: 'users', label: 'Użytkownicy', icon: Users},
    {id: 'stats', label: 'Statystyki', icon: BarChart3},
] as const;

const Sidebar: React.FC<SidebarProps> = ({activeTab, onTabChange}) => {
    const [isCollapsed, setIsCollapsed] = useState(true);
    const {logout} = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/login');
            toast.success('Wylogowano pomyślnie');
        } catch (error) {
            toast.error('Wystąpił błąd podczas wylogowywania');
        }
    };

    return (
        <div className={cn(
            "bg-white shadow-lg h-screen flex flex-col transition-all duration-300",
            isCollapsed ? "w-20" : "w-64"
        )}>
            {/* Header */}
            <div className=" relative p-4 border-b border-gray-200 flex items-center">
                {!isCollapsed && (
                    <h1 className="text-xl font-bold text-gray-800">Panel Admina Szyta Dieta</h1>
                )}

                <button
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    className={cn(
                        "absolute p-1 rounded-full bg-white shadow-md hover:bg-gray-50",
                        "transition-all duration-200",
                        isCollapsed ? "-right-3" : "-right-4"
                    )}
                >
                    {isCollapsed ? (
                        <ChevronRight className="w-4 h-4"/>
                    ) : (
                        <ChevronLeft className="h-4 w-4"/>
                    )}
                </button>
            </div>

            {/* Navigation */}
            <nav className="flex-1 mt-4">
                {navigationItems.map((item) => (
                    <NavButton
                        key={item.id}
                        icon={item.icon}
                        label={item.label}
                        isActive={activeTab === item.id}
                        onClick={() => onTabChange(item.id as TabName)}
                        isCollapsed={isCollapsed}
                    />
                ))}
            </nav>

            {/* Logout Button */}
            <div className="p-4 border-t border-gray-200">
                <NavButton
                    icon={LogOut}
                    label="Wyloguj"
                    isActive={false}
                    onClick={handleLogout}
                    isCollapsed={isCollapsed}
                    className="text-gray-600 hover:text-red-600 hover:bg-red-50"
                />
            </div>
        </div>
    );
};

export default Sidebar;