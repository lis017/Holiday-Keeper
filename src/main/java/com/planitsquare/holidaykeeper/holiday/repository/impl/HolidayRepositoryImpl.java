package com.planitsquare.holidaykeeper.holiday.repository.impl;

import com.planitsquare.holidaykeeper.holiday.QHoliday;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepositoryCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class HolidayRepositoryImpl implements HolidayRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Holiday> search(Integer year, String countryCode, Pageable pageable) {

        QHoliday h = QHoliday.holiday;

        // --- where 조건 분리 ---
        var yearCond = (year != null) ? h.year.eq(year) : null;
        var countryCond = (countryCode != null) ?
                h.countryCode.countryCode.eq(countryCode) : null;

        // --- content query ---
        var contentQuery = queryFactory
                .selectFrom(h)
                .where(yearCond, countryCond)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // --- 정렬 처리 ---
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            switch (order.getProperty()) {
                case "name" -> orders.add(order.isAscending() ? h.name.asc() : h.name.desc());
                case "year" -> orders.add(order.isAscending() ? h.year.asc() : h.year.desc());
                case "countryCode" ->
                        orders.add(order.isAscending()
                                ? h.countryCode.countryCode.asc()
                                : h.countryCode.countryCode.desc());
            }
        });
        if (!orders.isEmpty()) contentQuery.orderBy(orders.toArray(new OrderSpecifier[0]));

        List<Holiday> content = contentQuery.fetch();

        // --- count query ---
        Long total = queryFactory
                .select(h.count())
                .from(h)
                .where(yearCond, countryCond)
                .fetchOne();

        if (total == null) total = 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
