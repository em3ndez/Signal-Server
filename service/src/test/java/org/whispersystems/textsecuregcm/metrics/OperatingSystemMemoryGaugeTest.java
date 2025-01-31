/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.metrics;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class OperatingSystemMemoryGaugeTest {

    private static final String MEMINFO =
        """
            MemTotal:       16052208 kB
            MemFree:         4568468 kB
            MemAvailable:    7702848 kB
            Buffers:          636372 kB
            Cached:          5019116 kB
            SwapCached:         6692 kB
            Active:          7746436 kB
            Inactive:        2729876 kB
            Active(anon):    5580980 kB
            Inactive(anon):  1648108 kB
            Active(file):    2165456 kB
            Inactive(file):  1081768 kB
            Unevictable:      443948 kB
            Mlocked:            4924 kB
            SwapTotal:       1003516 kB
            SwapFree:         935932 kB
            Dirty:             28308 kB
            Writeback:             0 kB
            AnonPages:       5258396 kB
            Mapped:          1530740 kB
            Shmem:           2419340 kB
            KReclaimable:     229392 kB
            Slab:             408156 kB
            SReclaimable:     229392 kB
            SUnreclaim:       178764 kB
            KernelStack:       17360 kB
            PageTables:        50436 kB
            NFS_Unstable:          0 kB
            Bounce:                0 kB
            WritebackTmp:          0 kB
            CommitLimit:     9029620 kB
            Committed_AS:   16681884 kB
            VmallocTotal:   34359738367 kB
            VmallocUsed:       41944 kB
            VmallocChunk:          0 kB
            Percpu:             4240 kB
            HardwareCorrupted:     0 kB
            AnonHugePages:         0 kB
            ShmemHugePages:        0 kB
            ShmemPmdMapped:        0 kB
            FileHugePages:         0 kB
            FilePmdMapped:         0 kB
            CmaTotal:              0 kB
            CmaFree:               0 kB
            HugePages_Total:       0
            HugePages_Free:        7
            HugePages_Rsvd:        0
            HugePages_Surp:        0
            Hugepagesize:       2048 kB
            Hugetlb:               0 kB
            DirectMap4k:      481804 kB
            DirectMap2M:    14901248 kB
            DirectMap1G:     2097152 kB
            """;

    @Test
    @Parameters(method = "argumentsForTestGetValue")
    public void testGetValue(final String metricName, final long expectedValue) {
        assertEquals(expectedValue, new OperatingSystemMemoryGauge(metricName).getValue(MEMINFO.lines()));
    }

    private static Object argumentsForTestGetValue() {
        return new Object[] {
                new Object[] { "MemTotal",       16052208L },
                new Object[] { "Active(anon)",   5580980L  },
                new Object[] { "Committed_AS",   16681884L },
                new Object[] { "HugePages_Free", 7L        },
                new Object[] { "NonsenseMetric", 0L        }
        };
    }
}
