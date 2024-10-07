package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataEncryptionResp {
	@JsonProperty("ciphertext")
	private String ciphertext;

	@Override
	public String toString() {
		return "DataEncryptionResp [ciphertext=" + ciphertext + "]";
	}

	public String getCiphertext() {
		return ciphertext;
	}

	public void setCiphertext(String ciphertext) {
		this.ciphertext = ciphertext;
	}
}
