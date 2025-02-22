import React, {useState} from "react";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "../ui/card";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "../ui/Tabs";
import {AlertCircle, Clock, FileSpreadsheet, ListChecks} from "lucide-react";

type GuideSectionProps = {
    title: string;
    children: React.ReactNode;
}

const GuideSection = ({title, children}: GuideSectionProps) => (
    <div className="space-y-4 mb-6">
        <h3 className="text-xl font-semibold">
            {title}
        </h3>
        {children}
    </div>
);

const ExcelExample = () => (
    <div className="bg-gray-50 rounded-lg overflow-x-auto">
        <table className="min-w-full">
            <thead>
            <tr>
                <th className="px-4 py-2 bg-gray-100">Kolumna A</th>
                <th className="px-4 py-2 bg-gray-100">Kolumna B</th>
                <th className="px-4 py-2 bg-gray-100">Kolumna C</th>
                <th className="px-4 py-2 bg-gray-100">Kolumna D</th>
                <th className="px-4 py-2 bg-gray-100">Kolumna E</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td className="px-4 py-2 text-gray-500">Notatki (pomijane)</td>
                <td className="px-4 py-2">Nazwa posiłku</td>
                <td className="px-4 py-2">Sposób przygotowania</td>
                <td className="px-4 py-2">Lista składników</td>
                <td className="px-4 py-2">Wartości odżywcze</td>
            </tr>
            <tr>
                <td className="px-4 py-2 text-gray-500">Dowolne notatki</td>
                <td className="px-4 py-2">Owsianka z jabłkiem</td>
                <td className="px-4 py-2">Ugotuj płatki owsiane na mleku...</td>
                <td className="px-4 py-2">płatki owsiane, mleko, jabłko...</td>
                <td className="px-4 py-2">300,15,5,45</td>
            </tr>
            </tbody>
        </table>
    </div>
);

