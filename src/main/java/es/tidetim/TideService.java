package es.tidetim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tideengine.BackEndTideComputer;
import tideengine.Stations;
import tideengine.TimedValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/tides")
public class TideService {

    private static final Logger LOG = LoggerFactory.getLogger(TideService.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    TideCalculator calculator;

    @RequestMapping(value = "{location}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    List<TimedValue> getTideHeightAtTimeAndPlace(@PathVariable("location") String location, @RequestParam(value = "date", required = false) String date) {

        LocalDate calendar = getDate(date);


        try {
            return calculator.getHighAndLowTides(location, calendar);
        } catch (Exception e) {
            LOG.error("Unable to find tide times for " + location, e);
            return null;
        }
    }

    private LocalDate getDate(String date) {
        if (!StringUtils.isEmpty(date)) {
            try {
                return LocalDateTime.ofInstant(dateFormat.parse(date).toInstant(), ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                LOG.error("Unable to parse {} to date", date, e);
            }
        }
        return LocalDate.now();
    }

    @RequestMapping(value = "hourly/{location}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    List<TimedValue> getTides(@PathVariable("location") String location, @RequestParam(value = "date", required = false) String date) {

        LocalDate calendar = getDate(date);

        try {
            return calculator.getTides(location, calendar, 60);
        } catch (Exception e) {
            LOG.error("Unable to find tide times for " + location, e);
            return null;
        }
    }

    @RequestMapping(value = "stations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Stations getStations() throws Exception {

        try {
            BackEndTideComputer.connect();
            return BackEndTideComputer.getStations();
        } finally {
            BackEndTideComputer.disconnect();
        }
    }

}
