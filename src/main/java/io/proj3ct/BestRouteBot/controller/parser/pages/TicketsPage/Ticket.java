package io.proj3ct.BestRouteBot.controller.parser.pages.TicketsPage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class Ticket {

    @Setter
    @Getter
    private static String url;
    private final String wayPoints, timeStart, timeEnd, dateStart, dateEnd, tripTime, transferAmount, price;
}
