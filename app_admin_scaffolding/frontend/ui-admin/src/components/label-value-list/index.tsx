"use client";

import { Stack, Typography } from "@mui/material";
import React from "react";

export type LabelValueItem = {
  label: string;
  value: React.ReactNode;
};

type LabelValueListProps = {
  items: LabelValueItem[];
};

export const LabelValueList = ({ items }: LabelValueListProps) => {
  return (
    <Stack gap={1.25}>
      {items.map((item) => (
        <Typography key={item.label} variant="body1">
          <Typography component="span" fontWeight={700}>
            {item.label}
            {": "}
          </Typography>
          <Typography component="span">
            {item.value ?? "-"}
          </Typography>
        </Typography>
      ))}
    </Stack>
  );
};

