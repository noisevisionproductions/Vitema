import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";

const CompanyMission = () => {
    return (
        <section className="py-20 bg-surface">
            <Container>
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.5}}
                    className="max-w-3xl mx-auto text-center"
                >
                    <div className="flex flex-col items-center mb-8">
                        <div className="p-6">
                            <img
                                src="/images/logo-n.png"
                                alt="NutriLog Logo"
                                className="h-28 w-auto mx-auto"
                            />
                        </div>
                    </div>

                    <h1 className="text-4xl font-bold text-text-primary mb-6">
                        Nasza misja
                    </h1>
                    <p className="text-lg text-text-secondary mb-8">
                        W Noise Vision Software wierzymy, że technologia może znacząco usprawnić pracę specjalistów z
                        branży dietetycznej i zdrowotnej. Naszą misją jest tworzenie innowacyjnych rozwiązań, które
                        pomagają dietetykom skupić się na tym, co najważniejsze - na zdrowiu ich pacjentów.
                    </p>
                </motion.div>
            </Container>
        </section>
    );
};

export default CompanyMission;