package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;

public class DgrGtwIdpInfoLDaoImpl extends SuperDaoImpl<DgrGtwIdpInfoL> implements DgrGtwIdpInfoLSuperDao {

	@Override
	public Class<DgrGtwIdpInfoL> getEntityType() {
		return DgrGtwIdpInfoL.class;
	}
}
