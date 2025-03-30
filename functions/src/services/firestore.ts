import * as admin from "firebase-admin";
import {Gender} from "../types/gender";
import {v4 as uuidv4} from "uuid";
import {BodyMeasurements, MeasurementSourceType, MeasurementType} from "../types/bodyMeasurements";
import {PendingMeasurement, PendingUser} from "../types/pendingUser";
import {logger} from "firebase-functions/v2";

export async function updateUserData(
  email: string,
  storedAge: number,
  firstAndLastName: string,
  gender: Gender
) {
  const userSnapshot = await admin.firestore()
    .collection("users")
    .where("email", "==", email)
    .get();

  if (userSnapshot.empty) {
    const pendingUserRef = admin.firestore()
      .collection("pendingUsers")
      .doc(email);

    const pendingUserSnapshot = await pendingUserRef.get();
    const timestamp = admin.firestore.Timestamp.now();

    if (!pendingUserSnapshot.exists) {
      const pendingUser: PendingUser = {
        email,
        gender,
        firstAndLastName,
        storedAge,
        lastUpdated: timestamp.toDate(),
        measurements: [],
      };

      await pendingUserRef.set(pendingUser);
      logger.info(`Utworzono nowego pending user: ${email}`);
    } else {
      await pendingUserRef.update({
        gender,
        storedAge,
        firstAndLastName,
        lastUpdated: timestamp.toDate(),
      });
      logger.info(`Zaktualizowano dane pending user: ${email}`);
    }

    return {
      id: email,
      email,
      gender,
      storedAge,
      firstAndLastName,
      isPending: true,
    };
  }

  const userDoc = userSnapshot.docs[0];
  const timestamp = admin.firestore.Timestamp.now();

  const updateData = {
    gender,
    storedAge,
    firstAndLastName,
    surveyCompleted: true,
    lastUpdated: timestamp,
  };

  await userDoc.ref.update(updateData);

  return {
    id: userDoc.id,
    ...userDoc.data(),
    ...updateData,
    isPending: false,
  };
}

export async function addBodyMeasurements(
  userId: string,
  measurements: Partial<BodyMeasurements>,
  isPending: boolean
) {
  if (!measurements.date) {
    throw new Error("Data pomiaru jest wymagana");
  }

  const measurementDate = new Date(measurements.date);

  if (isPending) {
    const pendingUserRef = admin.firestore()
      .collection("pendingUsers")
      .doc(userId);

    try {
      await admin.firestore().runTransaction(async (transaction) => {
        const pendingDoc = await transaction.get(pendingUserRef);

        if (!pendingDoc.exists) {
          throw new Error(`Nie znaleziono dokumentu pending user dla: ${userId}`);
        }

        const userData = pendingDoc.data() as PendingUser;
        const currentMeasurements = userData.measurements || [];

        const newMeasurement: PendingMeasurement = {
          date: measurementDate.getTime(),
          height: measurements.height || 0,
          weight: measurements.weight || 0,
          neck: measurements.neck || 0,
          biceps: measurements.biceps || 0,
          chest: measurements.chest || 0,
          waist: measurements.waist || 0,
          belt: measurements.belt || 0,
          hips: measurements.hips || 0,
          thigh: measurements.thigh || 0,
          calf: measurements.calf || 0,
        };

        const updatedMeasurements = [...currentMeasurements, newMeasurement];

        transaction.update(pendingUserRef, {
          measurements: updatedMeasurements,
          lastUpdated: admin.firestore.Timestamp.now(),
        });
      });

      logger.info(`Dodano nowy pomiar do pending user: ${userId}`);
    } catch (error) {
      logger.error(`Błąd podczas dodawania pomiaru do pending user: ${userId}`, error);
      throw error;
    }
    return;
  }

  const bodyMeasurements: BodyMeasurements = {
    id: uuidv4(),
    userId,
    date: measurementDate.getTime(),
    height: measurements.height || 0,
    weight: measurements.weight || 0,
    neck: measurements.neck || 0,
    biceps: measurements.biceps || 0,
    chest: measurements.chest || 0,
    waist: measurements.waist || 0,
    belt: measurements.belt || 0,
    hips: measurements.hips || 0,
    thigh: measurements.thigh || 0,
    calf: measurements.calf || 0,
    note: "",
    weekNumber: getCurrentWeekNumber(measurementDate),
    measurementType: hasMeasurements(measurements) ?
      MeasurementType.FULL_BODY :
      MeasurementType.WEIGHT_ONLY,
    sourceType: MeasurementSourceType.GOOGLE_SHEET,
  };

  await admin.firestore()
    .collection("bodyMeasurements")
    .doc(bodyMeasurements.id)
    .set(bodyMeasurements);

  logger.info(`Dodano nowy pomiar do kolekcji bodyMeasurements dla użytkownika: ${userId}`);
  return bodyMeasurements;
}

function getCurrentWeekNumber(date: Date): number {
  const start = new Date(date.getFullYear(), 0, 1);
  const diff = date.getTime() - start.getTime();
  return Math.ceil(diff / (1000 * 60 * 60 * 24 * 7));
}

function hasMeasurements(measurements: Partial<BodyMeasurements>): boolean {
  return !!(measurements.neck || measurements.biceps || measurements.chest ||
        measurements.waist || measurements.hips || measurements.thigh || measurements.calf);
}
