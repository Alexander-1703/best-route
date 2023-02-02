package io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class Ticket {

    private final String wayPoints, timeStart, timeEnd, dateStart, dateEnd, /**tripTime,*/ transferAmount, price;
    // private final int tripTime, price;      время пересадки - на точечке  сделать селект городов
    private final int tripTime;
    @Setter
    @Getter
    private static String url;
}