/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 * Copyright (C) 2026 Orange SA
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
import java.util.List;
import java.util.Map;

import hudson.model.Item;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletDiskFileUpload;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.ow2.clif.jenkins.jobs.Zip;
import com.google.common.collect.Maps;
import hudson.Extension;


@Extension
public class ImportZipAction implements RootAction {
	final Map<String, PreviewZipAction> previews = Maps.newHashMap();

	public ImportZipAction() {
	}

	@Override
	public String getIconFileName() {
		return "/plugin/clif-performance-testing/images/clif-24x24.png";
	}

	@Override
	public String getDisplayName() {
		return Messages.ZipImporter_DisplayName();
	}

	@Override
	public String getUrlName() {
		return "clif";
	}

	@RequirePOST
	public void doImport(StaplerRequest2 req, StaplerResponse2 res)
			throws IOException {
		Jenkins.get().hasAnyPermission(Item.CREATE, Item.CONFIGURE);
		new PreviewZipAction(new Zip(readZipFile(req))).with(this).process(res);
	}

	@SuppressWarnings("unchecked")
	private File readZipFile(StaplerRequest2 req)
			throws IOException {
		List<DiskFileItem> items = new JakartaServletDiskFileUpload(DiskFileItemFactory.builder().get())
				.parseRequest(req);
		File file = File.createTempFile("zip", null);
		try {
			items.get(0).write(file.toPath());
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return file;
	}

	public PreviewZipAction getPreviews(String id) {
		return previews.get(id);
	}

	public PreviewZipAction addPreview(PreviewZipAction preview) {
		return previews.put(preview.id(), preview);
	}

	public PreviewZipAction removePreview(String id) {
		return previews.remove(id);
	}
}
