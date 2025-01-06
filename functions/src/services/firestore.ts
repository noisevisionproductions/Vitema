import * as admin from "firebase-admin";
import { Gender } from '../types/gender';

export async function updateUserData(email: string, age: number, gender: Gender) {
  const userSnapshot = await admin.firestore()
    .collection("users")
    .where("email", "==", email)
    .get();

  if (userSnapshot.empty) {
    throw new Error(`Nie znaleziono użytkownika o emailu: ${email}`);
  }

  await admin.firestore()
    .collection("users")
    .doc(userSnapshot.docs[0].id)
    .update({
      age: age,
      gender: gender,
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
    });
}
