package tpi.dgrv4.dpaa.component;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.TsmpCertificate;


public class CertificateHelper {
	
	public static X509Certificate getCertificate(byte[] certStr){
		ByteArrayInputStream bytes = new ByteArrayInputStream(certStr);
		
		CertificateFactory certFactory;
		X509Certificate cert = null;
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(bytes);
		} catch (CertificateException e) {
			throw TsmpDpAaRtnCode._1291.throwing();
		}
		
		return cert;
	}
	
	/**
	 * 取得解析後的憑證資料
	 * 
	 * @param fileContent
	 * @return
	 */
	public static TsmpCertificate getTsmpCertificate(byte[] fileContent) {
		X509Certificate x509Cert = CertificateHelper.getCertificate(fileContent);
		
		TsmpCertificate c = new TsmpCertificate();
		c.setPubKey(ServiceUtil.base64Encode(x509Cert.getPublicKey().getEncoded()));//公鑰
		c.setCertVersion(x509Cert.getVersion() + "");//憑證版本
		c.setCertSerialNum(x509Cert.getSerialNumber().toString());//憑證序號
		c.setsAlgorithmId(x509Cert.getSigAlgName());//簽章演算法
		c.setAlgorithmId(x509Cert.getPublicKey().getAlgorithm());//公鑰演算法
		c.setCertThumbprint(ServiceUtil.base64Encode(x509Cert.getSignature()));//CA數位指紋
		c.setIuid("");//發行方ID
		String issuerName = "";		
		try {
			issuerName = getIssuerName(x509Cert);//發行方名稱
		} catch (InvalidNameException e1) {
			issuerName = "";
		}
		c.setIssuerName(issuerName);
		
		String subjectUniqueID = "";
		try {
			subjectUniqueID = getCommonName(x509Cert);//持有者身分ID
		} catch (InvalidNameException e) {
			subjectUniqueID = "";
		}
		c.setSuid(subjectUniqueID);
		c.setCreateAt(x509Cert.getNotBefore().getTime());//憑證創建日 (轉long)
		c.setExpiredAt(x509Cert.getNotAfter().getTime());//憑證到期日 (轉long)
		c.setKeySize(getKeySize(x509Cert));
		
		return c;
	}

	/** 
	 * 取得發行方名稱
	 * 
	 * 備註:
	 * see https://www.sslsupportdesk.com/java-code-signing-certificate-instructions/
	 * Fill out the applicable information:
	 * (1).First and Last Name? or Common Name (CN)
	 * (2).Organizational Unit (OU)
	 * (3).Organization (O)
	 * (4).Locality or City (L)
	 * (5).State or Province (ST)
	 * (6).Country Name (C)
	 */
	public static String getIssuerName(X509Certificate cert) throws InvalidNameException {		
		
		LdapName ldapDN = new LdapName(cert.getIssuerX500Principal().getName());
		String cn = "";
		for (Rdn rdn : ldapDN.getRdns()) {
//			System.out.println(rdn.getType() + " -> " + rdn.getValue());
			
			if (rdn.getType().equals("CN")) {
				cn = rdn.getValue().toString();
			}
		}
		return cn;
	}
	
	public static String getCommonName(X509Certificate cert)
	        throws InvalidNameException {
	    LdapName ldapDN
	            = new LdapName(cert.getSubjectX500Principal().getName());
	    String cn = "";
	    for (Rdn rdn : ldapDN.getRdns()) {
	        if (rdn.getType().equals("CN")) {
	            cn = rdn.getValue().toString();
	        }
	    }
	    return cn;
	}
	
	/**
	 * 取得 key size,不同公鑰演算法有不同取法
	 * 
	 * see https://stackoverflow.com/questions/32573317/how-to-determine-length-of-x509-public-key
	 * @return
	 */
	public static int getKeySize(X509Certificate cert) {
		PublicKey pk = cert.getPublicKey();
		
	    int len = -1;
	    if (pk instanceof RSAPublicKey) {
	        final RSAPublicKey rsapub = (RSAPublicKey) pk;
	        len = rsapub.getModulus().bitLength();
	        
	    } else if (pk instanceof DSAPublicKey) {
	        final DSAPublicKey dsapub = (DSAPublicKey) pk;
	        if ( dsapub.getParams() != null ) {
	            len = dsapub.getParams().getP().bitLength();
	        } else {
	            len = dsapub.getY().bitLength();
	        }
	    } 
	    return len;
	}

	/**
	 * 憑證範本1: 檔案內容
	 * @return
	 */
	public static String getExampleCertStr() {
		String sCert = "-----BEGIN CERTIFICATE-----\n" + 
				"MIICMzCCAZygAwIBAgIJALiPnVsvq8dsMA0GCSqGSIb3DQEBBQUAMFMxCzAJBgNV\n" + 
				"BAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNVBAcTA2ZvbzEMMAoGA1UEChMDZm9v\n" + 
				"MQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2ZvbzAeFw0xMzAzMTkxNTQwMTlaFw0x\n" + 
				"ODAzMTgxNTQwMTlaMFMxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNV\n" + 
				"BAcTA2ZvbzEMMAoGA1UEChMDZm9vMQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2Zv\n" + 
				"bzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAzdGfxi9CNbMf1UUcvDQh7MYB\n" + 
				"OveIHyc0E0KIbhjK5FkCBU4CiZrbfHagaW7ZEcN0tt3EvpbOMxxc/ZQU2WN/s/wP\n" + 
				"xph0pSfsfFsTKM4RhTWD2v4fgk+xZiKd1p0+L4hTtpwnEw0uXRVd0ki6muwV5y/P\n" + 
				"+5FHUeldq+pgTcgzuK8CAwEAAaMPMA0wCwYDVR0PBAQDAgLkMA0GCSqGSIb3DQEB\n" + 
				"BQUAA4GBAJiDAAtY0mQQeuxWdzLRzXmjvdSuL9GoyT3BF/jSnpxz5/58dba8pWen\n" + 
				"v3pj4P3w5DoOso0rzkZy2jEsEitlVM2mLSbQpMM+MUVQCQoiG6W9xuCFuxSrwPIS\n" + 
				"pAqEAuV4DNoxQKKWmhVv+J0ptMWD25Pnpxeq5sXzghfJnslJlQND\n" + 
				"-----END CERTIFICATE-----";
		
		/*
		Certificate Information:
			Common Name: foo
			Organization: foo
			Organization Unit: foo
			Locality: foo
			State: foo
			Country: US
			Valid From: March 19, 2013
			Valid To: March 18, 2018
			Issuer: foo
			Serial Number: 13299021239615735660
			Key size: 1024
		*/

		return sCert;
	}
	/**
	 * 憑證範本2: 檔案內容
	 * @return
	 */
	public static String getExampleCertStr2() {
		String sCert = "-----BEGIN CERTIFICATE-----\n" + 
				"MIIEqDCCA5CgAwIBAgIJAJNurL4H8gHfMA0GCSqGSIb3DQEBBQUAMIGUMQswCQYD\n" + 
				"VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4g\n" + 
				"VmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UE\n" + 
				"AxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAe\n" + 
				"Fw0wODAyMjkwMTMzNDZaFw0zNTA3MTcwMTMzNDZaMIGUMQswCQYDVQQGEwJVUzET\n" + 
				"MBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4G\n" + 
				"A1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9p\n" + 
				"ZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZI\n" + 
				"hvcNAQEBBQADggENADCCAQgCggEBANaTGQTexgskse3HYuDZ2CU+Ps1s6x3i/waM\n" + 
				"qOi8qM1r03hupwqnbOYOuw+ZNVn/2T53qUPn6D1LZLjk/qLT5lbx4meoG7+yMLV4\n" + 
				"wgRDvkxyGLhG9SEVhvA4oU6Jwr44f46+z4/Kw9oe4zDJ6pPQp8PcSvNQIg1QCAcy\n" + 
				"4ICXF+5qBTNZ5qaU7Cyz8oSgpGbIepTYOzEJOmc3Li9kEsBubULxWBjf/gOBzAzU\n" + 
				"RNps3cO4JFgZSAGzJWQTT7/emMkod0jb9WdqVA2BVMi7yge54kdVMxHEa5r3b97s\n" + 
				"zI5p58ii0I54JiCUP5lyfTwE/nKZHZnfm644oLIXf6MdW2r+6R8CAQOjgfwwgfkw\n" + 
				"HQYDVR0OBBYEFEhZAFY9JyxGrhGGBaR0GawJyowRMIHJBgNVHSMEgcEwgb6AFEhZ\n" + 
				"AFY9JyxGrhGGBaR0GawJyowRoYGapIGXMIGUMQswCQYDVQQGEwJVUzETMBEGA1UE\n" + 
				"CBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMH\n" + 
				"QW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAG\n" + 
				"CSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbYIJAJNurL4H8gHfMAwGA1Ud\n" + 
				"EwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAHqvlozrUMRBBVEY0NqrrwFbinZa\n" + 
				"J6cVosK0TyIUFf/azgMJWr+kLfcHCHJsIGnlw27drgQAvilFLAhLwn62oX6snb4Y\n" + 
				"LCBOsVMR9FXYJLZW2+TcIkCRLXWG/oiVHQGo/rWuWkJgU134NDEFJCJGjDbiLCpe\n" + 
				"+ZTWHdcwauTJ9pUbo8EvHRkU3cYfGmLaLfgn9gP+pWA7LFQNvXwBnDa6sppCccEX\n" + 
				"31I828XzgXpJ4O+mDL1/dBd+ek8ZPUP0IgdyZm5MTYPhvVqGCHzzTy3sIeJFymwr\n" + 
				"sBbmg2OAUNLEMO6nwmocSdN2ClirfxqCzJOLSDE4QyS9BAH6EhY6UFcOaE0=\n" + 
				"-----END CERTIFICATE-----";
		
		/*
		Certificate Information:
			Common Name: Android
			Organization: Android
			Organization Unit: Android
			Locality: Mountain View
			State: California
			Country: US
			Valid From: February 28, 2008
			Valid To: July 16, 2035
			Issuer: Android, Android
			Serial Number: 10623618503190643167 (0x936eacbe07f201df)
			Key size: 2048
		 */
		
		return sCert;
	}
	
	/**
	 * 憑證範本3: 檔案內容
	 * @return
	 */
	public static String getExampleCertStr3() {
		String sCert = "-----BEGIN CERTIFICATE-----\n" + 
				"MIIFezCCA2OgAwIBAgIJAPtcpxmNWe/MMA0GCSqGSIb3DQEBCwUAMFQxCzAJBgNV\n" + 
				"BAYTAlRXMQ8wDQYDVQQIDAZUYWlwZWkxCzAJBgNVBAcMAmtoMQswCQYDVQQKDAJw\n" + 
				"ZDELMAkGA1UECwwCcGQxDTALBgNVBAMMBGpvaG4wHhcNMjAwNTIwMDYyNTM4WhcN\n" + 
				"MjEwNTIwMDYyNTM4WjBUMQswCQYDVQQGEwJUVzEPMA0GA1UECAwGVGFpcGVpMQsw\n" + 
				"CQYDVQQHDAJraDELMAkGA1UECgwCcGQxCzAJBgNVBAsMAnBkMQ0wCwYDVQQDDARq\n" + 
				"b2huMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEArpls4bMlhU6YyBx8\n" + 
				"Sj+gB5bZQbZntScVrKSQsVCIHfBxDW3ow7KWHPF8cwexJsuUw0zQlhTYLBZfQarF\n" + 
				"yM35+fioUdrFH3+YaVFvlBkVi8q5bcnhKXzBIZXHIb+mhDrkSs1Ifze2rmHwCpyO\n" + 
				"sfSBhBjjry0bTvFUSh+UMOxjy0/Z2EgbqfG8QALXZoTtLEZflZUG781jHyRYrGQG\n" + 
				"FWO00ihS3GCi4GkXCy3IlKf7z5hKvYAo7tPGSneNLHmecJNghuHpsnD4oo+FW1q0\n" + 
				"4zZ1Sg2tqep6nDvvpaVxAvAFJZr1NeLrIoXRYW2otWbGAF2oMmKK9fICVCDGKP3H\n" + 
				"XpF6qIqNYyNiK2+Y8jlSmUN+fJc/NUJpLNUZKrN50gtSKzUfmehbnPcFWX/iamKH\n" + 
				"yDNeDzy6ZutYfpDxh1M0DrT3AliOeKTQdcPKNtN/pn1WJTq4P9sIxTDi/dYgl/FM\n" + 
				"TK7jNYOC9YxKOQ6bgeqBhjWZ4/ZG1u78iA0ZGnr1tirBOr0AACw39HNh2s7NDOzP\n" + 
				"9lcthBJkWlkt9cq23DBTqlZcerc7hZo8gY+ZiWtN0t9WSKjTyDqxt6DG3/muuYj6\n" + 
				"mXlL4tcPTZxeGKa8d1GwKXBQQBC2xy7UiZuWUEucD6HjvenawcRReP0ukZtLCDUF\n" + 
				"YjBIvx5JyEKAFiH7GtdGQkOS5wcCAwEAAaNQME4wHQYDVR0OBBYEFJxEQnRt2b2K\n" + 
				"dZfytQpAZoKZMVLbMB8GA1UdIwQYMBaAFJxEQnRt2b2KdZfytQpAZoKZMVLbMAwG\n" + 
				"A1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIBAGx2eX6pbPaxNh0qvGxbli/4\n" + 
				"KFQb7vokauH8RX6nKXyPO7xsbU6CURMc44UZ+nL+/GNmvt6Fqj83bROo0HoWJFUJ\n" + 
				"SSJJu1F+XtmQtYnvbY2eRmXtrmUlV2SLm7Ibx561EaTxyDs55MCil/F4gu7A2712\n" + 
				"UfOJCp48hjgMeBYCHUimpsQ8TKbSc3lvOzVWgvhIruG70xYI9Nk7F3pMYF5775FU\n" + 
				"Y0+Hfr26UchXjewB6uINfySwQPv92d9uHJNbVdiTp5rANrD4G0OQ4mnyJn+2SYj8\n" + 
				"x7Ul6zcBGNyzYSe9n65fq+mTwINTzfk5b+jrFJKxWOVbU3ZPJjlYZ4TsMj6Ol6ao\n" + 
				"zI36zn4SaIqdO4STzLDBnPci0+9Ho03Uxyoq0Wv/uCMUye5m1AuXgG/yXdiXJwdD\n" + 
				"/eBO6e9TBG5Eof1lRTH1iYszK9hYn+HhewdZ9PEJAME0iHWZ4/6ruKCcV0pSH4TD\n" + 
				"0FeRF5H45KyNCAz9Muqcf30kW//z4DlmTSmENHXJifszr/j74U3YEPf3PFUKbvxU\n" + 
				"S47QnNeAZgoYRCvVpSPe+ui1mZ7zOj+IRxvceOqZ8pH+vCng/Aqy/8Vcwr26h/Sk\n" + 
				"6zJhXJ9sc/g6b+fB0xae4KIq6jOhuC+IjOFNcFnnKg8zEl+DwA1omG9Z8P608J+l\n" + 
				"vnIRi0DWWydFnNjp9Hgd\n" + 
				"-----END CERTIFICATE-----";
		
		/*
		Certificate Information:
			Common Name: john
			Organization: pd
			Organization Unit: pd
			Locality: kh
			State: Taipei
			Country: TW
			Valid From: May 19, 2020
			Valid To: May 19, 2021
			Issuer: john, pd
			Serial Number: 18112535529564794828
			Key size: 4096
		 */
		return sCert;
	}
}
