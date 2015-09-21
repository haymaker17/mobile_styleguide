package test.java.com.concur.mobile.core.expense.travelallowance.util.test;

import com.concur.mobile.core.expense.travelallowance.datamodel.IDatePeriodUTC;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

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

        Calendar resultCal = DateUtils.getCalendarKeepingDate(inputCal.getTime(), 2, 2, 0, 0);

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

    @Test
    public void startEndDateToString(){
        Date startDate = getDate(2015,Calendar.AUGUST, 31, 9, 21);
        Date endDate = getDate(2015,Calendar.SEPTEMBER, 17, 11, 42);
        IDateFormat dateFormatter = null;
//      Enhance method as soon as the mocking of the context is possible
//      IDateFormat dateFormatter = new DefaultDateFormat(context);

        assertEquals(StringUtilities.EMPTY_STRING, DateUtils.startEndDateToString(null, null, dateFormatter, false, false, false));
        assertEquals(StringUtilities.EMPTY_STRING, DateUtils.startEndDateToString(startDate, endDate, dateFormatter, false, false, false));
    }

    @Test
    public void getDateIgnoringTime(){
        assertNull(DateUtils.getDateIgnoringTime(null));

        Calendar cal = Calendar.getInstance();
        cal.set(2015, Calendar.SEPTEMBER, 17);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);


        assertEquals(cal.getTime(),  DateUtils.getDateIgnoringTime(getDate(2015, Calendar.SEPTEMBER, 17, 11, 42)));

    }

    @Test
    public void getCalendarKeepingDate(){
        assertNull(DateUtils.getCalendarKeepingDate(null, 0, 0, 0, 0));

        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(getReferenceDate());
        calendar.set(2015, Calendar.SEPTEMBER, 17, 16, 42);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar, DateUtils.getCalendarKeepingDate(getReferenceDate(), 16, 42, 0, 0));

    }

    @Test
    public void getCalendarKeepingTime(){
        assertNull(DateUtils.getCalendarKeepingTime(null,2014,Calendar.MARCH,22));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.MARCH, 22, 16, 42);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar, DateUtils.getCalendarKeepingTime(getReferenceDate(),2014,Calendar.MARCH,22));
    }

    private Date getDate(int year, int month, int day, int hourOfDay,
                         int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute);
        return calendar.getTime();
    }

    private Date getReferenceDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.SEPTEMBER, 17, 16, 42);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

}
