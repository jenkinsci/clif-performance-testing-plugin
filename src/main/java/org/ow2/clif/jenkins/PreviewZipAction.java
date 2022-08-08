/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 * Copyright (C) 2016 Orange SA
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
package org.ow2.clif.jenkins;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.Installations;
import org.ow2.clif.jenkins.jobs.Jobs;
import org.ow2.clif.jenkins.jobs.ParameterParser;
import org.ow2.clif.jenkins.jobs.Workspaces;
import org.ow2.clif.jenkins.jobs.Zip;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import jenkins.model.Jenkins;
import static com.google.common.collect.Lists.newArrayList;

public class PreviewZipAction {
	Jenkins jenkins;
	Configurer clif;
	ImportZipAction parent;
	Installations installations;

	private final Zip zip;
	File dir;
	String pattern;

	List<String> uninstalls;
	List<String> upgrades;
	List<String> installs;

	public PreviewZipAction(Zip zip) {
		this(zip, Workspaces.dir());
	}

	public PreviewZipAction(Zip zip, File dir) {
		this.zip = zip;
		this.pattern = "([^/]*)/([^/]*)\\.ctp";
		this.dir = dir;
		this.clif = new Configurer();
		this.jenkins = Jenkins.get();
		installations = new Installations();
	}

	public List<String> getUninstalls() {
		return uninstalls;
	}

	public List<String> getUpgrades() {
		return upgrades;
	}

	public List<String> getInstalls() {
		return installs;
	}

	@SuppressWarnings("unchecked")
	PreviewZipAction diff() throws IOException {
		List<String> newPlans = zip.entries(pattern);
		String dir = zip.basedir();

		List<String> oldPlans = Lists.newArrayList();
		for (Item item : jenkins.getAllItems()) {
			String name = item.getName();
			if (name.startsWith(dir + "-")) {
				oldPlans.add(Jobs.toPlan(name));
			}
		}

		installs = newArrayList();
		uninstalls = newArrayList();
		upgrades = newArrayList();

		installs.addAll(CollectionUtils.subtract(newPlans, oldPlans));
		uninstalls.addAll(CollectionUtils.subtract(oldPlans, newPlans));
		upgrades.addAll(CollectionUtils.intersection(newPlans, oldPlans));

		return this;
	}

	/**
	 * responds to POST /clif/previews/12345
	 *
	 * @param req request
	 * @param res response
	 * @throws IOException process exception
	 * @throws InterruptedException process was interrupted
	 */
	public void doProcess(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException {
		clif.use(req.getParameter("clif"));

		Map<String, Set<String>> actions = parse(req);
		for (Map.Entry<String, Set<String>> e : actions.entrySet()) {
			String plan = e.getKey();
			if (plan != null && !plan.trim().isEmpty())
			{
				Set<String> verbs = e.getValue();
				if (verbs.contains("create")) {
					create(plan);
				}
				if (verbs.contains("delete")) {
					if (!verbs.contains("rm")) {
						keepReportsForJob(plan);
					}
					delete(plan);
				}
			}
		}
		zip.extractTo(dir).delete();
		res.sendRedirect2("/");
	}

	void keepReportsForJob(String plan) {
		getJob(plan).getProperty(ClifJobProperty.class).setDeleteReport(false);
	}

	// boilerplate
	@SuppressWarnings("rawtypes")
	Map<String, Set<String>> parse(StaplerRequest req) {
		Map<String, Set<String>> results = Maps.newHashMap();
		ParameterParser parser = new ParameterParser();
		for (Enumeration names = req.getParameterNames(); names.hasMoreElements(); ) {
			Map<String, String> p = parser.parse((String) names.nextElement());
			for (Map.Entry<String, String> e : p.entrySet()) {
				Set<String> set = results.get(e.getKey());
				if (set == null) {
					set = Sets.newHashSet(e.getValue());
					results.put(e.getKey(), set);
				}
				else {
					set.add(e.getValue());
				}
			}
		}
		return results;
	}

	public PreviewZipAction with(ImportZipAction parent) {
		this.parent = parent;
		return this;
	}

	public PreviewZipAction process(StaplerResponse res)
			throws IOException, InterruptedException {

		diff();

		parent.addPreview(this);
		res.sendRedirect2("previews/" + id());

		return this;
	}

	FreeStyleProject create(@NonNull String plan)
			throws IOException, InterruptedException {
		FreeStyleProject project = newProject(plan);
		jenkins().putItem(project);
		jenkins().save();
		return project;
	}

	FreeStyleProject delete(String plan)
			throws IOException, InterruptedException {
		FreeStyleProject job = getJob(plan);
		job.delete();
		return job;
	}

	FreeStyleProject getJob(String plan) {
		return (FreeStyleProject) jenkins().getItem(Jobs.toJob(plan));
	}

	private Jenkins jenkins() {
		return jenkins;
	}

	FreeStyleProject newProject(@NonNull String plan)
			throws IOException, InterruptedException {
		FreeStyleProject project = Jobs.newJob(jenkins, Jobs.toJob(plan));
		return clif.configure(project, dir, plan);
	}

	public String id() {
		return zip.id();
	}

	public ClifInstallation[] clifs() {
		return installations.all(ClifInstallation.DescriptorImpl.class);
	}
}
