"use client";

import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import type { BaseRecord, HttpError } from "@refinedev/core";
import {
  DeleteButton,
  EditButton,
  List,
  SaveButton,
  ShowButton,
  useDataGrid,
} from "@refinedev/mui";
import { useModalForm } from "@refinedev/react-hook-form";
import React from "react";

import { DomainListToolbar } from "@components/domain-list-toolbar";
import {
  CODE_FIELD,
  CODE_LABEL,
  DESCRIPTION_FIELD,
  DESCRIPTION_LABEL,
  ENTITY_LABEL,
  RESOURCE,
  RESOURCE_META,
  createListColumns,
} from "./constants";

type CreatePermissionFormValues = {
  [CODE_FIELD]: string;
  [DESCRIPTION_FIELD]: string;
};

export default function PermissionListPage() {
  const [keyword, setKeyword] = React.useState("");

  const { dataGridProps, search } = useDataGrid({
    resource: RESOURCE,
    meta: RESOURCE_META,
    onSearch: ({ keyword: searchKeyword }: { keyword: string }) => {
      const value = searchKeyword.trim();

      if (!value) {
        return [];
      }

      return [
        {
          field: "keyword",
          operator: "contains",
          value,
        },
      ];
    },
  });

  const {
    modal,
    saveButtonProps,
    register,
    handleSubmit,
    formState: { errors },
  } = useModalForm<BaseRecord, HttpError, CreatePermissionFormValues>({
    refineCoreProps: {
      resource: RESOURCE,
      meta: RESOURCE_META,
      action: "create",
    },
  });

  const handleSearch = React.useCallback(
    (nextKeyword: string) => {
      setKeyword(nextKeyword);
      search({ keyword: nextKeyword });
    },
    [search],
  );

  const columns = React.useMemo(
    () =>
      createListColumns(({ row }) => (
        <>
          <EditButton hideText recordItemId={row.id} />
          <ShowButton hideText recordItemId={row.id} />
          <DeleteButton hideText recordItemId={row.id} />
        </>
      )),
    [],
  );

  return (
    <List headerButtons={() => null}>
      <DomainListToolbar
        keyword={keyword}
        onKeywordChange={setKeyword}
        onSearch={handleSearch}
        onCreate={modal.show}
      />
      <DataGrid {...dataGridProps} columns={columns} autoHeight />
      <Dialog open={modal.visible} onClose={modal.close} fullWidth maxWidth="sm">
        <DialogTitle>{`Create ${ENTITY_LABEL}`}</DialogTitle>
        <DialogContent>
          <Box
            component="form"
            id="create-permission-form"
            onSubmit={handleSubmit(modal.submit)}
            sx={{ mt: 1, display: "flex", flexDirection: "column" }}
            autoComplete="off"
          >
            <TextField
              {...register(CODE_FIELD, { required: `${CODE_FIELD} is required` })}
              error={!!errors[CODE_FIELD]}
              helperText={errors[CODE_FIELD]?.message}
              margin="normal"
              fullWidth
              label={CODE_LABEL}
            />
            <TextField
              {...register(DESCRIPTION_FIELD)}
              error={!!errors[DESCRIPTION_FIELD]}
              helperText={errors[DESCRIPTION_FIELD]?.message}
              margin="normal"
              fullWidth
              multiline
              minRows={3}
              label={DESCRIPTION_LABEL}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={modal.close}>Cancel</Button>
          <SaveButton {...saveButtonProps} type="submit" form="create-permission-form">
            Create
          </SaveButton>
        </DialogActions>
      </Dialog>
    </List>
  );
}
