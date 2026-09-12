import type { GridColDef } from "@mui/x-data-grid";

// Refine resource name for permission pages (must match resources[].name).
export const RESOURCE = "permissions";
export const API_PATH = "/api/permission";
export const RESOURCE_META = { apiPath: API_PATH } as const;

// Entity label used for titles and action text.
export const ENTITY_LABEL = "Permission";

// Primary permission fields.
export const CODE_FIELD = "code";
export const CODE_LABEL = "Code";
export const DESCRIPTION_FIELD = "description";
export const DESCRIPTION_LABEL = "Description";

type CellRenderer = NonNullable<GridColDef["renderCell"]>;

export const createListColumns = (renderActions: CellRenderer): GridColDef[] => [
  { field: "id", headerName: "ID", minWidth: 260, flex: 1 },
  { field: CODE_FIELD, headerName: CODE_LABEL, minWidth: 200, flex: 1 },
  { field: DESCRIPTION_FIELD, headerName: DESCRIPTION_LABEL, minWidth: 260, flex: 1 },
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
