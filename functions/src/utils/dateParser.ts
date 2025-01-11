export function parseGoogleFormDate(dateString: string): number {
  try {
    const [datePart, timePart] = dateString.split(" ");
    const [year, month, day] = datePart.split("-");
    const [hours, minutes, seconds] = timePart.split(":");

    const date = new Date(
      parseInt(year),
      parseInt(month) - 1,
      parseInt(day),
      parseInt(hours),
      parseInt(minutes),
      parseInt(seconds)
    );

    return date.getTime();
  } catch (error) {
    console.error("Błąd parsowania daty:", error);
    return Date.now();
  }
}

export function getWeekNumber(date: Date): number {
  const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
  const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime() / 86400000);
  return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
}
