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
package org.ow2.clif.jenkins;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Zip;
import com.google.common.collect.Maps;
import hudson.Extension;
import hudson.model.RootAction;


@Extension
public class ImportZipAction implements RootAction {
	final Map<String, PreviewZipAction> previews = Maps.newHashMap();

	public ImportZipAction() {
	}

	public String getIconFileName() {
		return "/plugin/clif-performance-testing/images/clif-24x24.png";
	}

	public String getDisplayName() {
		return Messages.ZipImporter_DisplayName();
	}

	public String getUrlName() {
		return "clif";
	}

	public void doImport(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException, FileUploadException {
		new PreviewZipAction(new Zip(readZipFile(req))).with(this).process(res);
	}

	@SuppressWarnings("unchecked")
	private File readZipFile(StaplerRequest req)
			throws IOException, FileUploadException {
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory())
				.parseRequest(req);
		File file = File.createTempFile("zip", null);
		try {
			items.get(0).write(file);
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
