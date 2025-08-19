import React, {useEffect} from 'react';
import {Route, Routes} from "react-router-dom";
import ExcelUpload from "../../components/nutrilog/navigation/dietitian/creation/excel/ExcelUpload";
import UsersManagement from "../../components/nutrilog/navigation/dietitian/creation/excel/UsersManagement";
import DietManagement from "../../components/nutrilog/diet/DietManagement";
import StatsPanel from "../../components/nutrilog/stats/StatsPanel";
import DietGuide from "../../components/nutrilog/navigation/dietitian/creation/excel/DietGuide";
import Changelog from "../../components/nutrilog/navigation/dietitian/creation/excel/Changelog";
import RecipesPage from "../../components/nutrilog/navigation/dietitian/creation/excel/RecipesPage";
import usePageTitle from "../../hooks/nutrilog/usePageTitle";
import DietitianDashboard from "../../components/nutrilog/navigation/dietitian/creation/excel/DietitianDashboard";
import DietitianSidebar from "../../components/nutrilog/navigation/DietitianSidebar";
import DietCreationContainer from "../../components/nutrilog/navigation/dietitian/creation/DietCreationContainer";
import {useDietitianNavigation} from "../../hooks/nutrilog/useDietitianNavigation";
import {MainNav} from "../../types/nutrilog/navigation";
import DietTemplatesManager from "../../components/nutrilog/diet/templates/DietTemplatesManager";

const DietitianPanel: React.FC = () => {
    const {currentTab, navigateToTab} = useDietitianNavigation();

    const titleMap: Record<MainNav, string> = {
        dietitianDashboard: 'Pulpit',
        dietCreation: 'Tworzenie diety',
        dietTemplates: 'Gotowe szablony diet',
        upload: 'Import Excel',
        diets: 'Zarządzanie dietami',
        users: 'Klienci',
        stats: 'Statystyki',
        guide: 'Przewodnik',
        changelog: 'Historia zmian',
        landing: 'Strona główna',
        recipes: 'Przepisy'
    };

    usePageTitle(titleMap[currentTab], 'Panel Dietetyka');

    useEffect(() => {
        const handleTabChangeEvent = (event: CustomEvent) => {
            const tab = event.detail as MainNav;
            navigateToTab(tab);
        };

        window.addEventListener('panel-tab-change', handleTabChangeEvent as EventListener);

        return () => {
            window.removeEventListener('panel-tab-change', handleTabChangeEvent as EventListener);
        };
    }, [navigateToTab]);

    return (
        <DietitianSidebar activeTab={currentTab} onTabChange={navigateToTab}>
            <Routes>
                <Route path="" element={<DietitianDashboard/>}/>

                {/* Zagnieżdżone rout dla tworzenia diety */}
                <Route
                    path="diet-creation/*"
                    element={<DietCreationContainer onTabChange={navigateToTab}/>}
                />

                {/* Stara ścieżka upload-dla kompatybilności */}
                <Route path="upload" element={<ExcelUpload onTabChange={navigateToTab}/>}/>

                <Route path="diets" element={<DietManagement/>}/>
                <Route path="diet-templates" element={<DietTemplatesManager/>}/>
                <Route path="users" element={<UsersManagement/>}/>
                <Route path="stats" element={<StatsPanel/>}/>
                <Route path="guide" element={<DietGuide/>}/>
                <Route path="changelog" element={<Changelog/>}/>
                <Route path="recipes" element={<RecipesPage/>}/>

                {/* Fallback */}
                <Route path="*" element={<DietitianDashboard/>}/>
            </Routes>
        </DietitianSidebar>
    );
};

export default DietitianPanel;