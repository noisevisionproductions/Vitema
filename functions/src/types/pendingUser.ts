import {Gender} from "./gender";

export interface PendingMeasurement {
  date: number;
  height: number;
  weight: number;
  neck: number;
  biceps: number;
  chest: number;
  waist: number;
  belt: number;
  hips: number;
  thigh: number;
  calf: number;
}

export interface PendingUser {
  email: string;
  gender: Gender;
  firstAndLastName: string;
  storedAge: number;
  lastUpdated: Date;
  measurements: PendingMeasurement[];
}
