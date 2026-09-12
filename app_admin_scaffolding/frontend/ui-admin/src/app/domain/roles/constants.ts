import type { GridColDef } from "@mui/x-data-grid";

// Refine resource name for role pages (must match resources[].name).
export const RESOURCE = "roles";
export const API_PATH = "/api/role";
export const RESOURCE_META = { apiPath: API_PATH } as const;

// Entity label used for titles and action text.
export const ENTITY_LABEL = "Role";

// Primary role fields.
export const NAME_FIELD = "name";
export const NAME_LABEL = "Name";
export const DESCRIPTION_FIELD = "description";
export const DESCRIPTION_LABEL = "Description";

type CellRenderer = NonNullable<GridColDef["renderCell"]>;

export const createListColumns = (renderActions: CellRenderer): GridColDef[] => [
  { field: "id", headerName: "ID", minWidth: 260, flex: 1 },
  { field: NAME_FIELD, headerName: NAME_LABEL, minWidth: 180, flex: 1 },
  { field: DESCRIPTION_FIELD, headerName: DESCRIPTION_LABEL, minWidth: 220, flex: 1 },
  {
    field: "admins",
    headerName: "Admins",
    minWidth: 120,
    valueGetter: (_, row) => (Array.isArray(row?.admins) ? row.admins.length : 0),
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
