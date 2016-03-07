/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Norwegian Defence Research Establishment / NTNU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package no.ntnu.okse.core;

import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;

import static org.testng.Assert.*;

public class UtilitiesTest {

    @Test
    public void testGetDurationAsISO8601() throws Exception {
        Duration d = Duration.ofHours(1);
        String ISOstring = Utilities.getDurationAsISO8601(d);
        assertEquals(ISOstring, "01:00:00.000");
        d = Duration.ofMinutes(1);
        ISOstring = Utilities.getDurationAsISO8601(d);
        assertEquals(ISOstring, "00:01:00.000");
        d = Duration.ofSeconds(1);
        ISOstring = Utilities.getDurationAsISO8601(d);
        assertEquals(ISOstring, "00:00:01.000");
        d = Duration.ofMillis(1);
        ISOstring = Utilities.getDurationAsISO8601(d);
        assertEquals(ISOstring, "00:00:00.001");
    }

    // Test creation of configuration files from templates in resources.
    // If the config directory already exists, move it away for the duration
    // of the test. This code will not work correctly if subdirectories are
    // in the temporarily created config directory.
    @Test
    public void testCreateConfigurationFiles() {
        File config = new File("config");
        File configTmp = new File("configTmp");
        if(config.exists()) config.renameTo(configTmp);

        Utilities.createConfigDirectoryAndFilesIfNotExists();
        for(String filename : Utilities.configFiles) {
            File file = new File(filename);
            assertTrue(file.exists());
        }

        for(File f : config.listFiles()) f.delete();
        config.delete();
        if(configTmp.exists()) configTmp.renameTo(config);
    }

    @Test
    public void testReadConfigurationsFromFile() throws Exception {
        Utilities.createConfigDirectoryAndFilesIfNotExists();
        Arrays.stream(Utilities.configFiles).filter(file -> file.endsWith(".properties")).forEach(file -> {
            assertNotNull(Utilities.readConfigurationFromFile(file));
        });
    }
}