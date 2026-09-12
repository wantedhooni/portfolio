"use client";

import { Box, TextField } from "@mui/material";
import type { BaseRecord, HttpError } from "@refinedev/core";
import { Create } from "@refinedev/mui";
import { useForm } from "@refinedev/react-hook-form";
import { EMAIL_FIELD, EMAIL_LABEL, PASSWORD_FIELD, PASSWORD_LABEL, RESOURCE, RESOURCE_META } from "../constants";

type AdminCreateFormValues = {
  email: string;
  password: string;
};

export default function AdminCreatePage() {
  const {
    saveButtonProps,
    refineCore: { formLoading },
    register,
    formState: { errors },
  } = useForm<BaseRecord, HttpError, AdminCreateFormValues>({
    refineCoreProps: {
      resource: RESOURCE,
      meta: RESOURCE_META,
    },
  });

  return (
    <Create isLoading={formLoading} saveButtonProps={saveButtonProps}>
      <Box component="form" sx={{ display: "flex", flexDirection: "column" }} autoComplete="off">
        <TextField
          {...register(EMAIL_FIELD, {
            required: "Email is required",
            setValueAs: (value: string) => value.trim(),
          })}
          error={!!(errors as any)?.[EMAIL_FIELD]}
          helperText={(errors as any)?.[EMAIL_FIELD]?.message}
          margin="normal"
          fullWidth
          label={EMAIL_LABEL}
          type="email"
        />
        <TextField
          {...register(PASSWORD_FIELD, { required: "Password is required" })}
          error={!!(errors as any)?.[PASSWORD_FIELD]}
          helperText={(errors as any)?.[PASSWORD_FIELD]?.message}
          margin="normal"
          fullWidth
          label={PASSWORD_LABEL}
          type="password"
        />
      </Box>
    </Create>
  );
}
