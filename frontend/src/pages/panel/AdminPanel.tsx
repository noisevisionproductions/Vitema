import React, {useState} from 'react';
import {AdminNav} from '../../types/nutrilog/navigation';
import AdminDashboard from "../../components/nutrilog/admin/AdminDashboard";
import AdminNewsletterPanel from "../newsletter/AdminNewsletterPanel";
import BulkEmailSender from '../../components/nutrilog/admin/newsletter/email-sender/BulkEmailSender';
import ContactMessagesPanel from '../../components/nutrilog/admin/contact/ContactMessagesPanel';
import AdminSidebar from "../../components/nutrilog/navigation/AdminSidebar";
import usePageTitle from "../../hooks/nutrilog/usePageTitle";

const AdminPanel: React.FC = () => {
    const [activeTab, setActiveTab] = useState<AdminNav>('adminDashboard');

    const titleMap: Record<AdminNav, string> = {
        adminDashboard: 'Pulpit',
        newsletter: 'Newsletter',
        subscribers: 'Subskrybenci',
        surveys: 'Ankiety',
        bulkEmail: 'Masowa wysyłka',
        contactMessages: 'Wiadomości kontaktowe',
    };

    usePageTitle(titleMap[activeTab], 'Panel Administratora');

    const renderContent = () => {
        switch (activeTab) {
            case 'adminDashboard':
                return <AdminDashboard/>;
            case 'newsletter':
                return <AdminNewsletterPanel/>;
            case 'bulkEmail':
                return <BulkEmailSender/>;
            case 'contactMessages':
                return <ContactMessagesPanel/>;
            default:
                return <AdminDashboard/>;
        }
    };

    return (
        <AdminSidebar activeTab={activeTab} onTabChange={setActiveTab}>
            {renderContent()}
        </AdminSidebar>
    );
};

export default AdminPanel;