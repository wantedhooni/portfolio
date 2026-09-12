"use client";

import { DataGrid } from "@mui/x-data-grid";
import { DeleteButton, EditButton, List, ShowButton, useDataGrid } from "@refinedev/mui";
import React from "react";

import { DomainListToolbar } from "@components/domain-list-toolbar";
import { RESOURCE, RESOURCE_META, createListColumns } from "./constants";

export default function DummyListTemplatePage() {
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
        createVisible={false}
      />
      <DataGrid {...dataGridProps} columns={columns} autoHeight />
    </List>
  );
}
