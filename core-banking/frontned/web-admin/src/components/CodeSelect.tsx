'use client';

import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { useCodeOptions } from '@/hooks/useCode';

interface CodeSelectProps {
    /** enum 클래스명 — 예: `JobType`, `ScheduleType`, `QuartzJobExecutionStatus` */
    codeKey: string;
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
}

/**
 * 백엔드 `ExposedEnum`을 자동으로 셀렉트 옵션으로 노출하는 범용 컴포넌트.
 *
 * <p>백엔드에서 enum 추가/수정해도 프론트 수정 없이 자동 반영됩니다.
 * ({@link CodeProvider}가 5분 폴링으로 인메모리 캐시 갱신)
 *
 * @example
 * <CodeSelect codeKey="JobType" value={form.jobType} onChange={setJobType} />
 */
export function CodeSelect({
    codeKey,
    value,
    onChange,
    placeholder = '선택하세요',
    disabled,
    className,
}: CodeSelectProps) {
    const options = useCodeOptions(codeKey);

    return (
        <Select value={value ?? ''} onValueChange={onChange} disabled={disabled}>
            <SelectTrigger className={className}>
                <SelectValue placeholder={placeholder} />
            </SelectTrigger>
            <SelectContent>
                {options.map((o) => (
                    <SelectItem key={o.code} value={o.code}>
                        {o.label}
                    </SelectItem>
                ))}
            </SelectContent>
        </Select>
    );
}
