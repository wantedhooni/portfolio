"use client";

import {
  Autocomplete,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Select,
  TextField,
  Typography,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useMany } from "@refinedev/core";
import {
  DateField,
  DeleteButton,
  EditButton,
  List,
  ShowButton,
  useAutocomplete,
  useDataGrid,
} from "@refinedev/mui";
import { useModalForm } from "@refinedev/react-hook-form";
import React from "react";
import { Controller } from "react-hook-form";

import { DomainListToolbar } from "@components/domain-list-toolbar";

export default function BlogPostList() {
  const [keyword, setKeyword] = React.useState("");

  const { result, dataGridProps, search } = useDataGrid({
    onSearch: ({ keyword: searchKeyword }: { keyword: string }) => {
      const value = searchKeyword.trim();

      if (!value) {
        return [];
      }

      return [
        {
          field: "title",
          operator: "contains",
          value,
        },
      ];
    },
  });

  const {
    modal,
    register,
    control,
    handleSubmit,
    formState: { errors },
    refineCore: { formLoading },
  } = useModalForm({
    refineCoreProps: {
      action: "create",
    },
  });

  const { autocompleteProps: categoryAutocompleteProps } = useAutocomplete({
    resource: "categories",
  });

  const handleSearch = React.useCallback(
    (nextKeyword: string) => {
      setKeyword(nextKeyword);
      search({ keyword: nextKeyword });
    },
    [search],
  );

  const {
    result: { data: categories },
    query: { isLoading: categoryIsLoading },
  } = useMany({
    resource: "categories",
    ids:
      result?.data?.map((item: any) => item?.category?.id).filter(Boolean) ??
      [],
    queryOptions: {
      enabled: !!result?.data,
    },
  });

  const columns = React.useMemo<GridColDef[]>(
    () => [
      {
        field: "id",
        headerName: "ID",
        type: "number",
        minWidth: 50,
        display: "flex",
        align: "left",
        headerAlign: "left",
      },
      {
        field: "title",
        headerName: "Title",
        minWidth: 200,
        display: "flex",
      },
      {
        field: "content",
        flex: 1,
        headerName: "Content",
        minWidth: 250,
        display: "flex",
        renderCell: function render({ value }) {
          if (!value) return "-";
          return (
            <Typography
              component="p"
              whiteSpace="pre"
              overflow="hidden"
              textOverflow="ellipsis"
            >
              {value}
            </Typography>
          );
        },
      },
      {
        field: "category",
        headerName: "Category",
        minWidth: 160,
        display: "flex",
        valueGetter: (_, row) => {
          const value = row?.category;
          return value;
        },
        renderCell: function render({ value }) {
          return categoryIsLoading ? (
            <>Loading...</>
          ) : (
            categories?.find((item) => item.id === value?.id)?.title
          );
        },
      },
      {
        field: "status",
        headerName: "Status",
        minWidth: 80,
        display: "flex",
      },
      {
        field: "createdAt",
        headerName: "Created at",
        minWidth: 120,
        display: "flex",
        renderCell: function render({ value }) {
          return <DateField value={value} />;
        },
      },
      {
        field: "actions",
        headerName: "Actions",
        align: "right",
        headerAlign: "right",
        minWidth: 120,
        sortable: false,
        display: "flex",
        renderCell: function render({ row }) {
          return (
            <>
              <EditButton hideText recordItemId={row.id} />
              <ShowButton hideText recordItemId={row.id} />
              <DeleteButton hideText recordItemId={row.id} />
            </>
          );
        },
      },
    ],
    [categories, categoryIsLoading]
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
      <Dialog open={modal.visible} onClose={modal.close} fullWidth maxWidth="md">
        <DialogTitle>Create Blog Post</DialogTitle>
        <DialogContent>
          <Box
            component="form"
            id="create-blog-post-form"
            onSubmit={handleSubmit(modal.submit)}
            sx={{ mt: 1, display: "flex", flexDirection: "column" }}
            autoComplete="off"
          >
            <TextField
              {...register("title", {
                required: "This field is required",
              })}
              error={!!(errors as any)?.title}
              helperText={(errors as any)?.title?.message}
              margin="normal"
              fullWidth
              InputLabelProps={{ shrink: true }}
              type="text"
              label="Title"
            />
            <TextField
              {...register("content", {
                required: "This field is required",
              })}
              error={!!(errors as any)?.content}
              helperText={(errors as any)?.content?.message}
              margin="normal"
              fullWidth
              InputLabelProps={{ shrink: true }}
              multiline
              label="Content"
            />
            <Controller
              control={control}
              name={"category.id"}
              rules={{ required: "This field is required" }}
              defaultValue={null as any}
              render={({ field }) => (
                <Autocomplete
                  {...categoryAutocompleteProps}
                  {...field}
                  onChange={(_, value) => {
                    field.onChange(value.id);
                  }}
                  getOptionLabel={(item) => {
                    return (
                      categoryAutocompleteProps?.options?.find((p) => {
                        const itemId =
                          typeof item === "object" ? item?.id?.toString() : item?.toString();
                        const pId = p?.id?.toString();
                        return itemId === pId;
                      })?.title ?? ""
                    );
                  }}
                  isOptionEqualToValue={(option, value) => {
                    const optionId = option?.id?.toString();
                    const valueId =
                      typeof value === "object" ? value?.id?.toString() : value?.toString();
                    return value === undefined || optionId === valueId;
                  }}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Category"
                      margin="normal"
                      variant="outlined"
                      error={!!(errors as any)?.category?.id}
                      helperText={(errors as any)?.category?.id?.message}
                      required
                    />
                  )}
                />
              )}
            />
            <Controller
              name="status"
              control={control}
              render={({ field }) => {
                return (
                  <Select {...field} value={field?.value || "draft"} label="Status">
                    <MenuItem value="draft">Draft</MenuItem>
                    <MenuItem value="published">Published</MenuItem>
                    <MenuItem value="rejected">Rejected</MenuItem>
                  </Select>
                );
              }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={modal.close}>Cancel</Button>
          <Button type="submit" form="create-blog-post-form" variant="contained" disabled={formLoading}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </List>
  );
}
