import {ClockIcon, UsersIcon} from "lucide-react";
import {ChartBarIcon} from "@heroicons/react/24/outline";
import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";

const MarketStats = () => {
    const statistics = [
        {
            icon: ClockIcon,
            value: "12h",
            label: "tygodniowo tracone na ręczne zarządzanie dietami",
            description: "Dietetycy marnują średnio 12 godzin tygodniowo na przygotowywanie dokumentacji, układanie planów i ręczne obliczanie wartości odżywczych."
        },
        {
            icon: UsersIcon,
            value: "35%",
            label: "klientów rezygnuje z powodu braku wygodnego dostępu do diety",
            description: "Brak łatwego dostępu do planów żywieniowych i komunikacji z dietetykiem jest głównym powodem rezygnacji klientów z kontynuacji współpracy."
        },
        {
            icon: ChartBarIcon,
            value: "68%",
            label: "klientów preferuje aplikację mobilną do kontaktu z dietetykiem",
            description: "Większość klientów oczekuje możliwości sprawdzenia swojej diety, postępów i komunikacji z dietetykiem przez aplikację mobilną."
        }
    ];

    return (
        <section className="py-20 bg-surface">
            <Container>
                <SectionHeader
                    title="Trudności, które rozwiązuje nasza platforma"
                    subtitle="Codzienne wyzwania dietetyków, które pomagamy przezwyciężyć"
                />

                <div className="mt-12 grid md:grid-cols-3 gap-8">
                    {statistics.map((stat, index) => (
                        <div
                            key={index}
                            className="p-6 bg-white rounded-xl border border-border hover:border-primary/20 transition-all duration-200 hover:shadow-lg"
                        >
                            <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                                <stat.icon className="w-6 h-6 text-primary"/>
                            </div>

                            <div className="text-3xl font-bold text-primary mb-2">
                                {stat.value}
                            </div>

                            <div className="text-lg font-medium text-text-primary mb-3">
                                {stat.label}
                            </div>

                            <p className="text-text-secondary">
                                {stat.description}
                            </p>
                        </div>
                    ))}
                </div>

                <div className="mt-12 text-xs text-center">
                    <p className="text-text-secondary">
                        * Dane na podstawie badań przeprowadzonych wśród dietetyków i ich klientów w 2023 roku
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default MarketStats;