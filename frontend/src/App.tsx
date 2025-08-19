import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import ProtectedRoute from './components/nutrilog/auth/ProtectedRoute';
import DietitianPanel from "./pages/panel/DietitianPanel";
import Unauthorized from "./pages/Unauthorized";
import ErrorPage from "./pages/ErrorPage";
import {SuggestedCategoriesProvider} from "./contexts/SuggestedCategoriesContext";
import {ProductCategoriesProvider} from './hooks/nutrilog/shopping/useProductCategories';
import {ToastProvider} from "./contexts/ToastContext";
import LandingLayout from "./components/landing/layout/LandingLayout";
import Landing from "./pages/Landing";
import About from "./pages/About";
import Login from "./pages/Login";
import Unsubscribe from "./pages/newsletter/Unsubscribe";
import VerifyEmail from "./pages/newsletter/VerifyEmail";
import PrivacyPolicy from "./pages/PrivacyPolicy";
import ScrollToTop from "./components/shared/common/ScrollToTop";
import CookieConsent from "./components/shared/common/CookieConsent";
import AdminPanel from "./pages/panel/AdminPanel";
import {UserRole} from "./types/nutrilog/user";
import Newsletter from './pages/Newsletter';
import {SettingsProvider} from './contexts/SettingsContextType';
import {RouteRestorationProvider} from "./contexts/RouteRestorationContext";
import SSProtectedRoute from "./components/scandallShuffle/auth/SSProtectedRoute";
import ScandalShufflePanel from "./components/scandallShuffle/panel/ScandalShufflePanel";
import {ApplicationProvider} from "./contexts/ApplicationContext";

function App() {
    return (
        <Router
            future={{
                v7_relativeSplatPath: true,
                v7_startTransition: true
            }}
        >
            <ScrollToTop/>
            <ToastProvider>
                <ApplicationProvider>
                    <RouteRestorationProvider>
                        <AuthProvider>
                            <Routes>
                                {/* Landing page routes */}
                                <Route path="/" element={
                                    <LandingLayout>
                                        <Landing/>
                                    </LandingLayout>
                                }/>
                                <Route path="/about" element={
                                    <LandingLayout>
                                        <About/>
                                    </LandingLayout>
                                }/>
                                <Route path="/privacy-policy" element={
                                    <LandingLayout>
                                        <PrivacyPolicy/>
                                    </LandingLayout>
                                }/>

                                {/* Newsletter routes */}
                                <Route path="/verify-email" element={<VerifyEmail/>}/>
                                <Route path="/unsubscribe" element={<Unsubscribe/>}/>
                                <Route path="/newsletter" element={
                                    <LandingLayout>
                                        <Newsletter/>
                                    </LandingLayout>
                                }/>

                                {/* Auth routes */}
                                <Route path="/login" element={<Login/>}/>
                                <Route path="/unauthorized" element={<Unauthorized/>}/>
                                <Route path="/error" element={<ErrorPage/>}/>

                                {/* Main Dashboard */}
                                <Route
                                    path="/dashboard/*"
                                    element={
                                        <ProtectedRoute requiredRole={UserRole.ADMIN}>
                                            <SettingsProvider>
                                                <SuggestedCategoriesProvider>
                                                    <ProductCategoriesProvider>
                                                        <DietitianPanel/>
                                                    </ProductCategoriesProvider>
                                                </SuggestedCategoriesProvider>
                                            </SettingsProvider>
                                        </ProtectedRoute>
                                    }
                                />

                                {/* Admin Dashboard */}
                                <Route
                                    path="/admin/*"
                                    element={
                                        <ProtectedRoute requiredRole={UserRole.OWNER}>
                                            <AdminPanel/>
                                        </ProtectedRoute>
                                    }
                                />

                                {/* Scandal Shuffle Dashboard */}
                                <Route
                                    path="/scandal-shuffle/dashboard/*"
                                    element={
                                        <SSProtectedRoute requiredRole="admin">
                                            <ScandalShufflePanel/>
                                        </SSProtectedRoute>
                                    }
                                />

                            </Routes>
                            <CookieConsent/>
                        </AuthProvider>
                    </RouteRestorationProvider>
                </ApplicationProvider>
            </ToastProvider>
        </Router>

    );
}

export default App;