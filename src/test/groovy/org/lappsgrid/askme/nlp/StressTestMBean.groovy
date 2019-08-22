package org.lappsgrid.askme.nlp

/**
 *
 */
interface StressTestMBean {
    void stop()
    String stats()
    void setMaxOutstanding(int n)
}
