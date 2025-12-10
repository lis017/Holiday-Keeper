(개발기간 6일)

본 프로젝트는 개발 이전에 도메인 모델링, API 흐름, 외부 API 연동 구조를 먼저 설계한 뒤 구현을 진행했습니다.
설계 중심 개발을 통해 코드 변경에도 구조가 깨지지 않도록 하는 것을 목표로 했습니다.
<br><br>
1.프로젝트 개요
Holiday Keeper는 Nager.Date 외부 API를 활용하여 최근 5년(2020~2025)의 전 세계 공휴일을 수집·저장·조회·관리하는 Mini Service입니다.
두 개의 외부 API만으로 국가 목록 조회 + 연도별 공휴일 데이터 적재 기능을 제공합니다.
<br><br>
2. 설계 의도
- 외부 API 의존도를 최소화하고, 초기 적재 후 내부 DB에서 빠르게 조회할 수 있도록 설계했습니다.
- 국가 코드/연도/날짜 등에 대한 조회가 많음을 고려하여 인덱스 설계를 최적화했습니다.
- API 호출 로직을 Client 레이어로 분리하여 테스트 가능성과 확장성을 확보했습니다.
- Holiday 데이터는 변경이 거의 없으므로 Read-Optimized 구조로 설계했습니다.
<br><br>
3. 전체 구조 (Layered Architecture)
<br> com.planitsquare.holidaykeeper
<br>  ├── api        - 외부 Nager API 호출(Client, DTO, Config)
 <br>├── country    - Country 도메인(Entity/Repo/Service/Controller)
<br> ├── holiday    - Holiday 도메인(Entity/Repo/Service/Controller)
 <br>├── common     - 공통 예외, 유틸, 공용 설정
<br> └── scheduler  - 데이터 Refresh 스케줄러(Optional)
<br><br>
4. 핵심 기능 요약
- AvailableCountries API → 국가 목록 DB 저장
- PublicHolidays API → 2020~2025 연도별 공휴일 저장
- 공휴일 검색 (국가, 연도, 페이징 포함)
- 데이터 Refresh (재적재), 삭제
- OpenAPI 문서 제공
<br><br>
5. 데이터 적재 로직 개요
1) 국가 목록 호출 → Country 테이블 저장
2) 각 국가별 2020~2025 공휴일 반복 수집 → Holiday 테이블 저장
3) 인덱스를 활용해 빠른 조회 보장
<br><br>
6. 테이블 설계 의도
Country(id, code, name)
Holiday(id, countryCode, date, localName, name, year, global)

- 연도(year) 컬럼 분리: 조회 속도 향상
- (countryCode, year) 복합 인덱스: 국가+연도 기반 조회 최적화

---------------------------------------
## 개선(Refactoring) 아이디어 <br>
toDto mapper로 refactor
