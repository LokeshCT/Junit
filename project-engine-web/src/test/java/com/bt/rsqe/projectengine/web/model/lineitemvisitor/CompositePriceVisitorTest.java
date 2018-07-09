package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class CompositePriceVisitorTest {

    private Mockery context = new RSQEMockery();
    private CompositeLineItemVisitor compositePriceVisitor;
    private PriceVisitor priceVisitor;

    @Before
    public void before() {
        priceVisitor = context.mock(PriceVisitor.class);
        compositePriceVisitor = new CompositeLineItemVisitor(priceVisitor, priceVisitor);
    }

    @Test
    public void shouldCallAllVisitorsForEachPriceModelAccept() throws Exception {
        final PriceModel priceModel = context.mock(PriceModel.class);
        context.checking(new Expectations() {{
            exactly(2).of(priceVisitor).visit(priceModel);
        }});
        compositePriceVisitor.visit(priceModel);
    }

    @Test
    public void shouldCallAllVisitorsForEachLineItemModelAccept() throws Exception {
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            exactly(2).of(priceVisitor).visit(lineItem);
        }});
        compositePriceVisitor.visit(lineItem);
    }
}
