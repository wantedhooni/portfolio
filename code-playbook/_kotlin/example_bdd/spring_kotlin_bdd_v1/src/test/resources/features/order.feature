Feature: 주문 관리
  쇼핑몰 고객은 상품을 주문하고 관리할 수 있다.

  Background:
    Given 다음 상품이 등록되어 있다
      | name       | price | stock |
      | 노트북      | 1200000 | 10  |
      | 마우스      | 35000   | 50  |

  Scenario: 정상 주문 생성
    When 고객(1)이 다음 상품을 주문한다
      | productName | quantity |
      | 노트북       | 2        |
      | 마우스       | 1        |
    Then 주문 상태는 "PENDING" 이어야 한다
    And 주문 총액은 2435000 원 이어야 한다
    And 노트북 재고는 8 개 이어야 한다

  Scenario: 재고 부족 시 주문 실패
    When 고객(1)이 다음 상품을 주문한다
      | productName | quantity |
      | 노트북       | 99       |
    Then 주문은 실패하고 "재고가 부족합니다" 메시지가 반환되어야 한다

  Scenario: 주문 확정 및 취소 불가 검증
    Given 고객(1)이 노트북 1개를 주문했다
    When 주문을 확정한다
    Then 주문 상태는 "CONFIRMED" 이어야 한다
    When 주문을 다시 확정 시도한다
    Then "대기 중인 주문만 확정할 수 있습니다" 오류가 발생해야 한다

  Scenario: 주문 취소 시 재고 복구
    Given 고객(1)이 마우스 5개를 주문했다
    When 주문을 취소한다
    Then 주문 상태는 "CANCELLED" 이어야 한다
    And 마우스 재고는 50 개로 복구되어야 한다