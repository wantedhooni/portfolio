"use client";

import { Stack, Typography } from "@mui/material";
import { useShow } from "@refinedev/core";
import { Show, TextFieldComponent as TextField } from "@refinedev/mui";
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
      <Stack gap={1}>
        <Typography variant="body1" fontWeight="bold">ID</Typography>
        <TextField value={record?.id} />

        <Typography variant="body1" fontWeight="bold">{EMAIL_LABEL}</Typography>
        <TextField value={record?.[EMAIL_FIELD]} />

        <Typography variant="body1" fontWeight="bold">Status</Typography>
        <TextField value={record?.status} />

        <Typography variant="body1" fontWeight="bold">Enabled</Typography>
        <TextField value={String(record?.enabled)} />

        <Typography variant="body1" fontWeight="bold">Roles</Typography>
        <TextField value={Array.isArray(record?.roles) ? record.roles.join(", ") : "-"} />
      </Stack>
    </Show>
  );
}
