package test.java.com.concur.mobile.core.expense.travelallowance.util.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.IDatePeriodUTC;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Becherer on 15-Jul-15.
 */
@Config(manifest = Config.NONE, sdk = 21)
@RunWith(RobolectricTestRunner.class)
public class DateUtilsTest extends TestCase {

    private class PeriodUTC implements IDatePeriodUTC {

        private Date startDate;
        private Date endDate;

        public PeriodUTC(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public Date getStartDateUTC() {
            return startDate;
        }

        @Override
        public Date getEndDateUTC() {
            return endDate;
        }
    }

    @Before
    public void setup() {
    }

    @Test
    public void testGetCalendarKeepingDate() {
        Calendar inputCal = Calendar.getInstance();
        inputCal.set(2000, Calendar.JULY, 22);
        inputCal.set(Calendar.HOUR_OF_DAY, 1);
        inputCal.set(Calendar.MINUTE, 1);
        inputCal.set(Calendar.SECOND, 0);
        inputCal.set(Calendar.MILLISECOND, 0);

        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2000, Calendar.JULY, 22);
        expectedCal.set(Calendar.HOUR_OF_DAY, 2);
        expectedCal.set(Calendar.MINUTE, 2);
        expectedCal.set(Calendar.SECOND, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);

        Calendar resultCal = DateUtils.getCalendarKeepingDate(inputCal.getTime(), 2, 2);

        assertEquals(expectedCal, resultCal);
    }

    @Test
    public void testGetCalendarKeepingTime() {
        Calendar inputCal = Calendar.getInstance();
        inputCal.set(2000, Calendar.JULY, 22);
        inputCal.set(Calendar.HOUR_OF_DAY, 1);
        inputCal.set(Calendar.MINUTE, 1);
        inputCal.set(Calendar.SECOND, 0);
        inputCal.set(Calendar.MILLISECOND, 0);

        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2001, Calendar.AUGUST, 23);
        expectedCal.set(Calendar.HOUR_OF_DAY, 1);
        expectedCal.set(Calendar.MINUTE, 1);
        expectedCal.set(Calendar.SECOND, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);

        Calendar resultCal = DateUtils.getCalendarKeepingTime(inputCal.getTime(), 2001, Calendar.AUGUST, 23);

        assertEquals(expectedCal, resultCal);
    }

    @Test
    public void testHasSubsequentDatesAscending() {
        List<PeriodUTC> periods = new ArrayList<PeriodUTC>();
        periods.add(new PeriodUTC(getDate(2000, 6, 22, 12, 0), getDate(2000, 6, 22, 13, 0)));
        periods.add(new PeriodUTC(getDate(2000, 6, 23, 14, 0), getDate(2000, 6, 23, 15, 0)));
        assertTrue(DateUtils.hasSubsequentDates(false, true, 2, periods));
    }

    @Test
    public void testHasSubsequentDatesAscendingIgnoringTime() {
        List<PeriodUTC> periods = new ArrayList<PeriodUTC>();
        periods.add(new PeriodUTC(getDate(2000, 6, 22, 12, 0), getDate(2000, 6, 23, 13, 0)));
        periods.add(new PeriodUTC(getDate(2000, 6, 23, 14, 0), getDate(2000, 6, 23, 15, 0)));
        assertFalse(DateUtils.hasSubsequentDates(true, true, 2, periods));
    }

    @Test
    public void testHasSubsequentDatesAscendingWithGaps() {
        List<PeriodUTC> periods = new ArrayList<PeriodUTC>();
        periods.add(new PeriodUTC(getDate(2000, 6, 22, 12, 0), null));
        periods.add(new PeriodUTC(null, getDate(2000, 6, 23, 15, 0)));
        assertTrue(DateUtils.hasSubsequentDates(false, true, 1, periods));
    }

    @Test
    public void testHasSubsequentDatesDescending() {
        List<PeriodUTC> periods = new ArrayList<PeriodUTC>();
        periods.add(new PeriodUTC(getDate(2000, 6, 23, 15, 0), getDate(2000, 6, 22, 14, 0)));
        periods.add(new PeriodUTC(getDate(2000, 6, 22, 13, 0), getDate(2000, 6, 22, 12, 0)));
        assertTrue(DateUtils.hasSubsequentDates(false, false, 2, periods));
    }

    @Test
    public void testHasSubsequentDatesDescendingIgnoringTime() {
        List<PeriodUTC> periods = new ArrayList<PeriodUTC>();
        periods.add(new PeriodUTC(getDate(2000, 6, 23, 15, 0), getDate(2000, 6, 22, 14, 0)));
        periods.add(new PeriodUTC(getDate(2000, 6, 22, 13, 0), getDate(2000, 6, 22, 12, 0)));
        assertFalse(DateUtils.hasSubsequentDates(true, false, 2, periods));
    }

    private Date getDate(int year, int month, int day, int hourOfDay,
                         int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute);
        return calendar.getTime();
    }
}
