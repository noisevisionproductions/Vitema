interface SectionHeaderProps {
    title: string;
    subtitle: string;
    alignment?: 'left' | 'center';
}

const SectionHeader = ({
                           title,
                           subtitle,
                           alignment = 'center'
                       }: SectionHeaderProps) => {
    return (
        <div className={`max-w-3xl ${alignment === 'center' ? 'mx-auto text-center' : ''}`}>
            <h2 className="text-3xl sm:text-4xl font-bold text-text-primary mb-4">
                {title}
            </h2>
            <p className="text-lg text-text-secondary">
                {subtitle}
            </p>
        </div>
    );
};

export default SectionHeader;