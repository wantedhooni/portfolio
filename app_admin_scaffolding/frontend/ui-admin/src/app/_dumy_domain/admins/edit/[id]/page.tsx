"use client";

import {
  Box,
  Chip,
  FormControlLabel,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
  Switch,
  TextField,
} from "@mui/material";
import { useList } from "@refinedev/core";
import { Edit } from "@refinedev/mui";
import { useForm } from "@refinedev/react-hook-form";
import { Controller } from "react-hook-form";
import {
  EMAIL_FIELD,
  EMAIL_LABEL,
  PASSWORD_FIELD,
  PASSWORD_LABEL,
  RESOURCE,
  RESOURCE_META,
  ROLE_RESOURCE,
  ROLE_RESOURCE_META,
} from "../../constants";

export default function AdminEditPage() {
  const {
    saveButtonProps,
    refineCore: { formLoading },
    register,
    control,
    formState: { errors },
  } = useForm({
    refineCoreProps: {
      resource: RESOURCE,
      meta: RESOURCE_META,
    },
  });

  const { result: roleList } = useList({
    resource: ROLE_RESOURCE,
    meta: ROLE_RESOURCE_META,
    pagination: {
      currentPage: 1,
      pageSize: 100,
    },
  });

  const roleOptions = roleList?.data ?? [];

  return (
    <Edit isLoading={formLoading} saveButtonProps={saveButtonProps}>
      <Box component="form" sx={{ display: "flex", flexDirection: "column" }} autoComplete="off">
        <TextField
          {...register(EMAIL_FIELD)}
          error={!!(errors as any)?.[EMAIL_FIELD]}
          helperText={(errors as any)?.[EMAIL_FIELD]?.message}
          margin="normal"
          fullWidth
          label={EMAIL_LABEL}
          type="email"
        />

        <TextField
          {...register(PASSWORD_FIELD)}
          error={!!(errors as any)?.[PASSWORD_FIELD]}
          helperText={(errors as any)?.[PASSWORD_FIELD]?.message}
          margin="normal"
          fullWidth
          label={`${PASSWORD_LABEL} (optional)`}
          type="password"
        />

        <Controller
          control={control}
          name="status"
          render={({ field }) => (
            <Select {...field} value={field.value ?? "ACTIVE"} margin="dense" fullWidth>
              <MenuItem value="ACTIVE">ACTIVE</MenuItem>
              <MenuItem value="WITHDRAWN">WITHDRAWN</MenuItem>
            </Select>
          )}
        />

        <Controller
          control={control}
          name="enabled"
          render={({ field }) => (
            <FormControlLabel
              sx={{ mt: 1 }}
              control={<Switch checked={Boolean(field.value)} onChange={(_, checked) => field.onChange(checked)} />}
              label="Enabled"
            />
          )}
        />

        <Controller
          control={control}
          name="roleIds"
          render={({ field }) => (
            <>
              <InputLabel sx={{ mt: 1 }}>Roles</InputLabel>
              <Select
                {...field}
                multiple
                fullWidth
                value={Array.isArray(field.value) ? field.value : []}
                input={<OutlinedInput label="Roles" />}
                onChange={(event) => field.onChange(event.target.value as string[])}
                renderValue={(selected) => (
                  <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
                    {(selected as string[]).map((value) => {
                      const role = roleOptions.find((item: any) => item.id === value);
                      return (
                        <Chip
                          key={value}
                          label={role?.name ?? value}
                          size="small"
                        />
                      );
                    })}
                  </Box>
                )}
              >
                {roleOptions.map((role: any) => (
                  <MenuItem key={role.id} value={role.id}>
                    {role.name}
                  </MenuItem>
                ))}
              </Select>
            </>
          )}
        />
      </Box>
    </Edit>
  );
}
