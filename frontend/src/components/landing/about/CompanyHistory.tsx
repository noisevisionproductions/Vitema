import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";

const CompanyHistory = () => {
    return (
        <section className="py-20 bg-background">
            <Container>
                <motion.div
                    initial={{opacity: 0}}
                    whileInView={{opacity: 1}}
                    viewport={{once: true}}
                    className="max-w-4xl mx-auto"
                >
                    <h2 className="text-3xl font-bold text-text-primary mb-8 text-center">
                        Nasza historia
                    </h2>

                    <div className="space-y-8">
                        <div className="bg-surface p-6 rounded-xl border border-border">
                            <h3 className="text-xl font-semibold text-text-primary mb-4">
                                Od pojedynczego projektu do kompleksowego rozwiązania
                            </h3>
                            <p className="text-text-secondary">
                                Wszystko zaczęło się od współpracy z lokalnym dietetykiem, dla którego stworzyliśmy
                                aplikację mobilną umożliwiającą jego klientom łatwy dostęp do diet i automatyczne
                                tworzenie list zakupów. Sukces tego rozwiązania zainspirował nas do stworzenia Zdrowego
                                Panelu - kompleksowej platformy dostępnej dla wszystkich specjalistów z branży
                                dietetycznej.
                            </p>
                        </div>

                        <div className="bg-surface p-6 rounded-xl border border-border">
                            <h3 className="text-xl font-semibold text-text-primary mb-4">
                                Dziś i jutro
                            </h3>
                            <p className="text-text-secondary">
                                Obecnie, jako dynamicznie rozwijający się startup, koncentrujemy się na tworzeniu
                                innowacyjnych rozwiązań, które odpowiadają na realne potrzeby dietetyków i ich klientów.
                                Nasz zespół, prowadzony przez założyciela firmy, nieustannie pracuje nad rozwojem
                                platformy, wsłuchując się w feedback użytkowników i implementując nowe funkcjonalności.
                            </p>
                        </div>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default CompanyHistory;