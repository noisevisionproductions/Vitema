import React, {useState} from 'react';
import {
    LayoutDashboard,
    Users,
    BookOpen,
    Play,
    BarChart3,
    Home,
    LogOut,
    ChevronLeft,
    ChevronRight
} from 'lucide-react';
import {cn} from '../../../utils/cs';
import {useAuth} from '../../../contexts/AuthContext';
import {useNavigate} from 'react-router-dom';
import {toast} from '../../../utils/toast';

type ScandalShuffleNav = 'dashboard' | 'users' | 'scenarios' | 'games' | 'stats';

interface ScandalShuffleSidebarProps {
    activeTab: ScandalShuffleNav;
    onTabChange: (tab: ScandalShuffleNav) => void;
    children: React.ReactNode;
}

interface NavButtonProps {
    icon: React.ElementType;
    label: string;
    isActive: boolean;
    onClick: () => void;
    isCollapsed: boolean;
}

const NavButton: React.FC<NavButtonProps> = ({icon: Icon, label, isActive, onClick, isCollapsed}) => {
    return (
        <button
            onClick={onClick}
            className={cn(
                "w-full transition-colors relative",
                isCollapsed
                    ? "flex justify-center items-center h-14"
                    : "flex items-center py-3 px-4",
                isActive
                    ? "bg-green-500 text-white font-medium"
                    : "text-gray-700 hover:bg-green-50"
            )}
        >
            <Icon className={cn("h-5 w-5", isActive ? "text-white" : "text-green-600")}/>
            {!isCollapsed && (
                <span className="ml-3 truncate">{label}</span>
            )}
        </button>
    );
};

const navigationItems = [
    {id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard},
    {id: 'users', label: 'Users', icon: Users},
    {id: 'scenarios', label: 'Scenarios', icon: BookOpen},
    {id: 'games', label: 'Active Games', icon: Play},
    {id: 'stats', label: 'Statistics', icon: BarChart3},
] as const;

const ScandalShuffleSidebar: React.FC<ScandalShuffleSidebarProps> = ({activeTab, onTabChange, children}) => {
    const [isCollapsed, setIsCollapsed] = useState(true);
    const {logout} = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/');
            toast.success('Wylogowano pomyślnie');
        } catch (error) {
            toast.error('Wystąpił błąd podczas wylogowywania');
        }
    };

    return (
        <div className="flex h-screen bg-surface">
            <div className={cn(
                "bg-white shadow-lg h-screen flex flex-col transition-all duration-300",
                isCollapsed ? "w-20" : "w-64"
            )}>
                {/* Header */}
                <div className={cn(
                    "relative border-b border-gray-200 bg-green-600 text-white",
                    isCollapsed ? "py-4 flex justify-center" : "p-4 flex items-center"
                )}>
                    {!isCollapsed && (
                        <div className="flex items-center space-x-2">
                            <Play className="h-6 w-6"/>
                            <h1 className="text-xl font-bold">Scandal Shuffle</h1>
                        </div>
                    )}

                    {isCollapsed && (
                        <Play className="h-8 w-8"/>
                    )}

                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        className={cn(
                            "absolute p-1 rounded-full bg-white text-green-600 shadow-md hover:bg-gray-50",
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
                            onClick={() => onTabChange(item.id as ScandalShuffleNav)}
                            isCollapsed={isCollapsed}
                        />
                    ))}
                </nav>

                {/* Bottom buttons */}
                <div className="p-4 border-t border-gray-200 space-y-2">
                    <NavButton
                        icon={Home}
                        label="Homepage"
                        isActive={false}
                        onClick={() => navigate('/')}
                        isCollapsed={isCollapsed}
                    />
                    <NavButton
                        icon={LogOut}
                        label="Logout"
                        isActive={false}
                        onClick={handleLogout}
                        isCollapsed={isCollapsed}
                    />
                </div>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
                {children}
            </div>
        </div>
    );
};

export default ScandalShuffleSidebar;
export type {ScandalShuffleNav};