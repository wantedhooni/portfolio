"use client";

import { useShow } from "@refinedev/core";
import { Show } from "@refinedev/mui";
import { LabelValueList } from "@components/label-value-list";
import { EMAIL_FIELD, EMAIL_LABEL, RESOURCE, RESOURCE_META } from "../../constants";

export default function AdminShowPage() {
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
          { label: EMAIL_LABEL, value: record?.[EMAIL_FIELD] },
          { label: "Status", value: record?.status },
          { label: "Enabled", value: typeof record?.enabled === "boolean" ? String(record.enabled) : "-" },
          { label: "Role IDs", value: Array.isArray(record?.roleIds) ? record.roleIds.join(", ") : "-" },
          { label: "Roles", value: Array.isArray(record?.roles) ? record.roles.join(", ") : "-" },
          { label: "Created At", value: record?.createdAt },
          { label: "Updated At", value: record?.updatedAt },
        ]}
      />
    </Show>
  );
}
