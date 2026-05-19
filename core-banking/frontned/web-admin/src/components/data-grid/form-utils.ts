import type { CrudField } from '@/types/page-config';

export function buildEmptyForm(fields: CrudField[]): Record<string, string> {
  return Object.fromEntries(fields.map((f) => [f.key, '']));
}

export function validateFields(
  fields: CrudField[],
  form: Record<string, string>,
): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const field of fields) {
    const value = form[field.key] ?? '';
    if (field.required !== false && !value.trim()) {
      errors[field.key] = `${field.label}을(를) 입력해주세요.`;
      continue;
    }
    const msg = field.validate?.(value);
    if (msg) errors[field.key] = msg;
  }
  return errors;
}
