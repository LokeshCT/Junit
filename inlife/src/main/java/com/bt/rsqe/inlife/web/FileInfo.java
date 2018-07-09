package com.bt.rsqe.inlife.web;

import org.apache.commons.io.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FileInfo {
    private final String name;
    private final Date lastModified;
    private final long length;

    public FileInfo(String name, Date lastModified, long length) {
        this.name = name;
        this.lastModified = lastModified;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getLastModified() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(lastModified);
    }

    public long getLength() {
        return length;
    }

    public String getHumanReadableLength ()
    {
        return FileUtils.byteCountToDisplaySize(length) ;
    }
}
