package org.openobd2.core.command.obd;

import java.util.ArrayList;
import java.util.List;

import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CommandReplyDecoder;
import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPidsCommand extends ObdFrame implements Codec<List<String>> {

	public SupportedPidsCommand(String pid) {
		super(new PidDefinition(0, "", pid, "01", "", "PIDs supported", "", ""));
	}

	@Override
	public List<String> decode(@NonNull String data) {
		CommandReplyDecoder replyDecoder = new CommandReplyDecoder();
		final List<String> supportedPids = new ArrayList<String>();
		if (replyDecoder.isSuccessAnswerCode(pid, data)) {
			final String binStr = Long.toBinaryString(replyDecoder.getDecimalAnswerData(pid, data));

			for (int idx = 0; idx < binStr.length(); idx++) {
				if ('1' == binStr.charAt(idx)) {
					String hexString = Integer.toHexString((idx + 1));
					if (hexString.length() == 1) {
						hexString = "0" + hexString;
					}
					supportedPids.add(hexString);
				}
			}
		} else {
			log.warn("Failed to transform data: {}", data);
		}
		return supportedPids;
	}
}
