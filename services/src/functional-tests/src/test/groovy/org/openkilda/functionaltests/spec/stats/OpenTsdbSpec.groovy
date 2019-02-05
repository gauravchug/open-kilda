package org.openkilda.functionaltests.spec.stats

import org.openkilda.functionaltests.BaseSpecification
import org.openkilda.testing.Constants.DefaultRule

import groovy.time.TimeCategory
import spock.lang.Issue
import spock.lang.Narrative
import spock.lang.Unroll
import spock.util.mop.Use

@Use(TimeCategory)
@Narrative("Verify that basic stats logging happens.")
class OpenTsdbSpec extends BaseSpecification {

    @Issue("https://github.com/telstra/open-kilda/issues/1434")
    @Unroll("Stats are being logged for metric:#metric, tags:#tags")
    def "Basic stats are being logged"(metric, tags) {
        requireProfiles("hardware") //due to #1434

        expect: "At least 1 result in the past 2 minutes"
        otsdb.query(2.minutes.ago, metric, tags).dps.size() > 0

        where:
        [metric, tags] << ([
                ["sdn.switch.rx-bytes", "sdn.switch.rx-bits", "sdn.switch.rx-packets",
                 "sdn.switch.tx-bytes", "sdn.switch.tx-bits", "sdn.switch.tx-packets"],
                uniqueSwitches.collect { [switchid: it.dpId.toOtsdFormat()] }].combinations()
                + [["sdn.isl.latency"], uniqueSwitches.collect { [src_switch: it.dpId.toOtsdFormat()] }].combinations()
                + [["sdn.isl.latency"], uniqueSwitches.collect { [dst_switch: it.dpId.toOtsdFormat()] }].combinations()
                + [["sdn.switch.flow.system.packets", "sdn.switch.flow.system.bytes", "sdn.switch.flow.system.bits"],
                   [[cookieHex: DefaultRule.VERIFICATION_BROADCAST_RULE.toHexString()]]].combinations())
    }

    @Unroll("Stats are being logged for metric:#metric, tags:#tags")
    def "Stats for default rule meters"(metric, tags) {
        requireProfiles("hardware")
        
        expect: "At least 1 result in the past 2 minutes"
        otsdb.query(2.minutes.ago, metric, tags).dps.size() > 0
        where:
        [metric, tags] << ([
                ["sdn.switch.flow.system.meter.packets", "sdn.switch.flow.system.meter.bytes",
                 "sdn.switch.flow.system.meter.bits"],
                [[cookieHex: String.format("%X", DefaultRule.VERIFICATION_BROADCAST_RULE.cookie)],
                 [cookieHex: String.format("%X", DefaultRule.VERIFICATION_UNICAST_RULE.cookie)]]
        ].combinations())
    }

    @Unroll("Stats are being logged for metric:#metric")
    def "Stats for flow meters"(metric) {
        requireProfiles("hardware")

        expect: "At least 1 result in the past 2 minutes"
        otsdb.query(2.minutes.ago, metric, [:]).dps.size() > 0
        where:
        metric << ["sdn.flow.meter.packets", "sdn.flow.meter.bytes", "sdn.flow.meter.bits"]
    }

    def getUniqueSwitches() {
        topology.activeSwitches.unique { it.ofVersion }
    }
}
