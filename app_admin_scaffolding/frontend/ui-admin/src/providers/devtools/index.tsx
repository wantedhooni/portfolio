"use client";

import {
  DevtoolsPanel,
  DevtoolsProvider as DevtoolsProviderBase,
} from "@refinedev/devtools";
import React from "react";

export const DevtoolsProvider = (props: React.PropsWithChildren) => {
  const isDevtoolsEnabled = process.env.NEXT_PUBLIC_ENABLE_DEVTOOLS !== "false";
  const isDevtoolsDisabled = process.env.NEXT_PUBLIC_DISABLE_DEVTOOLS === "true";

  if (!isDevtoolsEnabled || isDevtoolsDisabled) {
    return <>{props.children}</>;
  }

  return (
    <DevtoolsProviderBase>
      {props.children}
      <DevtoolsPanel />
    </DevtoolsProviderBase>
  );
};
