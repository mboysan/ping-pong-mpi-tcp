package network.address;

import config.LoggerConfig;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated
 */
public class TCPAddressResolver {
    /**
     * Ip address prefix in format "192.168.1."
     */
    private final String ipRangePrefix;
    /**
     * Ip address suffixes in format "1-255".
     * If only one range will be used, use format "1".
     * Appended to {@link #ipRangePrefix}.
     */
    private final String ipRangeSuffix;
    /**
     * Port range for synced message passing. In format "8080-8099".
     * If only one range will be used, use format "8080".
     */
    private final String syncPortRange;

    private List<TCPAddress> possibleAddresses;

    public TCPAddressResolver(String ipRangePrefix, String ipRangeSuffix, String syncPortRange) {
        this.ipRangePrefix = ipRangePrefix;
        this.ipRangeSuffix = ipRangeSuffix;
        this.syncPortRange = syncPortRange;

        this.possibleAddresses = calcualatePossibleAddresses();

        Logger.debug(String.format("Range of TCP addresses calculated:%n") +
                possibleAddresses.stream()
                        .map(TCPAddress::toString)
                        .collect(Collectors.joining(String.format("%n"))));
    }

    private List<TCPAddress> calcualatePossibleAddresses() {
        List<TCPAddress> ads = new ArrayList<>();
        try {
            String[] suffixes = ipRangeSuffix.split("-");
            int suf0 = Integer.parseInt(suffixes[0]);
            int suf1 = suf0;
            try {
                suf1 = Integer.parseInt(suffixes[1]);
            } catch (Exception ignore) {
            }

            String[] sportRng = syncPortRange.split("-");
            int sp0 = Integer.parseInt(sportRng[0]);
            int sp1 = sp0;
            try {
                sp1 = Integer.parseInt(sportRng[1]);
            } catch (Exception ignore) {
            }

            for (int i = sp0; i <= sp1; i++) {
                int sport = i;
                for (int j = suf0; j <= suf1; j++) {
                    String ipSuf = j + "";
                    String ip = ipRangePrefix + ipSuf;
                    ads.add(new TCPAddress(ip, sport));
                }
            }
        } catch (Exception e) {
            Logger.error(e, "TCP addresses could not be generated");
        }
        return ads;
    }

    public List<TCPAddress> getPossibleAddresses() {
        return possibleAddresses;
    }

    public static void main(String[] args) {
        LoggerConfig.configureLogger();
        new TCPAddressResolver("127.0.0.","1-10", "8080");
    }
}
