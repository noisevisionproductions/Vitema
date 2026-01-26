import {useEffect} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import Hero from "../components/landing/hero/Hero";
import Features from "../components/landing/features/Features";
import ForWho from "../components/landing/forWho/ForWho";
import FAQ from "../components/landing/faq/FAQ";
import Contact from "../components/landing/contact/Contact";
import CTA from "../components/landing/cta/CTA";
import MarketStats from "../components/landing/marketStats/MarketStats";
import DownloadAppSection from "../components/landing/appStore/DownloadAppSection";
import {Helmet} from "react-helmet-async";

const Landing = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        if (location.state && location.state.scrollTo) {
            const sectionId = location.state.scrollTo;

            const element = document.getElementById(sectionId);

            if (element) {
                setTimeout(() => {
                    const headerOffset = 80;
                    const elementPosition = element.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.scrollY - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });

                    navigate('/', {replace: true, state: {}});
                }, 100);
            }
        }
    }, [location, navigate]);

    return (
        <>
            <Helmet>
                <title>Vitema - Aplikacja dla dietetyków i trenerów personalnych</title>
                <meta name="description"
                      content="Vitema to kompleksowe narzędzie do układania diet, zarządzania pacjentami i automatyzacji pracy dietetyka. Sprawdź nasze funkcje!"/>
                <meta name="keywords"
                      content="Vitema, program dla dietetyka, układanie diet, aplikacja dietetyczna, trener personalny"/>

                <meta property="og:title" content="Vitema - Twój asystent żywienia"/>
                <meta property="og:description" content="Przyspiesz swoją pracę z pacjentami dzięki Vitema."/>
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://vitema.pl/"/>
            </Helmet>

            <Hero/>
            <MarketStats/>
            <Features/>
            <ForWho/>
            <DownloadAppSection/>
            <FAQ/>
            <Contact/>
            <CTA/>
        </>
    );
};

export default Landing;