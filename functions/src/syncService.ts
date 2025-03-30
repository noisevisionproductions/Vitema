import {logger} from "firebase-functions/v2";
import {getSheetData} from "./services/googleSheets";
import {addBodyMeasurements, updateUserData} from "./services/firestore";
import {mapGender} from "./utils/genderMapper";
import {parseGoogleFormDate} from "./utils/dateParser";
import admin from "firebase-admin";

interface LastSyncData {
    lastSyncTimestamp: number;
    lastProcessedRowIndex: number;
}

async function processRow(row: any[]) {
  const timestamp = row[0];
  const firstAndLastName = row[1];
  const email = row[2];
  const age = row[3];
  const genderString = row[4];
  const height = row[5];
  const weight = row[6];
  const neck = row[9];
  const chest = row[10];
  const biceps = row[11];
  const waist = row[12];
  const belt = row[13];
  const hips = row[14];
  const thigh = row[15];
  const calf = row[16];

  if (!email) return;

  try {
    const gender = mapGender(genderString);
    const userSnapshot = await updateUserData(
      email,
      parseInt(age),
      firstAndLastName,
      gender
    );

    if (userSnapshot && !userSnapshot.isPending) {
        const usersSnapshot = await admin.firestore()
            .collection("users")
            .where("email", "==", email)
            .get();

        if (!usersSnapshot.empty) {
            const userDoc = usersSnapshot.docs[0];
            await userDoc.ref.update({
                surveyCompleted: true
            });
            logger.info(`Oznaczono ankietę jako wypełnioną dla użytkownika: ${email}`);
        }
    }

    if (userSnapshot && (height || weight)) {
      const measurementDate = parseGoogleFormDate(timestamp);

      const measurementData = {
        date: measurementDate,
        height: parseInt(height) || 0,
        weight: parseInt(weight) || 0,
        neck: parseInt(neck) || 0,
        chest: parseInt(chest) || 0,
        biceps: parseInt(biceps) || 0,
        waist: parseInt(waist) || 0,
        belt: parseInt(belt) || 0,
        hips: parseInt(hips) || 0,
        thigh: parseInt(thigh) || 0,
        calf: parseInt(calf) || 0,
      };

      await addBodyMeasurements(
        userSnapshot.id,
        measurementData,
        userSnapshot.isPending
      );
    }
  } catch (error) {
    logger.error(`Błąd podczas aktualizacji użytkownika ${email}:`, error);
  }
}

// Pełna synchronizacja (raz do roku)
export async function performSync() {
  try {
    const rows = await getSheetData();

    if (!rows || rows.length <= 1) {
      throw new Error("Brak danych w arkuszu");
    }

    for (let i = 1; i < rows.length; i++) {
      await processRow(rows[i]);
    }

    return {success: true, processedRows: rows.length - 1};
  } catch (error) {
    logger.error("Błąd podczas pełnej synchronizacji:", error);
    throw error;
  }
}

// Synchronizacja przyrostowa (co godzinę)
export async function performIncrementalSync() {
  try {
    const rows = await getSheetData();
    if (!rows || rows.length <= 1) {
      logger.info("Brak danych w arkuszu do synchronizacji przyrostowej");
      return;
    }

    const lastSyncRef = admin.firestore()
      .collection("system")
      .doc("sheetSync");

    const lastSyncDoc = await lastSyncRef.get();
    const lastSyncData = lastSyncDoc.data() as LastSyncData;
    const lastSyncTimestamp = lastSyncData?.lastSyncTimestamp || 0;

    let lastProcessedIndex = -1;
    for (let i = 1; i < rows.length; i++) {
      const timestamp = parseGoogleFormDate(rows[i][0]);
      if (timestamp <= lastSyncTimestamp) {
        lastProcessedIndex = i;
      }
    }

    const newRows = rows.slice(lastProcessedIndex + 1);

    if (newRows.length > 0) {
      for (const row of newRows) {
        await processRow(row);
      }

      const lastTimestamp = parseGoogleFormDate(rows[rows.length - 1][0]);

      await lastSyncRef.set({
        lastSyncTimestamp: lastTimestamp,
        lastProcessedRowIndex: rows.length - 1,
      });
    }

    return {
      success: true,
      processedRows: newRows.length,
      newEntriesFound: newRows.length,
    };
  } catch (error) {
    logger.error("Błąd podczas synchronizacji przyrostowej:", error);
    throw error;
  }
}
