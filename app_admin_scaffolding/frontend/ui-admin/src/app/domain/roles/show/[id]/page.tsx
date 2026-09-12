"use client";

import { useShow } from "@refinedev/core";
import { Show } from "@refinedev/mui";
import { LabelValueList } from "@components/label-value-list";
import { DESCRIPTION_FIELD, DESCRIPTION_LABEL, NAME_FIELD, NAME_LABEL, RESOURCE, RESOURCE_META } from "../../constants";

export default function RoleShowPage() {
  const { query } = useShow({
    resource: RESOURCE,
    meta: RESOURCE_META,
  });

  const { data, isLoading } = query;
  const record = data?.data;

  return (
    <Show isLoading={isLoading}>
      <LabelValueList
        items={[
          { label: "ID", value: record?.id },
          { label: NAME_LABEL, value: record?.[NAME_FIELD] },
          { label: DESCRIPTION_LABEL, value: record?.[DESCRIPTION_FIELD] },
          {
            label: "Admins",
            value: Array.isArray(record?.admins) ? record.admins.map((admin: any) => admin.email).join(", ") : "-",
          },
          { label: "Created At", value: record?.createdAt },
          { label: "Updated At", value: record?.updatedAt },
        ]}
      />
    </Show>
  );
}
