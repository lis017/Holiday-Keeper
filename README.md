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
reSync 중복 호출 방지 (Debounce)
toDto mapper로 refactor
---------------------------------------
#빌드 & 실행 방법 <br>
1. 클론 후 ./gradlew bootRun
2. http://localhost:8080/swagger-ui/index.html 접속후 get(조회) delete(삭제)선택후
   year, countryCode, size, page등 입력하여 기능





#설계한 REST API 명세 요약** (엔드포인트·파라미터·응답 예시)
📌 1. 공휴일 조회 API
GET /v1/holiday
✔ 기능

특정 **연도(year)**와 국가 코드(countryCode) 기준으로 공휴일 목록을 조회합니다.
QueryDSL 기반 동적 검색을 적용했고, 페이징·정렬을 지원합니다.

✔ 요청 파라미터
🔸 Query Params
이름	타입	필수	설명
year	Integer	N	조회할 연도(예: 2024)
countryCode	String	N	ISO 국가 코드 (예: KR, US)
page	Integer	N	페이지 번호
size	Integer	N	페이지 크기
sort	String	N	정렬 기준(name, year, countryCode 가능)
✔ 응답 예시 (200 OK)
{
  "content": [
    {
      "date": "2024-01-01",
      "localName": "새해",
      "name": "New Year's Day",
      "countryCode": "KR",
      "fixed": true,
      "global": true,
      "counties": null,
      "launchYear": 1949,
      "types": ["Public"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1
}

✔ 동작 방식 요약

year, countryCode가 null이면 조건에서 제외 (QueryDSL dynamic search)

인덱스: holiday_year, country_id ⇢ 연도 + 국가 조건 검색 최적화

정렬 가능 필드: name, year, countryCode

DTO 매핑 후 Page 형태로 반환

📌 2. 공휴일 삭제 API
DELETE /v1/holiday
✔ 기능

연도 + 국가 코드를 만족하는 Holiday 레코드를 일괄 삭제합니다.

✔ 요청 파라미터
이름	타입	필수	설명
year	Integer	Y	삭제할 연도
countryCode	String	Y	삭제할 국가 코드
✔ 응답

204 No Content

✔ 동작 방식

year + countryCode 조건으로 Holiday ID 리스트 조회

조회된 ID 목록 기반으로 batch delete 수행

내부 구현: QueryDSL delete(h).where(h.id.in(ids))

📌 3. 데이터 모델 구조 요약
✔ Country
@Id Long id  
String countryCode   // ISO 국가 코드  
String name

✔ Holiday

핵심 검색 필드

holiday_year (int) → 인덱스

country_id → Country FK

@Table(indexes = {
    @Index(name="idx_year_country", columnList="holiday_year,country_id")
})


즉, “연도 + 국가코드(countryCode)” 조회가 최적화됨
자주 쓰는 업무 패턴(특정 연도 + 특정 국가의 공휴일 조회)를 정확히 커버.

📌 4. Repository 동작 요약
✔ search()

QueryDSL 기반 동적 검색

pageable + sort 적용

total count 별도 쿼리

✔ findByYearAndCountryCode()

외부 API 데이터 upsert 로직에서 활용 가능

✔ deleteByYearAndCountryCode()

N+1 없이 ID 기반 batch delete 수행





#Swagger UI 또는 OpenAPI JSON 노출 확인 방법
1. Swagger UI 확인
서버 실행 후 아래 URL로 접속하세요:
http://localhost:8080/swagger-ui/index.html

2. OpenAPI JSON 확인
Swagger 없이 문서 JSON만 보고 싶다면:
http://localhost:8080/v3/api-docs

3. OpenAPI YAML 확인
YAML 형태 문서:
http://localhost:8080/v3/api-docs.yaml
