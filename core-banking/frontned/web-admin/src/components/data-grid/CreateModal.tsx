'use client';

import { useActionState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Spinner } from '@/components/ui/spinner';
import FormRenderer from './FormRenderer';
import { validateFields } from './form-utils';
import { createItem, getApiError } from '@/services/crud';
import type { CrudField } from '@/types/page-config';

interface Props {
  open: boolean;
  title: string;
  fields: CrudField[];
  endpoint: string;
  onClose: () => void;
  onSuccess: () => void;
}

interface State { errors: Record<string, string>; serverError: string }
const INIT: State = { errors: {}, serverError: '' };

function CreateForm({ title, fields, endpoint, onClose, onSuccess }: Omit<Props, 'open'>) {
  const [state, action, pending] = useActionState(
    async (_prev: State, formData: FormData): Promise<State> => {
      const form = Object.fromEntries(
        fields.map((f) => [f.key, (formData.get(f.key) as string) ?? '']),
      );
      const errors = validateFields(fields, form);
      if (Object.keys(errors).length) return { errors, serverError: '' };
      try {
        await createItem(endpoint, form);
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '등록에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <form action={action} className="contents" noValidate>
      <DialogHeader>
        <DialogTitle>{title} 등록</DialogTitle>
      </DialogHeader>
      <FormRenderer fields={fields} errors={state.errors} />
      {state.serverError && (
        <Alert variant="destructive">
          <AlertDescription>{state.serverError}</AlertDescription>
        </Alert>
      )}
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>취소</Button>
        <Button type="submit" disabled={pending}>
          {pending && <Spinner data-icon="inline-start" />}
          {pending ? '등록 중...' : '등록'}
        </Button>
      </DialogFooter>
    </form>
  );
}

export default function CreateModal({ open, ...props }: Props) {
  return (
    <Dialog open={open} onOpenChange={(o) => !o && props.onClose()}>
      <DialogContent showCloseButton={false}>
        <CreateForm {...props} />
      </DialogContent>
    </Dialog>
  );
}
