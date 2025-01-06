import { Gender } from '../types/gender';

export function mapGender(genderString: string): Gender {
  switch (genderString.toLowerCase().trim()) {
    case 'Mężczyzna':
      return Gender.MALE;
    case 'Kobieta':
      return Gender.FEMALE;
    default:
      return Gender.OTHER;
  }
}
