# EstatePay - Property Installment & Client Payment Tracker

A modern Android application for real estate developers, agents, and property managers to seamlessly manage property buyers, structure installment schedules, record payments, track outstanding balances, and generate shareable payment receipts and statements.

## User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were dismissed, so recommended industry-standard defaults for property installment management have been adopted. You can review and adjust any of these aspects before execution:

- **Primary Persona & Role**: Built with a comprehensive multi-client & property portfolio structure tailored for property sales agents, developers, and brokers, while also offering individual client summary sheets that can be viewed or shared directly with property buyers.
- **Flexible Schedule Generation**: Supports multiple property financing schemes:
  1. Equal Monthly Installments (fixed monthly payments over 12, 24, 36, 60+ months).
  2. Down payment split + Monthly amortization + Turnover / handover balance.
  3. Construction progress / milestone-based payment schedules.
- **Client Receipts & Statements of Account (SOA)**: Built-in shareable visual digital receipts and complete Statement of Account summary cards with quick actions (share text/summary, export details, WhatsApp/SMS/phone direct communication).
- **Offline-First Resilience**: Powered by Android Room Database with reactive Kotlin Flows, ensuring all records, calculations, and payment logs persist securely without requiring external cloud subscriptions.

---

## 1. Overview & Core Concept

### What It Does
EstatePay streamlines real estate installment collection and client management. Property professionals can register clients, link them to specific property units or developments, generate tailored installment schedules (with automatic due dates and amounts), record incoming payments, track payment methods and bank references, calculate real-time remaining balances, and identify overdue or upcoming payments with urgency badges.

### Target Audience & Persona
- Real estate agents, brokers, and sales teams managing multiple buyers across condominiums, subdivisions, or commercial properties.
- Property developers tracking customer accounts receivable and collection milestones.
- Independent landlords or property sellers offering in-house installment financing.

### Key Value
Eliminates messy spreadsheets, missed due dates, and manual calculation errors. Provides instant clarity on collection performance, upcoming receivables, and overdue payments, while giving clients professional payment receipts and balance statements.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Portfolio Overview**:
   - Executive statistics cards: Total Portfolio Value, Total Collected to Date, Outstanding Receivables, and Overdue Collections.
   - Quick-action urgent alerts: "Due in 7 Days" and "Overdue" chips with direct client follow-up.
   - Recent activity feed showing recently recorded payments.

2. **Client & Property Directory**:
   - Searchable, filterable client roster showing unit tag, property name, balance progress bar (% paid), and payment health badge (Current, Due Soon, Overdue, Completed).
   - "New Property Client" multi-step dialog: Client details (name, phone, email, unit/block, project name, total contract price, down payment, installment plan, interest/penalty rules, start date).

3. **Client Property Detail & Schedule View**:
   - Property summary header: Unit details, contract price, total paid vs. remaining balance circular/linear progress indicator.
   - Installment Schedule tab: Chronological schedule of installments with status badges (`PAID`, `PARTIAL`, `DUE`, `OVERDUE`), due date countdown, and "Pay Now" action.
   - Payment History tab: Chronological ledger of all recorded payments with receipt numbers, payment methods (Wire, Check, Cash, Card), reference numbers, and timestamps.
   - Statement of Account (SOA) generator: Clean formatted preview ready for sharing via Android share sheet.

4. **Payment Recording & Receipt Modal**:
   - Tap "Record Payment" on an installment or client sheet.
   - Auto-allocates to oldest unpaid/partial installment or custom installment selection.
   - Enter amount paid, payment method, bank/transaction reference number, payment date, and optional receipt notes.
   - Instant generation of an official digital receipt card with transaction ID, remaining balance, and one-tap share/copy.

5. **Analytics & Reports Screen**:
   - Collection efficiency metrics, monthly cash inflow projections, overdue breakdown by aging (1-30 days, 31-60 days, 60+ days), and currency customization.

### Visual Identity & Theme
- **Aesthetic Direction**: *Architectural Luxury & Trustworthy Financial Precision*. Clean, geometric, contemporary aesthetic inspired by high-end architectural firms and modern fintech platforms.
- **Color Palette**:
  - Primary: Deep Emerald Navy (`#0F2926` / `#164E43`) representing security, land, and stability.
  - Accent / Brand: Warm Champagne Gold (`#D4AF37` / `#E5C158`) for accents, progress milestones, and VIP badges.
  - Backgrounds: Crisp Architectural Off-White (`#F8F9FA`) in light theme; Rich Slate Obsidian (`#12161A`) in dark theme.
  - Semantic Status:
    - Paid / On Track: Mint Green (`#10B981`)
    - Upcoming / Due Soon: Amber Gold (`#F59E0B`)
    - Overdue / Alert: Crimson Red (`#EF4444`)
- **Typography & Hierarchy**:
  - Display & Headers: Bold, confident architectural geometry for currency amounts and client names.
  - Monospace Numerics: High-legibility tabular numbers for installment tables, receipt numbers, and balances to avoid visual jitter.
