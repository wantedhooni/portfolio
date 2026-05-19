import { login } from './service';

/** 로그인 폼 action — 빈 문자열이면 성공, 아니면 에러 메시지 */
export async function loginAction(email: string, password: string): Promise<string> {
  if (!email) return '이메일을 입력해주세요.';
  if (!password) return '비밀번호를 입력해주세요.';
  try {
    await login(email, password);
    return '';
  } catch {
    return '이메일 또는 비밀번호가 올바르지 않습니다.';
  }
}
