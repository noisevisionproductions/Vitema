import {Lightbulb, Shield, Users, Zap} from "lucide-react";
import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";

const values = [
    {
        icon: Lightbulb,
        title: "Innowacyjność",
        description: "Nieustannie poszukujemy nowych rozwiązań i możliwości rozwoju naszej platformy."
    },
    {
        icon: Users,
        title: "Zorientowanie na użytkownika",
        description: "Wsłuchujemy się w potrzeby naszych klientów i dostosowujemy nasze rozwiązania do ich wymagań."
    },
    {
        icon: Shield,
        title: "Bezpieczeństwo",
        description: "Priorytetowo traktujemy ochronę danych i prywatność użytkowników naszej platformy."
    },
    {
        icon: Zap,
        title: "Efektywność",
        description: "Tworzymy narzędzia, które realnie oszczędzają czas i zwiększają produktywność."
    }
];

const CompanyValues = () => {
    return (
        <section className="py-20 bg-surface">
            <Container>
                <h2 className="text-3xl font-bold text-text-primary mb-12 text-center">
                    Nasze wartości
                </h2>

                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {values.map((value, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{delay: index * 0.1}}
                            className="bg-background p-6 rounded-xl border border-border"
                        >
                            <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                                <value.icon className="w-6 h-6 text-primary"/>
                            </div>
                            <h3 className="text-xl font-semibold text-text-primary mb-2">
                                {value.title}
                            </h3>
                            <p className="text-text-secondary">
                                {value.description}
                            </p>
                        </motion.div>
                    ))}
                </div>
            </Container>
        </section>
    );
};

export default CompanyValues;