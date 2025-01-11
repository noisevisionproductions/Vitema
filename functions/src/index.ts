import * as admin from "firebase-admin";
import {pubsub} from "firebase-functions/v1";
import {logger} from "firebase-functions/v2";
import {performIncrementalSync, performSync} from "./syncService";

admin.initializeApp();

// Istniejący scheduler (raz do roku)
export const googleSheetSynchornizeEverything = pubsub
  .schedule("0 0 1 1 *")
  .onRun(async () => {
    try {
      await performSync();
      logger.info("Pełna synchronizacja zakończona pomyślnie");
    } catch (error) {
      logger.error("Błąd pełnej synchronizacji:", error);
      throw error;
    }
  });

// Nowy scheduler (co godzinę)
export const googleSheetIncrementalSync = pubsub
  .schedule("0 * * * *")
  .onRun(async () => {
    try {
      await performIncrementalSync();
      logger.info("Synchronizacja przyrostowa zakończona pomyślnie");
    } catch (error) {
      logger.error("Błąd synchronizacji przyrostowej:", error);
      throw error;
    }
  });
