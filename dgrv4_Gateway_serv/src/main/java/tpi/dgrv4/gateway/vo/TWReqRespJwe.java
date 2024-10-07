package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 加密後-API 訊息區塊（jwe-enveloped）
 * HTTP Request/Response
 * 
 * @author Mini
 */
public class TWReqRespJwe {
	
	/** 描述本文加密演算法資訊  須採 BASE64URL 編碼 */
	@JsonProperty("protected")
	private String protectedData;
	
	/** 描述本文加密金鑰資訊  本階段不使用，此欄位值放空字串"" */
	@JsonProperty("unprotected")
	private String unprotected = "";

	/** 描述【保護本文加密金鑰】之演算法及【本文加密金鑰】 */
	@JsonProperty("recipients")
	private List<TWReqRespJweRecipients> twReqRespJweRecipientsList;
	
	/** Initialization Vector  加密本文所採用演算法所需之初始參數，通常為亂數,須採 BASE64URL 編碼 */
	@JsonProperty("iv")
	private String iv;
	
	/** 加密密文  須採 BASE64URL 編碼 */
	@JsonProperty("ciphertext")
	private String cipherText;
	
	/** JWE Authentication Tag  須採 BASE64URL 編碼 */
	@JsonProperty("tag")
	private String tag;

	@Override
	public String toString() {
		return "TWReqRespJwe [protectedData=" + protectedData + ", unprotected=" + unprotected
				+ ", twReqRespJweRecipientsList=" + twReqRespJweRecipientsList + ", iv=" + iv + ", cipherText="
				+ cipherText + ", tag=" + tag + "]";
	}

	public String getProtectedData() {
		return protectedData;
	}

	public void setProtectedData(String protectedData) {
		this.protectedData = protectedData;
	}

	public String getUnprotected() {
		return unprotected;
	}

	public void setUnprotected(String unprotected) {
		this.unprotected = unprotected;
	}

	public List<TWReqRespJweRecipients> getTwReqRespJweRecipientsList() {
		return twReqRespJweRecipientsList;
	}

	public void setTwReqRespJweRecipientsList(List<TWReqRespJweRecipients> twReqRespJweRecipientsList) {
		this.twReqRespJweRecipientsList = twReqRespJweRecipientsList;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public String getCipherText() {
		return cipherText;
	}

	public void setCipherText(String cipherText) {
		this.cipherText = cipherText;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
