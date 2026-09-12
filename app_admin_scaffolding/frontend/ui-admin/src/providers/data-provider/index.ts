"use client";

import type {
  DataProvider,
  BaseRecord,
  CrudFilters,
  CrudSorting,
  GetListParams,
  GetOneParams,
  GetManyParams,
  CreateParams,
  UpdateParams,
  DeleteOneParams,
} from "@refinedev/core";

import { apiClient } from "@providers/http-client";

type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: {
    message?: string;
  };
};

type PageResponse<T> = {
  content: T[];
  totalElements: number;
};

type ResourceMeta = {
  apiPath?: string;
};

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const unwrap = <T>(payload: ApiResponse<T>): T => {
  if (!payload.success) {
    throw new Error(payload.error?.message ?? "Request failed");
  }

  return payload.data;
};

const extractApiPath = (meta: unknown): string | undefined => {
  if (!meta || typeof meta !== "object") {
    return undefined;
  }

  const apiPath = (meta as ResourceMeta).apiPath;
  if (typeof apiPath !== "string") {
    return undefined;
  }

  const trimmed = apiPath.trim();
  return trimmed.length > 0 ? trimmed : undefined;
};

const resolveResourcePath = (resource: string, meta: unknown): string => {
  return extractApiPath(meta) ?? `/api/${resource}`;
};

const toSortParams = (sorters?: CrudSorting) => {
  const firstSorter = sorters?.[0];

  if (!firstSorter) {
    return {
      sortBy: undefined,
      sortDirection: undefined,
    };
  }

  return {
    sortBy: firstSorter.field,
    sortDirection: firstSorter.order?.toUpperCase(),
  };
};

const toPageParam = (current?: number): number => {
  return Math.max(0, (current ?? 1) - 1);
};

const toPageSize = (pageSize?: number): number => {
  return pageSize ?? 20;
};

const toParamQuery = (filters?: CrudFilters): string | undefined => {
  if (!filters || filters.length === 0) {
    return undefined;
  }

  const tokens = filters
    .flatMap((filter) => {
      if (!("field" in filter) || typeof filter.field !== "string" || !("value" in filter)) {
        return [];
      }

      const value = filter.value;
      if (typeof value !== "string" && typeof value !== "number" && typeof value !== "boolean") {
        return [];
      }

      return [`${filter.field}:${String(value).trim()}`];
    })
    .filter((token) => !token.endsWith(":"));

  if (tokens.length === 0) {
    return undefined;
  }

  return tokens.join(",");
};

const getData = async <T>(url: string, params?: Record<string, unknown>): Promise<T> => {
  const response = await apiClient.get<ApiResponse<T>>(url, { params });

  return unwrap(response.data);
};

const postData = async <T>(url: string, body?: unknown): Promise<T> => {
  const response = await apiClient.post<ApiResponse<T>>(url, body);
  return unwrap(response.data);
};

export const dataProvider: DataProvider = {
  getApiUrl: () => API_BASE_URL,

  getList: async <TData extends BaseRecord = BaseRecord>(
    params: GetListParams,
  ) => {
    const { resource, pagination, sorters, filters, meta } = params;
    const path = resolveResourcePath(resource, meta);
    const normalizedPagination = pagination as
      | { current?: number; currentPage?: number; pageSize?: number }
      | undefined;
    const current = normalizedPagination?.current ?? normalizedPagination?.currentPage;
    const page = toPageParam(current);
    const size = toPageSize(normalizedPagination?.pageSize);
    const { sortBy, sortDirection } = toSortParams(sorters);
    const queryParams = {
      page,
      size,
      sortBy,
      sortDirection,
      paramQuery: toParamQuery(filters),
    };

    const response = await apiClient.get<ApiResponse<PageResponse<TData>>>(
      `${path}/search`,
      {
        params: queryParams,
      },
    );

    const pageData = unwrap(response.data);

    return {
      data: pageData.content,
      total: pageData.totalElements,
    };
  },

  getOne: async <TData extends BaseRecord = BaseRecord>(params: GetOneParams) => {
    const { resource, id, meta } = params;
    const path = resolveResourcePath(resource, meta);
    const data = await getData<TData>(`${path}/${id}`);

    return {
      data,
    };
  },

  getMany: async <TData extends BaseRecord = BaseRecord>(params: GetManyParams) => {
    const { resource, ids, meta } = params;
    const path = resolveResourcePath(resource, meta);

    const data = await Promise.all(ids.map((id) => getData<TData>(`${path}/${id}`)));

    return {
      data,
    };
  },

  create: async <TData extends BaseRecord = BaseRecord, TVariables = {}>(
    params: CreateParams<TVariables>,
  ) => {
    const { resource, variables, meta } = params;
    const path = resolveResourcePath(resource, meta);
    const data = await postData<TData>(`${path}/create`, variables);

    return {
      data,
    };
  },

  update: async <TData extends BaseRecord = BaseRecord, TVariables = {}>(
    params: UpdateParams<TVariables>,
  ) => {
    const { resource, id, variables, meta } = params;
    const path = resolveResourcePath(resource, meta);
    const data = await postData<TData>(`${path}/${id}/update`, variables);

    return {
      data,
    };
  },

  deleteOne: async <TData extends BaseRecord = BaseRecord, TVariables = {}>(
    params: DeleteOneParams<TVariables>,
  ) => {
    const { resource, id, meta } = params;
    const path = resolveResourcePath(resource, meta);
    await postData<void>(`${path}/${id}/delete`);

    return {
      data: {
        id,
      } as TData,
    };
  },

  custom: async ({ url, method, payload, query, headers }) => {
    const response = await apiClient.request({
      url,
      method,
      data: payload,
      params: query,
      headers,
    });

    return {
      data: response.data,
    };
  },
};
