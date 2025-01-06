import * as admin from "firebase-admin";
import { pubsub } from "firebase-functions/v1";
import { logger } from "firebase-functions/v2";
import { performSync } from './syncService';

admin.initializeApp();

export const scheduledSync = pubsub
  .schedule("every 1 hours")
  .onRun(async () => {
    try {
      await performSync();
      logger.info("Synchronizacja zakończona pomyślnie");
    } catch (error) {
      logger.error("Błąd synchronizacji:", error);
      throw error;
    }
  });
