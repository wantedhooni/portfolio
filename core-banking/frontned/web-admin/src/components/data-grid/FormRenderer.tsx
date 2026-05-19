'use client';

import { Input } from '@/components/ui/input';
import { Field, FieldGroup, FieldLabel, FieldError } from '@/components/ui/field';
import type { CrudField } from '@/types/page-config';

interface FormRendererProps {
  fields: CrudField[];
  defaultValues?: Record<string, string>;
  errors?: Record<string, string>;
}

export default function FormRenderer({ fields, defaultValues = {}, errors = {} }: FormRendererProps) {
  return (
    <FieldGroup>
      {fields.map((field) => (
        <Field key={field.key} data-invalid={!!errors[field.key] || undefined}>
          <FieldLabel htmlFor={field.key}>
            {field.label}
            {field.required !== false && <span className="text-destructive ml-0.5">*</span>}
          </FieldLabel>
          <Input
            id={field.key}
            name={field.key}
            type={field.type ?? 'text'}
            defaultValue={defaultValues[field.key] ?? ''}
            placeholder={field.placeholder}
            aria-invalid={!!errors[field.key] || undefined}
          />
          {errors[field.key] && <FieldError>{errors[field.key]}</FieldError>}
        </Field>
      ))}
    </FieldGroup>
  );
}
