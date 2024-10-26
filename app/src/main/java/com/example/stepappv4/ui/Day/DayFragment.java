package com.example.stepappv4.ui.Day;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.R;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.databinding.FragmentDayBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Calendar;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.LinkedHashMap;
//import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends Fragment {

    AnyChartView anyChartView;

    private FragmentDayBinding binding;

    public Map<Integer, Integer> stepsByHour = null;


    public DayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        anyChartView = root.findViewById(R.id.dayBarChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));

        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);
        // Inflate the layout for this fragment
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Cartesian createColumnChart(){
        //***** Read data from SQLiteDatabase *********/
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = calendar.getTime();
        String nowS =  now.toString();

        List<String> last7Days = getLastWeekDates();

        Map<String, Integer> last7DaysSteps = StepAppOpenHelper.loadStepsByLast7Days(getContext(), last7Days);
        // test data
//        last7DaysSteps = Map.of(
//                "2024-10-26", 10,
//                "2024-10-22", 18,
//                "2024-10-24", 15,
//                "2024-10-23", 20,
//                "2024-10-25", 5,
//                "2024-10-21", 7,
//                "2024-10-20", 24);

         LinkedHashMap<String, Integer> linkedHashMap = last7DaysSteps.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    return new int[]{year, month, day}; // Use array for multiple comparisons
                }, Comparator.comparingInt((int[] arr) -> arr[0]) // Compare year
                        .thenComparingInt(arr -> arr[1])                // Then compare month
                        .thenComparingInt(arr -> arr[2]))).collect(Collectors.toMap(
                entry -> entry.getKey(),   // Corrected with lambda expressions
                entry -> entry.getValue(), // Corrected with lambda expressions
                (existing, replacement) -> existing,  // Handle key conflicts
                LinkedHashMap::new         // Use LinkedHashMap to maintain order
        ));              // Then compare day

        Map<String, Integer> graph_map = new LinkedHashMap<>();

        for (String d: last7Days) {
            Integer v = linkedHashMap.get(d);
            if (v == null) v = 0;
            graph_map.put(d,v);
        }

        //***** Create column chart using AnyChart library *********/
        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        Column column = cartesian.column(data);

        //***** Modify the UI of the chart *********/
        column.fill("#1EB980");
        column.stroke("#1EB980");

        column.tooltip()
                .titleFormat("At day: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM);

        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);


        cartesian.yAxis(0).title("Number of steps");
        cartesian.xAxis(0).title("Date");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }

    public List<String> getLastWeekDates() {
        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        // Store the dates in a list
        List<String> lastWeekDates = new ArrayList<>();

        // Get the dates for the last 7 days
        for (int i = 0; i < 7; i++) {
            // Format and add the current date to the list
            String formattedDate = dateFormat.format(calendar.getTime());
            lastWeekDates.add(formattedDate);

            // Move the calendar back by one day
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        Collections.reverse(lastWeekDates);
        return lastWeekDates;
    }

}