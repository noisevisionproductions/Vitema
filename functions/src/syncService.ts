import { logger } from "firebase-functions/v2";
import { getSheetData } from './services/googleSheets';
import { updateUserData } from './services/firestore';
import { mapGender } from "./utils/genderMapper";

export async function performSync() {
  try {
    const rows = await getSheetData();

    if (!rows || rows.length <= 1) {
      throw new Error("Brak danych w arkuszu");
    }

    for (let i = 1; i < rows.length; i++) {
      const row = rows[i];
      const [_, email, age, genderString] = row;

      if (!email) continue;

      try {
        const gender = mapGender(genderString);
        await updateUserData(email, parseInt(age), gender);
        logger.info(`Zaktualizowano dane dla użytkownika: ${email}`);
      } catch (error) {
        logger.error(`Błąd podczas aktualizacji użytkownika ${email}:`, error);
      }
    }

    return { success: true, processedRows: rows.length - 1 };
  } catch (error) {
    logger.error("Błąd podczas synchronizacji:", error);
    throw error;
  }
}
