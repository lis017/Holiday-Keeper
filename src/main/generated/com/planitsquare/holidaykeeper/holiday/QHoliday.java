package com.planitsquare.holidaykeeper.holiday;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHoliday is a Querydsl query type for Holiday
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHoliday extends EntityPathBase<Holiday> {

    private static final long serialVersionUID = 850071950L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHoliday holiday = new QHoliday("holiday");

    public final ListPath<String, StringPath> counties = this.<String, StringPath>createList("counties", String.class, StringPath.class, PathInits.DIRECT2);

    public final com.planitsquare.holidaykeeper.country.QCountry countryCode;

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final BooleanPath fixed = createBoolean("fixed");

    public final BooleanPath global = createBoolean("global");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> launchYear = createNumber("launchYear", Integer.class);

    public final StringPath localName = createString("localName");

    public final StringPath name = createString("name");

    public final ListPath<String, StringPath> types = this.<String, StringPath>createList("types", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QHoliday(String variable) {
        this(Holiday.class, forVariable(variable), INITS);
    }

    public QHoliday(Path<? extends Holiday> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHoliday(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHoliday(PathMetadata metadata, PathInits inits) {
        this(Holiday.class, metadata, inits);
    }

    public QHoliday(Class<? extends Holiday> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.countryCode = inits.isInitialized("countryCode") ? new com.planitsquare.holidaykeeper.country.QCountry(forProperty("countryCode")) : null;
    }

}

