'use client';
import {DEMO_USER, DEMO_PASSWORD}from '@/constant/constants';
import { useActionState } from 'react';
import { useRouter } from 'next/navigation';
import { loginAction } from './action';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Spinner } from '@/components/ui/spinner';
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field';

interface State { error: string }



export default function LoginForm() {
  const router = useRouter();
    console.log("demo info:", DEMO_USER, DEMO_PASSWORD);
  const [state, action, pending] = useActionState(
    async (_prev: State, formData: FormData): Promise<State> => {
      const email = (formData.get('email') as string) ?? '';
      const password = (formData.get('password') as string) ?? '';
      const error = await loginAction(email, password);
      if (!error) router.replace('/dashboard');
      return { error };
    },
    { error: '' },
  );

  return (
    <div className="w-full max-w-sm">
      <div className="mb-8 flex flex-col items-center">
        <div className="mb-3 flex size-10 items-center justify-center rounded-xl bg-primary">
          <svg width="18" height="18" viewBox="0 0 14 14" fill="none">
            <rect x="1" y="1" width="5" height="5" rx="1" fill="white" fillOpacity="0.9"/>
            <rect x="8" y="1" width="5" height="5" rx="1" fill="white" fillOpacity="0.6"/>
            <rect x="1" y="8" width="5" height="5" rx="1" fill="white" fillOpacity="0.6"/>
            <rect x="8" y="8" width="5" height="5" rx="1" fill="white" fillOpacity="0.9"/>
          </svg>
        </div>
        <h1 className="text-xl font-semibold">Admin Panel</h1>
        <p className="mt-1 text-sm text-muted-foreground">계정 정보를 입력해주세요</p>
      </div>

      <Card>
        <CardContent>
          <form action={action} className="flex flex-col gap-4">
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">이메일</FieldLabel>
                <Input id="email" name="email" type="email" placeholder="admin@example.com" defaultValue={DEMO_USER} />
              </Field>
              <Field>
                <FieldLabel htmlFor="password">비밀번호</FieldLabel>
                <Input id="password" name="password" type="password" placeholder="••••••••" defaultValue={DEMO_PASSWORD}/>
              </Field>
            </FieldGroup>
            {state.error && (
              <Alert variant="destructive">
                <AlertDescription>{state.error}</AlertDescription>
              </Alert>
            )}
            <Button type="submit" disabled={pending} className="w-full">
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '로그인 중...' : '로그인'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
