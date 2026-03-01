# 📱 Android Auto-Dialer App — Design Prompt

> Use this prompt with any AI design or development tool to build a fully functional Android Auto-Dialer app that reads contact data from pasted Excel/CSV content and dials numbers automatically one by one.

---

## 🎯 App Overview

Design a modern **Android Auto-Dialer application** where the user pastes Excel/spreadsheet data column-wise, and the app automatically dials phone numbers one by one while displaying the associated contact information on screen during each call.

---

## 📋 Screen 1: Data Import Screen

- A large **paste area / text input box** where the user can paste data copied directly from Excel or Google Sheets (tab-separated or comma-separated)
- A **"Detect Columns"** button that auto-parses the pasted content and identifies columns (e.g., Name, Phone Number, Company, Email, Notes)
- A **column mapping UI** that lets the user assign which column is the "Phone Number" field and which are display fields
- A **preview table** showing the parsed rows before starting
- A **"Start Auto-Dial" button** that initiates the dialing session
- Support for at least 5 columns of custom data per contact

---

## 📞 Screen 2: Active Dialing Screen (Main Screen)

This is the screen shown **during each active call**. It should display:

- **Large contact name** at the top (bold, prominent)
- **Phone number being dialed** clearly visible
- A **progress indicator**: e.g., "Calling 3 of 47"
- A **card/panel showing all extra info** from the spreadsheet columns:
  - Company Name
  - Email
  - Any custom notes or tags
- **Call status badge**: Ringing / Connected / Call Ended
- **Action buttons:**
  - ⏭ Skip to Next
  - ⏸ Pause Session
  - 🔁 Retry Current Number
  - 🛑 End Session
- A **call outcome selector** (after each call ends): Answered / No Answer / Busy / Left Voicemail / Callback Requested
- A **notes field** to quickly jot a note for that contact after the call

---

## 📊 Screen 3: Session Summary Screen

- Total calls made
- Breakdown by outcome (Answered, No Answer, Busy, etc.)
- List of all contacts with their logged outcomes and notes
- **Export button** to save results back as a CSV/Excel file
- Option to **re-dial only specific outcomes** (e.g., retry all "No Answer")

---

## 🎨 UI/UX Design Requirements

- Follow **Material Design 3** guidelines
- Use a **clean, professional color scheme** (suggested: deep blue primary, white background, green for active call state)
- **Dark mode** support
- Large, readable fonts for contact info (accessibility-friendly)
- Smooth **transitions between calls** (slide-in animation for next contact card)
- **Haptic feedback** when a call starts or ends
- The dialing screen should feel like a **mission control dashboard** — calm, organized, information-dense but not cluttered

---

## ⚙️ Technical Requirements

- Language: **Kotlin** (Android Native) or **Flutter** (cross-platform)
- Permissions required: `CALL_PHONE`, `READ_CONTACTS`, `WRITE_EXTERNAL_STORAGE`
- Use Android's `Intent.ACTION_CALL` to initiate calls programmatically
- Detect call state changes using `PhoneStateListener` or `TelephonyCallback`
- Auto-advance to the next number after the current call ends
- Store session data locally using **Room Database** or SharedPreferences
- CSV parsing library: **OpenCSV** (for Kotlin) or **csv** package (for Flutter)

---

## 📂 Suggested Data Columns (Excel Format)

| Column | Description |
|--------|-------------|
| Name | Full name of the contact |
| Phone | Phone number to dial |
| Company | Organization name |
| Email | Email address |
| Notes | Pre-call notes or script hints |
| Priority | High / Medium / Low |
| Last Contacted | Date of last interaction |

---

## 🔄 Auto-Dial Flow

```
Paste Excel Data
      ↓
Parse & Map Columns
      ↓
Preview Contact List
      ↓
Start Session → Dial Contact #1
      ↓
Show Contact Info During Call
      ↓
Call Ends → Log Outcome + Notes
      ↓
Auto-Dial Contact #2
      ↓
... Repeat Until List Complete ...
      ↓
Show Session Summary → Export Results
```

---

## 🧩 Optional / Bonus Features

- **Call script display**: Show a customizable script or talking points during the call
- **Do Not Call list**: Flag numbers to skip automatically
- **Time-based dialing**: Only dial between certain hours
- **Voicemail drop**: Play a pre-recorded message if voicemail is detected
- **Team mode**: Sync session data across multiple devices
- **CRM integration**: Push call outcomes directly to HubSpot, Salesforce, or Google Sheets via API

---

*Use this prompt as a complete specification for design mockups (Figma), AI-generated code, or developer handoff documentation.*
