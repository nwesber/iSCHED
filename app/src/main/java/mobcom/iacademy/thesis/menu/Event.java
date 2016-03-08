package mobcom.iacademy.thesis.menu;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import mobcom.iacademy.thesis.R;
import mobcom.iacademy.thesis.event.adapter.EventAdapter;
import mobcom.iacademy.thesis.event.adapter.EventDecorator;
import mobcom.iacademy.thesis.event.adapter.HighlightWeekendsDecorator;
import mobcom.iacademy.thesis.event.adapter.MySelectorDecorator;
import mobcom.iacademy.thesis.event.adapter.OneDayDecorator;
import mobcom.iacademy.thesis.event.main.EditEventActivity;
import mobcom.iacademy.thesis.event.main.NewEventActivity;
import mobcom.iacademy.thesis.model.DayBean;
import mobcom.iacademy.thesis.model.EventBean;

public class Event extends Fragment implements OnDateSelectedListener, OnMonthChangedListener {

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    private MaterialCalendarView calendarView;
    private Intent intent;
    private DayBean day;
    private CalendarDay calendarDay;
    private ProgressBar progressBar;
    private List<EventBean> list;
    private EventAdapter eventAdapter;

    private RecyclerView rv;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        calendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        rv.setLayoutManager(llm);
        setHasOptionsMenu(true);
        //list.clear();
        init();


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), NewEventActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_event_interface, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_event_today:
                goToday();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        calendarConfig();
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.GONE);
            new EventSync().executeOnExecutor(Executors.newSingleThreadExecutor());
        } else {
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            calendarView.setVisibility(View.GONE);
            // If there is no connection, let the user know the sync didn't happen
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    "Your device appears to be offline. Unable to fetch events.",
                    Toast.LENGTH_LONG).show();
        }
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayNow = cal.get(Calendar.DAY_OF_MONTH);
        String currentDate = (getMonth(monthNow) + " " + dayNow + ", " + yearNow);
        getSelectedUserEvent(currentDate);

        eventAdapter.SetOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventBean event = list.get(position);
                Intent intent = new Intent(getActivity().getApplication(), EditEventActivity.class);
                intent.putExtra("eventId", event.getId());
                intent.putExtra("eventTitle", event.getEvent());
                intent.putExtra("eventLocation", event.getLocation());
                intent.putExtra("eventDateStart", event.getDateStart());
                intent.putExtra("eventDateEnd", event.getDateEnd());
                intent.putExtra("eventTimeStart", event.getTimeStart());
                intent.putExtra("eventTimeEnd", event.getTimeEnd());
                intent.putExtra("eventContent", event.getDescription());
                intent.putExtra("eventOwner", event.getUsername());
                intent.putExtra("dayYear", day.getYear());
                intent.putExtra("dayMonth", day.getMonth());
                intent.putExtra("dayNow", day.getDayNow());
                intent.putExtra("isAllDay", event.isAllDay());
                startActivity(intent);

            }
        });
    }


    private void calendarConfig() {
        calendarView.setOnDateChangedListener(this);
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_NONE);
        calendarView.setCalendarDisplayMode(CalendarMode.MONTHS);
        Calendar calendar = Calendar.getInstance();
        calendarView.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        calendarView.setMinimumDate(calendar.getTime());
        calendarView.addDecorators(
                new MySelectorDecorator(getActivity()),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );
    }


    private class EventSync extends AsyncTask<Void, Void, List<CalendarDay>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final ArrayList<CalendarDay> dates = new ArrayList<>();
            //fetch events in background

            String currentUser = ParseUser.getCurrentUser().getObjectId();
            String[] names = {currentUser, "holiday"};
            ParseQuery<ParseObject> userGroup = ParseQuery.getQuery("Event");
            userGroup.whereEqualTo("isCompleted", false);
            userGroup.whereContainedIn("username", Arrays.asList(names));
            userGroup.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    rv.setVisibility(View.VISIBLE);
                    calendarView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    dates.clear();
                    for (ParseObject days : objects) {
                        day = new DayBean(days.getObjectId(), days.getString("event"), days.getInt("year"), days.getInt("month"), days.getInt("day"));
                        calendarDay = CalendarDay.from(day.getYear(), day.getMonth(), day.getDayNow());
                        dates.add(calendarDay);
                        calendarView.addDecorator(new EventDecorator(Color.RED, dates));
                    }
                }
            });


            return dates;
        }
    }

    private void getSelectedUserEvent(String event) {

        list = new ArrayList<>();
        eventAdapter = new EventAdapter(list);
        rv.setAdapter(eventAdapter);
        String currentUser = ParseUser.getCurrentUser().getObjectId();
        String[] names = {currentUser, "holiday"};
        ParseQuery<ParseObject> userGroup = ParseQuery.getQuery("Event");
        userGroup.whereContainedIn("username", Arrays.asList(names));
        userGroup.whereEqualTo("isCompleted", false);
        userGroup.whereEqualTo("dateStart", event);
        userGroup.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                list.clear();
                if (e == null) {
                    for (ParseObject userevent : objects) {
                        EventBean eventsUser = new EventBean(userevent.getObjectId(), userevent.getString("event"), userevent.getString("description"), userevent.getString("timeStart"), userevent.getString("timeEnd"), userevent.getString("location"), userevent.getString("dateStart"), userevent.getString("dateEnd"), userevent.getString("username"), userevent.getBoolean("isAllDay"));
                        list.add(eventsUser);
                    }
                    eventAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Parse Error: ", e.getMessage());
                }
            }
        });
    }


    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        String selectedDate = getSelectedDatesString(date);
        getSelectedUserEvent(selectedDate);
    }


    public void goToday() {
        Calendar calendar = Calendar.getInstance();
        calendarView.setCurrentDate(calendar);
        calendarView.setSelectedDate(calendar);
    }

    private String getSelectedDatesString(CalendarDay selectedDay) {
        selectedDay = calendarView.getSelectedDate();
        if (day == null) {
            return "No Selection";
        }
        return FORMATTER.format(selectedDay.getDate());
    }

    private String getMonth(int month) {
        String currentMonth = "";
        switch (month) {
            case 0:
                currentMonth = "Jan";
                break;
            case 1:
                currentMonth = "Feb";
                break;
            case 2:
                currentMonth = "Mar";
                break;
            case 3:
                currentMonth = "Apr";
                break;
            case 4:
                currentMonth = "May";
                break;
            case 5:
                currentMonth = "Jun";
                break;
            case 6:
                currentMonth = "Jul";
                break;
            case 7:
                currentMonth = "Aug";
                break;
            case 8:
                currentMonth = "Sep";
                break;
            case 9:
                currentMonth = "Oct";
                break;
            case 10:
                currentMonth = "Nov";
                break;
            case 11:
                currentMonth = "Dec";
                break;
        }
        return currentMonth;
    }
}
