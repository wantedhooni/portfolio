import type { GridColDef } from "@mui/x-data-grid";

// Resource name used by Refine hooks and API mapping (must match resources[].name).
export const RESOURCE = "users";
export const API_PATH = "/api/user";
export const RESOURCE_META = { apiPath: API_PATH } as const;

// Human-readable entity label for button/dialog text.
export const ENTITY_LABEL = "User";

export const EMAIL_FIELD = "email";
export const EMAIL_LABEL = "Email";
export const PASSWORD_FIELD = "password";
export const PASSWORD_LABEL = "Password";
export const FIRST_NAME_FIELD = "firstName";
export const FIRST_NAME_LABEL = "First Name";
export const LAST_NAME_FIELD = "lastName";
export const LAST_NAME_LABEL = "Last Name";
export const NICK_NAME_FIELD = "nickName";
export const NICK_NAME_LABEL = "Nick Name";
export const STATUS_FIELD = "status";
export const STATUS_LABEL = "Status";
export const PERMISSIONS_FIELD = "permissions";
export const PERMISSIONS_LABEL = "Permissions";
export const FAIL_COUNT_FIELD = "failCount";
export const FAIL_COUNT_LABEL = "Fail Count";
export const ENABLED_FIELD = "enabled";
export const ENABLED_LABEL = "Enabled";

type ActionRenderer = NonNullable<GridColDef["renderCell"]>;

export const createListColumns = (renderActions: ActionRenderer): GridColDef[] => [
  { field: "id", headerName: "ID", minWidth: 300, flex: 1 },
  { field: EMAIL_FIELD, headerName: EMAIL_LABEL, minWidth: 180, flex: 1 },
  { field: FIRST_NAME_FIELD, headerName: FIRST_NAME_LABEL, minWidth: 180, flex: 1 },
  { field: LAST_NAME_FIELD, headerName: LAST_NAME_LABEL, minWidth: 180, flex: 1 },
  { field: NICK_NAME_FIELD, headerName: NICK_NAME_LABEL, minWidth: 180, flex: 1 },
  { field: STATUS_FIELD, headerName: STATUS_LABEL, minWidth: 180, flex: 1 },
  {
    field: PERMISSIONS_FIELD,
    headerName: PERMISSIONS_LABEL,
    minWidth: 220,
    flex: 1,
    valueGetter: (_, row) => (Array.isArray(row?.[PERMISSIONS_FIELD]) ? row[PERMISSIONS_FIELD].join(", ") : "-"),
  },
  { field: FAIL_COUNT_FIELD, headerName: FAIL_COUNT_LABEL, minWidth: 120 },
  { field: ENABLED_FIELD, headerName: ENABLED_LABEL, minWidth: 120 },
  {
    field: "actions",
    headerName: "Actions",
    align: "right",
    headerAlign: "right",
    minWidth: 140,
    sortable: false,
    renderCell: renderActions,
  },
];
