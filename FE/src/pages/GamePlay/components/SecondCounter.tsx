import { useState, useEffect, useRef } from "react";

interface ISecondsProps {
  initialSeconds: number;
  maxSeconds: number | string;
}

function SecondCounter({ initialSeconds, maxSeconds }: ISecondsProps) {
  const [seconds, setSeconds] = useState(initialSeconds);
  const circleRef = useRef<SVGCircleElement | null>(null);

  useEffect(() => {
    const maxVal = Number(maxSeconds);

    // maxSeconds가 0 이하면 타이머 동작을 멈춘다.
    if (maxVal <= 0) return;

    // 1초마다 seconds를 1씩 감소시키는 로직
    const intervalId = setInterval(() => {
      setSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(intervalId);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    // 언마운트 시 인터벌 정리
    return () => clearInterval(intervalId);
  }, [maxSeconds]);

  useEffect(() => {
    // circle 진행도 업데이트
    if (!circleRef.current) return;

    const circumference = 151; // circle에 설정한 strokeDasharray
    const maxVal = Number(maxSeconds);

    let offset = circumference;
    if (maxVal > 0) {
      // maxSeconds가 정상이라면 (진행률 = seconds / maxSeconds)
      const progress = seconds / maxVal;
      offset = circumference - progress * circumference;
    }
    // maxVal <= 0이면 offset = circumference(= 전체 길이) → 초록색 원이 전혀 표시되지 않음

    circleRef.current.style.strokeDashoffset = String(offset);
  }, [seconds, maxSeconds]);

  // maxSeconds가 0 이하이면 화면 표시를 0초로 고정
  const displaySeconds = Number(maxSeconds) <= 0 ? 0 : seconds;

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="relative">
        <svg className="-rotate-90 h-[4rem] w-[4rem]">
          <circle
            r="24"
            cx="32"
            cy="32"
            className="fill-transparent stroke-[black] stroke-[4px]"
          />
          <circle
            r="24"
            cx="32"
            cy="32"
            ref={circleRef}
            style={{
              strokeDasharray: "151px",
              strokeDashoffset: 0,
            }}
            className="fill-transparent stroke-[#44C553] stroke-[4px]"
          />
        </svg>
        <div className="text-white absolute text-lg font-semibold flex flex-col items-center w-[4rem] h-20 top-[16px]">
          <span>{displaySeconds}s</span>
        </div>
      </div>
    </div>
  );
}

export default SecondCounter;