const DietGuide = () => {
    const [activeTab, setActiveTab] = useState('excel');

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle>
                    Przewodnik po dietach
                </CardTitle>
                <CardDescription>
                    Wszystko, co musisz wiedzieć o tworzeniu i zarządzaniu dietami
                </CardDescription>
            </CardHeader>
            <CardContent>
                <Tabs value={activeTab} onValueChange={setActiveTab}>
                    <TabsList className="grid w-full grid-cols-3">
                        <TabsTrigger value="excel" className="flex items-center  gap-2">
                            <FileSpreadsheet className="w-4 h-4"/>
                            Struktura Excel
                        </TabsTrigger>
                        <TabsTrigger value="rules" className="flex items-center gap-2">
                            <ListChecks className="w-4 h-4"/>
                            Zasady diet
                        </TabsTrigger>
                        <TabsTrigger value="schedule" className="flex items-center gap-2">
                            <Clock className="w-4 h-4"/>
                            Harmonogram
                        </TabsTrigger>
                    </TabsList>

                    <TabsContent value="excel" className="mt-4">
                        <GuideSection title="Struktura pliku Excel">
                            <div className="space-y-4">
                                <p>
                                    Plik excel powinien zawierać następujące kolumny:
                                </p>
                                <ul className="list-disc pl-6 space-y-2">
                                    <li><strong>Kolumna A (pomijana)</strong> - może zawierać dowolne notatki, nie jest
                                        brana pod uwagę przy przetwarzaniu
                                    </li>
                                    <li><strong>Kolumna B (Nazwa)</strong> - nazwa posiłku</li>
                                    <li><strong>Kolumna C (Sposób przygotowania)</strong> - dokładny opis przygotowania
                                        posiłku
                                    </li>
                                    <li><strong>Kolumna D (Lista składników)</strong> - składniki oddzielone przecinkami.
                                        <span className="text-red-500"> Z tego względu pojemności powinny być zaznaczane kropką zamiast przecinkiem, np. 1.2 litry, zamiast 1,2 litry</span>
                                    </li>
                                    <li><strong>Kolumna E (Wartości odżywcze)</strong> - opcjonalne, format:
                                        kalorie,białko,tłuszcze,węglowodany. Być może każda z wartości odżywczych będzie miałą swoją kolumnę
                                    </li>
                                </ul>

                                <div className="bg-blue-50 p-4 rounded-lg flex items-start gap-2">
                                    <AlertCircle className="w-5 h-5 text-blue-500 mt-0.5"/>
                                    <div>
                                        <p className="font-medium">
                                            Ważne zasady:
                                        </p>
                                        <ul className="list-disc pl-6 mt-2 space-y-1">
                                            <li>Pierwszy wiersz (1) to nagłówki, np. "Nazwa posiłku" w kolumnie B. Nagłówki są pomijane, ale muszą być podane raz w pierwszym wierszu w każdym pliku.</li>
                                            <li>Każdy wiersz to jeden posiłek</li>
                                            <li>Wartości odżywcze póki co są opcjonalne i nie do końca skonfigurowane.
                                                Do zrobienia
                                            </li>
                                            <li>Puste wiersze są pomijane</li>
                                        </ul>
                                    </div>
                                </div>

                                <p className="font-medium mt-4">
                                    Przykład poprawnego pliku:
                                </p>
                                <ExcelExample/>
                            </div>
                        </GuideSection>
                    </TabsContent>

                    <TabsContent value="rules" className="mt-4">
                        <GuideSection title="Zasady układania diet">
                            <div className="space-y-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <Card>
                                        <CardHeader>
                                            <CardTitle className="text-lg">Podstawowe zasady</CardTitle>
                                        </CardHeader>
                                        <CardContent>
                                            <ul className="list-disc pl-4 space-y-2">
                                                <li>Każdy dzień może mieć od 3 do 5 posiłków</li>
                                                <li>Posiłki muszą być rozłożone równomiernie w ciągu dnia</li>
                                                <li>Należy uwzględnić wszystkie główne grupy pokarmowe</li>
                                                <li>Dieta powinna być zróżnicowana</li>
                                            </ul>
                                        </CardContent>
                                    </Card>

                                    <Card>
                                        <CardHeader>
                                            <CardTitle className="text-lg">
                                                Weryfikacja danych
                                            </CardTitle>
                                        </CardHeader>
                                        <CardContent>
                                            <ul className="list-disc pl-4 space-y-2">
                                                <li>Wszystkie nazwy posiłków muszą być unikalne</li>
                                                <li>Każdy posiłek musi mieć opis przygotowania</li>
                                                <li>Lista składników jest obowiązkowa oraz jest traktowana również jako
                                                    lista zakupów
                                                </li>
                                                <li>Wartości odżywcze są opcjonalne</li>
                                            </ul>
                                        </CardContent>
                                    </Card>
                                </div>
                            </div>
                        </GuideSection>
                    </TabsContent>

                    <TabsContent value="schedule" className="mt-4">
                        <GuideSection title="Harmonogram posiłków">
                            <div className="space-y-4">
                                <p>
                                    Przykładowe rozłożenie posiłków w ciągu dnia:
                                </p>

                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                    <div className="bg-green-50 p-4 rounded-lg">
                                        <h4 className="font-medium mb-2">
                                            3 posiłki
                                        </h4>
                                        <ul className="space-y-2">
                                            <li>Śniadanie: 8:00</li>
                                            <li>Obiad: 13:00</li>
                                            <li>Kolacja: 18:00</li>
                                        </ul>
                                    </div>

                                    <div className="bg-purple-50 p-4 rounded-lg">
                                        <h4 className="font-medium mb-2">
                                            5 posiłków
                                        </h4>
                                        <ul className="space-y-2">
                                            <li>Śniadanie: 7:00</li>
                                            <li>II śniadanie: 10:00</li>
                                            <li>Obiad: 13:00</li>
                                            <li>Podwieczorek: 16:00</li>
                                            <li>Kolacja: 19:00</li>
                                        </ul>
                                    </div>
                                </div>

                                <div className="bg-yellow-50 p-4 rounded-lg mt-4">
                                    <h4 className="font-medium flex items-center gap-2">
                                        <AlertCircle className="w-5 h-5 text-yellow-600"/>
                                        Ważne uwagi
                                    </h4>
                                    <ul className="list-disc pl-6 mt-2 space-y-1">
                                        <li>Odstępy między posiłkami powinny wynosić 2-4 godziny</li>
                                        <li>Ostatni posiłek nie później niż 2-3 godziny przed snem</li>
                                        <li>Godziny można dostosować do preferencji użytkownika</li>
                                        <li>Należy zachować regularne pory posiłków</li>
                                    </ul>
                                </div>
                            </div>
                        </GuideSection>
                    </TabsContent>
                </Tabs>
            </CardContent>
        </Card>
    );
};

export default DietGuide;