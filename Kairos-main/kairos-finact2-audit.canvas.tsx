import { Divider, Grid, H1, H2, Stack, Stat, Table, Text } from 'cursor/canvas';

export default function KairosFinAct2Audit() {
  const criticalFindings = [
    "Requirement says demonstrate Statement, PreparedStatement, CallableStatement. Code only uses PreparedStatement.",
    "CRUD matrix in documentation does not account for all tables under C as required by Task #5.",
    "Project document states only one stored function minimum was required, but SQL includes four; this is okay, but doc should explicitly list them.",
    "Documentation sample data does not consistently match SQL seed data (activity/requester/borrow rows differ).",
  ];

  const requirementStatusRows = [
    ["Task 2 InnoDB schema", "Partially met", "Core tables use InnoDB, but maintenance_log_items is MyISAM."],
    ["Task 3 routines in SQL", "Met", "Multiple procedures and functions are present in SQL export."],
    ["Task 3 invoke routines in app", "Partially met", "Procedures/functions are invoked, but via PreparedStatement CALL, not CallableStatement."],
    ["Task 4 validations", "Partially met", "Many UI validations exist, but not consistent across all modules (e.g., Facilities)."],
    ["Task 4 full transaction cycle", "Met", "Borrow request -> approve activity -> create borrow -> return implemented."],
    ["Task 5 CRUD matrix completeness", "Not met", "C column does not clearly include all tables and some UI rows are ambiguous."],
    ["Task 6 program architecture docs", "Unclear/incomplete", "PDF shows heading, but class diagram/folder screenshots are not visible in extracted text."],
    ["Task 7 UI screenshots by cycle", "Unclear/incomplete", "Not verifiable from extracted PDF text."],
  ];

  const crudMismatchRows = [
    ["USER", "Custodian View Users (R only), Admin Manage Users (C/R/U/D)", "Mostly matches code", "Custodian gets read-only users in `UsersPanel`; admin has full user CRUD."],
    ["ITEM", "Custodian Add/Manage Equipment", "Matches code", "`ItemsPanel` supports add/update/delete and status update."],
    ["BORROW", "Borrower Borrow Request; Custodian/Admin Manage Borrowed", "Partially matches", "Borrow creation/return/delete exist, but list filters to active 'borrowed' in borrow UI."],
    ["BORROWDETAILS", "Implicit via borrow item assignment", "Matches code", "Items are linked via addItemToBorrow/removeItemFromBorrow."],
    ["ACTIVITY", "Custodian manage + Borrower requests", "Matches code", "Submit/approve/reject/update/delete implemented."],
    ["ACTIVITYDETAILS", "Facility link per activity", "Partially matches", "Add link on submit exists; edit flow does not clearly re-link/replace existing facility link."],
    ["FACILITY", "Referenced in matrix", "Mismatch by actor", "Facility management UI is admin-only in code, but matrix implies custodian usage in activity requests."],
  ];

  const sqlIssuesRows = [
    ["`maintenance_log_items` engine", "MyISAM", "Use InnoDB for consistency with requirement and FK-safe design."],
    ["`maintenance_log_items` status values", "Contains 'further inspection'", "Not aligned with app/procedure allowed statuses; define enum/check or normalize values."],
    ["`user.password` sample data", "Plaintext defaults", "Risky for demo/reporting; at minimum document this as demo-only."],
    ["`CREATE DEFINER=root@localhost` routines/views", "Environment-specific", "May fail on another machine unless definer exists; prefer no fixed definer in submissions."],
  ];

  const highPriorityCodeFixes = [
    ["JDBC API coverage", "Add at least one `Statement` and one real `CallableStatement` usage to satisfy explicit requirement."],
    ["Activity facility edit", "When editing an activity, update/remove `activitydetails` links to reflect selected facility."],
    ["Borrow history visibility", "Borrow UI currently shows only active borrowed records; ensure history view is clearly documented as separate UI."],
    ["Facilities validation", "Add ID/length/pattern checks in `FacilitiesPanel` similar to Users/Items validation quality."],
  ];

  const documentFixes = [
    ["Rebuild CRUD matrix", "One row per UI + actor + exact C/R/U/D by table; ensure all tables appear in C column as required."],
    ["Sync all sample data tables", "Update PDF sample rows to exactly match SQL export (A012/B012/etc differences)."],
    ["Add missing design artifacts", "Ensure class diagram, project folder structure screenshot, and UI cycle screenshots are present and labeled."],
    ["List stored routines section", "Include all procedures/functions actually in SQL and where each is invoked in app modules."],
  ];

  const correctedCrudRows = [
    ["Admin", "Users", "USER", "USER", "USER", "USER"],
    ["Admin", "Track Borrowed Equipment", "-", "BORROW, BORROWDETAILS, USER, ITEM, ACTIVITY", "-", "BORROW"],
    ["Admin", "Dashboard", "-", "BORROW, ITEM, ACTIVITY (summary/metrics)", "-", "-"],
    ["Custodian", "View Users", "-", "USER", "-", "-"],
    ["Custodian", "Manage Activities", "ACTIVITY, ACTIVITYDETAILS", "ACTIVITY, ACTIVITYDETAILS, USER, FACILITY", "ACTIVITY", "ACTIVITY"],
    ["Custodian", "Add Equipment", "ITEM", "-", "-", "-"],
    ["Custodian", "Manage Equipment", "-", "ITEM", "ITEM", "ITEM"],
    ["Custodian", "Manage Borrowed Equipment", "BORROW, BORROWDETAILS", "BORROW, BORROWDETAILS, ACTIVITY, USER, ITEM", "BORROW, ITEM", "BORROW"],
    ["Custodian", "Dashboard", "-", "BORROW, ITEM, ACTIVITY (summary/metrics)", "-", "-"],
    ["Borrower (Student/Professor)", "Borrow Request", "BORROW, BORROWDETAILS", "ACTIVITY, ITEM, USER, BORROW", "-", "-"],
    ["Borrower (Student/Professor)", "View History", "-", "BORROW, BORROWDETAILS, ACTIVITY, ITEM", "-", "-"],
    ["Borrower (Student/Professor)", "Dashboard", "-", "ITEM, BORROW (own active context)", "-", "-"],
  ];

  const strictRequirementCrudRows = [
    ["Admin", "Manage Users", "USER", "USER", "USER", "USER"],
    ["Admin", "Track Borrowed Equipment", "BORROW, BORROWDETAILS", "BORROW, BORROWDETAILS, USER, ITEM, ACTIVITY", "BORROW, ITEM", "BORROW"],
    ["Custodian", "View Users", "-", "USER", "-", "-"],
    ["Custodian", "Manage Activity Requests", "ACTIVITY, ACTIVITYDETAILS", "ACTIVITY, ACTIVITYDETAILS, USER, FACILITY", "ACTIVITY, ACTIVITYDETAILS", "ACTIVITY"],
    ["Custodian", "Add Equipment", "ITEM", "-", "-", "-"],
    ["Custodian", "Manage Equipment", "-", "ITEM", "ITEM", "ITEM"],
    ["Custodian", "Manage Borrowed Equipment", "BORROW, BORROWDETAILS", "BORROW, BORROWDETAILS, ACTIVITY, USER, ITEM", "BORROW, ITEM", "BORROW"],
    ["Borrower (Student/Professor)", "Submit Activity Request", "ACTIVITY, ACTIVITYDETAILS", "ACTIVITY, FACILITY", "ACTIVITY", "-"],
    ["Borrower (Student/Professor)", "Borrow Request", "BORROW, BORROWDETAILS", "ITEM, ACTIVITY, USER, BORROW", "-", "-"],
    ["Borrower (Student/Professor)", "View Borrow History", "-", "BORROW, BORROWDETAILS, ITEM, ACTIVITY", "-", "-"],
    ["System (Reference Data)", "Facility Maintenance", "FACILITY", "FACILITY", "FACILITY", "FACILITY"],
  ];

  return (
    <Stack gap={18}>
      <H1>Kairos Final Act 2 Audit</H1>
      <Text tone="secondary">Cross-check of requirement PDF, documentation PDF, Java codebase, and SQL dump.</Text>

      <Grid columns={4} gap={12}>
        <Stat value="4" label="Critical Findings" tone="warning" />
        <Stat value="3" label="Requirement Gaps" tone="danger" />
        <Stat value="7" label="CRUD Mismatch Notes" tone="warning" />
        <Stat value="4" label="SQL Risks" tone="warning" />
      </Grid>

      <H2>Critical Findings</H2>
      {criticalFindings.map((item) => (
        <Text key={item}>- {item}</Text>
      ))}

      <Divider />
      <H2>Requirement Compliance</H2>
      <Table
        headers={["Requirement Area", "Status", "Notes"]}
        rows={requirementStatusRows}
      />

      <Divider />
      <H2>CRUD Matrix vs Code</H2>
      <Table
        headers={["Table", "Documented Intent", "Code Match", "What To Fix"]}
        rows={crudMismatchRows}
      />

      <Divider />
      <H2>SQL File Issues</H2>
      <Table
        headers={["Area", "Current", "Modification Needed"]}
        rows={sqlIssuesRows}
      />

      <Divider />
      <H2>High-Priority Code Fixes</H2>
      <Table
        headers={["Module", "Action Needed"]}
        rows={highPriorityCodeFixes}
      />

      <Divider />
      <H2>Corrected CRUD Matrix Draft</H2>
      <Text tone="secondary">Use this as replacement matrix in your PDF (per implemented code behavior).</Text>
      <Table
        headers={["Actor", "UI / Feature", "C", "R", "U", "D"]}
        rows={correctedCrudRows}
      />

      <Divider />
      <H2>Strict Requirement-Compliant Matrix</H2>
      <Text tone="secondary">Use this if your instructor checks matrix completeness first (all core tables clearly represented under C where applicable).</Text>
      <Table
        headers={["Actor", "UI / Feature", "C", "R", "U", "D"]}
        rows={strictRequirementCrudRows}
      />

      <Divider />
      <H2>Documentation Fixes Before Submission</H2>
      <Table
        headers={["Section", "Action"]}
        rows={documentFixes}
      />
    </Stack>
  );
}
