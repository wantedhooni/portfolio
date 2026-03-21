from pathlib import Path
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, PageBreak, ListFlowable, ListItem
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

root = Path('/Users/revy/workspace_revy/securities_monolithic')
out_dir = root / 'output' / 'doc'
img_dir = root / 'output' / 'playwright'
pdf_path = out_dir / 'web-ui-portfolio.pdf'
font_path = '/System/Library/Fonts/Supplemental/AppleGothic.ttf'
font_name = 'AppleGothic'

pdfmetrics.registerFont(TTFont(font_name, font_path))

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='KTitle', fontName=font_name, fontSize=22, leading=28, textColor=colors.HexColor('#191f28'), spaceAfter=8))
styles.add(ParagraphStyle(name='KSubTitle', fontName=font_name, fontSize=12, leading=16, textColor=colors.HexColor('#6b7684'), spaceAfter=6))
styles.add(ParagraphStyle(name='KHeading1', fontName=font_name, fontSize=16, leading=20, textColor=colors.HexColor('#191f28'), spaceBefore=10, spaceAfter=8))
styles.add(ParagraphStyle(name='KHeading2', fontName=font_name, fontSize=12, leading=16, textColor=colors.HexColor('#191f28'), spaceBefore=8, spaceAfter=6))
styles.add(ParagraphStyle(name='KBody', fontName=font_name, fontSize=10.5, leading=15, textColor=colors.HexColor('#191f28')))

sections = [
    ('홈 화면', img_dir / 'portfolio-home-desktop.png'),
    ('시장 화면', img_dir / 'portfolio-chart-desktop.png'),
    ('계좌 화면', img_dir / 'portfolio-account-desktop.png'),
    ('환전 화면', img_dir / 'portfolio-exchange-desktop.png'),
    ('거래 화면', img_dir / 'portfolio-trade-desktop.png'),
    ('주문 화면', img_dir / 'portfolio-orders-desktop.png'),
    ('모바일 홈 화면', img_dir / 'portfolio-home-mobile.png'),
    ('모바일 거래 화면', img_dir / 'portfolio-trade-mobile.png'),
    ('모바일 주문 화면', img_dir / 'portfolio-orders-mobile.png'),
]

def bullets(items):
    return ListFlowable(
        [ListItem(Paragraph(item, styles['KBody'])) for item in items],
        bulletType='bullet',
        start='circle',
        leftIndent=14,
    )

def page_num(canvas, doc):
    canvas.setFont(font_name, 9)
    canvas.setFillColor(colors.HexColor('#6b7684'))
    canvas.drawCentredString(A4[0] / 2.0, 10 * mm, str(doc.page))

story = []
story.append(Spacer(1, 10 * mm))
story.append(Paragraph('Web UI 개선 포트폴리오', styles['KTitle']))
story.append(Paragraph('securities_monolithic / web-ui', styles['KSubTitle']))
story.append(Paragraph('고객용 금융 포털 화면을 실제 서비스형 UI로 재정리한 작업 정리 문서', styles['KBody']))
story.append(Spacer(1, 8 * mm))

story.append(Paragraph('1. 프로젝트 개요', styles['KHeading1']))
story.append(bullets([
    '프로젝트명: securities_monolithic 고객용 프론트엔드 web-ui',
    '프로젝트 성격: 금융 서비스형 고객 포털 UI/UX 개선',
    '목표: 데모 느낌의 화면을 실제 서비스형 UI로 정리하고, PC와 모바일에서 모두 안정적으로 동작하도록 개선',
]))
story.append(Spacer(1, 4 * mm))

story.append(Paragraph('2. 담당한 작업', styles['KHeading1']))
story.append(bullets([
    '공통 헤더와 홈 화면을 단순한 고객 포털 구조로 재구성',
    '시장, 계좌, 환전, 거래, 주문 화면의 정보 구조와 문구를 사용자 중심으로 재정리',
    '모바일 주문 화면을 카드형 목록으로 전환해 가로 깨짐 문제 완화',
    '차트, 거래, 주문 흐름의 상태 표시와 오류 메시지를 읽기 쉽게 정리',
    '전역 입력창, 버튼, 셀렉트박스 스타일을 통일해 일관성 개선',
]))
story.append(Spacer(1, 4 * mm))

story.append(Paragraph('3. 핵심 개선 포인트', styles['KHeading1']))
for title, items in [
    ('서비스형 톤으로 재정리', [
        '과한 소개성 문구와 장식 요소를 줄이고 필요한 메뉴만 빠르게 이동하는 구조로 변경',
        '카드, 버튼, 라운드, 그림자 밀도를 줄여 더 단정한 금융 서비스 UI로 정리',
    ]),
    ('업무 화면 중심 UX 개선', [
        '시장: 종목 검색, 현재가, 차트, 주문 이동 흐름을 단순화',
        '계좌: 계좌 현황 확인 후 개설, 입금, 출금, 이체 작업으로 자연스럽게 연결',
        '환전: 송금이 아닌 환전 계산 흐름 중심으로 정보 구조 재정리',
        '거래 및 주문: 오류 메시지와 상태 표현을 사람이 읽기 쉬운 방식으로 정리',
    ]),
    ('반응형 구조 재설계', [
        'PC는 다열 정보 구조 유지',
        '태블릿은 2열 중심으로 재배치',
        '모바일은 1열과 전체 폭 버튼 중심으로 재배치',
        '주문 화면은 모바일에서 테이블 대신 카드형 목록으로 변경',
    ]),
]:
    story.append(Paragraph(title, styles['KHeading2']))
    story.append(bullets(items))
    story.append(Spacer(1, 3 * mm))

story.append(Paragraph('4. 사용 기술', styles['KHeading1']))
story.append(bullets(['React', 'Vite', 'CSS', 'Playwright 기반 화면 캡처']))
story.append(PageBreak())

story.append(Paragraph('5. 결과 화면', styles['KHeading1']))
max_width = 170 * mm
max_height = 230 * mm
for idx, (title, image_path) in enumerate(sections):
    story.append(Paragraph(title, styles['KHeading2']))
    if image_path.exists():
        img = Image(str(image_path))
        iw, ih = img.imageWidth, img.imageHeight
        scale = min(max_width / iw, max_height / ih)
        img.drawWidth = iw * scale
        img.drawHeight = ih * scale
        story.append(img)
    else:
        story.append(Paragraph(f'이미지 없음: {image_path.name}', styles['KBody']))
    if idx != len(sections) - 1:
        story.append(PageBreak())

story.append(PageBreak())
story.append(Paragraph('6. 포트폴리오 메시지', styles['KHeading1']))
story.append(bullets([
    '데모 스타일 화면을 실제 금융 서비스형 UI로 재정리할 수 있음',
    '화면 디자인뿐 아니라 정보 구조, 오류 메시지, 반응형, 업무 흐름까지 함께 개선 가능',
    '고객용 화면에서 PC와 모바일을 분리해 사용 맥락에 맞게 설계 가능',
]))

doc = SimpleDocTemplate(str(pdf_path), pagesize=A4, leftMargin=18*mm, rightMargin=18*mm, topMargin=16*mm, bottomMargin=18*mm)
doc.build(story, onFirstPage=page_num, onLaterPages=page_num)
print(pdf_path)
