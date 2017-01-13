/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins.parser.clif;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.ow2.clif.jenkins.chart.ChartConfiguration;
import org.ow2.clif.jenkins.model.*;
import org.ow2.clif.storage.api.*;
import org.ow2.clif.storage.lib.filestorage.FileStorageReader;
import org.ow2.clif.storage.lib.util.DateEventFilter;
import org.ow2.clif.supervisor.api.ClifException;

/**
 * Class that parses ClifBuilder result directory to compute statistics.<br>
 * Only the latest test execution is scanned to retrieve probe and injector data.
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ClifParser {

	private static final String EVENT_ACTION_TYPE = "action type";

	private static final String EVENT_COMMENT = "comment";

	private static final String EVENT_RESULT = "result";

	private static final String EVENT_SUCCESS = "success";

	private static final String EVENT_DATE = "date";

	private static final String EVENT_DURATION = "duration";

	private static final String ALARM_EVENT_TYPE = "alarm";

	/*
		* Configuration properties
		*/
	protected final String clifReportDirectory;

	protected final File ouputDirectory;

	protected DateEventFilter dateEventFilter;

	protected final Map<String, Pattern> successfulResultPatterns = new HashMap<String, Pattern>();

	protected final Map<String, Pattern> actionAliasPatterns = new HashMap<String, Pattern>();

	private ChartConfiguration chartConfiguration;

	protected final ParsingContext context = new ParsingContext();

	/*
		* Internals attributs to compute the report
		*/
	protected ClifReport report;

	protected Map<String, ActionStatInfo> statsByAction;

	protected Map<String, ActionStatInfo> aggregatedStatsByAction;

	protected final List<String> eventTypeToExclude = Arrays.asList("lifecycle", ALARM_EVENT_TYPE);

	protected StorageRead storageRead;

	protected PrintStream logger;

	protected final Pattern patternProbeName = Pattern.compile("org\\.ow2\\.clif\\.probe.(.*)\\.Insert");

	/**
	 * Indicates if charts should be generated. Used for tests only
	 */
	private boolean generateCharts = true;

	public ClifParser(String clifReportDirectory, File ouputDirectory) {
		this.clifReportDirectory = clifReportDirectory;
		this.ouputDirectory = ouputDirectory;
	}

	// --------------- Configuration methods -------------------
	public void addDateFilter(long from, long to) {
		dateEventFilter = new DateEventFilter(from, to);
	}

	public void addSuccessfulResultPattern(String actionType, String pattern) {
		successfulResultPatterns.put(actionType, Pattern.compile(pattern));
	}

	public void addActionAliasPattern(String actionAlias, String pattern) {
		actionAliasPatterns.put(actionAlias, Pattern.compile(pattern));
	}

	/**
	 * Enable the data cleanup functionality
	 *
	 * @param keepFactor     number of standard deviation to keep around the mean value
	 * @param keepPercentage percentage of value to keep
	 */
	public void enableDataCleanup(double keepFactor, double keepPercentage) {
		if (keepFactor <= 0) {
			throw new IllegalArgumentException("keepFactor should be greater than zero");
		}
		if (keepPercentage < 0 || keepPercentage >= 100) {
			throw new IllegalArgumentException("keepPercentage should be greater than zero and lesser than 100");
		}
		context.setDataCleanup(true);
		context.setKeepFactor(keepFactor);
		context.setKeepPercentage(keepPercentage);
	}

	/**
	 * Parses the specified report directory to generate a {@link ClifReport}
	 * for the latest test execution.
	 *
	 * @param logger logger to use
	 * @return the ClifReport object build during the parsing
	 * @throws ClifParserException if any problem occurs
	 */
	public ClifReport parse(PrintStream logger)
			throws ClifParserException {
		this.logger = logger;
		this.report = new ClifReport();

		try {
			this.storageRead = new FileStorageReader(this.clifReportDirectory, false);
			TestDescriptor[] tests = this.storageRead.getTests(null);
			TestDescriptor latestTest = tests[0];
			for (TestDescriptor testDesc : tests) {
				if (testDesc.getDate().after(latestTest.getDate())) {
					latestTest = testDesc;
				}
			}
			context.setTest(latestTest);
			analyzeTestPlan(null);
		}
		catch (Exception e) {
			logger.println("Error during parsing of CLIF report directory " + e.getMessage());
			e.printStackTrace();
			throw new ClifParserException("Error during parsing of CLIF report directory");
		}
		return this.report;
	}

	protected void analyzeTestPlan(BladeFilter bladeFilter)
			throws ClifException {
		logger.println("Analyzing measures from test " + context.getTest().getName());
		BladeDescriptor[] blades = this.storageRead.getTestPlan(context.getTest().getName(), bladeFilter);
		if (blades.length > 0) {
			TestPlan testPlan = new TestPlan(context.getTestPlanShortName(), context.getTest().getDate());
			report.addTestplan(testPlan);

			aggregatedStatsByAction = new HashMap<String, ActionStatInfo>();

			for (BladeDescriptor bladeDescriptor : blades) {
				analyseBlade(testPlan, bladeDescriptor);
			}

			// Add aggregated measures
			for (Map.Entry<String, ActionStatInfo> entry : aggregatedStatsByAction.entrySet()) {
				Measure m = createInjectorMeasure(entry.getKey(), entry.getValue());
				testPlan.addAggregatedMeasure(m);
				generateChart(entry.getValue());
			}
		}
	}

	private void generateChart(ActionStatInfo actionStatInfo) {
		if (generateCharts) {
			actionStatInfo.generateCharts(this.ouputDirectory);
		}
	}

	protected void analyseBlade(TestPlan testPlan, BladeDescriptor bladeDescriptor) throws ClifException {
		context.setBlade(bladeDescriptor);
		if (bladeDescriptor.isProbe()) {
			analyzeProbe(testPlan);
		}
		else if (bladeDescriptor.isInjector()) {
			analyzeInjector(testPlan);
		}
		context.setBlade(null);
	}

	protected static String extractTestPlanName(String clifTestPlanName) {
		int nbUnderScore = StringUtils.countMatches(clifTestPlanName, "_");
		if (nbUnderScore < 2) {
			return clifTestPlanName;
		}
		int dateUnderScore = lastOrdinalIndexOf(clifTestPlanName, "_", 2);
		return clifTestPlanName.substring(0, dateUnderScore);
	}

	protected static int lastOrdinalIndexOf(String str, String searchStr, int ordinal) {
		if (str == null || searchStr == null || ordinal <= 0) {
			return -1;
		}
		if (searchStr.length() == 0) {
			return str.length();
		}
		int found = 0;
		int index = str.length();
		do {
			index = str.lastIndexOf(searchStr, index - 1);
			if (index < 0) {
				return index;
			}
			found++;
		}
		while (found < ordinal);
		return index;
	}

	protected void analyzeProbe(TestPlan testPlan)
			throws ClifException {
		logger.println("  * Analyzing probe: " + context.getBlade().getId());

		Probe probe = createProbe(context.getBlade());
		testPlan.addProbe(probe);

		for (String eventType : context.getBlade().getEventTypeLabels()) {
			if (!eventTypeToExclude.contains(eventType)) {
				context.setEventType(eventType);
				analyzeEventType(probe);
				context.setEventType(null);
			}

			if (ALARM_EVENT_TYPE.equals(eventType)) {
				loadAlarms(probe);
			}
		}
	}


	protected Probe createProbe(BladeDescriptor bladeDesc) {
		return new Probe(bladeDesc.getId(), extractProbeSimpleName(bladeDesc.getClassname()),
		                 bladeDesc.getServerName(), bladeDesc.getArgument(), bladeDesc.getClassname(),
		                 bladeDesc.getComment());
	}

	protected void analyzeInjector(TestPlan testPlan)
			throws ClifException {
		logger.println("  * Analyzing injector: " + context.getBlade().getId());
		Injector injector = createInjector(context.getBlade());
		testPlan.addInjector(injector);

		for (String eventType : context.getBlade().getEventTypeLabels()) {
			if (!eventTypeToExclude.contains(eventType)) {
				context.setEventType(eventType);
				analyzeEventType(injector);
				context.setEventType(null);
			}

			if (ALARM_EVENT_TYPE.equals(eventType)) {
				loadAlarms(injector);
			}
		}
	}

	protected Injector createInjector(BladeDescriptor bladeDesc) {
		return new Injector(bladeDesc.getId(), bladeDesc.getClassname(), bladeDesc.getServerName(),
		                    bladeDesc.getArgument(), bladeDesc.getClassname(), bladeDesc.getComment());
	}

	protected void analyzeEventType(Probe probe)
			throws ClifException {
		logger.println("    - Analyzing probe event type: " + context.getEventType());

		String[] labels = this.storageRead.getEventFieldLabels(context.getTest().getName(), context.getBlade().getId(),
		                                                       context.getEventType());
		BladeEvent[] events =
				this.storageRead
						.getEvents(context.getTest().getName(), context.getBlade().getId(), context.getEventType(),
						           dateEventFilter, 0, -1);

		// Init stats and charts
		ActionStatInfo[] statsInfo = new ActionStatInfo[labels.length];

		for (int i = 1; i < labels.length; i++) {
			context.setEventType(labels[i]);
			statsInfo[i] = new ActionStatInfo(context, this.chartConfiguration);
		}

		// Parsing bladeEvents to compute stats and build charts
		logger.println("    - " + events.length + " events to analyze");
		for (BladeEvent bladeEvent : events) {
			// Start from 1 in order to ignore "date" label
			for (int i = 1; i < labels.length; i++) {
				context.setEventType(labels[i]);
				double value = toDouble(bladeEvent.getFieldValue(labels[i]));
				long date = (Long) bladeEvent.getFieldValue(EVENT_DATE);
				statsInfo[i].addStat(date, value);
			}
		}

		// Create measures and charts
		for (int i = 1; i < labels.length; i++) {
			context.setEventType(labels[i]);
			Measure m = createProbeMeasure(labels[i], statsInfo[i]);
			probe.addMeasure(m);
			generateChart(statsInfo[i]);
		}
	}


	protected void analyzeEventType(Injector injector)
			throws ClifException {
		logger.println("    - Analyzing injector event type: " + context.getEventType());

		statsByAction = new HashMap<String, ActionStatInfo>();
		ActionStatInfo.resetTime();

		BladeEvent[] events =
				this.storageRead
						.getEvents(context.getTest().getName(), context.getBlade().getId(), context.getEventType(),
						           dateEventFilter, 0, -1);
		logger.println("    - " + events.length + " events to analyze");
		for (BladeEvent actionEvent : events) {
			String action = buildAction(actionEvent);
			context.setEventType(action);

			if (isEventInError(actionEvent)) {
				addError(action);
			}
			else {
				addEventToStat(action, actionEvent);
			}
		}

		for (Map.Entry<String, ActionStatInfo> entry : statsByAction.entrySet()) {
			Measure m = createInjectorMeasure(entry.getKey(), entry.getValue());
			injector.addMeasure(m);
			generateChart(entry.getValue());
		}
	}

	protected void addError(String action) {
		ActionStatInfo statInfo = statsByAction.get(action);
		if (statInfo == null) {
			statInfo = new ActionStatInfo(context, this.chartConfiguration);
			statsByAction.put(action, statInfo);
		}
		statInfo.incrementErrors();
		addAggregatedError(action);
	}

	private void addAggregatedError(String action) {
		ActionStatInfo statInfo = aggregatedStatsByAction.get(action);
		if (statInfo == null) {
			BladeDescriptor currentBlade = context.getBlade();
			context.setBlade(null);
			statInfo = new ActionStatInfo(context, this.chartConfiguration);
			context.setBlade(currentBlade);
			aggregatedStatsByAction.put(action, statInfo);
		}

		statInfo.incrementErrors();
	}

	protected void addEventToStat(String action, BladeEvent actionEvent) {
		ActionStatInfo statInfo = statsByAction.get(action);
		if (statInfo == null) {
			statInfo = new ActionStatInfo(context, this.chartConfiguration);
			statsByAction.put(action, statInfo);
		}

		int duration = (Integer) actionEvent.getFieldValue(EVENT_DURATION);
		long date = (Long) actionEvent.getFieldValue(EVENT_DATE);

		statInfo.addStat(date, duration);
		addEventToAggregatedStat(action, date, duration);
	}

	protected void addEventToAggregatedStat(String action, long date, int duration) {
		ActionStatInfo statInfo = aggregatedStatsByAction.get(action);
		if (statInfo == null) {
			BladeDescriptor currentBlade = context.getBlade();
			context.setBlade(null);
			statInfo = new ActionStatInfo(context, this.chartConfiguration);
			context.setBlade(currentBlade);
			aggregatedStatsByAction.put(action, statInfo);
		}

		statInfo.addStat(date, duration);
	}

	protected boolean isEventInError(BladeEvent actionEvent) {
		boolean isError = true;
		// At least, checks the success field BUT some application
		// errors can be detected thru the result field
		if ((Boolean) actionEvent.getFieldValue(EVENT_SUCCESS)) {
			isError = !isSuccessfulResult(actionEvent);
		}
		return isError;
	}

	protected boolean isSuccessfulResult(BladeEvent actionEvent) {
		String result = actionEvent.getFieldValue(EVENT_RESULT).toString();
		String actionType = actionEvent.getFieldValue(EVENT_ACTION_TYPE).toString();

		Pattern pattern = this.successfulResultPatterns.get(actionType);
		if (pattern != null) {
			Matcher m = pattern.matcher(result);
			return m.matches();
		}
		return true;
	}

	protected String buildAction(BladeEvent actionEvent) {
		Object actionType = actionEvent.getFieldValue(EVENT_ACTION_TYPE);
		Object comment = actionEvent.getFieldValue(EVENT_COMMENT);
		StringBuilder sb = new StringBuilder();
		sb.append(actionType);
		if (comment != null) {
			sb.append("-").append(comment);
		}

		return getAlias(sb.toString());
	}

	protected Measure createProbeMeasure(String name, ActionStatInfo statInfo) {
		Measure m = new Measure();
		statInfo.compute();
		m.setName(name);
		m.setAverage((long) statInfo.getMean());
		m.setMax((long) statInfo.getMax());
		m.setMin((long) statInfo.getMin());
		m.setStdDev(statInfo.getStandardDeviation());
		m.setMedian((long) statInfo.getPercentile(50));
		m.setSize(statInfo.getN());
		return m;
	}

	protected Measure createInjectorMeasure(String name, ActionStatInfo statInfo) {
		Measure m = new Measure();
		statInfo.compute();
		m.setName(name);
		m.setAverage((long) statInfo.getMean());
		m.setMax((long) statInfo.getMax());
		m.setMin((long) statInfo.getMin());
		m.setStdDev(statInfo.getStandardDeviation());
		m.setMedian((long) statInfo.getPercentile(50));
		m.setSize(statInfo.getN());
		m.setCountErrors(statInfo.getErrors());
		m.setThroughput(statInfo.getThroughput());
		return m;
	}


	protected String extractProbeSimpleName(String probeClassName) {

		Matcher m = patternProbeName.matcher(probeClassName);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	protected String getAlias(String action) {
		for (Map.Entry<String, Pattern> entry : actionAliasPatterns.entrySet()) {
			Matcher m = entry.getValue().matcher(action);
			if (m.matches()) {
				return entry.getKey();
			}
		}
		return action;
	}


	private void loadAlarms(Blade blade)
			throws ClifException {
		BladeEvent[] events =
				this.storageRead.getEvents(context.getTest().getName(), context.getBlade().getId(), ALARM_EVENT_TYPE,
				                           dateEventFilter, 0, -1);
		logger.println("    - " + events.length + " alarms to load");
		for (BladeEvent bladeEvent : events) {
			AlarmEvent alarmEvent = (AlarmEvent) bladeEvent;
			Alarm alarm = new Alarm(alarmEvent.getDate(),
			                        Alarm.Severity.fromValue((Integer) alarmEvent.getFieldValue("severity")),
			                        (String) alarmEvent.getFieldValue("argument"));
			blade.addAlarm(alarm);
		}
	}

	/**
	 * Convert an object to a double value.<br>
	 * Any {@link Number} is directly converted. {@link Boolean} are also converted: Boolean.TRUE = 1 and Boolean.FALSE = 0.
	 * For other objects, the return value is 1
	 *
	 * @param value Obejct to convert
	 * @return double value associeted
	 */
	protected static double toDouble(Object value) {
		double vals = 0;
		if (value != null) {
			if (value instanceof Number) {
				vals = ((Number) value).doubleValue();
			}
			else if (value instanceof Boolean) {
				vals = (Boolean) value ? 1.0 : 0.0;
			}
			else {
				vals = 1.0;
			}
		}
		return vals;
	}

	public void setChartConfiguration(ChartConfiguration chartConfiguration) {
		this.chartConfiguration = chartConfiguration;
	}

	public void setGenerateCharts(boolean generateCharts) {
		this.generateCharts = generateCharts;
	}
}
