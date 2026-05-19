import { redirect } from 'next/navigation';
import { getServerTokens } from '@/lib/authStore-server';
import LoginForm from '@/features/auth/LoginForm';

export default async function LoginPage() {
  const { accessToken, refreshToken } = await getServerTokens();

  if (accessToken || refreshToken) {
    redirect('/dashboard');
  }

  return <LoginForm />;
}
