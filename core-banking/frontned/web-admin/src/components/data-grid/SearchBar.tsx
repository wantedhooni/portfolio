'use client';

import { useRef } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Field, FieldLabel } from '@/components/ui/field';
import { SearchIcon } from 'lucide-react';

export interface SearchField<T> {
  key: keyof T & string;
  label: string;
  placeholder?: string;
  type?: 'text' | 'email';
}

interface SearchBarProps<T> {
  fields: SearchField<T>[];
  onSearch: (values: T) => void;
  initialSearch: T;
}

export default function SearchBar<T>({ fields, onSearch, initialSearch }: SearchBarProps<T>) {
  const formRef = useRef<HTMLFormElement>(null);

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const values = Object.fromEntries(
      fields.map((f) => [f.key, (formData.get(f.key) as string) ?? '']),
    ) as T;
    onSearch(values);
  }

  function handleReset() {
    formRef.current?.reset();
    onSearch(initialSearch);
  }

  return (
    <form
      ref={formRef}
      onSubmit={handleSubmit}
      className="flex flex-wrap items-end gap-x-4 gap-y-3 rounded-xl border bg-card px-5 py-4"
    >
      {fields.map((field) => (
        <Field key={field.key} className="min-w-[180px] w-auto">
          <FieldLabel htmlFor={field.key}>{field.label}</FieldLabel>
          <Input
            id={field.key}
            name={field.key}
            type={field.type ?? 'text'}
            defaultValue={initialSearch[field.key] as string}
            placeholder={field.placeholder ?? `${field.label} 입력`}
          />
        </Field>
      ))}

      <div className="flex gap-2 self-end">
        <Button type="submit">
          <SearchIcon data-icon="inline-start" />
          검색
        </Button>
        <Button type="button" variant="outline" onClick={handleReset}>
          초기화
        </Button>
      </div>
    </form>
  );
}
