import Container from "../../shared/ui/landing/Container";
import SectionHeader from "../../shared/ui/landing/SectionHeader";
import {faqItems} from './faqData';
import FAQItem from "./FAQItem";

const FAQ = () => {
    return (
        <section id="faq" className="py-20 bg-background">
            <Container>
                <SectionHeader
                    title="Często zadawane pytania"
                    subtitle="Znajdź odpowiedzi na najczęściej zadawane pytania dotyczące NutriLog"
                />

                <div className="mt-12 max-w-3xl mx-auto divide-y divide-border">
                    {faqItems.map((item) => (
                        <FAQItem key={item.id} {...item}/>
                    ))}
                </div>
            </Container>
        </section>
    );
};

export default FAQ;