package io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class Ticket {

    private final String wayPoints, timeStart, timeEnd, dateStart, dateEnd, /**tripTime,*/ transferAmount;

    private final int tripTime, price;
}