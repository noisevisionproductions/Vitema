import React, {useState} from 'react';
import ScandalShuffleSidebar, {ScandalShuffleNav} from "../navigation/ScandalShuffleSidebar";
import ScandalShuffleDashboard from "../dashboard/ScandalShuffleDashboard";
import UsersManagement from "../users/UsersManagement";
import ScenariosManagement from "../scenarios/ScenariosManagement";
import usePageTitle from "../../../hooks/nutrilog/usePageTitle";
import GamesManagement from "../games/GamesManagement";
import StatisticsPage from "../stats/StatisticsPage";

const ScandalShufflePanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<ScandalShuffleNav>('dashboard');

    const titleMap: Record<ScandalShuffleNav, string> = {
        dashboard: 'Dashboard',
        users: 'Users',
        scenarios: 'Scenarios',
        games: 'Active Games',
        stats: 'Statistics'
    };

    usePageTitle(titleMap[activeTab], 'Scandal Shuffle Panel');

    const renderContent = () => {
        switch (activeTab) {
            case 'dashboard':
                return <ScandalShuffleDashboard onNavigate={setActiveTab}/>;
            case 'users':
                return <UsersManagement/>;
            case 'scenarios':
                return <ScenariosManagement/>;
            case 'games':
                return <GamesManagement/>;
            case 'stats':
                return <StatisticsPage/>;
            default:
                return <ScandalShuffleDashboard/>;
        }
    };

    return (
        <ScandalShuffleSidebar activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </ScandalShuffleSidebar>
    );
};

export default ScandalShufflePanel;