package dates;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RWDateFormat extends StdDateFormat {

    public RWDateFormat() {
        super(TimeZone.getDefault(), Locale.getDefault(), true);
    }

    @Override
    public Date parse(String dateStr) throws ParseException {
        try {
        	return super.parse(dateStr);
        } catch (ParseException e) {
        	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	fmt.setTimeZone(TimeZone.getDefault());
        	return fmt.parse(dateStr);
        }
    }

    @Override
    public StdDateFormat clone() {
        return new RWDateFormat();
    }
}
