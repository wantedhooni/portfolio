"use client";

import { Box, TextField } from "@mui/material";
import type { BaseRecord, HttpError } from "@refinedev/core";
import { Create } from "@refinedev/mui";
import { useForm } from "@refinedev/react-hook-form";
import { CODE_FIELD, CODE_LABEL, DESCRIPTION_FIELD, DESCRIPTION_LABEL, RESOURCE, RESOURCE_META } from "../constants";

type PermissionCreateFormValues = {
  code: string;
  description?: string;
};

export default function PermissionCreatePage() {
  const {
    saveButtonProps,
    refineCore: { formLoading },
    register,
    formState: { errors },
  } = useForm<BaseRecord, HttpError, PermissionCreateFormValues>({
    refineCoreProps: {
      resource: RESOURCE,
      meta: RESOURCE_META,
    },
  });

  return (
    <Create isLoading={formLoading} saveButtonProps={saveButtonProps}>
      <Box component="form" sx={{ display: "flex", flexDirection: "column" }} autoComplete="off">
        <TextField
          {...register(CODE_FIELD, {
            required: "Code is required",
            setValueAs: (value: string) => value.trim(),
          })}
          error={!!(errors as any)?.[CODE_FIELD]}
          helperText={(errors as any)?.[CODE_FIELD]?.message}
          margin="normal"
          fullWidth
          label={CODE_LABEL}
        />
        <TextField
          {...register(DESCRIPTION_FIELD, {
            setValueAs: (value: string) => value?.trim() || undefined,
          })}
          error={!!(errors as any)?.[DESCRIPTION_FIELD]}
          helperText={(errors as any)?.[DESCRIPTION_FIELD]?.message}
          margin="normal"
          fullWidth
          multiline
          minRows={3}
          label={DESCRIPTION_LABEL}
        />
      </Box>
    </Create>
  );
}
