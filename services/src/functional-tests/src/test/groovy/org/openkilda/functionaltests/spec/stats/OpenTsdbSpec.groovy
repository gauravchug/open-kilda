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
        [metric, tags] << (
                //basic stats for rx/tx, use every unique switch in tags
                [["sdn.switch.rx-bytes", "sdn.switch.rx-bits", "sdn.switch.rx-packets",
                 "sdn.switch.tx-bytes", "sdn.switch.tx-bits", "sdn.switch.tx-packets"],
                uniqueSwitches.collect { [switchid: it.dpId.toOtsdFormat()] }].combinations()
                //isl latency stats, use every unique switch in src_switch tag
                + [["sdn.isl.latency"], uniqueSwitches.collect { [src_switch: it.dpId.toOtsdFormat()] }].combinations()
                //isl latency stats, use every unique switch in dst_switch tag
                + [["sdn.isl.latency"], uniqueSwitches.collect { [dst_switch: it.dpId.toOtsdFormat()] }].combinations()
                //stats for default rules. use discovery packet cookie in tags, as is doesn't need any specific actions
                + [["sdn.switch.flow.system.packets", "sdn.switch.flow.system.bytes", "sdn.switch.flow.system.bits"],
                   [[cookieHex: DefaultRule.VERIFICATION_BROADCAST_RULE.toHexString()]]].combinations())
    }

    @Unroll("Stats are being logged for metric:#metric, tags:#tags")
    def "Basic stats are being logged (10min interval)"() {
        requireProfiles("hardware")

        expect: "At least 1 result in the past 10 minutes"
        otsdb.query(10.minutes.ago, metric, [:]).dps.size() > 0
        where:
        [metric, tags] << (
                [["sdn.switch.flow.system.meter.packets", "sdn.switch.flow.system.meter.bytes",
                  "sdn.switch.flow.system.meter.bits"],
                 [[cookieHex: String.format("0x%X", DefaultRule.VERIFICATION_BROADCAST_RULE.cookie)]]].combinations())
    }

    def getUniqueSwitches() {
        topology.activeSwitches.unique { it.ofVersion }
    }
}
