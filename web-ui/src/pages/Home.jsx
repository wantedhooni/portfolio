import React from 'react'

export default function Home() {
  return (
    <div className="intro-page">
      <h2>Demo 주식 거래</h2>
      <div className="intro-card">
          <h3>소개 </h3>
          <p className="intro-muted">Revy 포트폴리오용 계좌 / 주식 거래 사이트</p>
          <p className="intro-muted">
            github:{' '}
            <a href="https://github.com/wantedhooni/securities_monolithic" target="_blank" rel="noreferrer">
              https://github.com/wantedhooni/securities_monolithic
            </a>
          </p>
          <p className="intro-muted">계속 개발중</p>
          <p className="intro-muted">AWS 요금이 겁나서 멀 못해놓겠다.</p>          
      </div>
      <div className="intro-grid">
        <div className="intro-card">
          <h3>사용법 / 로그인 방법(데모계정 정보) </h3>
          <p className="intro-muted">
            email: user1~10@example.com<br/>
            password: Password!
          </p>
          <div className="intro-muted">
            login example:
            <br />
            user1@example.com : Password!
          </div>
        </div>
        <div className="intro-card">
          <h3>account</h3>
          <p className="intro-muted">
            계좌 생성, 입금 / 출금 등을 수행
          </p>
        </div>
        <div className="intro-card">
          <h3>trade</h3>
          <p className="intro-muted">
            주식 조회 및 체결 등을 수행 
          </p>
        </div>
      </div>
    </div>
  )
}
