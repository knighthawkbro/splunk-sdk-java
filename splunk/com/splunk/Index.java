/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.net.Socket;

public class Index extends Entity {
    public Index(Service service, String path) {
        super(service, path);
    }

    public Socket attach() throws IOException {
        Socket socket = service.streamConnect();
        OutputStream ostream = socket.getOutputStream();
        Writer out = new OutputStreamWriter(ostream, "UTF8");

        String header = String.format(
            "POST /services/receivers/stream?index=%s HTTP/1.1\r\n" +
            "Host: %s:%d\r\n" +
            "Accept-Encoding: identity\r\n" +
            "Authorization: %s\r\n" +
            "X-Splunk-Input-Mode: Streaming\r\n\r\n",
            getName(),
            service.getHost(), service.getPort(),
            service.token);
        out.write(header);
        out.flush();
        return socket;
    }

    public Index clean() {
        Args saved = new Args();
        saved.put("maxTotalDataSizeMB",
                  Integer.toString(this.getMaxTotalDataSizeMB()));
        saved.put("frozenTimePeriodInSecs",
                  Integer.toString(this.getFrozenTimePeriodInSecs()));

        Args reset = new Args();
        reset.put("maxTotalDataSizeMB", "1");
        reset.put("frozenTimePeriodInSecs", "1");
        update(reset);
        rollHotBuckets();

        while (true) {
            try {
                Thread.sleep(1000); // 1000ms (1 second sleep)
            }
            catch (InterruptedException e) {
                return this; // eat
            }
            if (this.getTotalEventCount() == 0)
                break;
            refresh();
        }
        update(saved);
        return this;
    }

    public boolean getAssureUTF8() {
        return getBoolean("assureUTF8");
    }

    public String getBlockSignatureDatabase() {
        return getString("blockSignatureDatabase");
    }

    public int getBlockSignSize() {
        return getInteger("blockSignSize");
    }

    public String getColdPath() {
        return getString("coldPath", null);
    }

    public String getColdPathExpanded() {
        return getString("coldPath_expanded", null);
    }

    public String getColdToFrozenDir() {
        return getString("coldToFrozenDir", null);
    }

    public String getColdToFrozenScript() {
        return getString("coldToFrozenScript", null);
    }

    public boolean getCompressRawdata() {
        return getBoolean("compressRawdata");
    }

    public int getCurrentDBSizeMB() {
        return getInteger("currentDBSizeMB");
    }

    public String getDefaultDatabase() {
        return getString("defaultDatabase");
    }

    public boolean getEnableRealtimeSearch() {
        return getBoolean("enableRealtimeSearch");
    }

    public int getFrozenTimePeriodInSecs() {
        return getInteger("frozenTimePeriodInSecs");
    }

    public String getHomePath() {
        return getString("homePath", null);
    }

    public String getHomePathExpanded() {
        return getString("homePath_expanded", null);
    }

    public String getIndexThreads() {
        return getString("indexThreads");
    }

    public String getLastInitTime() {
        return getString("lastInitTime", null);
    }

    public int getMaxConcurrentOptimizes() {
        return getInteger("maxConcurrentOptimizes");
    }

    public String getMaxDataSize() {
        return getString("maxDataSize");
    }

    public int getMaxHotBuckets() {
        return getInteger("maxHotBuckets");
    }

    public int getMaxHotIdleSecs() {
        return getInteger("maxHotIdleSecs");
    }

    public int getMaxHotSpanSecs() {
        return getInteger("maxHotSpanSecs");
    }

    public int getMaxMemMB() {
        return getInteger("maxMemMB");
    }

    public int getMaxMetaEntries() {
        return getInteger("maxMetaEntries");
    }

    public int getMaxRunningProcessGroups() {
        return getInteger("maxRunningProcessGroups");
    }

    public Date getMaxTime() {
        return getDate("maxTime", null);
    }

    public int getMaxTotalDataSizeMB() {
        return getInteger("maxTotalDataSizeMB");
    }

    public int getMaxWarmDBCount() {
        return getInteger("maxWarmDBCount");
    }

    public String getMemPoolMB() {
        return getString("memPoolMB");
    }

    public String getMinRawFileSyncSecs() {
        return getString("minRawFileSyncSecs");
    }

    public Date getMinTime() {
        return getDate("minTime", null);
    }

    public int getPartialServiceMetaPeriod() {
        return getInteger("partialServiceMetaPeriod");
    }

    public int getQuarantineFutureSecs() {
        return getInteger("quarantineFutureSecs");
    }

    public int getQuarantinePastSecs() {
        return getInteger("quarantinePastSecs");
    }

    public int getRawChunkSizeBytes() {
        return getInteger("rawChunkSizeBytes");
    }

    public int getRotatePeriodInSecs() {
        return getInteger("rotatePeriodInSecs");
    }

    public int getServiceMetaPeriod() {
        return getInteger("serviceMetaPeriod");
    }

    public String getSuppressBannerList() {
        return getString("suppressBannerList", "");
    }

    public boolean getSync() {
        return getBoolean("sync");
    }

    public boolean getSyncMeta() {
        return getBoolean("syncMeta");
    }

    public String getThawedPath() {
        return getString("thawedPath", null);
    }

    public String getThawedPathExpanded() {
        return getString("thawedPath_expanded", null);
    }

    public int getThrottleCheckPeriod() {
        return getInteger("throttleCheckPeriod");
    }

    public int getTotalEventCount() {
        return getInteger("totalEventCount");
    }

    public boolean isDisabled() {
        return getBoolean("disabled");
    }

    public boolean isInternal() {
        return getBoolean("isInternal");
    }

    public void rollHotBuckets() {
        ResponseMessage response = service.post(path + "/roll-hot-buckets");
        assert(response.getStatus() == 200); // UNDONE
    }

    public void submit(String data) {
        RequestMessage request = new RequestMessage("POST");
        request.setContent(data);
        service.send("receivers/simple?index=" + getName(), request);
    }

    public void upload(String filename) {
        Args args = new Args();
        args.put("name", filename);
        args.put("index", getName());
        service.post("/services/data/inputs/oneshot", args);
    }
}