- **Component Styling & Layout**:
  - Elevated M3 cards with subtle borders and tonality.
  - Tactile interactive chips for status filtering.
  - Comprehensive edge-to-edge support with safe drawing insets.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Why | Alternatives Considered |
| :--- | :--- | :--- | :--- |
| **Data Persistence** | Room Database (SQLite + Flow) | 100% offline reliability, instant startup, reactive updates without network latency or cloud bills. | Remote-only REST API (requires backend server), SharedPreferences (unfit for relational schedules). |
| **Schedule Generation** | Client-side algorithmic amortizer supporting monthly, down payment split, and custom milestones | Allows flexible real-world property deals (e.g. 20% down payment over 12 months, 80% bank/in-house over 36 months). | Rigid fixed EMI only (unrealistic for real estate sales). |
| **Receipt & Statement Export** | Rich Compose UI Receipt Card with clipboard and native Android text/graphic share intent | Instant sharing to WhatsApp, Email, or Messaging without requiring external PDF rendering binaries. | Heavy PDF generation library (bloats APK size and can cause rendering inconsistencies). |
| **Initial Demo Data** | Rich realistic property sales seed data loaded on first run | Immediate hands-on experience showing active clients, overdue installments, and paid receipts. | Empty blank screen on first launch requiring tedious manual entry. |

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├────────────────────────────────┬───────────────────────────────────────┤
│  DashboardScreen               │  ClientDetailScreen                   │
│  - Portfolio Summary Cards     │  - Unit Financial Header              │
│  - Collection Alerts Banner    │  - Installment Schedule Timeline      │
│  - Quick Filter Chips          │  - Payment Ledger & Statement View    │
├────────────────────────────────┼───────────────────────────────────────┤
│  ClientsListScreen             │  RecordPaymentDialog / Sheet          │
│  - Search & Status Filters     │  - Installment Allocation Selector    │
│  - Client Card & Progress Bar  │  - Method, Date, Reference Inputs     │
│  - New Client Multi-Step Modal │  - Generated Digital Receipt Card     │
└────────────────────────────────┴───────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      ViewModel Layer (StateFlow)                       │
│  PropertyInstallmentViewModel                                          │
│  - uiState: Combined Flow of Clients, Properties, Installments, Stats  │
│  - Intent handlers: addClientWithProperty, recordPayment, filterData   │
└────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Repository Pattern Layer                        │
│  PropertyRepository                                                    │
│  - Combines DAOs, manages transactions, calculates amortizations       │
└────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Room Database (SQLite + KSP)                       │
├────────────────────┬────────────────────┬──────────────────────────────┤
│  clients_table     │  properties_table  │  installments_table          │
│  - id (PK)         │  - id (PK)         │  - id (PK)                   │
│  - name, phone     │  - clientId (FK)   │  - propertyId (FK)           │
│  - email, notes    │  - unitName, dev   │  - installmentNo, dueDate    │
│  - createdAt       │  - totalPrice      │  - amountDue, amountPaid     │
│                    │  - downPayment     │  - status (DUE/PAID/OVERDUE) │
├────────────────────┴────────────────────┼──────────────────────────────┤
│  payments_table                         │  Database Type Converters    │
│  - id (PK), installmentId (FK)          │  - Date/Timestamp Converter  │
│  - amountPaid, paymentDate, method      │  - PaymentStatus Enum        │
│  - referenceNo, receiptNo, notes        │  - PaymentMethod Enum        │
└─────────────────────────────────────────┴──────────────────────────────┘
```

### Data Model & State

```kotlin
// Entity definitions:
ClientEntity(id, fullName, phone, email, notes, createdAt)
PropertyEntity(id, clientId, unitIdentifier, developmentName, propertyType, totalPrice, downPayment, notes)
InstallmentEntity(id, propertyId, installmentNumber, title, dueDate, amountDue, amountPaid, status, penaltyAmount)
PaymentRecordEntity(id, installmentId, propertyId, amountPaid, paymentDate, paymentMethod, referenceNumber, receiptNumber, recordedBy, notes)
```

### Interactive Component & State Mapping

- **Portfolio Statistics Card**: Dynamically aggregates `totalPrice`, `sum(amountPaid)`, and `count(status == OVERDUE)` reactively from Room Flows.
- **Client Creation Modal**: Allows configuring property purchase price, down payment percentage/amount, number of installment months, and interest rate or split terms. Generates installment records automatically in a single atomic database transaction.
- **Pay Installment Dialog**: Pre-populates selected installment, displays remaining balance, accepts full or partial payments, logs payment record with receipt ID, and recalculates installment status (`PAID` vs `PARTIAL`).
- **Share Receipt / Statement**: Generates a formatted text summary or image-ready card with unit details, payment date, reference number, amount paid, and remaining balance for easy client communication.
- **Quick Filter Bar**: Instant filtering by "All", "Action Needed (Overdue & Due Soon)", "Active Plans", and "Fully Paid".
