"use client";

import { useShow } from "@refinedev/core";
import { Show } from "@refinedev/mui";
import { LabelValueList } from "@components/label-value-list";
import {
  EMAIL_FIELD,
  EMAIL_LABEL,
  ENABLED_FIELD,
  ENABLED_LABEL,
  FAIL_COUNT_FIELD,
  FAIL_COUNT_LABEL,
  FIRST_NAME_FIELD,
  FIRST_NAME_LABEL,
  LAST_NAME_FIELD,
  LAST_NAME_LABEL,
  NICK_NAME_FIELD,
  NICK_NAME_LABEL,
  PERMISSIONS_FIELD,
  PERMISSIONS_LABEL,
  RESOURCE,
  RESOURCE_META,
  STATUS_FIELD,
  STATUS_LABEL,
} from "../../constants";

export default function DummyShowTemplatePage() {
  const { query } = useShow({
    resource: RESOURCE,
    meta: RESOURCE_META,
  });

  const { data, isLoading } = query;
  const record = data?.data as Record<string, any> | undefined;

  return (
    <Show isLoading={isLoading}>
      <LabelValueList
        items={[
          { label: "ID", value: record?.id },
          { label: EMAIL_LABEL, value: record?.[EMAIL_FIELD] },
          { label: FIRST_NAME_LABEL, value: record?.[FIRST_NAME_FIELD] },
          { label: LAST_NAME_LABEL, value: record?.[LAST_NAME_FIELD] },
          { label: NICK_NAME_LABEL, value: record?.[NICK_NAME_FIELD] },
          { label: STATUS_LABEL, value: record?.[STATUS_FIELD] },
          {
            label: PERMISSIONS_LABEL,
            value: Array.isArray(record?.[PERMISSIONS_FIELD]) ? record[PERMISSIONS_FIELD].join(", ") : "-",
          },
          { label: FAIL_COUNT_LABEL, value: record?.[FAIL_COUNT_FIELD] },
          {
            label: ENABLED_LABEL,
            value: typeof record?.[ENABLED_FIELD] === "boolean" ? String(record[ENABLED_FIELD]) : "-",
          },
        ]}
      />
    </Show>
  );
}
