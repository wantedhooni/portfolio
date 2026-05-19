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
import { updateItem, deleteItem, getApiError } from '@/services/crud';
import type { CrudField, EditMode } from '@/types/page-config';

interface Props {
  open: boolean;
  mode: EditMode;
  title: string;
  fields: CrudField[];
  endpoint: string;
  itemId?: number | string | null;
  initialData?: Record<string, string>;
  onClose: () => void;
  onSuccess: () => void;
}

interface EditState { errors: Record<string, string>; serverError: string }

function EditForm({ title, fields, endpoint, itemId, initialData = {}, onClose, onSuccess }: Omit<Props, 'open' | 'mode'>) {
  const [state, action, pending] = useActionState(
    async (_prev: EditState, formData: FormData): Promise<EditState> => {
      const form = Object.fromEntries(
        fields.map((f) => [f.key, (formData.get(f.key) as string) ?? '']),
      );
      const errors = validateFields(fields, form);
      if (Object.keys(errors).length) return { errors, serverError: '' };
      try {
        await updateItem(endpoint, itemId!, form);
        onSuccess();
        onClose();
        return { errors: {}, serverError: '' };
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '수정에 실패했습니다.') };
      }
    },
    { errors: {}, serverError: '' },
  );

  return (
    <form key={String(itemId)} action={action} className="contents" noValidate>
      <DialogHeader>
        <DialogTitle>{title} 수정</DialogTitle>
      </DialogHeader>
      <FormRenderer fields={fields} defaultValues={initialData} errors={state.errors} />
      {state.serverError && (
        <Alert variant="destructive">
          <AlertDescription>{state.serverError}</AlertDescription>
        </Alert>
      )}
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>취소</Button>
        <Button type="submit" disabled={pending}>
          {pending && <Spinner data-icon="inline-start" />}
          {pending ? '수정 중...' : '수정'}
        </Button>
      </DialogFooter>
    </form>
  );
}

interface DeleteState { serverError: string }

function DeleteConfirm({ title, endpoint, itemId, onClose, onSuccess }: Pick<Props, 'title' | 'endpoint' | 'itemId' | 'onClose' | 'onSuccess'>) {
  const [state, action, pending] = useActionState(
    async (_prev: DeleteState, _formData: FormData): Promise<DeleteState> => {
      try {
        await deleteItem(endpoint, itemId!);
        onSuccess();
        onClose();
        return { serverError: '' };
      } catch (err) {
        return { serverError: getApiError(err, '삭제에 실패했습니다.') };
      }
    },
    { serverError: '' },
  );

  return (
    <form action={action} className="contents">
      <DialogHeader>
        <DialogTitle>{title} 삭제</DialogTitle>
      </DialogHeader>
      <div className="flex flex-col gap-3">
        <p className="text-sm text-muted-foreground">삭제 후에는 되돌릴 수 없습니다. 계속하시겠습니까?</p>
        {state.serverError && (
          <Alert variant="destructive">
            <AlertDescription>{state.serverError}</AlertDescription>
          </Alert>
        )}
      </div>
      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>취소</Button>
        <Button type="submit" variant="destructive" disabled={pending}>
          {pending && <Spinner data-icon="inline-start" />}
          {pending ? '삭제 중...' : '삭제'}
        </Button>
      </DialogFooter>
    </form>
  );
}

export default function EditModal({ open, mode, ...props }: Props) {
  return (
    <Dialog open={open} onOpenChange={(o) => !o && props.onClose()}>
      <DialogContent showCloseButton={false}>
        {mode === 'delete' && <DeleteConfirm {...props} />}
        {mode === 'edit' && <EditForm {...props} />}
      </DialogContent>
    </Dialog>
  );
}
