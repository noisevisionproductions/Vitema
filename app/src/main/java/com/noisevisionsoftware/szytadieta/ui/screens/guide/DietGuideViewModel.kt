package com.noisevisionsoftware.szytadieta.ui.screens.guide

import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.model.guide.DietTip
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DietGuideViewModel @Inject constructor(
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    val dietTips = listOf(
        DietTip(
            id = 1,
            title = "Nie porównuj się z innymi",
            content = "Każdy organizm reaguje inaczej; skup się na swoich celach.",
        ),
        DietTip(
            id = 2,
            title = "Realistyczne cele",
            content = "Stawiaj na stopniowe zmiany (np. 0,5–1 kg tygodniowo), aby uniknąć efektu jo-jo.",
        ),
        DietTip(
            id = 3,
            title = "Sen i redukcja stresu",
            content = "Niedobór snu i stres zaburzają gospodarkę hormonalną, zwiększając apetyt.",
        ),
        DietTip(
            id = 4,
            title = "Ruszaj się codziennie",
            content = "Nawet 30-minutowy spacer poprawia metabolizm i nastrój.",
        ),
        DietTip(
            id = 5,
            title = "Mindful eating",
            content = "Jedz wolno, bez rozpraszaczy (np. telewizora), skupiając się na sygnałach głodu i sytości.",
        ),
        DietTip(
            id = 6,
            title = "Czytaj etykiety",
            content = "Sprawdzaj skład produktów, unikaj tych z syropem glukozowo-fruktozowym, tłuszczami trans lub konserwantami.",
        ),
        DietTip(
            id = 7,
            title = "Ogranicz sól",
            content = "Unikaj nadmiaru soli, która podnosi ciśnienie.",
        ),
        DietTip(
            id = 8,
            title = "Nawodnienie",
            content = "Pij minimum 1,5–2 litry wody dzienne; ogranicz słodzone napoje.",
        ),
        DietTip(
            id = 9,
            title = "Świeże produkty",
            content = "Wybieraj świeże warzywa, owoce, pełnoziarniste produkty i naturalne białka.",
        ),
        DietTip(
            id = 10,
            title = "Białko",
            content = "Białko w każdym posiłku – Wspiera sytość i budowę mięśni (jaja, ryby, tofu, rośliny strączkowe).",
        ),
        DietTip(
            id = 11,
            title = "Błonnik pokarmowy",
            content = "Warzywa, owoce, pełnoziarniste produkty i rośliny strączkowe dla lepszego trawienia.",
        ),
        DietTip(
            id = 12,
            title = "Ogranicz cukier",
            content = "Unikaj słodyczy, soków i napojów słodzonych; zastąp je owocami.",
        ),
        DietTip(
            id = 13,
            title = "Zdrowe tłuszcze",
            content = "Wybieraj źródła nienasyconych kwasów tłuszczowych (awokado, orzechy, oliwa z oliwek, ryby).",
        )
    )
}