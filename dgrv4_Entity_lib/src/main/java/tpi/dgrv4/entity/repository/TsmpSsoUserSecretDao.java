package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpSsoUserSecret;

@Repository
public interface TsmpSsoUserSecretDao extends JpaRepository<TsmpSsoUserSecret, Long> {
	
	public List<TsmpSsoUserSecret> findByCreateUser(String createUser);
	
	public TsmpSsoUserSecret findFirstByUserName(String userName);
}
