"use client";

import { useShow } from "@refinedev/core";
import { Show } from "@refinedev/mui";
import { LabelValueList } from "@components/label-value-list";
import { CODE_FIELD, CODE_LABEL, DESCRIPTION_FIELD, DESCRIPTION_LABEL, RESOURCE, RESOURCE_META } from "../../constants";

export default function PermissionShowPage() {
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
          { label: CODE_LABEL, value: record?.[CODE_FIELD] },
          { label: DESCRIPTION_LABEL, value: record?.[DESCRIPTION_FIELD] },
          { label: "Created At", value: record?.createdAt },
          { label: "Updated At", value: record?.updatedAt },
        ]}
      />
    </Show>
  );
}
