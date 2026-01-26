import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {Toaster} from 'sonner'
import App from './App'
import './index.css'
import './i18n'
import {HelmetProvider} from "react-helmet-async";
import ReactGA from "react-ga4";

ReactGA.initialize("G-Q4SWCQ49QM");

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: false,
        },
    },
})

const rootElement = document.getElementById('root')
if (!rootElement) throw new Error('Root element not found')

createRoot(rootElement).render(
    <StrictMode>
        <HelmetProvider>
            <QueryClientProvider client={queryClient}>
                <App/>
                <Toaster position="top-right" richColors/>
            </QueryClientProvider>
        </HelmetProvider>
    </StrictMode>,
)