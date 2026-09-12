"use client";

import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from "@mui/material";
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
  EMAIL_FIELD,
  EMAIL_LABEL,
  ENTITY_LABEL,
  PASSWORD_FIELD,
  PASSWORD_LABEL,
  RESOURCE,
  RESOURCE_META,
  createListColumns,
} from "./constants";

type CreateAdminFormValues = {
  [EMAIL_FIELD]: string;
  [PASSWORD_FIELD]: string;
};

export default function AdminListPage() {
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
        {
          field: EMAIL_FIELD,
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
  } = useModalForm<BaseRecord, HttpError, CreateAdminFormValues>({
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
      createListColumns(
        ({ value }) =>
          value ? <Chip color="success" label="Y" size="small" /> : <Chip color="default" label="N" size="small" />,
        ({ row }) => (
          <>
            <EditButton hideText recordItemId={row.id} />
            <ShowButton hideText recordItemId={row.id} />
            <DeleteButton hideText recordItemId={row.id} />
          </>
        ),
      ),
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
            id="create-entity-form"
            onSubmit={handleSubmit(modal.submit)}
            sx={{ mt: 1, display: "flex", flexDirection: "column" }}
            autoComplete="off"
          >
            <TextField
              {...register(EMAIL_FIELD, { required: `${EMAIL_FIELD} is required` })}
              error={!!errors[EMAIL_FIELD]}
              helperText={errors[EMAIL_FIELD]?.message}
              margin="normal"
              fullWidth
              label={EMAIL_LABEL}
              type="email"
            />
            <TextField
              {...register(PASSWORD_FIELD, { required: `${PASSWORD_FIELD} is required` })}
              error={!!errors[PASSWORD_FIELD]}
              helperText={errors[PASSWORD_FIELD]?.message}
              margin="normal"
              fullWidth
              label={PASSWORD_LABEL}
              type="password"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={modal.close}>Cancel</Button>
          <SaveButton {...saveButtonProps} type="submit" form="create-entity-form">
            Create
          </SaveButton>
        </DialogActions>
      </Dialog>
    </List>
  );
}
