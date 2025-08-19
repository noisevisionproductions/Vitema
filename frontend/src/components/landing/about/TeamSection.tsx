import Container from "../../shared/ui/landing/Container";
import {motion} from "framer-motion";

const TeamSection = () => {
    return (
        <section className="py-20 bg-background">
            <Container>
                <motion.div
                    initial={{opacity: 0}}
                    whileInView={{opacity: 1}}
                    viewport={{once: true}}
                    className="max-w-3xl mx-auto text-center"
                >
                    <h2 className="text-3xl font-bold text-text-primary mb-8">
                        Zespół
                    </h2>
                    <div className="bg-surface p-8 rounded-xl border border-border">
                        <div
                            className="w-40 h-40 mx-auto rounded-full overflow-hidden mb-6">
                            <img
                                src="/images/noisevisionselfie.jpg"
                                alt="Tomasz Jurczyk - Założyciel NutriLog"
                                className="w-full h-full object-cover"
                            />
                        </div>
                        <h3 className="text-xl font-semibold text-text-primary mb-2">
                            Założyciel NutriLog
                        </h3>
                        <p className="text-text-secondary mb-4">
                            Tomasz Jurczyk
                        </p>
                        <p className="text-text-secondary">
                            Z pasją do technologii i wizją usprawnienia pracy dietetyków, prowadzi rozwój NutriLog,
                            łącząc doświadczenie w tworzeniu oprogramowania z głębokim zrozumieniem potrzeb
                            branży dietetycznej.
                        </p>
                    </div>
                </motion.div>
            </Container>
        </section>
    );
};

export default TeamSection;