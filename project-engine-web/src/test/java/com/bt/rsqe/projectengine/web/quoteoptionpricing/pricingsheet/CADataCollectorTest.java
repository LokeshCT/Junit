package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CADataCollectorTest {

    @Mock
    LineItemVisitorFactory lineItemVisitorFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCollectDataForCA() throws Exception {
        LineItemModel lineItemModel1 = mock(LineItemModel.class);
        LineItemModel lineItemModel2 = mock(LineItemModel.class);
        List<LineItemModel> lineItemModels = newArrayList(lineItemModel1, lineItemModel2);
        LineItemVisitor visitor = mock(LineItemVisitor.class);
        when(lineItemVisitorFactory.createGeneralPricingSheetVisitor(Matchers.<List<Map<String, Object>>>any(), Matchers.<Map<String, Object>>any(),
                                                                     Matchers.<PricesTotalAggregator>any(), Matchers.<List<Map<String, Object>>>any())).
            thenReturn(visitor);
        when(lineItemVisitorFactory.createTrafficMatrixPricingSheetVisitor(Matchers.<List<Map<String, Object>>>any(), Matchers.<List<Map<String, Object>>>any(),
                                                                           Matchers.<Map<String, Object>>any())).thenReturn(visitor);
        doNothing().when(lineItemModel1).accept(visitor);
        doNothing().when(lineItemModel2).accept(visitor);

        CADataCollector dataCollector = new CADataCollector(lineItemVisitorFactory);
        dataCollector.process(lineItemModels, newHashMap());
        
        verify(lineItemVisitorFactory, times(2)).createGeneralPricingSheetVisitor(Matchers.<List<Map<String, Object>>>any(), Matchers.<Map<String, Object>>any(),
                                                                        Matchers.<PricesTotalAggregator>any(), Matchers.<List<Map<String, Object>>>any());
        
        verify(lineItemModel1, times(2)).accept(visitor);
        verify(lineItemModel2, times(2)).accept(visitor);
    }

    @Test
    public void shouldSortListOfMapsBasedOnAKey() {
        Map<String, Object> map1 = newHashMap();
        map1.put("abc", "2");
        map1.put("xyz", "2");
        Map<String, Object> map2 = newHashMap();
        map2.put("abc", "1");
        map2.put("xyz", "2");
        List<Map<String, Object>> mapArrayList = newArrayList(map1, map2);
        CADataCollector caDataCollector = new CADataCollector(lineItemVisitorFactory);
        caDataCollector.sortListOfMapsBasedOnAKey(mapArrayList, "abc", "xyz");
        assertThat(mapArrayList.get(0), is(map2));
    }
}
