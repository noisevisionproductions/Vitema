import {Gender} from "../types/gender";

export function mapGender(genderString: string): Gender {
  switch (genderString.toLowerCase().trim()) {
  case "mężczyzna":
  case "mezczyzna":
    return Gender.MALE;
  case "kobieta":
    return Gender.FEMALE;
  default:
    return Gender.OTHER;
  }
}
