import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {userTypes} from './forWhoData';
import UserTypeCard from "./UserTypeCard";

const ForWho = () => {
    const sortedUserTypes = [...userTypes].sort((a, b) => {
        if (a.primary && !b.primary) return -1;
        if (!a.primary && b.primary) return 1;
        return 0;
    });

    return (
        <section id="for-who" className="py-20 bg-surface">
            <Container>
                <SectionHeader
                    title="Dla kogo jest NutriLog?"
                    subtitle="Kompleksowe rozwiązanie dla wszystkich uczestników procesu dietetycznego"
                />

                <div className="mt-16 grid gap-8 md:grid-cols-2 lg:grid-cols-4">
                    {sortedUserTypes.map((type) => (
                        <UserTypeCard key={type.id} {...type}/>
                    ))}
                </div>

                <div className="mt-12 text-center">
                    <p className="text-text-secondary max-w-2xl mx-auto">
                        NutriLog łączy dietetyków z ich klientami w jednym ekosystemie,
                        zapewniając płynną komunikację i efektywną współpracę.
                    </p>
                </div>
            </Container>
        </section>
    );
};

export default ForWho;