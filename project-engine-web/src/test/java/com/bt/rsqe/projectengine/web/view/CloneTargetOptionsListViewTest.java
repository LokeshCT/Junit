package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.QuoteOptionDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class CloneTargetOptionsListViewTest {

    private CloneTargetOptionsListView view;
    private static final QuoteOptionDTO MOST_RECENT_QUOTE_OPTION = aQuoteOptionDTO().withId("1").withCreationDate("2012-03-15T15:34:53.669Z").build();
    private static final QuoteOptionDTO OLDER_QUOTE_OPTION = aQuoteOptionDTO().withId("2").withCreationDate("2011-03-15T15:34:53.669Z").build();

    @Before
    public void setUp() {
        view = new CloneTargetOptionsListView(new ArrayList<QuoteOptionDTO>() {{
            add(OLDER_QUOTE_OPTION);
            add(MOST_RECENT_QUOTE_OPTION);
        }});
    }

    @Test
    public void shouldReturnListOfQuoteOptionDTOsInCreatedDateDescendingOrder() throws Exception {
        List<QuoteOptionDTO> quoteOptionDTOs = view.getQuoteOptionDTOs();
        assertThat(quoteOptionDTOs.get(0), is(MOST_RECENT_QUOTE_OPTION));
        assertThat(quoteOptionDTOs.get(1), is(OLDER_QUOTE_OPTION));
    }

}
