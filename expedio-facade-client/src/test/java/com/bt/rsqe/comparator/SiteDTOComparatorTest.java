package com.bt.rsqe.comparator;

import com.bt.rsqe.customerrecord.SiteDTO;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import static com.bt.rsqe.comparator.SiteDTOComparatorTest.SiteDTOComparisonMatcher.*;
import static com.bt.rsqe.comparator.SiteDTOComparatorTest.SiteDTOFixture.*;
import static org.hamcrest.MatcherAssert.*;

public class SiteDTOComparatorTest {

    @Test
    public void shouldReturnFalseIfNull() throws Exception {
        assertThat(aSiteDTO("site1"), isEqualToWhenComparedWith(null));
    }

    @Test
    public void shouldSortBySiteNameIfBothSiteDTOInstances() throws Exception {

        assertThat(aSiteDTO("123"), isLessThanWhenComparedWith(aSiteDTO("321")));

        assertThat(aSiteDTO("321"), isGreaterThanWhenComparedWith(aSiteDTO("123")));

        assertThat(aSiteDTO("123"), isEqualToWhenComparedWith(aSiteDTO("123")));

    }

    protected static class SiteDTOFixture {
        static SiteDTO aSiteDTO(final String siteName) {
            return new SiteDTO("123", siteName);
        }
    }

    protected static abstract class SiteDTOComparisonMatcher extends TypeSafeMatcher<SiteDTO> {

        final SiteDTOComparator comparator;
        final SiteDTO comparedToSite;
        private final String comparisonText;

        protected SiteDTOComparisonMatcher(SiteDTO comparedToSite, String comparisonText) {
            this.comparisonText = comparisonText;
            this.comparator = new SiteDTOComparator();
            this.comparedToSite = comparedToSite;
        }

        protected static SiteDTOComparisonMatcher isEqualToWhenComparedWith(SiteDTO site) {
            return new SiteDTOEqualMatcher(site);
        }

        protected static SiteDTOComparisonMatcher isGreaterThanWhenComparedWith(SiteDTO site) {
            return new SiteDTOMoreThanMatcher(site);
        }

        protected static SiteDTOComparisonMatcher isLessThanWhenComparedWith(SiteDTO site) {
            return new SiteDTOLessThanMatcher(site);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("expected to %s when compared", comparisonText));
        }


        private static class SiteDTOEqualMatcher extends SiteDTOComparisonMatcher {
            public SiteDTOEqualMatcher(SiteDTO site) {
                super(site, "be equal");
            }

            @Override
            public boolean matchesSafely(SiteDTO site) {
                int compareResult = comparator.compare(site, comparedToSite);
                return compareResult == 0;
            }
        }

        private static class SiteDTOLessThanMatcher extends SiteDTOComparisonMatcher {
            protected SiteDTOLessThanMatcher(SiteDTO site) {
                super(site, "be less than");
            }

            @Override
            public boolean matchesSafely(SiteDTO site) {
                int compareResult = comparator.compare(site, comparedToSite);
                return compareResult < 0;
            }
        }

        private static class SiteDTOMoreThanMatcher extends SiteDTOComparisonMatcher {
            protected SiteDTOMoreThanMatcher(SiteDTO site) {
                super(site, "be greater than than");
            }

            @Override
            public boolean matchesSafely(SiteDTO site) {
                int compareResult = comparator.compare(site, comparedToSite);
                return compareResult > 0;
            }
        }


    }
}
