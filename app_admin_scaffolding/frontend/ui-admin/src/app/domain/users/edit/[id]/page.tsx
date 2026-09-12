"use client";

import { Box, TextField } from "@mui/material";
import type { BaseRecord, HttpError } from "@refinedev/core";
import { Edit } from "@refinedev/mui";
import { useForm } from "@refinedev/react-hook-form";
import {
  EMAIL_FIELD,
  EMAIL_LABEL,
  FIRST_NAME_FIELD,
  FIRST_NAME_LABEL,
  LAST_NAME_FIELD,
  LAST_NAME_LABEL,
  NICK_NAME_FIELD,
  NICK_NAME_LABEL,
  PASSWORD_FIELD,
  PASSWORD_LABEL,
  RESOURCE,
  RESOURCE_META,
} from "../../constants";

type UserUpdateFormValues = {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  nickName?: string;
};

export default function DummyEditTemplatePage() {
  const {
    saveButtonProps,
    refineCore: { formLoading },
    register,
    formState: { errors },
  } = useForm<BaseRecord, HttpError, UserUpdateFormValues>({
    refineCoreProps: {
      resource: RESOURCE,
      meta: RESOURCE_META,
    },
  });

  return (
    <Edit isLoading={formLoading} saveButtonProps={saveButtonProps}>
      <Box component="form" sx={{ display: "flex", flexDirection: "column" }} autoComplete="off">
        <TextField
          {...register(EMAIL_FIELD, { setValueAs: (value: string) => value?.trim() || undefined })}
          error={!!(errors as any)?.[EMAIL_FIELD]}
          helperText={(errors as any)?.[EMAIL_FIELD]?.message}
          margin="normal"
          fullWidth
          label={EMAIL_LABEL}
          type="email"
        />
        <TextField
          {...register(PASSWORD_FIELD, { setValueAs: (value: string) => value || undefined })}
          error={!!(errors as any)?.[PASSWORD_FIELD]}
          helperText={(errors as any)?.[PASSWORD_FIELD]?.message}
          margin="normal"
          fullWidth
          label={`${PASSWORD_LABEL} (optional)`}
          type="password"
        />
        <TextField
          {...register(FIRST_NAME_FIELD, { setValueAs: (value: string) => value?.trim() || undefined })}
          error={!!(errors as any)?.[FIRST_NAME_FIELD]}
          helperText={(errors as any)?.[FIRST_NAME_FIELD]?.message}
          margin="normal"
          fullWidth
          label={FIRST_NAME_LABEL}
        />
        <TextField
          {...register(LAST_NAME_FIELD, { setValueAs: (value: string) => value?.trim() || undefined })}
          error={!!(errors as any)?.[LAST_NAME_FIELD]}
          helperText={(errors as any)?.[LAST_NAME_FIELD]?.message}
          margin="normal"
          fullWidth
          label={LAST_NAME_LABEL}
        />
        <TextField
          {...register(NICK_NAME_FIELD, { setValueAs: (value: string) => value?.trim() || undefined })}
          error={!!(errors as any)?.[NICK_NAME_FIELD]}
          helperText={(errors as any)?.[NICK_NAME_FIELD]?.message}
          margin="normal"
          fullWidth
          label={NICK_NAME_LABEL}
        />
      </Box>
    </Edit>
  );
}
