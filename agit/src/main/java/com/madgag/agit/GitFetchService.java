/*
 * Copyright (c) 2011 Roberto Tyley
 *
 * This file is part of 'Agit' - an Android Git client.
 *
 * Agit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Agit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.madgag.agit;

import android.util.Log;
import com.google.inject.Inject;
import com.madgag.agit.guice.OperationScoped;
import com.madgag.agit.operations.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.*;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;
import static com.madgag.agit.operations.JGitAPIExceptions.throwExceptionWithFriendlyMessageFor;

@OperationScoped
public class GitFetchService {
	
	private static String TAG = "GFS";

	@Inject Git git;
    @Inject MessagingProgressMonitor messagingProgressMonitor;
	@Inject CredentialsProvider credentialsProvider;
	@Inject	TransportConfigCallback transportConfigCallback;
	@Inject RepoUpdateBroadcaster repoUpdateBroadcaster;

	public FetchResult fetch(String remote, Collection<RefSpec> toFetch) {
		Log.d(TAG, "About to run fetch : " + remote);

		FetchResult fetchResult = null;
		try {
			fetchResult = git.fetch()
					.setRemote(remote)
					.setRefSpecs(toFetch == null ? Collections.<RefSpec>emptyList() : newArrayList(toFetch))
					.setProgressMonitor(messagingProgressMonitor)
					.setTransportConfigCallback(transportConfigCallback)
					.setCredentialsProvider(credentialsProvider)
					.call();
		} catch (InvalidRemoteException e) {
			throw new RuntimeException(e);
		} catch (JGitInternalException e) {
			throwExceptionWithFriendlyMessageFor(e);
		}
		Log.d(TAG, "Fetch complete with : " + fetchResult);
		for (TrackingRefUpdate update : fetchResult.getTrackingRefUpdates()) {
			Log.d(TAG, "TrackingRefUpdate : " + update.getLocalName()+" old="+update.getOldObjectId()+" new="+update.getNewObjectId());
		}
		repoUpdateBroadcaster.broadcastUpdate();
		return fetchResult;
	}

}
