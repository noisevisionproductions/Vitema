import { google } from 'googleapis';
import { defineString } from "firebase-functions/params";
import { CONFIG } from '../config/config';

const spreadsheetIdParam = defineString("SHEETS_SPREADSHEET_ID");

const auth = new google.auth.GoogleAuth({
  keyFile: CONFIG.SHEET.KEY_FILE,
  scopes: CONFIG.SHEET.SCOPES,
});

export const sheets = google.sheets({ version: "v4", auth });

export async function getSheetData() {
  const spreadsheetId = spreadsheetIdParam.value();
  
  const response = await sheets.spreadsheets.values.get({
    spreadsheetId,
    range: CONFIG.SHEET.RANGE,
  });

  return response.data.values;
}
