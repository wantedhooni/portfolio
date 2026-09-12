import type { GridColDef } from "@mui/x-data-grid";

// Refine resource name for admin pages (must match resources[].name).
export const RESOURCE = "admins";
export const API_PATH = "/api/admin";
export const RESOURCE_META = { apiPath: API_PATH } as const;

// Related resource used to fetch role options in admin edit form.
export const ROLE_RESOURCE = "roles";
export const ROLE_API_PATH = "/api/role";
export const ROLE_RESOURCE_META = { apiPath: ROLE_API_PATH } as const;

// Entity label used for titles and action text.
export const ENTITY_LABEL = "Admin";

// Primary account fields.
export const EMAIL_FIELD = "email";
export const EMAIL_LABEL = "Email";
export const PASSWORD_FIELD = "password";
export const PASSWORD_LABEL = "Password";

type CellRenderer = NonNullable<GridColDef["renderCell"]>;

export const createListColumns = (renderEnabled: CellRenderer, renderActions: CellRenderer): GridColDef[] => [
  {
    field: "id",
    headerName: "ID",
    minWidth: 260,
    flex: 1,
  },
  {
    field: EMAIL_FIELD,
    headerName: EMAIL_LABEL,
    minWidth: 220,
    flex: 1,
  },
  {
    field: "status",
    headerName: "Status",
    minWidth: 120,
  },
  {
    field: "enabled",
    headerName: "Enabled",
    minWidth: 120,
    renderCell: renderEnabled,
  },
  {
    field: "roles",
    headerName: "Roles",
    minWidth: 240,
    flex: 1,
    valueGetter: (_, row) => (Array.isArray(row?.roles) ? row.roles.join(", ") : "-"),
  },
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
